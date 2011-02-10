package uk.ac.ebi.intact.util.uniprotExport.filters;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import psidev.psi.mi.tab.model.*;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.converters.Intact2BinaryInteractionConverter;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.exporters.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.exporters.QueryFactory;
import uk.ac.ebi.intact.util.uniprotExport.exporters.rules.ExporterBasedOnDetectionMethod;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterConfig;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterContext;
import uk.ac.ebi.intact.util.uniprotExport.miscore.extension.IntActFileMiScoreDistribution;
import uk.ac.ebi.intact.util.uniprotExport.results.MethodAndTypePair;
import uk.ac.ebi.intact.util.uniprotExport.results.MiClusterScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.results.UniprotExportResults;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntActInteractionClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntactCluster;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * This filter is selecting interactions eligible for uniprot export from IntAct.
 * It will convert the IntAct interactions into binary interactions and will compute the mi score for each binary interaction.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15-Sep-2010</pre>
 */

public class IntactFilter implements InteractionFilter {

    private static final Logger logger = Logger.getLogger(IntactFilter.class);

    /**
     * the binary interaction converter
     */
    private Intact2BinaryInteractionConverter interactionConverter;

    /**
     * Threshold value for the maximum number of binary interactions we want to process at the same time.
     */
    private static final int MAX_NUMBER_INTERACTION = 200;

    public static final String UNIPROT_DATABASE = "uniprotkb";

    private InteractionExporter exporter;
    private QueryFactory queryFactory;

    /**
     * we create a new IntactFilter
     */
    public IntactFilter(InteractionExporter exporter){
        this.interactionConverter = new Intact2BinaryInteractionConverter();
        this.exporter = exporter;
        this.queryFactory = new QueryFactory();
    }

    /**
     * Converts a file containing mi-scores into a diagram
     * @param fileContainingResults : the current file containing the results
     */
    public void convertResultsIntoDiagram(String diagramName, String fileContainingResults){
        IntActFileMiScoreDistribution fileDistribution = new IntActFileMiScoreDistribution(fileContainingResults);

        fileDistribution.createChart(diagramName);
    }

    /**
     * Computes the MI cluster score for the list of interactions
     * @param interactions : the list of interactions for what we want to compute the MI cluster score
     */
    public MiClusterScoreResults computeMiScoresFor(List<String> interactions){
        IntActInteractionClusterScore clusterScore = new IntActInteractionClusterScore();
        MiClusterContext context = new MiClusterContext();

        MiClusterScoreResults results = new MiClusterScoreResults(clusterScore, context);
        int i = 0;
        // the list of binary interactions to process
        List<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>();

        System.out.println(interactions.size() + " interactions in IntAct will be processed.");

        // we process all the interactions of the list per chunk of 200 interactions
        while (i < interactions.size()){
            // we clear the previous chunk of binary interactions to only keep 200 binary interaction at a time
            binaryInteractions.clear();

            // we convert into binary interactions until we fill up the binary interactions list to MAX_NUMBER_INTERACTION. we get the new incremented i.
            i = convertIntoBinaryInteractions(interactions, i, binaryInteractions, context);

            // we compute the mi score for the list of binary interactions
            processMiClustering(binaryInteractions, MAX_NUMBER_INTERACTION, clusterScore);

            int interactionsToProcess = interactions.size() - Math.min(i, interactions.size());
            System.out.println("Still " + interactionsToProcess + " interactions to process in IntAct.");
        }

        return results;
    }

    /**
     * Computes the Mi score for the interactions involving only uniprot proteins
     * @param interactions
     * @throws uk.ac.ebi.intact.util.uniprotExport.UniprotExportException
     */
    public MiClusterScoreResults processExportWithFilterOnNonUniprot(List<String> interactions) throws UniprotExportException {
        MiClusterContext context = new MiClusterContext();
        IntActInteractionClusterScore clusterScore = new IntActInteractionClusterScore();

        FilterConfig config = FilterContext.getInstance().getConfig();
        boolean excludeSpokeExpanded = config.excludeSpokeExpandedInteractions();
        boolean excludeNonUniprotInteractors = config.excludeNonUniprotInteractors();

        MiClusterScoreResults results = new MiClusterScoreResults(clusterScore, context);
        int i = 0;
        // the list of binary interactions to process
        List<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>();

        logger.info(interactions.size() + " interactions in IntAct will be processed.");

        // we process all the interactions of the list per chunk of 200 interactions
        while (i < interactions.size()){
            // we clear the previous chunk of binary interactions to only keep 200 binary interaction at a time
            binaryInteractions.clear();

            if (excludeSpokeExpanded && excludeNonUniprotInteractors){
                i = convertIntoBinaryInteractions(interactions, i, binaryInteractions, context);
            }
            else {
                // we convert into binary interactions until we fill up the binary interactions list to MAX_NUMBER_INTERACTION. we get the new incremented i.
                i = convertIntoBinaryInteractionsExcludeNonUniprotProteins(interactions, i, binaryInteractions, context);
            }

            // we compute the mi score for the list of binary interactions
            processMiClustering(binaryInteractions, MAX_NUMBER_INTERACTION, clusterScore);

            int interactionsToProcess = interactions.size() - Math.min(i, interactions.size());
            logger.info("Still " + interactionsToProcess + " interactions to process in IntAct.");
        }

        return results;
    }

    /**
     * Converts the interactions from i into binary interactions until the list of binary interactions reach MAX_NUMBER_INTERACTIONS
     * @param interactions : the list of interactions accessions to process
     * @param i : the index from what we want to process the interactions of te list
     * @param binaryInteractions : the list which will contain the binary interactions
     * @return the index where we stopped in the list of interactions
     */
    private int convertIntoBinaryInteractions(List<String> interactions, int i, Collection<BinaryInteraction> binaryInteractions, MiClusterContext context) {
        // number of converted binary interactions
        int k = 0;

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();
        TransactionStatus transactionStatus = dataContext.beginTransaction();

        // we want to stp when the list of binary interactions exceeds MAX_NUMBER_INTERACTIONS or reaches the end of the list of interactions
        while (k < MAX_NUMBER_INTERACTION && i < interactions.size()){

            // get the IntAct interaction object
            String interactionAc = interactions.get(i);
            Interaction intactInteraction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(interactionAc);

            // the interaction must exist in Intact
            if (intactInteraction != null){
                // the interaction can be converted into binary interaction
                if (this.interactionConverter.getExpansionStrategy().isExpandable(intactInteraction)){
                    try {
                        // we convert the interaction in binary interaction
                        Collection<IntactBinaryInteraction> toBinary = this.interactionConverter.convert(intactInteraction);
                        if (toBinary.size() == 1){
                            logger.info("Processing interaction " + interactionAc);
                            processClusterContext(context, interactionAc, toBinary);

                            for (IntactBinaryInteraction binary : toBinary){

                                ExtendedInteractor interactorA = binary.getInteractorA();
                                String uniprotA = null;
                                ExtendedInteractor interactorB = binary.getInteractorB();
                                String uniprotB = null;

                                for (CrossReference refA : interactorA.getIdentifiers()){
                                    if (refA.getDatabase().equalsIgnoreCase("uniprotkb")){
                                        uniprotA = refA.getIdentifier();
                                        break;
                                    }
                                }
                                for (CrossReference refB : interactorB.getIdentifiers()){
                                    if (refB.getDatabase().equalsIgnoreCase("uniprotkb")){
                                        uniprotB = refB.getIdentifier();
                                        break;
                                    }
                                }

                                FilterUtils.processGeneNames(interactorA, uniprotA, interactorB, uniprotB, context);

                            }

                            binaryInteractions.addAll(toBinary);
                        }
                        else {
                            logger.info("The interaction " + interactionAc + ", " + intactInteraction.getShortLabel() + " is not a true binary interaction and is excluded.");
                        }
                    } catch (Exception e) {
                        logger.error("The interaction " + interactionAc + ", " + intactInteraction.getShortLabel() + " cannot be converted into binary interactions and is excluded.", e);
                    }
                }
                // if the interaction cannot be converted into binary interaction, we ignore the interaction.
                else {
                    logger.info("The interaction " + interactionAc + ", " + intactInteraction.getShortLabel() + " cannot be converted into binary interactions and is excluded.");
                }
            }
            else {
                logger.error("The interaction " + interactionAc + " doesn't exist in the database and is excluded.");
            }

            // we increments the number of binary interactions
            k = binaryInteractions.size();
            // we increments the index in the interaction list
            i++;
        }
        dataContext.commitTransaction(transactionStatus);

        return i;
    }

    /**
     * Converts the interactions from i into binary interactions until the list of binary interactions reach MAX_NUMBER_INTERACTIONS
     * Add a filter to exclude interactions involving non uniprot proteins
     * @param interactions : the list of interactions accessions to process
     * @param i : the index from what we want to process the interactions of te list
     * @param binaryInteractions : the list which will contain the binary interactions
     * @return the index where we stopped in the list of interactions
     */
    private int convertIntoBinaryInteractionsExcludeNonUniprotProteins(List<String> interactions, int i, Collection<BinaryInteraction> binaryInteractions, MiClusterContext context) {
        // number of converted binary interactions
        int k = 0;

        FilterConfig config = FilterContext.getInstance().getConfig();
        boolean excludeSpokeExpanded = config.excludeSpokeExpandedInteractions();
        boolean excludeNonUniprotInteractors = config.excludeNonUniprotInteractors();

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();
        TransactionStatus transactionStatus = dataContext.beginTransaction();

        // we want to stp when the list of binary interactions exceeds MAX_NUMBER_INTERACTIONS or reaches the end of the list of interactions
        while (k < MAX_NUMBER_INTERACTION && i < interactions.size()){

            // get the IntAct interaction object
            String interactionAc = interactions.get(i);
            Interaction intactInteraction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(interactionAc);

            // the interaction must exist in Intact
            if (intactInteraction != null){
                // the interaction can be converted into binary interaction
                if (this.interactionConverter.getExpansionStrategy().isExpandable(intactInteraction)){
                    try {
                        // we convert the interaction in binary interaction
                        Collection<IntactBinaryInteraction> toBinary = this.interactionConverter.convert(intactInteraction);

                        if (excludeSpokeExpanded && toBinary.size() == 1){
                            logger.info("Processing interaction " + interactionAc);
                            processClustering(binaryInteractions, context, interactionAc, toBinary, excludeNonUniprotInteractors);
                        }
                        else if (!excludeSpokeExpanded){
                            logger.info("Processing interaction " + interactionAc);
                            processClustering(binaryInteractions, context, interactionAc, toBinary, excludeNonUniprotInteractors);
                        }
                    } catch (Exception e) {
                        logger.error("The interaction " + interactionAc + ", " + intactInteraction.getShortLabel() + " cannot be converted into binary interactions and is excluded.", e);
                    }
                }
                // if the interaction cannot be converted into binary interaction, we ignore the interaction.
                else {
                    logger.info("The interaction " + interactionAc + ", " + intactInteraction.getShortLabel() + " cannot be converted into binary interactions and is excluded.");
                }
            }
            else {
                logger.error("The interaction " + interactionAc + " doesn't exist in the database and is excluded.");
            }

            // we increments the number of binary interactions
            k = binaryInteractions.size();
            // we increments the index in the interaction list
            i++;
        }
        dataContext.commitTransaction(transactionStatus);

        return i;
    }

    private void processClustering(Collection<BinaryInteraction> binaryInteractions, MiClusterContext context, String interactionAc, Collection<IntactBinaryInteraction> toBinary, boolean excludeNonUniprot) {
        processClusterContext(context, interactionAc, toBinary);

        for (IntactBinaryInteraction binary : toBinary){

            ExtendedInteractor interactorA = binary.getInteractorA();
            String uniprotA = null;
            ExtendedInteractor interactorB = binary.getInteractorB();
            String uniprotB = null;

            if (interactorA != null){
                for (CrossReference refA : interactorA.getIdentifiers()){
                    if (refA.getDatabase().equalsIgnoreCase("uniprotkb")){
                        uniprotA = refA.getIdentifier();
                        break;
                    }
                }
            }
            if (interactorB != null){
                for (CrossReference refB : interactorB.getIdentifiers()){
                    if (refB.getDatabase().equalsIgnoreCase("uniprotkb")){
                        uniprotB = refB.getIdentifier();
                        break;
                    }
                }
            }

            if ((uniprotA != null && uniprotB != null && excludeNonUniprot) || !excludeNonUniprot){

                FilterUtils.processGeneNames(interactorA, uniprotA, interactorB, uniprotB, context);
                binaryInteractions.add(binary);
            }
        }
    }

    private void processClusterContext(MiClusterContext context, String interactionAc, Collection<IntactBinaryInteraction> toBinary) {
        // process the context information
        List<InteractionDetectionMethod> detectionMethods = toBinary.iterator().next().getDetectionMethods();
        String detectionMI = detectionMethods.iterator().next().getIdentifier();

        if (!context.getMiTerms().containsKey(detectionMI)){
            context.getMiTerms().put(detectionMI, detectionMethods.iterator().next().getText());
        }

        List<InteractionType> interactionTypes = toBinary.iterator().next().getInteractionTypes();
        String typeMi = interactionTypes.iterator().next().getIdentifier();

        if (!context.getMiTerms().containsKey(typeMi)){
            context.getMiTerms().put(typeMi, interactionTypes.iterator().next().getText());
        }

        context.getInteractionToMethod_type().put(interactionAc, new MethodAndTypePair(detectionMI, typeMi));

        if (toBinary.size() > 1){

            context.getSpokeExpandedInteractions().add(interactionAc);
        }
    }

    /**
     *
     * @param binaryInteractions : the list of binary interactions to process
     * @param range : the maximum number of binary interactions we want to process at the same time
     */
    private void processMiClustering(List<BinaryInteraction> binaryInteractions, int range, IntActInteractionClusterScore clusterScore) {
        // the range must be positive
        if (range > 0){

            // number of binary interactions which have been processed
            int numberOfBinaryInteractions = 0;

            // process all the binary interactions
            while (numberOfBinaryInteractions < binaryInteractions.size()){
                try {
                    // we compute the MI cluster score
                    clusterScore.setBinaryInteractionList(binaryInteractions.subList(numberOfBinaryInteractions, numberOfBinaryInteractions + Math.min(range, binaryInteractions.size() - numberOfBinaryInteractions)));
                    clusterScore.runService();
                } catch (Exception e){
                    e.printStackTrace();
                    System.out.println("The score cannot be computed for the list of binary interactions of size " + binaryInteractions.size());
                }

                // increments the number of binary interactions which have been processed
                numberOfBinaryInteractions+= Math.min(range, binaryInteractions.size() - numberOfBinaryInteractions);
            }
        }
        else {
            throw new IllegalArgumentException("we cannot process the miClustering of a list of binary interactions if the range of interactions we can process at the same time is 0.");
        }
    }

    /**
     * Create a Map containing for each interaction as String interactorA-interactorB a MI score extracted from a file
     * @param fileName : the file containing the mi scores
     * @return Map containing for each interaction as String interactorA-interactorB a MI score
     * @throws IOException
     */
    /*private Map<String,Double> buildMapOfMiScoreFromFile(String fileName) throws IOException {

        // the buffer reader
        BufferedReader reader = new BufferedReader(new FileReader(fileName));

        // new Map to fill
        Map<String,Double> totalMiScore = new HashMap<String, Double>();

        // read the file
        String line = reader.readLine();
        while (line != null){

            // we can only read a line containing a score
            if (line.contains(SCORE_SEPARATOR)){
                String [] miScore = line.split(SCORE_SEPARATOR);

                // we normally have only 2 columns : one for the interaction String one for the score
                if (miScore.length == 2){
                    // the interaction description is the first column
                    String interaction = miScore[0];

                    // the score is the second column
                    Double score = Double.parseDouble(miScore[1]);

                    // we want to extract the interactors names from the interaction String containing the interaction id at the beginning
                    if (interaction.contains(INTERACTOR_SEPARATOR)){
                        // the first index of the interactor separator is the befinning of interactorA-interactorB
                        int index = interaction.indexOf(INTERACTOR_SEPARATOR);

                        // this index must be inferior to the interaction String length
                        if (index + 1 < interaction.length()){
                            // extract the interactor names
                            interaction = interaction.substring(index + 1);

                            // put them as a key in the map with the score value
                            totalMiScore.put(interaction, score);
                        }
                        else {
                            System.out.println("the line " + line + " cannot be loaded because is not of the form 'id-interactorA-interactorB:score'");
                        }
                    }
                    else {
                        System.out.println("the line " + line + " cannot be loaded because is not of the form 'id-interactorA-interactorB:score'");
                    }
                }
                else {
                    System.out.println("the line " + line + " cannot be loaded because is not of the form 'id-interactorA-interactorB:score'");
                }
            }
            else {
                System.out.println("the line " + line + " cannot be loaded because is not of the form 'id-interactorA-interactorB:score'");
            }

            line = reader.readLine();
        }

        reader.close();

        return totalMiScore;
    }*/

    /*public void extractMiScoresFromFile(List<String> interactions, String fileContainingTotalScore, String fileContainingData){
        try {

            // we extract the score results from the file
            Map<String, Double> totalNiScore = buildMapOfMiScoreFromFile(fileContainingTotalScore);

            // list of interactions ids exported in uniprot
            Set<String> interactionIdentifiersExported = new HashSet<String>();
            // list of interactions ids not exported in uniprot
            Set<String> interactionIdentifiersAlreadyProcessed = new HashSet<String>();

            // file converters for interactions exported in uniprot
            FileWriter writer1 = new FileWriter(fileContainingData);

            int i = 0;
            Set<BinaryInteraction> binaryInteractions = new HashSet<BinaryInteraction>();

            System.out.println(interactions.size() + " interactions in IntAct will be processed.");

            // each interaction will be processed
            while (i < interactions.size()){
                // we clear the previous binary interactions to keep only 200 binary interaction at the same time
                binaryInteractions.clear();

                // converts the interactions into binary interactions and increments the index in the list of interactions
                i = convertIntoBinaryInteractions(interactions, i, binaryInteractions);

                // filter the computed scores
                extractMiScoreForBinaryInteractionsFromFile(binaryInteractions, interactionIdentifiersExported, totalNiScore);
                //i += 200;
                int interactionsToProcess = interactions.size() - Math.min(i, interactions.size());
                System.out.println("Still " + interactionsToProcess + " interactions to process in IntAct.");
            }

            // close the writers
            writer1.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("We cannot write the results in " + fileContainingData);
        }
    }*/


    /**
     * Extracts the MI cluster score for each interaction exported in uniprot and write the results in a file. the results for the interactions not exported in uniprot
     * is also written in a file. This supposes that the MI score has been computed for all the interactions in IntAct before.
     * The computed scores of all the interactions is extracted from a file.
     *  @param interactions : the list of interactions currently exported in uniprot
     * @param fileContainingTotalScore : the file containing the mi score results
     * @param fileContainingDataExported : the files for the score results of the interactions exported in uniprot
     * @param fileContainingDataNotExported : the files for the score results of the interactions not exported in uniprot
     */
    /*public void extractMiScoresFromFile(List<String> interactions, String fileContainingTotalScore, String fileContainingDataExported, String fileContainingDataNotExported){
        try {

            // we extract the score results from the file
            Map<String, Double> totalNiScore = buildMapOfMiScoreFromFile(fileContainingTotalScore);

            // list of interactions ids exported in uniprot
            Set<String> interactionIdentifiersExported = new HashSet<String>();

            int i = 0;
            Set<BinaryInteraction> binaryInteractions = new HashSet<BinaryInteraction>();

            System.out.println(interactions.size() + " interactions in IntAct will be processed.");

            // each interaction will be processed
            while (i < interactions.size()){
                // we clear the previous binary interactions to keep only 200 binary interaction at the same time
                binaryInteractions.clear();

                // converts the interactions into binary interactions and increments the index in the list of interactions
                i = convertIntoBinaryInteractions(interactions, i, binaryInteractions);

                // filter the computed scores
                extractMiScoreForBinaryInteractionsFromFile(binaryInteractions, interactionIdentifiersExported, totalNiScore);
                //i += 200;
                int interactionsToProcess = interactions.size() - Math.min(i, interactions.size());
                System.out.println("Still " + interactionsToProcess + " interactions to process in IntAct.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("We cannot write the results in " + fileContainingDataExported + " or " + fileContainingDataNotExported);
        }
    }*/

    /**
     * Extracts the MI cluster score for each interaction exported in uniprot and write the results in a file. the results for the interactions not exported in uniprot
     * is also written in a file. This supposes that the MI score has been computed for all the interactions in IntAct before.
     * @param interactionIds : the list of interaction ids currently exported in uniprot
     * @param fileContainingDataExported : the files for the score results of the interactions exported in uniprot
     * @param fileContainingDataNotExported : the files for the score results of the interactions not exported in uniprot
     */
    public void extractComputedMiScoresFor(Set<Integer> interactionIds, String fileContainingDataExported, String fileContainingDataNotExported, IntactCluster clusterScore){

        System.out.println(interactionIds.size() + " interactions in IntAct will be processed.");

        String fileName1 = fileContainingDataExported + "_mitab.csv";
        String fileName2 = fileContainingDataNotExported + "_mitab.csv";

        clusterScore.saveClusteredInteractions(fileName1, interactionIds);
        clusterScore.saveClusteredInteractions(fileName2, new HashSet(CollectionUtils.subtract(clusterScore.getAllInteractionIds(), interactionIds)));
    }

    /**
     *
     * @param interactor : the interactorn to identify in uniprot
     * @return the uniprot accession of this interactor
     */
    public static String extractUniprotAccessionFrom(Interactor interactor){

        // the interactor cannot be null
        if (interactor == null){
            throw new IllegalArgumentException("It is not possible to extract the uniprot accession of an interactor which is null.");
        }

        // we look for the cross reference uniprotkb
        for (CrossReference xref : interactor.getIdentifiers()) {
            String acc = xref.getIdentifier();
            String sourceIdDbName = xref.getDatabase();

            if (UNIPROT_DATABASE.equalsIgnoreCase(sourceIdDbName)){
                return acc;
            }

        }
        return null;
    }

    /**
     * Extract the MI score of the binary interactions and write them in a file
     * @param binaryInteractions : binary interactions exported in uniprot
     * @param interactionIdentifiersExported : the list of interaction ids exported in uniprot
     */
    private void extractEncoreInteractionIdForBinaryInteractions(Set<BinaryInteraction> binaryInteractions, Set<Integer> interactionIdentifiersExported, IntActInteractionClusterScore clusterScore){

        // for each binary interaction exported in uniprot, add the Encore interaction Id to the list of exported interactions
        for (BinaryInteraction binary : binaryInteractions){
            // get the name of the interactor A
            String A = extractUniprotAccessionFrom(binary.getInteractorA());
            // get the name of the interactor B
            String B = extractUniprotAccessionFrom(binary.getInteractorB());

            // both interactors should have a uniprot accession
            if (A != null && B != null){
                // we extract the list of interaction Ids for the interactor A
                List<Integer> listOfInteractionsA = clusterScore.getInteractorMapping().get(A);
                // we extract the list of interaction Ids for the interactor B
                List<Integer> listOfInteractionsB = clusterScore.getInteractorMapping().get(B);

                // if A and B are involved in interactions having a MI score
                if (listOfInteractionsA != null && listOfInteractionsB != null){
                    // add all the interaction ids for interactor A with interactor B
                    Collection<Integer> intersection = CollectionUtils.intersection(listOfInteractionsA, listOfInteractionsB);
                    interactionIdentifiersExported.addAll(intersection);
                }
                else {
                    System.out.println(A + " and " + B + " doesn't have a MI score and the interaction is excluded.");
                }
            }
            else {
                throw new IllegalArgumentException("the list of binary interactions contains a binary interaction having one of its interactors whithout any uniprot accessions and this interaction cannot be taken into account.");
            }
        }
    }

    /**
     *
     * @param fileInteractionEligible
     * @param fileTotal
     * @throws IOException
     * @throws SQLException
     */
    public UniprotExportResults exportReleasedHighConfidencePPIInteractions(String fileInteractionEligible, String fileTotal, String fileInteractionExported, String fileDataExported, String fileDataNotExported) throws IOException, SQLException, UniprotExportException {

        System.out.println("export all interactions from intact which passed the dr export annotation");
        List<String> eligibleBinaryInteractions = this.queryFactory.getInteractionAcsFromReleasedExperimentsToBeProcessedForUniprotExport();

        System.out.println("computes MI score");
        MiClusterScoreResults results = computeMiScoresFor(eligibleBinaryInteractions);
        exporter.exportInteractionsFrom(results);

        results.getCluster().saveCluster(fileTotal);
        System.out.println("export binary interactions from intact");
        extractComputedMiScoresFor(results.getInteractionsToExport(), fileDataExported, fileDataNotExported, results.getCluster());

        return results;
    }

    public MiClusterScoreResults exportReleasedHighConfidenceTrueBinaryInteractions(String fileInteractionEligible, String fileTotal, String fileDataExported, String fileDataNotExported) throws IOException, SQLException, UniprotExportException {

        System.out.println("export all interactions from intact which passed the dr export annotation");
        List<String> eligibleInteractions = this.queryFactory.getInteractionAcsFromReleasedExperimentsToBeProcessedForUniprotExport();

        if (this.exporter instanceof ExporterBasedOnDetectionMethod){
            ExporterBasedOnDetectionMethod extractor = (ExporterBasedOnDetectionMethod) this.exporter;
            List<String> eligibleBinaryInteractions = extractor.filterBinaryInteractionsFrom(eligibleInteractions, fileInteractionEligible);

            System.out.println("computes MI score");
            MiClusterScoreResults results = computeMiScoresFor(eligibleBinaryInteractions);
            exporter.exportInteractionsFrom(results);

            results.getCluster().saveCluster(fileTotal);
            System.out.println("export binary interactions from intact");
            extractComputedMiScoresFor(results.getInteractionsToExport(), fileDataExported, fileDataNotExported, results.getCluster());

            return results;
        }

        return null;
    }

    public MiClusterScoreResults exportAllReleasedHighConfidencePPIInteractions(String fileInteractionEligible, String fileTotal, String fileDataExported, String fileDataNotExported) throws IOException, SQLException, UniprotExportException {

        System.out.println("export all interactions from intact which passed the dr export annotation");
        List<String> eligibleBinaryInteractions = this.queryFactory.getInteractionAcsFromReleasedExperimentsContainingNoUniprotProteinsToBeProcessedForUniprotExport();

        System.out.println("computes MI score");
        MiClusterScoreResults results = processExportWithFilterOnNonUniprot(eligibleBinaryInteractions);
        exporter.exportInteractionsFrom(results);

        results.getCluster().saveCluster(fileTotal);
        System.out.println("export binary interactions from intact");
        extractComputedMiScoresFor(results.getInteractionsToExport(), fileDataExported, fileDataNotExported, results.getCluster());

        return results;
    }

    @Override
    public MiClusterScoreResults exportInteractions() throws UniprotExportException {
        logger.info("Filtering interactions for uniprot export... \n");
        List<String> eligibleBinaryInteractions = this.queryFactory.getReleasedInteractionAcsPassingFilters();
        logger.info(eligibleBinaryInteractions.size() + " intact interactions passed the filters \n");

        logger.info("Clustering interactions... \n");
        MiClusterScoreResults results = processExportWithFilterOnNonUniprot(eligibleBinaryInteractions);
        logger.info("Clustered " + results.getCluster().getAllInteractionIds().size() + " binary interactions");

        logger.info("Exporting interactions... \n");
        exporter.exportInteractionsFrom(results);
        logger.info(results.getInteractionsToExport().size() + " binary interactions to export");

        return results;
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
