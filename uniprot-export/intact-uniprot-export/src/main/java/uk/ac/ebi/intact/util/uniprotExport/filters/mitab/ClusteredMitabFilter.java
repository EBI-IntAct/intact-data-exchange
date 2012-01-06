package uk.ac.ebi.intact.util.uniprotExport.filters.mitab;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.*;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.enfin.mi.cluster.MethodTypePair;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.InteractionDao;
import uk.ac.ebi.intact.model.CvInteraction;
import uk.ac.ebi.intact.model.CvInteractionType;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.InteractionImpl;
import uk.ac.ebi.intact.model.util.InteractionUtils;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.exporters.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterConfig;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterContext;
import uk.ac.ebi.intact.util.uniprotExport.results.ExportedClusteredInteractions;
import uk.ac.ebi.intact.util.uniprotExport.results.MiClusterScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.BinaryClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntActClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

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

public class ClusteredMitabFilter extends AbstractMitabFilter {
    private static final Logger logger = Logger.getLogger(ClusteredMitabFilter.class);
    protected PsimiTabReader mitabReader;

    public ClusteredMitabFilter(InteractionExporter exporter, String mitab){
        super(exporter, mitab);
        this.mitabReader = new PsimiTabReader(false);
    }

    public ClusteredMitabFilter(InteractionExporter exporter){
        super(exporter);
        this.mitabReader = new PsimiTabReader(false);
    }

    protected MiClusterScoreResults clusterMiScoreInteractionEligibleUniprotExport(String mitabFile) throws IOException, ConverterException {
        BinaryClusterScore clusterScore = new BinaryClusterScore();
        IntActClusterScore negativeClusterScore = new IntActClusterScore();
        MiClusterContext context = new MiClusterContext();
        context.setTranscriptsWithDifferentMasterAcs(this.transcriptsWithDifferentParentAcs);
        context.setInteractionComponentXrefs(this.interactionComponentXrefs);

        FilterConfig config = FilterContext.getInstance().getConfig();
        boolean excludeSpokeExpanded = config.excludeSpokeExpandedInteractions();
        boolean excludeNonUniprotInteractors = config.excludeNonUniprotInteractors();

        File mitabAsFile = new File(mitabFile);
        Iterator<BinaryInteraction> iterator = mitabReader.iterate(new FileInputStream(mitabAsFile));

        Integer binaryIdentifier = 1;

        while (iterator.hasNext()){
            BinaryInteraction<Interactor> interaction = iterator.next();

            Set<String> intactAcs = FilterUtils.extractIntactAcFrom(interaction.getInteractionAcs());

            Interactor interactorA = interaction.getInteractorA();
            String uniprotA = null;
            Interactor interactorB = interaction.getInteractorB();
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

            if (!intactAcs.isEmpty()){
                if (excludeNonUniprotInteractors && uniprotA != null && uniprotB != null){
                    processClustering(clusterScore, context, binaryIdentifier, interaction, intactAcs, interactorA, uniprotA, interactorB, uniprotB, excludeSpokeExpanded);
                }
                else if (!excludeNonUniprotInteractors){
                    processClustering(clusterScore, context, binaryIdentifier, interaction, intactAcs, interactorA, uniprotA, interactorB, uniprotB, excludeSpokeExpanded);
                }
            }

            binaryIdentifier++;
        }

        // process negative interactions not in mitab
        if (!this.negativeInteractions.isEmpty()){
            super.clusterNegativeIntactInteractions(context, negativeClusterScore);
        }

        return new MiClusterScoreResults(new ExportedClusteredInteractions(clusterScore), new ExportedClusteredInteractions(negativeClusterScore), context);
    }

    private void processClustering(BinaryClusterScore clusterScore, MiClusterContext context, Integer binaryIdentifier, BinaryInteraction<Interactor> interaction, Set<String> intactAcs, Interactor interactorA, String uniprotA, Interactor interactorB, String uniprotB, boolean excludeSpokeExpanded) {
        TransactionStatus status = IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        InteractionDao interactionDao = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao();

        List<InteractionImpl> interactionsInIntact = interactionDao.getByAc(intactAcs);
        List<InteractionImpl> interactionsInIntactPassingExport = new ArrayList(interactionsInIntact);

        boolean canBeProcessed = false;

        for (InteractionImpl intact : interactionsInIntact){
            String intactAc = intact.getAc();

            if (this.eligibleInteractionsForUniprotExport.contains(intactAc)){
                Collection<Experiment> experiments = intact.getExperiments();
                if (experiments.size() == 1){

                    Experiment exp = experiments.iterator().next();
                    CvInteraction detectionMethod = exp.getCvInteraction();
                    CvInteractionType interactionType = intact.getCvInteractionType();

                    if (detectionMethod != null && interactionType != null){
                        canBeProcessed = true;

                        String detectionMI = detectionMethod.getIdentifier();

                        String typeMi = interactionType.getIdentifier();

                        MethodTypePair entry = new MethodTypePair(detectionMI, typeMi);
                        context.getInteractionToMethod_type().put(intactAc, entry);

                        if (!InteractionUtils.isBinaryInteraction(intact) && !excludeSpokeExpanded){

                            context.getSpokeExpandedInteractions().add(intactAc);
                        }
                        else if (!InteractionUtils.isBinaryInteraction(intact) && excludeSpokeExpanded){
                            context.getSpokeExpandedInteractions().add(intactAc);
                            interactionsInIntactPassingExport.remove(intact);
                        }
                    }
                }
            }
            else{
                interactionsInIntactPassingExport.remove(intact);
            }
        }

        if (canBeProcessed && !interaction.getInteractionAcs().isEmpty()){
            logger.info(interactionsInIntactPassingExport.size() + " intact interactions involving "+uniprotA+" and "+uniprotB+" are eligible for uniprot export");

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

    protected void removeNotExportedInteractionEvidencesFrom(BinaryInteraction<Interactor> binary, List<InteractionImpl> exportedInteractionEvidences){
        List<CrossReference> publicationsToRemove = new ArrayList(binary.getPublications());
        List<CrossReference> interactionsToRemove = new ArrayList(binary.getInteractionAcs());
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

            for (CrossReference ref : binary.getInteractionAcs()){
                if (ref.getIdentifier().equals(interaction.getAc())){
                    interactionsToRemove.remove(ref);
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
        binary.getInteractionAcs().removeAll(interactionsToRemove);
    }

    public MiClusterScoreResults exportInteractionsFrom(String mitab) throws UniprotExportException {
        try {
            logger.info("Creating cluster of binary interactions...");
            MiClusterScoreResults clusterResults = clusterMiScoreInteractionEligibleUniprotExport(mitab);
            logger.info("Exporting binary interactions...");
            exporter.exportInteractionsFrom(clusterResults);

            ExportedClusteredInteractions positiveInteractions = clusterResults.getPositiveClusteredInteractions();
            ExportedClusteredInteractions negativeInteractions = clusterResults.getNegativeClusteredInteractions();
            logger.info(positiveInteractions.getInteractionsToExport().size() + " positive binary interactions to export");
            logger.info(negativeInteractions.getInteractionsToExport().size() + " negative binary interactions to export");

            return clusterResults;
        } catch (IOException e) {
            throw new UniprotExportException("It was not possible to convert the data in the mitab file " + mitab + " in an InputStream", e);
        } catch (ConverterException e) {
            throw new UniprotExportException("It was not possible to iterate the binary interactions in the mitab file " + mitab, e);
        }
    }

    @Override
    public MiClusterScoreResults exportInteractions() throws UniprotExportException {
        return exportInteractionsFrom(mitab);
    }

    @Override
    public InteractionExporter getInteractionExporter() {
        return this.exporter;
    }

    @Override
    public void setInteractionExporter(InteractionExporter exporter) {
        this.exporter = exporter;
    }
}
