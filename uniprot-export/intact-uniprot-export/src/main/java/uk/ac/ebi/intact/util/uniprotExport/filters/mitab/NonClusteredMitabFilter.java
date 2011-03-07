package uk.ac.ebi.intact.util.uniprotExport.filters.mitab;

import org.apache.log4j.Logger;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.InteractionDetectionMethod;
import psidev.psi.mi.tab.model.InteractionType;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabReader;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.exporters.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterConfig;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterContext;
import uk.ac.ebi.intact.util.uniprotExport.results.MethodAndTypePair;
import uk.ac.ebi.intact.util.uniprotExport.results.MiClusterScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntActInteractionClusterScore;
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
    protected IntactPsimiTabReader mitabReader;

    public NonClusteredMitabFilter(InteractionExporter exporter, String mitab){
        super(exporter, mitab);
        this.mitabReader = new IntactPsimiTabReader(true);
    }

    public NonClusteredMitabFilter(InteractionExporter exporter){
        super(exporter);
        this.mitabReader = new IntactPsimiTabReader(true);
    }

    protected MiClusterScoreResults computeMiScoreInteractionEligibleUniprotExport(String mitabFile) throws IOException, ConverterException {
        IntActInteractionClusterScore clusterScore = new IntActInteractionClusterScore();
        IntActInteractionClusterScore negativeClusterScore = new IntActInteractionClusterScore();
        MiClusterContext context = new MiClusterContext();

        FilterConfig config = FilterContext.getInstance().getConfig();
        boolean excludeSpokeExpanded = config.excludeSpokeExpandedInteractions();
        boolean excludeNonUniprotInteractors = config.excludeNonUniprotInteractors();

        File mitabAsFile = new File(mitabFile);
        Iterator<BinaryInteraction> iterator = mitabReader.iterate(new FileInputStream(mitabAsFile));

        // the binary interactions to cluster
        List<BinaryInteraction> interactionToProcess = new ArrayList<BinaryInteraction>();
        // the negative binary interactions to cluster
        List<BinaryInteraction> negativeInteractionToProcess = new ArrayList<BinaryInteraction>();

        while (iterator.hasNext()){
            interactionToProcess.clear();
            while (interactionToProcess.size() < 200 && iterator.hasNext()){
                IntactBinaryInteraction interaction = (IntactBinaryInteraction) iterator.next();
                String intactAc = null;

                for (CrossReference ref : interaction.getInteractionAcs()){
                    if (ref.getDatabase().equalsIgnoreCase(INTACT)){
                        intactAc = ref.getIdentifier();
                        break;
                    }
                }

                ExtendedInteractor interactorA = interaction.getInteractorA();
                String uniprotA = null;
                ExtendedInteractor interactorB = interaction.getInteractorB();
                String uniprotB = null;

                if (interactorA != null){
                    CrossReference uniprot1 = null;
                    CrossReference intact1 = null;

                    for (CrossReference refA : interactorA.getIdentifiers()){
                        if (UNIPROT.equalsIgnoreCase(refA.getDatabase())){
                            uniprotA = refA.getIdentifier();
                            //if (uniprotA.contains(FEATURE_CHAIN)){
                            //uniprotA = refA.getIdentifier().substring(0, uniprotA.indexOf(FEATURE_CHAIN));
                            //}
                            uniprot1 = refA;
                        }
                        else if (INTACT.equalsIgnoreCase(refA.getDatabase())){

                            intact1 = refA;
                        }
                    }

                    interactorA.getIdentifiers().clear();

                    if (uniprot1 != null){
                        interactorA.getIdentifiers().add(uniprot1);
                    }
                    if (intact1 != null){
                        interactorA.getIdentifiers().add(intact1);
                    }
                }
                if (interactorB != null){
                    CrossReference uniprot2 = null;
                    CrossReference intact2 = null;

                    for (CrossReference refB : interactorB.getIdentifiers()){
                        if (UNIPROT.equalsIgnoreCase(refB.getDatabase())){
                            uniprotB = refB.getIdentifier();
                            //if (uniprotA.contains(FEATURE_CHAIN)){
                            //uniprotA = refA.getIdentifier().substring(0, uniprotA.indexOf(FEATURE_CHAIN));
                            //}
                            uniprot2 = refB;
                        }
                        else if (INTACT.equalsIgnoreCase(refB.getDatabase())){

                            intact2 = refB;
                        }
                    }

                    interactorB.getIdentifiers().clear();

                    if (uniprot2 != null){
                        interactorB.getIdentifiers().add(uniprot2);
                    }
                    if (intact2 != null){
                        interactorB.getIdentifiers().add(intact2);
                    }
                }

                if (intactAc != null){
                    if (excludeNonUniprotInteractors && uniprotA != null && uniprotB != null){
                        processClustering(context, interactionToProcess, interaction, intactAc, interactorA, uniprotA, interactorB, uniprotB, excludeSpokeExpanded);
                    }
                    else if (!excludeNonUniprotInteractors){
                        processClustering(context, interactionToProcess, interaction, intactAc, interactorA, uniprotA, interactorB, uniprotB, excludeSpokeExpanded);
                    }
                }
            }

            if (interactionToProcess.isEmpty()){
                clusterScore.setBinaryInteractionList(interactionToProcess);
                clusterScore.runService();
            }
        }

        // process negative interactions not in mitab
        if (!this.negativeInteractions.isEmpty()){
            super.clusterNegativeIntactInteractions(this.negativeInteractions, context, negativeClusterScore);
        }

        // TODO - negative interactions and intra molecular are not in mitab!!!

        return new MiClusterScoreResults(clusterScore, negativeClusterScore, context);
    }

    private void processClustering(MiClusterContext context, List<BinaryInteraction> interactionToProcess, IntactBinaryInteraction interaction, String intactAc, ExtendedInteractor interactorA, String uniprotA, ExtendedInteractor interactorB, String uniprotB, boolean excludedSpokeExpanded) {
        if (this.eligibleInteractionsForUniprotExport.contains(intactAc)){

            FilterUtils.processGeneNames(interactorA, uniprotA, interactorB, uniprotB, context);
            processMiTerms(interaction, context);

            List<InteractionDetectionMethod> detectionMethods = interaction.getDetectionMethods();
            String detectionMI = detectionMethods.iterator().next().getIdentifier();

            List<InteractionType> interactionTypes = interaction.getInteractionTypes();
            String typeMi = interactionTypes.iterator().next().getIdentifier();

            MethodAndTypePair entry = new MethodAndTypePair(detectionMI, typeMi);
            context.getInteractionToMethod_type().put(intactAc, entry);

            interaction.getInteractorA().getAlternativeIdentifiers().clear();
            interaction.getInteractorA().getAliases().clear();
            interaction.getInteractorB().getAlternativeIdentifiers().clear();
            interaction.getInteractorB().getAliases().clear();

            if (!interaction.getExpansionMethods().isEmpty() && !excludedSpokeExpanded){
                logger.info(intactAc + " passes the filters");
                interactionToProcess.add(interaction);

                context.getSpokeExpandedInteractions().add(intactAc);
            }
            else if (interaction.getExpansionMethods().isEmpty()) {
                logger.info(intactAc + " passes the filters");
                interactionToProcess.add(interaction);
            }
            else if (!interaction.getExpansionMethods().isEmpty()){
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
            logger.info(clusterResults.getInteractionsToExport().size() + " binary interactions to export");

            //this.interactionClusterScore.getScorePerInteractions(fileExport, this.interactionsToBeExported);

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
