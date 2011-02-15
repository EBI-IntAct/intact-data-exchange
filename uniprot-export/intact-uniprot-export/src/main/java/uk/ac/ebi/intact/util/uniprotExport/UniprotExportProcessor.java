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
 * Processor for the uniprot export into CC lines, GO lines and DR lines
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class UniprotExportProcessor {
    private static final Logger logger = Logger.getLogger(UniprotExportProcessor.class);

    private EncoreInteractionToGoLineConverter goConverter;
    private EncoreInteractionToCCLineConverter ccConverter;
    private InteractorToDRLineConverter drConverter;

    private InteractionFilter filter;

    public UniprotExportProcessor(InteractionFilter filter){

        goConverter = new EncoreInteractionToGoLineConverter();
        ccConverter = new EncoreInteractionToCCLineConverter();
        drConverter = new InteractorToDRLineConverter();

        this.filter = filter;
    }

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

    public void exportGOLines(MiClusterScoreResults results, String GOFile) throws IOException {

        GOLineWriter goWriter = new GOLineWriterImpl(new FileWriter(GOFile));

        Map<Integer, EncoreInteraction> interactionMapping = results.getCluster().getEncoreInteractionCluster();

        for (Integer interactionId : results.getInteractionsToExport()){
            logger.info("Write GO lines for " + interactionId);

            EncoreInteraction interaction = interactionMapping.get(interactionId);

            GOParameters goParameters = this.goConverter.convertInteractionIntoGOParameters(interaction);

            goWriter.writeGOLine(goParameters);
        }

        goWriter.close();
    }

    public void exportDRAndCCLines(MiClusterScoreResults results, String DRFile, String CCFile, int version) throws IOException {
        DRLineWriter drWriter = new DRLineWriterImpl(new FileWriter(DRFile));

        Set<String> interactors = new TreeSet(results.getCluster().getInteractorCluster().keySet());

        CCLineWriter1 ccWriter1 = null;
        CCLineWriter2 ccWriter2 = null;

        if(version == 1){
            ccWriter1 = new DefaultCCLineWriter1(new FileWriter(CCFile));
        }
        else {
            ccWriter2 = new DefaultCCLineWriter2(new FileWriter(CCFile));
        }

        if (!interactors.isEmpty()){
            String parentAc = null;
            Map<Integer, EncoreInteraction> interactionMapping = results.getCluster().getEncoreInteractionCluster();
            List<EncoreInteraction> interactions = new ArrayList<EncoreInteraction>();
            int numberInteractions = 0;
            int totalNumberInteraction = 0;

            for (String interactor : interactors){

                if (parentAc == null){
                    parentAc = interactor;
                    if (interactor.contains("-")){
                        parentAc = interactor.substring(0, interactor.indexOf("-"));
                    }
                }

                // while the interactor starts with the same ac, increments the exported interactions
                if (interactor.startsWith(parentAc)){
                    int numberExported = collectExportedInteractions(results, interactions, interactionMapping, interactor);
                    numberInteractions += numberExported;
                    totalNumberInteraction += results.getCluster().getInteractorCluster().get(interactor).size();
                }
                // flushes the exported interactions of the previous interactor and process the new interactor
                else{
                    // write if the number of interactions is superior to 0
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

                    if (totalNumberInteraction > 0){
                        logger.info("Write DR lines for " + parentAc);
                        DRParameters parameter = this.drConverter.convertInteractorToDRLine(parentAc, totalNumberInteraction);
                        drWriter.writeDRLine(parameter);
                    }

                    // clean the global variables, so we can process the new interactor
                    numberInteractions = 0;
                    interactions.clear();
                    totalNumberInteraction = 0;

                    parentAc = interactor;
                    if (interactor.contains("-")){
                        parentAc = interactor.substring(0, interactor.indexOf("-"));
                    }

                    int numberExported = collectExportedInteractions(results, interactions, interactionMapping, interactor);
                    numberInteractions += numberExported;
                    totalNumberInteraction += results.getCluster().getInteractorCluster().get(interactor).size();
                }
            }
        }

        if (version == 1){
           ccWriter1.close();
        }
        else{
            ccWriter2.close();
        }
        drWriter.close();
    }

    private int collectExportedInteractions(MiClusterScoreResults results, List<EncoreInteraction> interactions, Map<Integer, EncoreInteraction> interactionMapping, String interactor) {
        Collection<Integer> exportedInteractions = CollectionUtils.intersection(results.getInteractionsToExport(), results.getCluster().getInteractorCluster().get(interactor));
        int numberInteractions = exportedInteractions.size();

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
