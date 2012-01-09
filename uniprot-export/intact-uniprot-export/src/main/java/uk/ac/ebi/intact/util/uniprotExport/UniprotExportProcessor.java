package uk.ac.ebi.intact.util.uniprotExport;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteractionForScoring;
import uk.ac.ebi.intact.util.uniprotExport.converters.DRLineConverter1;
import uk.ac.ebi.intact.util.uniprotExport.converters.DRLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.CCLineConverter1;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.CCLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.GoLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.GoLineConverter1;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.filters.InteractionFilter;
import uk.ac.ebi.intact.util.uniprotExport.iterator.UniprotEntryIterator;
import uk.ac.ebi.intact.util.uniprotExport.iterator.UniprotEntry;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DRParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters;
import uk.ac.ebi.intact.util.uniprotExport.results.ExportedClusteredInteractions;
import uk.ac.ebi.intact.util.uniprotExport.results.MiClusterScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntactCluster;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.IntactTransSplicedProteins;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.writers.CCWriterFactory;
import uk.ac.ebi.intact.util.uniprotExport.writers.DRWriterFactory;
import uk.ac.ebi.intact.util.uniprotExport.writers.GOWriterFactory;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;
import uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters.CCLineWriter;
import uk.ac.ebi.intact.util.uniprotExport.writers.drlinewriters.DRLineWriter;
import uk.ac.ebi.intact.util.uniprotExport.writers.golinewriters.GOLineWriter;

import java.io.IOException;
import java.util.*;

/**
 * Processor for the uniprot export and write the results in CC lines, GO lines and DR lines.
 *
 * This p[rocessor will use the mi cluster to process the interactions to export
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class UniprotExportProcessor {
    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(UniprotExportProcessor.class);

    /**
     * The converter of Encore interactions into GO line
     */
    private GoLineConverter goConverter;

    /**
     * The converter of Encore interactions into gold CC line
     */
    private CCLineConverter ccConverter;

    /**
     * The converter of Encore interactions into silver CC line
     */
    private CCLineConverter silverCcConverter;

    /**
     * The converter of an interactor into DR line
     */
    private DRLineConverter drConverter;

    /**
     * The filter of binary interactions
     */
    private InteractionFilter filter;

    /**
     * Factory to create CCWriters
     */
    private CCWriterFactory ccWriterFactory;

    /**
     * Factory to create DRWriters
     */
    private DRWriterFactory drWriterFactory;

    /**
     * Factory to create GOWriters
     */
    private GOWriterFactory goWriterFactory;

    private Set<EncoreInteractionForScoring> positiveInteractionsToExport;
    private Set<EncoreInteractionForScoring> negativeInteractionsToExport;
    private Set<EncoreInteractionForScoring> positiveInteractionsToExclude;
    private Set<EncoreInteractionForScoring> negativeInteractionsToExclude;

    /**
     *
     * @param filter : the filter to use for uniprot export
     */
    public UniprotExportProcessor(InteractionFilter filter){

        goConverter = new GoLineConverter1();

        // by default, initialize a converter of the first CC line format
        ccConverter = new CCLineConverter1();

        // by default, we use the same cc converter for silver and gold cc lines
        silverCcConverter = ccConverter;

        drConverter = new DRLineConverter1();

        this.filter = filter;

        this.ccWriterFactory = new CCWriterFactory();
        this.drWriterFactory = new DRWriterFactory();
        this.goWriterFactory = new GOWriterFactory();

        positiveInteractionsToExport = new HashSet<EncoreInteractionForScoring>();
        negativeInteractionsToExport = new HashSet<EncoreInteractionForScoring>();
        positiveInteractionsToExclude = new HashSet<EncoreInteractionForScoring>();
        negativeInteractionsToExclude = new HashSet<EncoreInteractionForScoring>();
    }

    /**
     *
     * @param filter : the filter to use for uniprot export
     */
    public UniprotExportProcessor(InteractionFilter filter, GoLineConverter goConverter, CCLineConverter ccConverter, CCLineConverter silverCcConverter, DRLineConverter drConverter){

        this.goConverter = goConverter != null ? goConverter : new GoLineConverter1();
        this.drConverter = drConverter != null ? drConverter : new DRLineConverter1();
        this.ccConverter = ccConverter != null ? ccConverter : new CCLineConverter1();
        this.silverCcConverter = silverCcConverter != null ? silverCcConverter : this.ccConverter;

        this.filter = filter;
        this.ccWriterFactory = new CCWriterFactory();
        this.drWriterFactory = new DRWriterFactory();
        this.goWriterFactory = new GOWriterFactory();

        positiveInteractionsToExport = new HashSet<EncoreInteractionForScoring>();
        negativeInteractionsToExport = new HashSet<EncoreInteractionForScoring>();
        positiveInteractionsToExclude = new HashSet<EncoreInteractionForScoring>();
        negativeInteractionsToExclude = new HashSet<EncoreInteractionForScoring>();
    }

    /**
     *
     * @param DRFile : name of the DR file
     * @param CCFile: name of the CC file
     * @param GOFile : name of the GO file
     * @throws UniprotExportException
     */
    public void runUniprotExport(String DRFile, String CCFile, String GOFile, String silverCCFile) throws UniprotExportException {

        logger.info("Export binary interactions from IntAct");
        MiClusterScoreResults results = filter.exportInteractions();

        try {
            logger.info("write GO lines");
            exportGOLines(results, GOFile);

            logger.info("Write DR and CC lines");
            exportDRAndCCLines(results, DRFile, CCFile, silverCCFile);

            ExportedClusteredInteractions positiveInteractions = results.getPositiveClusteredInteractions();
            ExportedClusteredInteractions negativeInteractions = results.getNegativeClusteredInteractions();

            logger.info("Save positive cluster informations in exported_positive.csv");
            positiveInteractions.getCluster().saveClusteredInteractions("exported_positive.csv", positiveInteractions.getInteractionsToExport());
            logger.info("Save negative cluster informations in exported_negative.csv");
            negativeInteractions.getCluster().saveClusteredInteractions("exported_negative.csv", negativeInteractions.getInteractionsToExport());

            logger.info("Save positive cluster informations in uniprotExport_excluded.log");
            positiveInteractions.getCluster().saveClusteredInteractions("excluded_positive.csv", new HashSet(CollectionUtils.subtract(positiveInteractions.getCluster().getAllInteractionIds(), positiveInteractions.getInteractionsToExport())));
            logger.info("Save positive cluster informations in excluded_negative.csv");
            negativeInteractions.getCluster().saveClusteredInteractions("excluded_negative.csv", new HashSet(CollectionUtils.subtract(negativeInteractions.getCluster().getAllInteractionIds(), negativeInteractions.getInteractionsToExport())));
        } catch (IOException e) {
            throw new UniprotExportException("Impossible to write the results of uniprot export in " + DRFile + " or " + CCFile + " or " + GOFile, e);
        }
    }

    /**
     * write the GO lines for the results of uniprot export
     * @param results : the results of clustering and filtering for uniprot export
     * @param GOFile : the file containing go lines
     * @throws IOException if impossible to write the results in GO lines
     */
    public void exportGOLines(MiClusterScoreResults results, String GOFile) throws IOException {

        // create the GO writer
        GOLineWriter goWriter = goWriterFactory.createGOLineWriterFor(this.goConverter, GOFile);

        if (goWriter != null){
            ExportedClusteredInteractions positiveInteractions = results.getPositiveClusteredInteractions();

            // the interactions
            Map<Integer, EncoreInteractionForScoring> interactionMapping = positiveInteractions.getCluster().getEncoreInteractionCluster();

            //Sorted set of uniprot acs, ordered by uniprot acs. If an interactor is an isoform or feature chain, it will follow the master protein (at least
            // the master protein of the same entry. However it can happen that we have an isoform with a uniprot ac not matching the main uniprot entry.
            // These isoforms are rare and are treated separately)
            UniprotEntryIterator simpleInteractorIterator = new UniprotEntryIterator(new TreeSet(positiveInteractions.getCluster().getInteractorCluster().keySet()), null);

            // the cluster is not empty
            if (!interactionMapping.isEmpty()){

                while (simpleInteractorIterator.hasNext()){
                    // collect all the uniprot acs of a same uniprot entry (master uniprot plus isoforms and feature chains)
                    UniprotEntry uniprotEntry = simpleInteractorIterator.next();

                    // clear the encore interactions to export in the GO lines for this uniprot entry
                    positiveInteractionsToExport.clear();

                    // collect all encore interactions from trans-splicing (isoforms with uniprot ac coming from another entry but which are also present in this uniprot entry)
                    List<EncoreInteractionForScoring> supplementaryPositiveInteractionFromTransSplicedIsoforms = collectSupplementaryInteractionsForMaster(uniprotEntry.getMasterUniprot(), positiveInteractions, results.getExportContext());

                    // collect number of exported interactions for the master uniprot and the isoforms of this uniprot entry starting with a different master
                    collectExportedInteractions(positiveInteractions, positiveInteractionsToExport, uniprotEntry.getMasterUniprot(), supplementaryPositiveInteractionFromTransSplicedIsoforms);

                    // collect the interactions for all the interactors which are not the master uniprot
                    for (String interactor : uniprotEntry.getPositiveInteractors()){
                        // collect number of exported interactions for this interactor
                        collectExportedInteractions(positiveInteractions, positiveInteractionsToExport, interactor, Collections.EMPTY_LIST);
                    }

                    // write GO lines if the number of interactions is superior to 0
                    flushGoLinesFor(goWriter, uniprotEntry.getMasterUniprot(), positiveInteractionsToExport, results.getExportContext());
                }
            }

            // close the writer
            goWriter.close();
        }
    }

    /**
     * Write the go lines for a uniprot protein
     * @param goWriter
     * @param master
     * @param interactions
     * @throws IOException
     */
    private void flushGoLinesFor(GOLineWriter goWriter, String master, Set<EncoreInteractionForScoring> interactions, MiClusterContext context) throws IOException {
        if (!interactions.isEmpty()){
            logger.info("Write GO lines for " + master);

            List<GOParameters> goParameters = this.goConverter.convertInteractionsIntoGOParameters(interactions, master, context);

            goWriter.writeGOLines(goParameters);
        }
    }

    /**
     * Collect all encore interactions which could be associated with this uniprot master but are trans-spliced variants with uniprot ac coming from another uniprot entry
     * @param master
     * @param results
     * @param context
     * @return
     */
    private List<EncoreInteractionForScoring> collectSupplementaryInteractionsForMaster(String master, ExportedClusteredInteractions results, MiClusterContext context){
        Map<String, Set<IntactTransSplicedProteins>> transSplicing = context.getTranscriptsWithDifferentMasterAcs();

        IntactCluster cluster = results.getCluster();

        if (transSplicing.containsKey(master)){
            Set<IntactTransSplicedProteins> intactIsoforms = transSplicing.get(master);

            List<EncoreInteractionForScoring> exportedInteractionsToAdd = new ArrayList<EncoreInteractionForScoring>();

            for (IntactTransSplicedProteins iso : intactIsoforms){
                String intactAc = iso.getIntactAc();

                if (cluster.getInteractorCluster().containsKey(iso.getUniprotAc())){
                    List<Integer> interactionIds = cluster.getInteractorCluster().get(iso.getUniprotAc());

                    for (Integer interactionId : interactionIds){
                        EncoreInteractionForScoring encore = cluster.getEncoreInteractionCluster().get(interactionId);

                        // check intact ac
                        List<String> intact1;
                        List<String> intact2;

                        if (encore.getInteractorAccsA().containsKey(WriterUtils.INTACT)){
                            intact1 = FilterUtils.extractAllIntactAcFromAccs(encore.getInteractorAccsA());
                        }
                        else {
                            intact1 = FilterUtils.extractAllIntactAcFromOtherAccs(encore.getOtherInteractorAccsA());
                        }

                        if (encore.getInteractorAccsB().containsKey(WriterUtils.INTACT)){
                            intact2 = FilterUtils.extractAllIntactAcFromAccs(encore.getInteractorAccsB());
                        }
                        else {
                            intact2 = FilterUtils.extractAllIntactAcFromOtherAccs(encore.getOtherInteractorAccsB());
                        }

                        if (intact1.contains(intactAc) || intact2.contains(intactAc)){
                            exportedInteractionsToAdd.add(encore);
                        }
                    }
                }
            }

            return exportedInteractionsToAdd;
        }

        return Collections.EMPTY_LIST;
    }

    /**
     * Write the DR and CC lines for the results of the export, CC format version 1
     * @param results : the results of clustering and filtering for uniprot export
     * @param DRFile : the file containing DR lines
     * @param CCFile : the file containing CC lines
     * @throws IOException
     */
    public void exportDRAndCCLines(MiClusterScoreResults results, String DRFile, String CCFile, String silverCCFile) throws IOException {
        MiClusterContext clusterContext = results.getExportContext();

        // the Dr writer
        DRLineWriter drWriter = drWriterFactory.createDRLineWriterFor(this.drConverter, DRFile);

        // two CC line writers which will be initialized depending on the version (1 = old CC line format, 2 = new CC line format)
        CCLineWriter ccWriter = ccWriterFactory.createCCLineWriterFor(this.ccConverter, CCFile);
        CCLineWriter ccWriterForSilver = ccWriterFactory.createCCLineWriterFor(this.silverCcConverter, silverCCFile);

        if (drWriter != null || ccWriter != null){
            ExportedClusteredInteractions positiveClusteredInteractions = results.getPositiveClusteredInteractions();
            ExportedClusteredInteractions negativeClusteredInteractions = results.getNegativeClusteredInteractions();

            // an iterator of all the interactors of this cluster
            // The list of interactors is sorted so if an interactor is an isoform or feature chain, it will follow the master protein
            // If we have negative interactions, it will be added as well
            UniprotEntryIterator uniprotEntryIterator = new UniprotEntryIterator(new TreeSet(positiveClusteredInteractions.getCluster().getInteractorCluster().keySet()), new TreeSet(negativeClusteredInteractions.getCluster().getInteractorCluster().keySet()));

            /*
            * The clustered interactions
            */
            Map<Integer, EncoreInteractionForScoring> interactionMapping = positiveClusteredInteractions.getCluster().getEncoreInteractionCluster();

            /*
            * The clustered negative interactions
            */
            Map<Integer, EncoreInteractionForScoring> negativeInteractionMapping = negativeClusteredInteractions.getCluster().getEncoreInteractionCluster();

            // the cluster is not empty and we don't process negative interactions
            if (!interactionMapping.isEmpty() || negativeInteractionMapping.isEmpty()){

                while (uniprotEntryIterator.hasNext()){
                    // collect all the uniprot acs of a same uniprot entry (master uniprot plus isoforms and feature chains)
                    UniprotEntry uniprotEntry = uniprotEntryIterator.next();

                    // clear the positive encore interactions to export in the DR and CC lines for this uniprot entry
                    positiveInteractionsToExport.clear();
                    // clear the negative encore interactions to export in the DR and CC lines for this uniprot entry
                    negativeInteractionsToExport.clear();
                    // clear the positive encore interactions to exclude in the DR and CC lines for this uniprot entry
                    positiveInteractionsToExclude.clear();
                    // clear the negative encore interactions to exclude in the DR and CC lines for this uniprot entry
                    negativeInteractionsToExclude.clear();

                    // collect all encore interactions (negative and positive)of master protein or from trans-splicing (isoforms with uniprot ac coming from another entry but which are also present in this uniprot entry)
                    List<EncoreInteractionForScoring> supplementaryPositiveInteractionFromTransSplicedIsoforms = collectSupplementaryInteractionsForMaster(uniprotEntry.getMasterUniprot(), positiveClusteredInteractions, clusterContext);
                    List<EncoreInteractionForScoring> supplementaryNegativeInteractionFromTransSplicedIsoforms = collectSupplementaryInteractionsForMaster(uniprotEntry.getMasterUniprot(), negativeClusteredInteractions, clusterContext);

                    // collect number of exported interactions for this interactor
                    // numberInteractions = collectExportedInteractions(positiveClusteredInteractions, interactions, interactor, supplementaryPositiveInteractionFromTransSplicedIsoforms);
                    collectExportedAndExcludedInteractions(positiveClusteredInteractions, positiveInteractionsToExport, positiveInteractionsToExclude, uniprotEntry.getMasterUniprot(), supplementaryPositiveInteractionFromTransSplicedIsoforms);
                    // collect number of exported negative interactions for this interactor
                    // numberNegativeInteractions = collectExportedInteractions(negativeClusteredInteractions, negativeInteractions, interactor, supplementaryNegativeInteractionFromTransSplicedIsoforms);
                    collectExportedAndExcludedInteractions(negativeClusteredInteractions, negativeInteractionsToExport, negativeInteractionsToExclude, uniprotEntry.getMasterUniprot(), supplementaryNegativeInteractionFromTransSplicedIsoforms);

                    // collect positive interactions involving isoforms and feature chains
                    for (String pos : uniprotEntry.getPositiveInteractors()){
                        // collect number of exported interactions for this interactor
                        collectExportedAndExcludedInteractions(positiveClusteredInteractions, positiveInteractionsToExport, positiveInteractionsToExclude, pos, Collections.EMPTY_LIST);
                    }

                    // collect negative interactions involving isoforms and feature chains
                    for (String neg : uniprotEntry.getNegativeInteractors()){
                        // collect number of exported interactions for this interactor
                        collectExportedAndExcludedInteractions(positiveClusteredInteractions, negativeInteractionsToExport, negativeInteractionsToExclude, neg, Collections.EMPTY_LIST);
                    }

                    // write CC lines if the number of interactions is superior to 0
                    flushDRAndCCLinesFor(results, drWriter, ccWriter, ccWriterForSilver, uniprotEntry.getMasterUniprot(), positiveInteractionsToExport, positiveInteractionsToExclude, negativeInteractionsToExport, negativeInteractionsToExclude);
                }
            }

            // close the writers
            drWriter.close();

            if (ccWriter != null){
                ccWriter.close();
            }

            if (ccWriterForSilver != null){
                ccWriterForSilver.close();
            }
        }
    }

    /**
     * Write DR and CC lines for a specific uniprot ac
     * @param results
     * @param drWriter
     * @param ccWriter
     * @param parentAc
     * @param interactions
     * @param negativeInteractions
     * @throws IOException
     */
    private void flushDRAndCCLinesFor(MiClusterScoreResults results, DRLineWriter drWriter, CCLineWriter ccWriter, CCLineWriter silverCcWriter, String parentAc, Set<EncoreInteractionForScoring> interactions, Set<EncoreInteractionForScoring> excludedInteractions, Set<EncoreInteractionForScoring> negativeInteractions, Set<EncoreInteractionForScoring> excludedNegativeInteractions) throws IOException {
        int numberInteractions = interactions.size();
        int numberNegativeInteractions = negativeInteractions.size();
        int numberExcludedInteractions = excludedInteractions.size();
        int numberExcludedNegativeInteractions = excludedNegativeInteractions.size();
        int totalNumberInteraction = numberNegativeInteractions + numberInteractions + numberExcludedInteractions + numberExcludedNegativeInteractions;

        if (numberInteractions > 0 || numberNegativeInteractions > 0){
            logger.info("Write gold CC lines for " + parentAc);
            if (ccWriter != null){
                CCParameters ccParameters = this.ccConverter.convertPositiveAndNegativeInteractionsIntoCCLines(interactions, negativeInteractions, results.getExportContext(), parentAc);
                ccWriter.writeCCLine(ccParameters);
            }
            else{
                logger.error("No CCWriter is compatible with the current ccline converter, the gold cc lines will not be written");
            }
        }

        if (numberExcludedInteractions > 0 || numberExcludedNegativeInteractions > 0){
            logger.info("Write silver CC lines for " + parentAc);
            if (silverCcWriter != null){
                CCParameters ccParameters = this.silverCcConverter.convertPositiveAndNegativeInteractionsIntoCCLines(excludedInteractions, excludedNegativeInteractions, results.getExportContext(), parentAc);
                silverCcWriter.writeCCLine(ccParameters);
            }
            else{
                logger.error("No CCWriter is compatible with the current ccline converter, the silver cc lines will not be written");
            }
        }

        // write DR lines if the total number of interactions is superior to 0
        if (totalNumberInteraction > 0){
            logger.info("Write DR lines for " + parentAc);
            if (drWriter != null){
                DRParameters parameter = this.drConverter.convertInteractorIntoDRLine(parentAc, totalNumberInteraction);
                drWriter.writeDRLine(parameter);
            }
            else{
                logger.error("No DRWriter is compatible with the current drline converter, the dr lines will not be written");
            }
        }
    }

    /**
     *
     * @param results : the results of the clustering and uniprot export
     * @param interactions : the encore interactions to export
     * @param interactor
     * @returnthe number of exported binary interactions for this interactor
     */
    private int collectExportedInteractions(ExportedClusteredInteractions results, Set<EncoreInteractionForScoring> interactions, String interactor, List<EncoreInteractionForScoring> supplementaryInteractionsFromTransSplicing) {

        // the clustered interactions
        Map<Integer, EncoreInteractionForScoring> interactionMapping = results.getCluster().getEncoreInteractionCluster();

        // the exported interactions for this interactor are the interactions attached to this interactor which are also in the list of exported interactions
        Collection<Integer> exportedInteractions = CollectionUtils.intersection(results.getInteractionsToExport(), results.getCluster().getInteractorCluster().get(interactor));

        if (!supplementaryInteractionsFromTransSplicing.isEmpty()){
            exportedInteractions.addAll(CollectionUtils.intersection(results.getInteractionsToExport(), supplementaryInteractionsFromTransSplicing));
        }

        // collect number of exported interactions
        int numberInteractions = exportedInteractions.size();

        // add the exported interactions to the list of interactions
        for (Integer interactionId : exportedInteractions){
            EncoreInteractionForScoring interaction = interactionMapping.get(interactionId);

            if (interaction != null){
                interactions.add(interaction);
            }
        }

        return numberInteractions;
    }

    /**
     *
     * @param results : the results of the clustering and uniprot export
     * @param interactions : the encore interactions to export
     * @param excludedInteractions : the encore interactions to exclude (silver)
     * @param interactor
     * @returnthe number of exported binary interactions for this interactor
     */
    private int collectExportedAndExcludedInteractions(ExportedClusteredInteractions results, Set<EncoreInteractionForScoring> interactions, Set<EncoreInteractionForScoring> excludedInteractions, String interactor, List<EncoreInteractionForScoring> supplementaryInteractionsFromTransSplicing) {

        // the clustered interactions
        Map<Integer, EncoreInteractionForScoring> interactionMapping = results.getCluster().getEncoreInteractionCluster();
        List<Integer> allInteractions = results.getCluster().getInteractorCluster().get(interactor);

        // no interactions involving this participant
        if (allInteractions == null){
            return 0;
        }

        // the exported interactions for this interactor are the interactions attached to this interactor which are also in the list of exported interactions
        Collection<Integer> exportedInteractions = CollectionUtils.intersection(results.getInteractionsToExport(), allInteractions);
        Collection<Integer> excludedInteractionIds = CollectionUtils.subtract(allInteractions, exportedInteractions);

        if (!supplementaryInteractionsFromTransSplicing.isEmpty()){
            Collection<Integer> intersection = CollectionUtils.intersection(results.getInteractionsToExport(), supplementaryInteractionsFromTransSplicing);
            exportedInteractions.addAll(intersection);
            excludedInteractions.addAll(CollectionUtils.subtract(supplementaryInteractionsFromTransSplicing, intersection));
        }

        // collect number of exported interactions
        int numberInteractions = exportedInteractions.size();
        int numberExcluded = excludedInteractionIds.size();

        // add the exported interactions to the list of interactions
        for (Integer interactionId : exportedInteractions){
            EncoreInteractionForScoring interaction = interactionMapping.get(interactionId);

            if (interaction != null){
                interactions.add(interaction);
            }
        }

        for (Integer interactionId : excludedInteractionIds){
            EncoreInteractionForScoring interaction = interactionMapping.get(interactionId);

            if (interaction != null){
                excludedInteractions.add(interaction);
            }
        }

        return numberInteractions;
    }

    public GoLineConverter getGoConverter() {
        return goConverter;
    }

    public void setGoConverter(GoLineConverter goConverter) {
        this.goConverter = goConverter;
    }

    public CCLineConverter getCcConverter() {
        return ccConverter;
    }

    public void setCcConverter(CCLineConverter ccConverter) {
        this.ccConverter = ccConverter;
    }

    public CCLineConverter getSilverCcConverter() {
        return silverCcConverter;
    }

    public void setSilverCcConverter(CCLineConverter silverCcConverter) {
        this.silverCcConverter = silverCcConverter;
    }

    public DRLineConverter getDrConverter() {
        return drConverter;
    }

    public void setDrConverter(DRLineConverter drConverter) {
        this.drConverter = drConverter;
    }

    public InteractionFilter getFilter() {
        return filter;
    }

    public void setFilter(InteractionFilter filter) {
        this.filter = filter;
    }
}
