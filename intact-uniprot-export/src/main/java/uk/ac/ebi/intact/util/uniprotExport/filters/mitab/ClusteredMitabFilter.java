package uk.ac.ebi.intact.util.uniprotExport.filters.mitab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Experiment;
import psidev.psi.mi.jami.model.ParticipantEvidence;
import psidev.psi.mi.jami.model.Stoichiometry;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.enfin.mi.cluster.MethodTypePair;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.dao.InteractionDao;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractionEvidence;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.exporters.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterConfig;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterContext;
import uk.ac.ebi.intact.util.uniprotExport.results.ExportedClusteredInteractions;
import uk.ac.ebi.intact.util.uniprotExport.results.MiClusterScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.BinaryClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

import javax.persistence.EntityTransaction;
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

    private static final Logger logger = LoggerFactory.getLogger(ClusteredMitabFilter.class);

    private IntactDao intactDao;
    protected PsimiTabReader mitabReader;

    public ClusteredMitabFilter(InteractionExporter exporter, String mitab){
        super(exporter, mitab);
        this.mitabReader = new PsimiTabReader();
    }

    public ClusteredMitabFilter(InteractionExporter exporter){
        super(exporter);
        this.mitabReader = new PsimiTabReader();
    }

    protected MiClusterScoreResults clusterMiScoreInteractionEligibleUniprotExport(String mitabFile) throws IOException, ConverterException {
        BinaryClusterScore clusterScore = new BinaryClusterScore();
        BinaryClusterScore negativeClusterScore = new BinaryClusterScore();
        MiClusterContext context = new MiClusterContext();
        context.setTranscriptsWithDifferentMasterAcs(this.transcriptsWithDifferentParentAcs);
        context.setInteractionComponentXrefs(this.interactionComponentXrefs);

        FilterConfig config = FilterContext.getInstance().getConfig();
        boolean excludeSpokeExpanded = config.excludeSpokeExpandedInteractions();
        boolean excludeNonUniprotInteractors = config.excludeNonUniprotInteractors();
        boolean excludeIntraMolecular = config.isExcludeIntraMolecularInteractions();

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

            // process intra molecular interactions ?
            if (excludeIntraMolecular && (uniprotA == null || uniprotB == null)){
                logger.info((uniprotA != null ? uniprotA : uniprotB) + " does not pass the filters because is intra molecular");
            }
            else{
                if (interactorA == null){
                    uniprotA = uniprotB;
                }
                else if (interactorB == null){
                    uniprotB = uniprotA;
                }

                if (!intactAcs.isEmpty()){
                    if (excludeNonUniprotInteractors && uniprotA != null && uniprotB != null){
                        if (!interaction.isNegativeInteraction()){
                            processClustering(clusterScore, context, binaryIdentifier, interaction, intactAcs, interactorA, uniprotA, interactorB, uniprotB, excludeSpokeExpanded, false);
                        }
                        else {
                            processClustering(negativeClusterScore, context, binaryIdentifier, interaction, intactAcs, interactorA, uniprotA, interactorB, uniprotB, excludeSpokeExpanded, true);
                        }
                    }
                    else if (!excludeNonUniprotInteractors){
                        if (!interaction.isNegativeInteraction()){
                            processClustering(clusterScore, context, binaryIdentifier, interaction, intactAcs, interactorA, uniprotA, interactorB, uniprotB, excludeSpokeExpanded, false);
                        }
                        else{
                            processClustering(negativeClusterScore, context, binaryIdentifier, interaction, intactAcs, interactorA, uniprotA, interactorB, uniprotB, excludeSpokeExpanded, true);

                        }
                    }
                }
            }

            binaryIdentifier++;
        }

        return new MiClusterScoreResults(new ExportedClusteredInteractions(clusterScore), new ExportedClusteredInteractions(negativeClusterScore), context);
    }

    private void processClustering(BinaryClusterScore clusterScore, MiClusterContext context, Integer binaryIdentifier, BinaryInteraction<Interactor> interaction, Set<String> intactAcs, Interactor interactorA, String uniprotA, Interactor interactorB, String uniprotB, boolean excludeSpokeExpanded, boolean isNegative) {
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();
        InteractionDao interactionDao = intactDao.getInteractionDao();

        List<IntactInteractionEvidence> interactionsInIntact = new ArrayList<>();
        for (String intactAc: intactAcs) {
            interactionsInIntact.add(interactionDao.getByAc(intactAc));
        }
        List<IntactInteractionEvidence> interactionsInIntactPassingExport = new ArrayList(interactionsInIntact);

        boolean canBeProcessed = false;

        for (IntactInteractionEvidence intact : interactionsInIntact){
            String intactAc = intact.getAc();

            if (!this.interactionsToExclude.contains(intactAc)){
                Experiment experiment = intact.getExperiment();
                if (experiment != null) {
                    CvTerm detectionMethod = experiment.getInteractionDetectionMethod();
                    CvTerm interactionType = intact.getInteractionType();

                    if (detectionMethod != null && interactionType != null){
                        canBeProcessed = true;

                        String detectionMI = detectionMethod.getMIIdentifier();

                        String typeMi = interactionType.getMIIdentifier();

                        MethodTypePair entry = new MethodTypePair(detectionMI, typeMi);
                        context.getInteractionToMethod_type().put(intactAc, entry);

                        if (!isBinaryInteraction(intact) && !excludeSpokeExpanded){

                            context.getSpokeExpandedInteractions().add(intactAc);
                        }
                        else if (!isBinaryInteraction(intact) && excludeSpokeExpanded){
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

        if (canBeProcessed && !intactAcs.isEmpty()){
            logger.info(interactionsInIntactPassingExport.size() + " intact interactions involving "+uniprotA+" and "+uniprotB+" are eligible for uniprot export");

            FilterUtils.processGeneNames(interactorA, uniprotA, interactorB, uniprotB, context);

            if (interactionsInIntactPassingExport.size() < interactionsInIntact.size()){
                removeNotExportedInteractionEvidencesFrom(interaction, interactionsInIntactPassingExport);
            }

            processMiTerms(interaction, context, intactAcs.iterator().next());
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

        transaction.commit();
    }

    protected void removeNotExportedInteractionEvidencesFrom(BinaryInteraction<Interactor> binary, List<IntactInteractionEvidence> exportedInteractionEvidences){
        List<CrossReference> publicationsToRemove = new ArrayList(binary.getPublications());
        List<CrossReference> interactionsToRemove = new ArrayList(binary.getInteractionAcs());
        List<CrossReference> methodsToRemove = new ArrayList(binary.getDetectionMethods());
        List<CrossReference> typeToRemove = new ArrayList(binary.getInteractionTypes());

        for (IntactInteractionEvidence interaction : exportedInteractionEvidences){

            Experiment exp = interaction.getExperiment();
            CvTerm detectionMethod = exp.getInteractionDetectionMethod();
            CvTerm interactionType = interaction.getInteractionType();

            String detectionMI = detectionMethod.getMIIdentifier();

            String typeMi = interactionType.getMIIdentifier();

            String pubmedId = exp.getPublication().getPubmedId();

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

            for (CrossReference method : binary.getDetectionMethods()){
                if (method.getIdentifier().equals(detectionMI)){
                    methodsToRemove.remove(method);
                    break;
                }
            }

            for (CrossReference type : binary.getInteractionTypes()){
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

    private static boolean isBinaryInteraction(IntactInteractionEvidence interaction) {
        Collection<ParticipantEvidence> components = interaction.getParticipants();
        int componentCount = components.size();

        if (componentCount == 1) {
            ParticipantEvidence component1 = components.iterator().next();
            // we consider one participant stoichiometry > 2 as binary even if it forms a complex to avoid duplicated binary
            if (component1.getStoichiometry() != null && component1.getStoichiometry().getMinValue() >= 2) {
                logger.debug("Binary interaction " + interaction.getAc() + ". Stoichiometry >= 2, each component with stoichiometry 1");
                return true;
            }
        } else if (componentCount == 2) {
            Iterator<ParticipantEvidence> iterator1 = components.iterator();

            ParticipantEvidence component1 = iterator1.next();
            Stoichiometry stochio1 = component1.getStoichiometry();
            if (stochio1 != null && stochio1.getMinValue() == 1) {
                ParticipantEvidence component2 = iterator1.next();
                if (component2.getStoichiometry() != null && component2.getStoichiometry().getMinValue() == 1) {
                    logger.debug("Binary interaction " + interaction.getAc() + ". Stoichiometry 2, each component with stoichiometry 1");
                    return true;
                }
            } else if (stochio1 != null && stochio1.getMinValue() == 0) {
                ParticipantEvidence component2 = iterator1.next();
                if (component2.getStoichiometry() != null && component2.getStoichiometry().getMinValue() == 0) {
                    logger.debug("Binary interaction " + interaction.getAc() + ". Stoichiometry 0, components 2");
                    return true;
                }
            }
        }
        return false;
    }
}
