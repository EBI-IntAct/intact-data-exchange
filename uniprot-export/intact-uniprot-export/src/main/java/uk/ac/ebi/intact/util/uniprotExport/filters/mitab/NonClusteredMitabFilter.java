package uk.ac.ebi.intact.util.uniprotExport.filters.mitab;

import org.apache.log4j.Logger;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.*;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.enfin.mi.cluster.MethodTypePair;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.exporters.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterConfig;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterContext;
import uk.ac.ebi.intact.util.uniprotExport.results.ExportedClusteredInteractions;
import uk.ac.ebi.intact.util.uniprotExport.results.MiClusterScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntActClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntactCluster;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This filter is selecting binary interactions eligible for uniprot export from a mitab file (one line = one non clustered IntAct binary interaction) and
 * will compute the mi score of each binary interaction
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>22-Oct-2010</pre>
 */

public class NonClusteredMitabFilter extends AbstractMitabFilter {
    private static final Logger logger = Logger.getLogger(NonClusteredMitabFilter.class);
    protected PsimiTabReader mitabReader;

    public NonClusteredMitabFilter(InteractionExporter exporter, String mitab){
        super(exporter, mitab);
        this.mitabReader = new PsimiTabReader();
    }

    public NonClusteredMitabFilter(InteractionExporter exporter){
        super(exporter);
        this.mitabReader = new PsimiTabReader();
    }

    protected MiClusterScoreResults computeMiScoreInteractionEligibleUniprotExport(String mitabFile) throws IOException, ConverterException {
        IntActClusterScore clusterScore = new IntActClusterScore();
        IntActClusterScore negativeClusterScore = new IntActClusterScore();
        MiClusterContext context = new MiClusterContext();
        context.setTranscriptsWithDifferentMasterAcs(this.transcriptsWithDifferentParentAcs);
        context.setInteractionComponentXrefs(this.interactionComponentXrefs);

        FilterConfig config = FilterContext.getInstance().getConfig();
        boolean excludeSpokeExpanded = config.excludeSpokeExpandedInteractions();
        boolean excludeNonUniprotInteractors = config.excludeNonUniprotInteractors();

        File mitabAsFile = new File(mitabFile);
        Iterator<BinaryInteraction> iterator = mitabReader.iterate(new FileInputStream(mitabAsFile));

        // the binary interactions to cluster
        List<BinaryInteraction> interactionToProcess = new ArrayList<BinaryInteraction>();

        while (iterator.hasNext()){
            interactionToProcess.clear();
            while (interactionToProcess.size() < 200 && iterator.hasNext()){
                BinaryInteraction<Interactor> interaction = iterator.next();
                String intactAc = null;

                for (CrossReference ref : interaction.getInteractionAcs()){
                    if (ref.getDatabase().equalsIgnoreCase(INTACT)){
                        intactAc = ref.getIdentifier();
                        break;
                    }
                }

                Interactor interactorA = interaction.getInteractorA();
                String uniprotA = null;
                Interactor interactorB = interaction.getInteractorB();
                String uniprotB = null;

                if (interactorA != null){
                    CrossReference uniprot1 = null;

                    for (CrossReference refA : interactorA.getIdentifiers()){
                        if (UNIPROT.equalsIgnoreCase(refA.getDatabase())){
                            uniprotA = refA.getIdentifier();
                            //if (uniprotA.contains(FEATURE_CHAIN)){
                            //uniprotA = refA.getIdentifier().substring(0, uniprotA.indexOf(FEATURE_CHAIN));
                            //}
                            uniprot1 = refA;
                        }
                    }

                    interactorA.getIdentifiers().clear();

                    if (uniprot1 != null){
                        interactorA.getIdentifiers().add(uniprot1);
                    }
                }
                if (interactorB != null){
                    CrossReference uniprot2 = null;

                    for (CrossReference refB : interactorB.getIdentifiers()){
                        if (UNIPROT.equalsIgnoreCase(refB.getDatabase())){
                            uniprotB = refB.getIdentifier();
                            //if (uniprotA.contains(FEATURE_CHAIN)){
                            //uniprotA = refA.getIdentifier().substring(0, uniprotA.indexOf(FEATURE_CHAIN));
                            //}
                            uniprot2 = refB;
                        }
                    }
                    interactorB.getIdentifiers().clear();

                    if (uniprot2 != null){
                        interactorB.getIdentifiers().add(uniprot2);
                    }
                }

                // process intra molecular interactions as self interactions
                if (interactorA == null){
                    uniprotA = uniprotB;
                    interaction.setInteractorA(interactorB);
                }
                else if (uniprotB == null){
                    uniprotB = uniprotA;
                    interaction.setInteractorB(interactorA);
                }

                if (intactAc != null){
                    if (excludeNonUniprotInteractors && uniprotA != null && uniprotB != null){
                        processClustering(context, interactionToProcess, interaction, intactAc, interaction.getInteractorA(), uniprotA, interaction.getInteractorB(), uniprotB, excludeSpokeExpanded);
                    }
                    else if (!excludeNonUniprotInteractors){
                        processClustering(context, interactionToProcess, interaction, intactAc, interaction.getInteractorA(), uniprotA, interaction.getInteractorB(), uniprotB, excludeSpokeExpanded);
                    }
                }
            }

            if (!interactionToProcess.isEmpty()){
                clusterScore.setBinaryInteractionIterator(interactionToProcess.iterator());
                clusterScore.runService();
            }
        }

        // process negative interactions not in mitab
        if (!this.negativeInteractions.isEmpty()){
            super.clusterNegativeIntactInteractions(context, negativeClusterScore);
        }

        // TODO - negative interactions and intra molecular are not in mitab!!!

        return new MiClusterScoreResults(new ExportedClusteredInteractions(clusterScore), new ExportedClusteredInteractions(negativeClusterScore), context);
    }

    private void processClustering(MiClusterContext context, List<BinaryInteraction> interactionToProcess, BinaryInteraction<Interactor> interaction, String intactAc, Interactor interactorA, String uniprotA, Interactor interactorB, String uniprotB, boolean excludedSpokeExpanded) {
        if (this.eligibleInteractionsForUniprotExport.contains(intactAc)){

            FilterUtils.processGeneNames(interactorA, uniprotA, interactorB, uniprotB, context);
            processMiTerms(interaction, context);
            removeNonPubmedPublicationsFrom(interaction);

            List<CrossReference> detectionMethods = interaction.getDetectionMethods();
            String detectionMI = detectionMethods.iterator().next().getIdentifier();

            List<CrossReference> interactionTypes = interaction.getInteractionTypes();
            String typeMi = interactionTypes.iterator().next().getIdentifier();

            MethodTypePair entry = new MethodTypePair(detectionMI, typeMi);
            context.getInteractionToMethod_type().put(intactAc, entry);

            removeNonIntactXrefsFrom(interaction.getInteractorA().getAlternativeIdentifiers());
            interaction.getInteractorA().getAliases().clear();
            removeNonIntactXrefsFrom(interaction.getInteractorB().getAlternativeIdentifiers());
            interaction.getInteractorB().getAliases().clear();

            if (!interaction.getComplexExpansion().isEmpty() && !excludedSpokeExpanded){
                logger.info(intactAc + " passes the filters");
                interactionToProcess.add(interaction);

                context.getSpokeExpandedInteractions().add(intactAc);
            }
            else if (interaction.getComplexExpansion().isEmpty()) {
                logger.info(intactAc + " passes the filters");
                interactionToProcess.add(interaction);
            }
            else if (!interaction.getComplexExpansion().isEmpty()){
                context.getSpokeExpandedInteractions().add(intactAc);
            }
        }
    }

    public MiClusterScoreResults exportInteractionsFrom(String mitab) throws UniprotExportException {
        try {
            logger.info("Filtering and clustering interactions for uniprot export... \n");
            MiClusterScoreResults clusterResults = computeMiScoreInteractionEligibleUniprotExport(mitab);
            logger.info("Exporting interactions... \n");
            exporter.exportInteractionsFrom(clusterResults);
            logger.info(clusterResults.getPositiveClusteredInteractions().getInteractionsToExport().size() + " positive binary interactions to export");
            logger.info(clusterResults.getNegativeClusteredInteractions().getInteractionsToExport().size() + " negative binary interactions to export");


            return clusterResults;
        } catch (IOException e) {
            throw new UniprotExportException("It was not possible to convert the data in the mitab file " + mitab + " in an InputStream", e);
        } catch (ConverterException e) {
            throw new UniprotExportException("It was not possible to iterate the binary interactions in the mitab file " + mitab, e);
        }
    }

    /**
     * Save the results of cluster and filtering without export rules
     * @param mitab
     * @param mitabResults
     * @throws UniprotExportException
     */
    public void saveClusterAndFilterResultsFrom(String mitab, String mitabResults) throws UniprotExportException {
        try {
            logger.info("Filtering and clustering interactions for uniprot export... \n");
            MiClusterScoreResults clusterResults = computeMiScoreInteractionEligibleUniprotExport(mitab);
            logger.info("Saving interactions... \n");

            IntactCluster positiveCluster = clusterResults.getPositiveClusteredInteractions().getCluster();
            IntactCluster negativeCluster = clusterResults.getNegativeClusteredInteractions().getCluster();

            positiveCluster.saveClusteredInteractions(mitabResults, positiveCluster.getEncoreInteractionCluster().keySet());
            negativeCluster.saveClusteredInteractions(mitabResults+"_negative.txt", negativeCluster.getEncoreInteractionCluster().keySet());

        } catch (IOException e) {
            throw new UniprotExportException("It was not possible to convert the data in the mitab file " + mitab + " in an InputStream", e);
        } catch (ConverterException e) {
            throw new UniprotExportException("It was not possible to iterate the binary interactions in the mitab file " + mitab, e);
        }
    }

    /**
     * Cluster all the binary interactions in the mitab file and save in clustered mitab file
     * @param mitabFile
     * @param clusteredMitabFile
     * @throws IOException
     * @throws ConverterException
     */
    public void clusterAndComputeMiScoreInteraction(String mitabFile, String clusteredMitabFile) throws IOException, ConverterException {
        IntActClusterScore clusterScore = new IntActClusterScore();

        File mitabAsFile = new File(mitabFile);
        Iterator<BinaryInteraction> iterator = mitabReader.iterate(new FileInputStream(mitabAsFile));

        // the binary interactions to cluster
        List<BinaryInteraction> interactionToProcess = new ArrayList<BinaryInteraction>();

        while (iterator.hasNext()){
            interactionToProcess.clear();
            while (interactionToProcess.size() < 200 && iterator.hasNext()){
                BinaryInteraction interaction = (BinaryInteraction) iterator.next();

                interactionToProcess.add(interaction);
            }

            clusterScore.setBinaryInteractionIterator(interactionToProcess.iterator());
            clusterScore.runService();
        }

        clusterScore.saveCluster(clusteredMitabFile);
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
