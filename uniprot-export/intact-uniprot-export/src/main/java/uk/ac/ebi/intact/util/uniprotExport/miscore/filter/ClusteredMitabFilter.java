package uk.ac.ebi.intact.util.uniprotExport.miscore.filter;

import org.springframework.transaction.TransactionStatus;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.InteractionDetectionMethod;
import psidev.psi.mi.tab.model.InteractionType;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.InteractionDao;
import uk.ac.ebi.intact.model.CvInteraction;
import uk.ac.ebi.intact.model.CvInteractionType;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.InteractionImpl;
import uk.ac.ebi.intact.model.util.InteractionUtils;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabReader;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.exporter.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.exporter.QueryFactory;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * This class is based on a clustered mitab file contrary to the NonClusteredMitabFilter which considers that one line contains one intact binary interaction
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/02/11</pre>
 */

public class ClusteredMitabFilter extends AbstractMitabFilter implements BinaryInteractionFilter{
    protected QueryFactory queryProvider;
    protected Set<String> eligibleInteractionsForUniprotExport;
    protected IntactPsimiTabReader mitabReader;
    protected static final String CONFIDENCE_NAME = "intactPsiscore";
    protected static final String INTACT = "intact";
    protected static final String UNIPROT = "uniprotkb";

    protected InteractionExporter exporter;

    protected String mitab;

    public ClusteredMitabFilter(InteractionExporter exporter, String mitab){
        super(exporter, mitab);
    }

    public ClusteredMitabFilter(InteractionExporter exporter){
        super(exporter);
    }

    protected BinaryClusterScoreResults clusterMiScoreInteractionEligibleUniprotExport(String mitabFile) throws IOException, ConverterException {
        BinaryClusterScore clusterScore = new BinaryClusterScore();
        MiClusterContext context = new MiClusterContext();

        File mitab = new File(mitabFile);
        Iterator<BinaryInteraction> iterator = mitabReader.iterate(new FileInputStream(mitab));

        Integer binaryIdentifier = 1;

        while (iterator.hasNext()){
            while (iterator.hasNext()){
                IntactBinaryInteraction interaction = (IntactBinaryInteraction) iterator.next();

                Set<String> intactAcs = FilterUtils.extractIntactAcFrom(interaction.getInteractionAcs());

                ExtendedInteractor interactorA = interaction.getInteractorA();
                String uniprotA = null;
                ExtendedInteractor interactorB = interaction.getInteractorB();
                String uniprotB = null;

                if (interactorA != null){
                    for (CrossReference refA : interactorA.getIdentifiers()){
                        if (UNIPROT.equalsIgnoreCase(refA.getDatabase())){
                            uniprotA = refA.getIdentifier();

                            break;
                        }
                    }
                }
                if (interactorB != null){
                    for (CrossReference refB : interactorB.getIdentifiers()){
                        if (UNIPROT.equalsIgnoreCase(refB.getDatabase())){
                            uniprotB = refB.getIdentifier();

                            break;
                        }
                    }
                }

                if (!intactAcs.isEmpty() && uniprotA != null && uniprotB != null){
                    TransactionStatus status = IntactContext.getCurrentInstance().getDataContext().beginTransaction();
                    InteractionDao interactionDao = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao();

                    List<InteractionImpl> interactionsInIntact = interactionDao.getByAc(intactAcs);
                    List<InteractionImpl> interactionsInIntactPassingExport = new ArrayList(interactionsInIntact);

                    boolean canBeProcessed = false;

                    for (InteractionImpl intact : interactionsInIntact){
                        String intactAc = intact.getAc();

                        if (this.eligibleInteractionsForUniprotExport.contains(intactAc)){
                            Collection<Experiment>  experiments = intact.getExperiments();
                            if (experiments.size() == 1){

                                Experiment exp = experiments.iterator().next();
                                CvInteraction detectionMethod = exp.getCvInteraction();
                                CvInteractionType interactionType = intact.getCvInteractionType();

                                if (detectionMethod != null && interactionType != null){
                                    canBeProcessed = true;

                                    String detectionMI = detectionMethod.getIdentifier();

                                    String typeMi = interactionType.getIdentifier();

                                    MethodAndTypePair entry = new MethodAndTypePair(detectionMI, typeMi);
                                    context.getInteractionToMethod_type().put(intactAc, entry);

                                    if (!InteractionUtils.isBinaryInteraction(intact)){

                                        context.getSpokeExpandedInteractions().add(intactAc);
                                    }
                                }
                            }
                        }
                        else{
                            interaction.getInteractionAcs().remove(intactAc);
                            interactionsInIntactPassingExport.remove(intact);
                        }
                    }

                    if (canBeProcessed){

                        FilterUtils.processGeneNames(interactorA, uniprotA, interactorB, uniprotB, context);

                        if (interactionsInIntactPassingExport.size() < interactionsInIntact.size()){
                             removeNotExportedInteractionEvidencesFrom(interaction, interactionsInIntactPassingExport);
                        }

                        processMiTerms(interaction, context);
                        clusterScore.getBinaryInteractionCluster().put(binaryIdentifier, interaction);

                        if (clusterScore.getInteractorCluster().containsKey(uniprotA)){
                            clusterScore.getInteractorCluster().get(uniprotA).add(binaryIdentifier);
                        }
                        else{
                            List<Integer> interactionIds = new ArrayList<Integer>();
                            interactionIds.add(binaryIdentifier);
                            clusterScore.getInteractorCluster().put(uniprotA, interactionIds);
                        }

                        if (clusterScore.getInteractorCluster().containsKey(uniprotB)){
                            clusterScore.getInteractorCluster().get(uniprotB).add(binaryIdentifier);
                        }
                        else{
                            List<Integer> interactionIds = new ArrayList<Integer>();
                            interactionIds.add(binaryIdentifier);
                            clusterScore.getInteractorCluster().put(uniprotB, interactionIds);
                        }
                    }

                    IntactContext.getCurrentInstance().getDataContext().commitTransaction(status);

                }
            }

            binaryIdentifier++;
        }

        return new BinaryClusterScoreResults(clusterScore, context);
    }

    protected void removeNotExportedInteractionEvidencesFrom(IntactBinaryInteraction binary, List<InteractionImpl> exportedInteractionEvidences){
        List<CrossReference> publicationsToRemove = new ArrayList(binary.getPublications());
        List<InteractionDetectionMethod> methodsToRemove = new ArrayList(binary.getDetectionMethods());
        List<InteractionType> typeToRemove = new ArrayList(binary.getInteractionTypes());

        for (InteractionImpl interaction : exportedInteractionEvidences){

            Experiment exp = interaction.getExperiments().iterator().next();
            CvInteraction detectionMethod = exp.getCvInteraction();
            CvInteractionType interactionType = interaction.getCvInteractionType();

            String detectionMI = detectionMethod.getIdentifier();

            String typeMi = interactionType.getIdentifier();

            String pubmedId = exp.getPublication().getPublicationId();

            for (CrossReference ref : binary.getPublications()){
                if (ref.getIdentifier().equals(pubmedId)){
                    publicationsToRemove.remove(ref);
                    break;
                }
            }

            for (InteractionDetectionMethod method : binary.getDetectionMethods()){
                if (method.getIdentifier().equals(detectionMI)){
                    methodsToRemove.remove(method);
                    break;
                }
            }

            for (InteractionType type : binary.getInteractionTypes()){
                if (type.getIdentifier().equals(typeMi)){
                    typeToRemove.remove(type);
                    break;
                }
            }
        }

        binary.getPublications().removeAll(publicationsToRemove);
        binary.getDetectionMethods().removeAll(methodsToRemove);
        binary.getInteractionTypes().removeAll(typeToRemove);
    }

    public BinaryClusterScoreResults exportInteractionsFrom(String mitab) throws UniprotExportException {
        try {

            BinaryClusterScoreResults clusterResults = clusterMiScoreInteractionEligibleUniprotExport(mitab);
            exporter.exportInteractionsFrom(clusterResults);

            //this.interactionClusterScore.saveScoresForSpecificInteractions(fileExport, this.interactionsToBeExported);

            return clusterResults;
        } catch (IOException e) {
            throw new UniprotExportException("It was not possible to convert the data in the mitab file " + mitab + " in an InputStream", e);
        } catch (ConverterException e) {
            throw new UniprotExportException("It was not possible to iterate the binary interactions in the mitab file " + mitab, e);
        }
    }

    public BinaryClusterScoreResults exportInteractions() throws UniprotExportException {
        return exportInteractionsFrom(mitab);
    }

    public InteractionExporter getInteractionExporter() {
        return this.exporter;
    }

    public void setInteractionExporter(InteractionExporter exporter) {
        this.exporter = exporter;
    }
}
