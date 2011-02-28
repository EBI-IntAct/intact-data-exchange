package uk.ac.ebi.intact.util.uniprotExport;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.converters.InteractorToDRLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.EncoreInteractionToCCLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.EncoreInteractionToGoLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.filters.InteractionFilter;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters1;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters2;
import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DRParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters;
import uk.ac.ebi.intact.util.uniprotExport.results.MiClusterScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters.CCLineWriter1;
import uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters.CCLineWriter2;
import uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters.DefaultCCLineWriter1;
import uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters.DefaultCCLineWriter2;
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
     *
     * @param filter : the filter to use for uniprot export
     */
    public UniprotExportProcessor(InteractionFilter filter){

        goConverter = new EncoreInteractionToGoLineConverter();
        ccConverter = new EncoreInteractionToCCLineConverter();
        drConverter = new InteractorToDRLineConverter();

        this.filter = filter;
    }

    /**
     *
     * @param filter : the filter to use for uniprot export
     */
    public UniprotExportProcessor(InteractionFilter filter, EncoreInteractionToGoLineConverter goConverter, EncoreInteractionToCCLineConverter ccConverter, InteractorToDRLineConverter drConverter){

        this.goConverter = goConverter != null ? goConverter : new EncoreInteractionToGoLineConverter();
        this.drConverter = drConverter != null ? drConverter : new InteractorToDRLineConverter();
        this.ccConverter = ccConverter != null ? ccConverter : new EncoreInteractionToCCLineConverter();

        this.filter = filter;
    }

    /**
     *
     * @param DRFile : name of the DR file
     * @param CCFile: name of the CC file
     * @param GOFile : name of the GO file
     * @param version : version of the export
     * @throws UniprotExportException
     */
    public void runUniprotExport(String DRFile, String CCFile, String GOFile, int version) throws UniprotExportException {

        logger.info("Export binary interactions from IntAct");
        MiClusterScoreResults results = filter.exportInteractions();

        try {
            logger.info("write GO lines");
            exportGOLines(results, GOFile);

            logger.info("Write DR and CC lines");
            exportDRAndCCLines(results, DRFile, CCFile, version);

            logger.info("Save cluster informations in uniprotExport.log");
            results.getCluster().saveClusteredInteractions("uniprotExport.log", results.getInteractionsToExport());
            logger.info("Save cluster informations in uniprotExport_excluded.log");
            results.getCluster().saveClusteredInteractions("uniprotExport_excluded.log", new HashSet(CollectionUtils.subtract(results.getCluster().getAllInteractionIds(), results.getInteractionsToExport())));
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

        // the interactions
        Map<Integer, EncoreInteraction> interactionMapping = results.getCluster().getEncoreInteractionCluster();

        /**
         * The list of interactors is sorted so if an interactor is an isoform or feature chain, it will follow the master protein
         */
        Set<String> interactors = new TreeSet(results.getCluster().getInteractorCluster().keySet());

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

            // while the sorted list of interactors is not totally processed
            while (interactorIterator.hasNext() ){
                // next interactor
                interactor =  interactorIterator.next();

                // while the next interactor starts with the master uniprot ac, it means it is the same uniprot entry and the interactions are clustered
                while (interactor.startsWith(uniprotAc)){

                    // collect number of exported interactions for this interactor
                    collectExportedInteractions(results, interactions, interactor);

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
                collectExportedInteractions(results, interactions, interactor);
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

    /**
     * Write the DR and CC lines for the results of the export
     * @param results : the results of clustering and filtering for uniprot export
     * @param DRFile : the file containing DR lines
     * @param CCFile : the file containing CC lines
     * @param version : the version of the export
     * @throws IOException
     */
    public void exportDRAndCCLines(MiClusterScoreResults results, String DRFile, String CCFile, int version) throws IOException {
        // the Dr writer
        DRLineWriter drWriter = new DRLineWriterImpl(new FileWriter(DRFile));

        // two CC line writers which will be initialized depending on the version (1 = old CC line format, 2 = new CC line format)
        CCLineWriter1 ccWriter1 = null;
        CCLineWriter2 ccWriter2 = null;

        if(version == 1){
            ccWriter1 = new DefaultCCLineWriter1(new FileWriter(CCFile));
        }
        else {
            ccWriter2 = new DefaultCCLineWriter2(new FileWriter(CCFile));
        }

        /**
         * The list of interactors is sorted so if an interactor is an isoform or feature chain, it will follow the master protein
         */
        Set<String> interactors = new TreeSet(results.getCluster().getInteractorCluster().keySet());

        /**
         * The clustered interactions
         */
        Map<Integer, EncoreInteraction> interactionMapping = results.getCluster().getEncoreInteractionCluster();

        // the cluster is not empty
        if (!interactors.isEmpty() && !interactionMapping.isEmpty()){
            // iterator of the interactors
            Iterator<String> interactorIterator = interactors.iterator();

            // collect the first interactor
            String interactor = interactorIterator.next();

            // the master uniprot ac of the first interactor
            String parentAc = interactor;
            if (interactor.contains("-")){
                parentAc = interactor.substring(0, interactor.indexOf("-"));
            }

            // the encore interactions to export in the CC lines for this interactor
            List<EncoreInteraction> interactions = new ArrayList<EncoreInteraction>();
            // the number of interactions exported in CC lines
            int numberInteractions = collectExportedInteractions(results, interactions, interactor);
            // the total number of binary interactions attached to this protein
            int totalNumberInteraction = results.getCluster().getInteractorCluster().get(interactor).size();

            // while the sorted list of interactors is not totally processed
            while (interactorIterator.hasNext() ){
                // next interactor
                interactor =  interactorIterator.next();

                // while the next interactor starts with the master uniprot ac, it means it is the same uniprot entry and the interactions are clustered
                while (interactor.startsWith(parentAc)){
                    // collect number of exported interactions for this interactor
                    int numberExported = collectExportedInteractions(results, interactions, interactor);
                    // increments the total number of exported interactions for the uniprot entry (master, isoforms and feature chain)
                    numberInteractions += numberExported;

                    // increments the total number of interactions for this uniprot entry
                    totalNumberInteraction += results.getCluster().getInteractorCluster().get(interactor).size();

                    // get the next interactor if it exists or exit
                    if (interactorIterator.hasNext()){
                        interactor = interactorIterator.next();
                    }
                    else {
                        break;
                    }
                }

                // write CC lines if the number of interactions is superior to 0
                if (numberInteractions > 0){
                    logger.info("Write CC lines for " + parentAc);
                    if (version == 1){
                        CCParameters1 ccParameters = this.ccConverter.convertInteractionsIntoCCLinesVersion1(interactions, results.getExportContext(), parentAc);
                        ccWriter1.writeCCLine(ccParameters);
                    }
                    else{
                        CCParameters2 ccParameters = this.ccConverter.convertInteractionsIntoCCLinesVersion2(interactions, results.getExportContext(), parentAc);
                        ccWriter2.writeCCLine(ccParameters);
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
                int numberExported = collectExportedInteractions(results, interactions, interactor);
                numberInteractions = numberExported;
                totalNumberInteraction = results.getCluster().getInteractorCluster().get(interactor).size();
            }
        }

        // close the writers
        if (version == 1){
            ccWriter1.close();
        }
        else{
            ccWriter2.close();
        }
        drWriter.close();
    }

    /**
     *
     * @param results : the results of the clustering and uniprot export
     * @param interactions : the encore interactions to export
     * @param interactor
     * @returnthe number of exported binary interactions for this interactor
     */
    private int collectExportedInteractions(MiClusterScoreResults results, List<EncoreInteraction> interactions, String interactor) {
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
