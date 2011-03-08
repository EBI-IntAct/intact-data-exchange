package uk.ac.ebi.intact.util.uniprotExport;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.converters.InteractorToDRLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.EncoreInteractionToCCLine1Converter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.EncoreInteractionToCCLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.EncoreInteractionToGoLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.filters.InteractionFilter;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.BasicCCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DRParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters;
import uk.ac.ebi.intact.util.uniprotExport.results.ExportedClusteredInteractions;
import uk.ac.ebi.intact.util.uniprotExport.results.MiClusterScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.writers.CCWriterFactory;
import uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters.BasicCCLineWriter;
import uk.ac.ebi.intact.util.uniprotExport.writers.drlinewriters.DRLineWriter;
import uk.ac.ebi.intact.util.uniprotExport.writers.drlinewriters.DRLineWriterImpl;
import uk.ac.ebi.intact.util.uniprotExport.writers.golinewriters.GOLineWriter;
import uk.ac.ebi.intact.util.uniprotExport.writers.golinewriters.GOLineWriterImpl;

import java.io.FileWriter;
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
    private EncoreInteractionToGoLineConverter goConverter;

    /**
     * The converter of Encore interactions into CC line
     */
    private EncoreInteractionToCCLineConverter ccConverter;

    /**
     * The converter of an interactor into DR line
     */
    private InteractorToDRLineConverter drConverter;

    /**
     * The filter of binary interactions
     */
    private InteractionFilter filter;

    /**
     * Factory to create CCWriters
     */
    private CCWriterFactory ccWriterFactory;

    /**
     *
     * @param filter : the filter to use for uniprot export
     */
    public UniprotExportProcessor(InteractionFilter filter){

        goConverter = new EncoreInteractionToGoLineConverter();

        // by default, initialize a converter of the first CC line format
        ccConverter = new EncoreInteractionToCCLine1Converter();

        drConverter = new InteractorToDRLineConverter();

        this.filter = filter;

        this.ccWriterFactory = new CCWriterFactory();
    }

    /**
     *
     * @param filter : the filter to use for uniprot export
     */
    public UniprotExportProcessor(InteractionFilter filter, EncoreInteractionToGoLineConverter goConverter, EncoreInteractionToCCLine1Converter ccConverter, InteractorToDRLineConverter drConverter){

        this.goConverter = goConverter != null ? goConverter : new EncoreInteractionToGoLineConverter();
        this.drConverter = drConverter != null ? drConverter : new InteractorToDRLineConverter();
        this.ccConverter = ccConverter != null ? ccConverter : new EncoreInteractionToCCLine1Converter();

        this.filter = filter;
        this.ccWriterFactory = new CCWriterFactory();
    }

    /**
     *
     * @param DRFile : name of the DR file
     * @param CCFile: name of the CC file
     * @param GOFile : name of the GO file
     * @throws UniprotExportException
     */
    public void runUniprotExport(String DRFile, String CCFile, String GOFile) throws UniprotExportException {

        logger.info("Export binary interactions from IntAct");
        MiClusterScoreResults results = filter.exportInteractions();

        try {
            logger.info("write GO lines");
            exportGOLines(results, GOFile);

            logger.info("Write DR and CC lines");
            exportDRAndCCLines(results, DRFile, CCFile);

            ExportedClusteredInteractions positiveInteractions = results.getPositiveClusteredInteractions();
            ExportedClusteredInteractions negativeInteractions = results.getNegativeClusteredInteractions();

            logger.info("Save positive cluster informations in uniprotExport.log");
            positiveInteractions.getCluster().saveClusteredInteractions("uniprotExport.log", positiveInteractions.getInteractionsToExport());
            logger.info("Save negative cluster informations in uniprotExport_negative.log");
            negativeInteractions.getCluster().saveClusteredInteractions("uniprotExport_negative.log", negativeInteractions.getInteractionsToExport());

            logger.info("Save positive cluster informations in uniprotExport_excluded.log");
            positiveInteractions.getCluster().saveClusteredInteractions("uniprotExport_excluded.log", new HashSet(CollectionUtils.subtract(positiveInteractions.getCluster().getAllInteractionIds(), positiveInteractions.getInteractionsToExport())));
            logger.info("Save positive cluster informations in uniprotExport_excluded.log");
            negativeInteractions.getCluster().saveClusteredInteractions("uniprotExport_excluded_negative.log", new HashSet(CollectionUtils.subtract(negativeInteractions.getCluster().getAllInteractionIds(), negativeInteractions.getInteractionsToExport())));
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
        GOLineWriter goWriter = new GOLineWriterImpl(new FileWriter(GOFile));

        ExportedClusteredInteractions positiveInteractions = results.getPositiveClusteredInteractions();

        // the interactions
        Map<Integer, EncoreInteraction> interactionMapping = positiveInteractions.getCluster().getEncoreInteractionCluster();

        /**
         * The list of interactors is sorted so if an interactor is an isoform or feature chain, it will follow the master protein
         */
        Set<String> interactors = new TreeSet(positiveInteractions.getCluster().getInteractorCluster().keySet());

        // the cluster is not empty
        if (!interactors.isEmpty() && !interactionMapping.isEmpty()){
            // iterator of the interactors
            Iterator<String> interactorIterator = interactors.iterator();

            // collect the first interactor
            String interactor = interactorIterator.next();

            // the uniprot ac of the first interactor
            String uniprotAc = interactor;
            if (interactor.contains("-")){
                uniprotAc = interactor.substring(0, interactor.indexOf("-"));
            }

            // the encore interactions to export in the GO lines for this uniprot entry
            List<EncoreInteraction> interactions = new ArrayList<EncoreInteraction>();
            // collect number of exported interactions for this first interactor
            collectExportedInteractions(positiveInteractions, interactions, interactor);

            // while the sorted list of interactors is not totally processed
            while (interactorIterator.hasNext() ){
                // next interactor
                interactor =  interactorIterator.next();

                // while the next interactor starts with the master uniprot ac, it means it is the same uniprot entry and the interactions are clustered
                while (interactor.startsWith(uniprotAc)){

                    // collect number of exported interactions for this interactor
                    collectExportedInteractions(positiveInteractions, interactions, interactor);

                    // get the next interactor if it exists or exit
                    if (interactorIterator.hasNext()){
                        interactor = interactorIterator.next();
                    }
                    else {
                        break;
                    }
                }

                // write GO lines if the number of interactions is superior to 0
                if (!interactions.isEmpty()){
                    logger.info("Write GO lines for " + uniprotAc);

                    // the isoforms have their own GO line contrary to the feaqture chains which are merged to the master entry
                    List<GOParameters> goParameters = this.goConverter.convertInteractionsIntoGOParameters(interactions, uniprotAc);

                    goWriter.writeGOLines(goParameters);
                }

                // clean the list of encore interactions attached to the uniprot entry, so we can process the new interactor
                interactions.clear();

                // extract new master uniprot ac
                uniprotAc = interactor;
                if (interactor.contains("-")){
                    uniprotAc = interactor.substring(0, interactor.indexOf("-"));
                }

                // collect new interactions for the new interactor
                collectExportedInteractions(positiveInteractions, interactions, interactor);

                // don't forget the latest !
                if (!interactorIterator.hasNext()){
                    // write GO lines if the number of interactions is superior to 0
                    if (!interactions.isEmpty()){
                        logger.info("Write GO lines for " + uniprotAc);

                        // the isoforms have their own GO line contrary to the feaqture chains which are merged to the master entry
                        List<GOParameters> goParameters = this.goConverter.convertInteractionsIntoGOParameters(interactions, uniprotAc);

                        goWriter.writeGOLines(goParameters);
                    }
                }
            }
        }

        // for each interactions we can export, get it from the cluster and convert it into GO line
        /*for (Integer interactionId : results.getInteractionsToExport()){
            EncoreInteraction interaction = interactionMapping.get(interactionId);
            logger.info("Write GO lines for " + interaction.getInteractorA() + " - " + interaction.getInteractorB());

            GOParameters goParameters = this.goConverter.convertInteractionIntoGOParameters(interaction);

            goWriter.writeGOLine(goParameters);
        }*/

        // close the writer
        goWriter.close();
    }

    private SortedSet<InteractingProtein> buildSortedListOfInteractingProteins(Set<String> interactors, boolean doesInteract){

        SortedSet<InteractingProtein> interactingProteins = new TreeSet();

        for (String interactor : interactors){
            interactingProteins.add(new InteractingProtein(interactor, doesInteract));
        }

        return interactingProteins;
    }

    /**
     * Write the DR and CC lines for the results of the export, CC format version 1
     * @param results : the results of clustering and filtering for uniprot export
     * @param DRFile : the file containing DR lines
     * @param CCFile : the file containing CC lines
     * @throws IOException
     */
    public void exportDRAndCCLines(MiClusterScoreResults results, String DRFile, String CCFile) throws IOException {

        // the Dr writer
        DRLineWriter drWriter = new DRLineWriterImpl(new FileWriter(DRFile));

        // two CC line writers which will be initialized depending on the version (1 = old CC line format, 2 = new CC line format)
        BasicCCLineWriter ccWriter = ccWriterFactory.createCCLineWriterFor(this.ccConverter, CCFile);

        ExportedClusteredInteractions positiveClusteredInteractions = results.getPositiveClusteredInteractions();
        ExportedClusteredInteractions negativeClusteredInteractions = results.getNegativeClusteredInteractions();

        /*
        * The list of interactors is sorted so if an interactor is an isoform or feature chain, it will follow the master protein
        * If we have negative interactions, it will be added as well
        */
        SortedSet<InteractingProtein> interactors = buildSortedListOfInteractingProteins(positiveClusteredInteractions.getCluster().getInteractorCluster().keySet(), true);
        interactors.addAll(buildSortedListOfInteractingProteins(negativeClusteredInteractions.getCluster().getInteractorCluster().keySet(), false));

        /*
        * The clustered interactions
        */
        Map<Integer, EncoreInteraction> interactionMapping = positiveClusteredInteractions.getCluster().getEncoreInteractionCluster();

        /*
        * The clustered negative interactions
        */
        Map<Integer, EncoreInteraction> negativeInteractionMapping = negativeClusteredInteractions.getCluster().getEncoreInteractionCluster();

        // the cluster is not empty and we don't process negative interactions
        if (!interactors.isEmpty() && (!interactionMapping.isEmpty() || negativeInteractionMapping.isEmpty())){
            // iterator of the interactors
            Iterator<InteractingProtein> interactorIterator = interactors.iterator();

            // collect the first interactor
            InteractingProtein interactingprot = interactorIterator.next();
            String interactor = interactingprot.getInteractor();

            // the master uniprot ac of the first interactor
            String parentAc = interactor;
            if (interactor.contains("-")){
                parentAc = interactor.substring(0, interactor.indexOf("-"));
            }

            // the encore interactions to export in the CC lines for this interactor
            List<EncoreInteraction> interactions = new ArrayList<EncoreInteraction>();
            // the negative encore interactions to export in the CC lines for this interactor
            List<EncoreInteraction> negativeInteractions = new ArrayList<EncoreInteraction>();

            // the number of interactions exported in CC lines
            int numberInteractions = 0;
            // the number of negative interactions exported in CC lines
            int numberNegativeInteractions = 0;
            // the total number of binary interactions attached to this protein
            int totalNumberInteraction = 0;

            if (interactingprot.doesInteract()){
                // collect number of exported interactions for this interactor
                numberInteractions = collectExportedInteractions(positiveClusteredInteractions, interactions, interactor);
                // increments the total number of interactions for this uniprot entry
                totalNumberInteraction = positiveClusteredInteractions.getCluster().getInteractorCluster().get(interactor).size();
            }
            else{
                // collect number of exported negative interactions for this interactor
                numberNegativeInteractions = collectExportedInteractions(negativeClusteredInteractions, negativeInteractions, interactor);
                // increments the total number of exported negative interactions for the uniprot entry (master, isoforms and feature chain)
                // increments the total number of interactions for this uniprot entry
                totalNumberInteraction = negativeClusteredInteractions.getCluster().getInteractorCluster().get(interactor).size();
            }

            // while the sorted list of interactors is not totally processed
            while (interactorIterator.hasNext() ){
                // next interactor
                interactingprot = interactorIterator.next();
                interactor =  interactingprot.getInteractor();

                // while the next interactor starts with the master uniprot ac, it means it is the same uniprot entry and the interactions are clustered
                while (interactor.startsWith(parentAc)){
                    if (interactingprot.doesInteract()){
                        // collect number of exported interactions for this interactor
                        numberInteractions += collectExportedInteractions(positiveClusteredInteractions, interactions, interactor);
                        // increments the total number of interactions for this uniprot entry
                        totalNumberInteraction += positiveClusteredInteractions.getCluster().getInteractorCluster().get(interactor).size();
                    }
                    else{
                        // collect number of exported negative interactions for this interactor
                        numberNegativeInteractions += collectExportedInteractions(negativeClusteredInteractions, negativeInteractions, interactor);
                        // increments the total number of interactions for this uniprot entry
                        totalNumberInteraction += negativeClusteredInteractions.getCluster().getInteractorCluster().get(interactor).size();
                    }

                    // get the next interactor if it exists or exit
                    if (interactorIterator.hasNext()){
                        interactingprot = interactorIterator.next();
                        interactor = interactingprot.getInteractor();
                    }
                    else {
                        break;
                    }
                }

                // write CC lines if the number of interactions is superior to 0
                if (numberInteractions > 0 || numberNegativeInteractions > 0){
                    logger.info("Write CC lines for " + parentAc);
                    if (ccWriter != null){
                        BasicCCParameters ccParameters = this.ccConverter.convertPositiveAndNegativeInteractionsIntoCCLines(interactions, negativeInteractions, results.getExportContext(), parentAc);
                        ccWriter.writeCCLine(ccParameters);
                    }
                    else{
                        logger.error("No CCWriter is compatible with the current ccline converter, the cc lines will not be written");
                    }
                }

                // write DR lines if the total number of interactions is superior to 0
                if (totalNumberInteraction > 0){
                    logger.info("Write DR lines for " + parentAc);
                    DRParameters parameter = this.drConverter.convertInteractorToDRLine(parentAc, totalNumberInteraction);
                    drWriter.writeDRLine(parameter);
                }

                // clean the list of encore interactions attached to the uniprot entry, so we can process the new interactor
                interactions.clear();

                // extract new master uniprot ac
                parentAc = interactor;
                if (interactor.contains("-")){
                    parentAc = interactor.substring(0, interactor.indexOf("-"));
                }

                // collect new interactions for the new interactor
                if (interactingprot.doesInteract()){
                    // collect number of exported interactions for this interactor
                    numberInteractions = collectExportedInteractions(positiveClusteredInteractions, interactions, interactor);
                    numberNegativeInteractions = 0;
                    // increments the total number of interactions for this uniprot entry
                    totalNumberInteraction = positiveClusteredInteractions.getCluster().getInteractorCluster().get(interactor).size();
                }
                else{
                    // collect number of exported negative interactions for this interactor
                    numberNegativeInteractions = collectExportedInteractions(negativeClusteredInteractions, negativeInteractions, interactor);
                    // increments the total number of exported negative interactions for the uniprot entry (master, isoforms and feature chain)
                    numberInteractions = 0;
                    // increments the total number of interactions for this uniprot entry
                    totalNumberInteraction = negativeClusteredInteractions.getCluster().getInteractorCluster().get(interactor).size();
                }

                // don't forget to write the latest interactor
                if (!interactorIterator.hasNext()){
                    // write CC lines if the number of interactions is superior to 0
                    if (numberInteractions > 0 || numberNegativeInteractions > 0){
                        logger.info("Write CC lines for " + parentAc);
                        if (ccWriter != null){
                            BasicCCParameters ccParameters = this.ccConverter.convertPositiveAndNegativeInteractionsIntoCCLines(interactions, negativeInteractions, results.getExportContext(), parentAc);
                            ccWriter.writeCCLine(ccParameters);
                        }
                        else{
                            logger.error("No CCWriter is compatible with the current ccline converter, the cc lines will not be written");
                        }
                    }

                    // write DR lines if the total number of interactions is superior to 0
                    if (totalNumberInteraction > 0){
                        logger.info("Write DR lines for " + parentAc);
                        DRParameters parameter = this.drConverter.convertInteractorToDRLine(parentAc, totalNumberInteraction);
                        drWriter.writeDRLine(parameter);
                    }
                }
            }
        }

        // close the writers
        drWriter.close();

        if (ccWriter != null){
            ccWriter.close();
        }
    }

    /**
     *
     * @param results : the results of the clustering and uniprot export
     * @param interactions : the encore interactions to export
     * @param interactor
     * @returnthe number of exported binary interactions for this interactor
     */
    private int collectExportedInteractions(ExportedClusteredInteractions results, List<EncoreInteraction> interactions, String interactor) {

        // the clustered interactions
        Map<Integer, EncoreInteraction> interactionMapping = results.getCluster().getEncoreInteractionCluster();

        // the exported interactions for this interactor are the interactions attached to this interactor which are also in the list of exported interactions
        Collection<Integer> exportedInteractions = CollectionUtils.intersection(results.getInteractionsToExport(), results.getCluster().getInteractorCluster().get(interactor));
        // collect number of exported interactions
        int numberInteractions = exportedInteractions.size();

        // add the exported interactions to the list of interactions
        for (Integer interactionId : exportedInteractions){
            EncoreInteraction interaction = interactionMapping.get(interactionId);

            if (interaction != null){
                interactions.add(interaction);
            }
        }

        return numberInteractions;
    }

    public EncoreInteractionToGoLineConverter getGoConverter() {
        return goConverter;
    }

    public void setGoConverter(EncoreInteractionToGoLineConverter goConverter) {
        this.goConverter = goConverter;
    }

    public EncoreInteractionToCCLineConverter getCcConverter() {
        return ccConverter;
    }

    public void setCcConverter(EncoreInteractionToCCLineConverter ccConverter) {
        this.ccConverter = ccConverter;
    }

    public InteractorToDRLineConverter getDrConverter() {
        return drConverter;
    }

    public void setDrConverter(InteractorToDRLineConverter drConverter) {
        this.drConverter = drConverter;
    }

    public InteractionFilter getFilter() {
        return filter;
    }

    public void setFilter(InteractionFilter filter) {
        this.filter = filter;
    }
}
