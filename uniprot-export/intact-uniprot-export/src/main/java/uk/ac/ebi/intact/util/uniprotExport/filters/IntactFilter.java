package uk.ac.ebi.intact.util.uniprotExport.filters;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import psidev.psi.mi.tab.model.*;
import uk.ac.ebi.enfin.mi.cluster.MethodTypePair;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.psimitab.converters.Intact2BinaryInteractionConverter;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.exporters.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.exporters.QueryBuilder;
import uk.ac.ebi.intact.util.uniprotExport.exporters.rules.ExporterBasedOnDetectionMethod;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterConfig;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterContext;
import uk.ac.ebi.intact.util.uniprotExport.miscore.extension.IntActFileMiScoreDistribution;
import uk.ac.ebi.intact.util.uniprotExport.results.ExportedClusteredInteractions;
import uk.ac.ebi.intact.util.uniprotExport.results.MiClusterScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.results.UniprotExportResults;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntActClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntactCluster;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.IntactTransSplicedProteins;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

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
    private static final int MAX_NUMBER_INTERACTION = 300;

    public static final String UNIPROT_DATABASE = "uniprotkb";

    /**
     * The exporter having the rules
     */
    protected InteractionExporter exporter;

    /**
     * The factory for querying the database
     */
    protected QueryBuilder queryFactory;

    /**
     * The list of negative interactions
     */
    protected Set<String> negativeInteractions = new HashSet<String>();

    /**
     * The list of positive interactions to be processed
     */
    protected Set<String> eligibleInteractionsForUniprotExport = new HashSet<String>();

    /**
     * The map of intact isoform proteins pointing to a parent with a different uniprot entry
     */
    protected Map<String, Set<IntactTransSplicedProteins>> transcriptsWithDifferentParentAcs = new HashMap<String, Set<IntactTransSplicedProteins>>();

    /**
     * The map associating interaction ac with a set of GO component xrefs
     */
    protected Map<String, Set<String>> interactionComponentXrefs = new HashMap<String, Set<String>>();

    /**
     * we create a new IntactFilter
     */
    public IntactFilter(InteractionExporter exporter){
        this.interactionConverter = new Intact2BinaryInteractionConverter();
        this.exporter = exporter;
        this.queryFactory = new QueryBuilder();
        negativeInteractions.addAll(this.queryFactory.getNegativeInteractionsPassingFilter());
        eligibleInteractionsForUniprotExport.addAll(this.queryFactory.getReleasedInteractionAcsPassingFilters());

        buildTranscriptsWithDifferentParents();
        buildInteractionGoComponentXrefs();
    }

    private void buildTranscriptsWithDifferentParents(){
        List<Object []> proteinsAndParents = this.queryFactory.getTranscriptsWithDifferentParents();

        for (Object [] proteinAndParent : proteinsAndParents){
            if (proteinAndParent.length == 3){
                String proteinAc = (String) proteinAndParent[0];
                String parentAc = (String) proteinAndParent[1];
                String isoformAc = (String) proteinAndParent[2];

                if (proteinAc != null && parentAc != null && isoformAc != null) {

                    IntactTransSplicedProteins spliceProtein = new IntactTransSplicedProteins(proteinAc, isoformAc);

                    if (transcriptsWithDifferentParentAcs.containsKey(parentAc)){
                        transcriptsWithDifferentParentAcs.get(parentAc).add(spliceProtein);
                    }
                    else{
                        Set<IntactTransSplicedProteins> intactAcs = new HashSet<IntactTransSplicedProteins>();
                        intactAcs.add(spliceProtein);
                        transcriptsWithDifferentParentAcs.put(parentAc, intactAcs);
                    }
                }
            }
        }
    }

    private void buildInteractionGoComponentXrefs(){
        List<Object []> goXrefs = this.queryFactory.getGoComponentXrefsInIntact();

        for (Object [] goXref : goXrefs){
            if (goXref.length == 2){
                String interactionAc = (String) goXref[0];
                String componentRef = (String) goXref[1];

                if (interactionAc != null && componentRef != null) {

                    if (interactionComponentXrefs.containsKey(interactionAc)){
                        interactionComponentXrefs.get(interactionAc).add(componentRef);
                    }
                    else{
                        Set<String> componentRefs = new HashSet<String>();
                        componentRefs.add(componentRef);
                        interactionComponentXrefs.put(interactionAc, componentRefs);
                    }
                }
            }
        }
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
    @Deprecated
    public MiClusterScoreResults computeMiScoresFor(List<String> interactions){
        IntActClusterScore clusterScore = new IntActClusterScore();
        IntActClusterScore negativeClusterScore = new IntActClusterScore();
        MiClusterContext context = new MiClusterContext();

        int i = 0;
        // the list of binary interactions to process
        List<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>();
        // the list of negative binary interactions to process
        List<BinaryInteraction> negativeBinaryInteractions = new ArrayList<BinaryInteraction>();

        // the total results of the export
        MiClusterScoreResults results = new MiClusterScoreResults(new ExportedClusteredInteractions(clusterScore), new ExportedClusteredInteractions(negativeClusterScore), context);

        System.out.println(interactions.size() + " interactions in IntAct will be processed.");

        // we process all the interactions of the list per chunk of 200 interactions
        while (i < interactions.size()){
            // we clear the previous chunk of binary interactions to only keep 200 binary interaction at a time
            binaryInteractions.clear();
            negativeBinaryInteractions.clear();

            String intactAc = interactions.get(i);

            if (this.negativeInteractions.contains(interactions.get(i))){
                // convert the negative binary interaction
                convertIntoBinaryInteractions(intactAc, negativeBinaryInteractions, context);
                i++;
            }
            else {
                // we convert into binary interactions until we fill up the binary interactions list to MAX_NUMBER_INTERACTION. we get the new incremented i.
                convertIntoBinaryInteractions(intactAc, binaryInteractions, context);
                i++;
            }

            // we compute the mi score for the list of binary interactions
            processMiClustering(binaryInteractions, clusterScore);

            // we compute the mi score for the list of negative binary interactions
            processMiClustering(negativeBinaryInteractions, negativeClusterScore);

            int interactionsToProcess = interactions.size() - Math.min(i, interactions.size());
            System.out.println("Still " + interactionsToProcess + " interactions to process in IntAct.");
        }

        return results;
    }

    /**
     * Computes the Mi score for the interactions eligible for uniprot export
     * @throws uk.ac.ebi.intact.util.uniprotExport.UniprotExportException
     */
    public MiClusterScoreResults processExportWithFilterOnNonUniprot() throws UniprotExportException {
        MiClusterContext context = new MiClusterContext();
        context.setTranscriptsWithDifferentMasterAcs(this.transcriptsWithDifferentParentAcs);
        context.setInteractionComponentXrefs(this.interactionComponentXrefs);

        IntActClusterScore clusterScore = new IntActClusterScore();
        IntActClusterScore negativeClusterScore = new IntActClusterScore();

        MiClusterScoreResults results = new MiClusterScoreResults(new ExportedClusteredInteractions(clusterScore), new ExportedClusteredInteractions(negativeClusterScore), context);
        clusterIntactInteractions(context, clusterScore, negativeClusterScore);

        return results;
    }

    protected void clusterIntactInteractions(MiClusterContext context, IntActClusterScore clusterScore, IntActClusterScore negativeClusterScore) {
        int i = 0;
        // the list of binary interactions to process
        List<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>();

        logger.info(this.eligibleInteractionsForUniprotExport.size() + " interactions in IntAct will be processed.");

        List<String> positiveInteractions = new ArrayList(this.eligibleInteractionsForUniprotExport);

        // we process all the interactions of the list per chunk of 200 interactions
        while (i < positiveInteractions.size()){
            // we clear the previous chunk of binary interactions to only keep 200 binary interaction at a time
            binaryInteractions.clear();

            // we convert into binary interactions until we fill up the binary interactions list to MAX_NUMBER_INTERACTION. we get the new incremented i.
            i = convertIntoBinaryInteractionsExcludeNonUniprotProteins(positiveInteractions, i, binaryInteractions, context);

            // we compute the mi score for the list of binary interactions
            processMiClustering(binaryInteractions, clusterScore);

            int interactionsToProcess = this.eligibleInteractionsForUniprotExport.size() - Math.min(i, this.eligibleInteractionsForUniprotExport.size());
            logger.info("Still " + interactionsToProcess + " positive interactions to process in IntAct.");
        }

        if (!this.negativeInteractions.isEmpty()){
            clusterNegativeIntactInteractions(context, negativeClusterScore);
        }
    }

    protected void clusterNegativeIntactInteractions(MiClusterContext context, IntActClusterScore negativeClusterScore) {
        int i = 0;

        // the list of negative binary interactions to process
        List<BinaryInteraction> negativeBinaryInteractions = new ArrayList<BinaryInteraction>();

        logger.info(this.negativeInteractions.size() + " negative interactions in IntAct will be processed.");

        List<String> negativeInteractions = new ArrayList(this.negativeInteractions);

        // we process all the interactions of the list per chunk of 200 interactions
        while (i < negativeInteractions.size()){
            // we clear the previous chunk of binary interactions to only keep 200 binary interaction at a time
            negativeBinaryInteractions.clear();

            // we convert into binary interactions until we fill up the binary interactions list to MAX_NUMBER_INTERACTION. we get the new incremented i.
            i = convertIntoBinaryInteractionsExcludeNonUniprotProteins(negativeInteractions, i, negativeBinaryInteractions, context);

            // we compute the mi score for the list of negative binary interactions
            processMiClustering(negativeBinaryInteractions, negativeClusterScore);

            int interactionsToProcess = this.negativeInteractions.size() - Math.min(i, this.negativeInteractions.size());
            logger.info("Still " + interactionsToProcess + " negative interactions to process in IntAct.");
        }
    }

    /**
     * Converts the interactions from i into binary interactions until the list of binary interactions reach MAX_NUMBER_INTERACTIONS
     * @param interactions : the list of interactions accessions to process
     * @param i : the index from what we want to process the interactions of te list
     * @param binaryInteractions : the list which will contain the binary interactions
     * @return the index where we stopped in the list of interactions
     */
    @Deprecated
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
                        Collection<BinaryInteraction> toBinary = this.interactionConverter.convert(intactInteraction);

                        if (!toBinary.isEmpty()){
                            //logger.info("Processing interaction " + interactionAc);
                            processClusterContext(context, interactionAc, toBinary);

                            for (BinaryInteraction<Interactor> binary : toBinary){

                                Interactor interactorA = binary.getInteractorA();
                                String uniprotA = null;
                                Interactor interactorB = binary.getInteractorB();
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
                                removeNonPubmedPublicationsFrom(binary);

                                binary.getInteractorA().getAlternativeIdentifiers().clear();
                                binary.getInteractorA().getAliases().clear();
                                binary.getInteractorB().getAlternativeIdentifiers().clear();
                                binary.getInteractorB().getAliases().clear();
                            }

                            binaryInteractions.addAll(toBinary);
                        }

                    } catch (Exception e) {
                        logger.error("The interaction " + interactionAc + ", " + intactInteraction.getShortLabel() + " cannot be converted into binary interactions and is excluded.", e);
                    }
                }
                // if the interaction cannot be converted into binary interaction, we ignore the interaction.
                else {
                    logger.info("The interaction " + interactionAc + ", " + intactInteraction.getShortLabel() + " cannot be converted into binary interactions.");
                    /*if (InteractionUtils.isSelfInteraction(intactInteraction)){
                        IntactCloner cloner = new IntactCloner(true);

                        try {
                            Interaction interaction = cloner.cloneInteraction(intactInteraction);

                        } catch (IntactClonerException e) {
                            logger.info("The interaction " + interactionAc + ", " + intactInteraction.getShortLabel() + " is ignored.");
                        }
                    }*/
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
        dataContext.rollbackTransaction(transactionStatus);

        return i;
    }

    /**
     *
     * @param interaction
     * @param binaryInteractions
     * @param context
     *
     */
    @Deprecated
    private void convertIntoBinaryInteractions(String interaction, Collection<BinaryInteraction> binaryInteractions, MiClusterContext context) {

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();
        TransactionStatus transactionStatus = dataContext.beginTransaction();

        // get the IntAct interaction object
        Interaction intactInteraction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(interaction);

        // the interaction must exist in Intact
        if (intactInteraction != null){
            // the interaction can be converted into binary interaction
            if (this.interactionConverter.getExpansionStrategy().isExpandable(intactInteraction)){
                try {
                    // we convert the interaction in binary interaction
                    Collection<BinaryInteraction> toBinary = this.interactionConverter.convert(intactInteraction);
                    if (toBinary.size() == 1){
                        //logger.info("Processing interaction " + interaction);
                        processClusterContext(context, interaction, toBinary);

                        for (BinaryInteraction binary : toBinary){

                            Interactor interactorA = binary.getInteractorA();
                            String uniprotA = null;
                            Interactor interactorB = binary.getInteractorB();
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

                            binary.getInteractorA().getAlternativeIdentifiers().clear();
                            binary.getInteractorA().getAliases().clear();
                            binary.getInteractorB().getAlternativeIdentifiers().clear();
                            binary.getInteractorB().getAliases().clear();
                        }

                        binaryInteractions.addAll(toBinary);
                    }
                    else {
                        logger.info("The interaction " + interaction + ", " + intactInteraction.getShortLabel() + " is not a true binary interaction and is excluded.");
                    }
                } catch (Exception e) {
                    logger.error("The interaction " + interaction + ", " + intactInteraction.getShortLabel() + " cannot be converted into binary interactions and is excluded.", e);
                }
            }
            // if the interaction cannot be converted into binary interaction, we ignore the interaction.
            else {
                logger.info("The interaction " + interaction + ", " + intactInteraction.getShortLabel() + " cannot be converted into binary interactions.");
            }
        }
        else {
            logger.error("The interaction " + interaction + " doesn't exist in the database and is excluded.");
        }

        dataContext.rollbackTransaction(transactionStatus);
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
                        Collection<BinaryInteraction> toBinary = this.interactionConverter.convert(intactInteraction);

                        if (excludeSpokeExpanded && toBinary.size() == 1){
                            //logger.info("Processing interaction " + interactionAc);
                            processClustering(binaryInteractions, context, interactionAc, toBinary, excludeNonUniprotInteractors);
                        }
                        else if (!excludeSpokeExpanded && !toBinary.isEmpty()){
                            //logger.info("Processing interaction " + interactionAc);
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
        dataContext.rollbackTransaction(transactionStatus);

        return i;
    }

    private void processClustering(Collection<BinaryInteraction> binaryInteractions, MiClusterContext context, String interactionAc, Collection<BinaryInteraction> toBinary, boolean excludeNonUniprot) {
        processClusterContext(context, interactionAc, toBinary);

        for (BinaryInteraction<Interactor> binary : toBinary){

            Interactor interactorA = binary.getInteractorA();
            String uniprotA = null;
            Interactor interactorB = binary.getInteractorB();
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

            // process intra molecular interactions as self interactions
            if (interactorA == null){
                uniprotA = uniprotB;
                binary.setInteractorA(interactorB);
            }
            else if (uniprotB == null){
                uniprotB = uniprotA;
                binary.setInteractorB(interactorA);
            }

            if ((uniprotA != null && uniprotB != null && excludeNonUniprot) || !excludeNonUniprot){

                FilterUtils.processGeneNames(interactorA, uniprotA, interactorB, uniprotB, context);
                removeNonPubmedPublicationsFrom(binary);

                removeNonIntactXrefsFrom(interactorA.getAlternativeIdentifiers());
                interactorA.getAliases().clear();
                removeNonIntactXrefsFrom(interactorB.getAlternativeIdentifiers());
                interactorB.getAliases().clear();
                binaryInteractions.add(binary);
            }
        }
    }

    protected void removeNonPubmedPublicationsFrom(BinaryInteraction<Interactor> interaction){
        List<CrossReference> publications = new ArrayList(interaction.getPublications());

        for (CrossReference pub : publications){
            if (!WriterUtils.PUBMED.equalsIgnoreCase(pub.getDatabase())){
                interaction.getPublications().remove(pub);
            }
        }
    }

    protected void removeNonIntactXrefsFrom(Collection<CrossReference> identifiers){
        List<CrossReference> xrefs = new ArrayList(identifiers);

        for (CrossReference xref : xrefs){
            if (!WriterUtils.INTACT.equalsIgnoreCase(xref.getDatabase())){
                identifiers.remove(xref);
            }
        }
    }

    private void processClusterContext(MiClusterContext context, String interactionAc, Collection<BinaryInteraction> toBinary) {
        // process the context information
        List<CrossReference> detectionMethods = toBinary.iterator().next().getDetectionMethods();
        String detectionMI = detectionMethods.iterator().next().getIdentifier();

        if (!context.getMiTerms().containsKey(detectionMI)){
            context.getMiTerms().put(detectionMI, detectionMethods.iterator().next().getText());
        }

        List<CrossReference> interactionTypes = toBinary.iterator().next().getInteractionTypes();
        String typeMi = interactionTypes.iterator().next().getIdentifier();

        if (!context.getMiTerms().containsKey(typeMi)){
            context.getMiTerms().put(typeMi, interactionTypes.iterator().next().getText());
        }

        context.getInteractionToMethod_type().put(interactionAc, new MethodTypePair(detectionMI, typeMi));

        if (toBinary.size() > 1){

            context.getSpokeExpandedInteractions().add(interactionAc);
        }
    }

    /**
     *
     * @param binaryInteractions : the list of binary interactions to process
     */
    private void processMiClustering(List<BinaryInteraction> binaryInteractions, IntActClusterScore clusterScore) {

        try {
            // we compute the MI cluster score
            clusterScore.setBinaryInteractionIterator(binaryInteractions.iterator());
            clusterScore.runService();
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("The score cannot be computed for the list of binary interactions of size " + binaryInteractions.size());
        }
    }

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
    private void extractEncoreInteractionIdForBinaryInteractions(Set<BinaryInteraction> binaryInteractions, Set<Integer> interactionIdentifiersExported, IntActClusterScore clusterScore){

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

        ExportedClusteredInteractions positiveInteractions = results.getPositiveClusteredInteractions();
        ExportedClusteredInteractions negativeInteractions = results.getNegativeClusteredInteractions();

        positiveInteractions.getCluster().saveCluster(fileTotal);
        negativeInteractions.getCluster().saveCluster(fileTotal);

        System.out.println("export positive binary interactions from intact");
        extractComputedMiScoresFor(positiveInteractions.getInteractionsToExport(), fileDataExported, fileDataNotExported, positiveInteractions.getCluster());
        System.out.println("export negative binary interactions from intact");
        extractComputedMiScoresFor(negativeInteractions.getInteractionsToExport(), fileDataExported, fileDataNotExported, negativeInteractions.getCluster());

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

            ExportedClusteredInteractions positiveInteractions = results.getPositiveClusteredInteractions();
            ExportedClusteredInteractions negativeInteractions = results.getNegativeClusteredInteractions();

            positiveInteractions.getCluster().saveCluster(fileTotal);
            negativeInteractions.getCluster().saveCluster(fileTotal);

            System.out.println("export positive binary interactions from intact");
            extractComputedMiScoresFor(positiveInteractions.getInteractionsToExport(), fileDataExported, fileDataNotExported, positiveInteractions.getCluster());
            System.out.println("export negative binary interactions from intact");
            extractComputedMiScoresFor(negativeInteractions.getInteractionsToExport(), fileDataExported, fileDataNotExported, negativeInteractions.getCluster());

            return results;
        }

        return null;
    }

    public MiClusterScoreResults exportAllReleasedHighConfidencePPIInteractions(String fileInteractionEligible, String fileTotal, String fileDataExported, String fileDataNotExported) throws IOException, SQLException, UniprotExportException {

        System.out.println("export all interactions from intact which passed the dr export annotation");
        System.out.println("computes MI score");
        MiClusterScoreResults results = processExportWithFilterOnNonUniprot();
        exporter.exportInteractionsFrom(results);

        ExportedClusteredInteractions positiveInteractions = results.getPositiveClusteredInteractions();
        ExportedClusteredInteractions negativeInteractions = results.getNegativeClusteredInteractions();

        positiveInteractions.getCluster().saveCluster(fileTotal);
        negativeInteractions.getCluster().saveCluster(fileTotal);

        System.out.println("export positive binary interactions from intact");
        extractComputedMiScoresFor(positiveInteractions.getInteractionsToExport(), fileDataExported, fileDataNotExported, positiveInteractions.getCluster());
        System.out.println("export negative binary interactions from intact");
        extractComputedMiScoresFor(negativeInteractions.getInteractionsToExport(), fileDataExported, fileDataNotExported, negativeInteractions.getCluster());

        return results;
    }

    @Override
    public MiClusterScoreResults exportInteractions() throws UniprotExportException {

        logger.info("Filtering interactions for uniprot export... \n");
        logger.info(this.eligibleInteractionsForUniprotExport.size() + " positive intact interactions passed the filters \n");
        logger.info(this.negativeInteractions.size() + " negative intact interactions passed the filters \n");

        logger.info("Clustering interactions... \n");
        MiClusterScoreResults results = processExportWithFilterOnNonUniprot();

        ExportedClusteredInteractions positiveInteractions = results.getPositiveClusteredInteractions();
        ExportedClusteredInteractions negativeInteractions = results.getNegativeClusteredInteractions();

        logger.info("Clustered " + positiveInteractions.getCluster().getAllInteractionIds().size() + " positive binary interactions");
        logger.info("Clustered " + negativeInteractions.getCluster().getAllInteractionIds().size() + " negative binary interactions");

        logger.info("Exporting interactions... \n");
        exporter.exportInteractionsFrom(results);
        logger.info(positiveInteractions.getInteractionsToExport().size() + " positive binary interactions to export");
        logger.info(negativeInteractions.getInteractionsToExport().size() + " negative binary interactions to export");

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

    @Override
    public void saveClusterAndFilterResultsFrom(String mitab, String mitabResults) throws UniprotExportException {
        logger.info("Filtering interactions for uniprot export... \n");
        logger.info(this.eligibleInteractionsForUniprotExport.size() + " positive intact interactions passed the filters \n");
        logger.info(this.negativeInteractions.size() + " negative intact interactions passed the filters \n");

        logger.info("Clustering interactions... \n");
        MiClusterScoreResults results = processExportWithFilterOnNonUniprot();

        ExportedClusteredInteractions positiveInteractions = results.getPositiveClusteredInteractions();
        ExportedClusteredInteractions negativeInteractions = results.getNegativeClusteredInteractions();

        logger.info("Clustered " + positiveInteractions.getCluster().getAllInteractionIds().size() + " positive binary interactions");
        logger.info("Clustered " + negativeInteractions.getCluster().getAllInteractionIds().size() + " negative binary interactions");

        logger.info("Saving interactions... \n");
        IntactCluster positiveCluster = positiveInteractions.getCluster();
        IntactCluster negativeCluster = negativeInteractions.getCluster();

        positiveCluster.saveClusteredInteractions(mitabResults, positiveCluster.getEncoreInteractionCluster().keySet());
        negativeCluster.saveClusteredInteractions(mitabResults+"_negative.txt", negativeCluster.getEncoreInteractionCluster().keySet());

    }
}
