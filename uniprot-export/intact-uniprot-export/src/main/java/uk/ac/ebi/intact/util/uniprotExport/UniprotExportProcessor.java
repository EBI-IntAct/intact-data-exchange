package uk.ac.ebi.intact.util.uniprotExport;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.converters.InteractorToDRLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.EncoreInteractionToCCLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.EncoreInteractionToGoLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.filters.InteractionFilter;
import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DRParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters;
import uk.ac.ebi.intact.util.uniprotExport.results.MiClusterScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters.CCLineWriter;
import uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters.CCLineWriterImpl;
import uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters.OldCCLineWriterImpl;
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

    public void runUniprotExport(String DRFile, String CCFile, String GOFile, boolean useNewFormat) throws UniprotExportException {

        logger.info("Export binary interactions from IntAct");
        MiClusterScoreResults results = filter.exportInteractions();

        try {
            logger.info("Write DR and CC lines");
            exportDRAndCCLines(results, DRFile, CCFile, useNewFormat);
            logger.info("write GO lines");
            exportGOLines(results, GOFile);

            logger.info("Save cluster informations in uniprotExport.log");
            results.getCluster().saveClusteredInteractions("uniprotExport.log", results.getInteractionsToExport());
        } catch (IOException e) {
            throw new UniprotExportException("Impossible to write the results of uniprot export in " + DRFile + " or " + CCFile + " or " + GOFile, e);
        }
    }

    /*public void exportCCLines(MiClusterScoreResults results, String CCFile) throws IOException {

        CCLineWriter ccWriter = new CCLineWriterImpl(new FileWriter(CCFile));
        List<EncoreInteraction> interactions = new ArrayList<EncoreInteraction>();

        for (Map.Entry<String, List<Integer>> interactor : results.getCluster().getInteractorMapping().entrySet()){
            interactions.clear();

            for (Integer interactionId : interactor.getValue()){
                EncoreInteraction interaction = results.getCluster().getInteractionMapping().get(interactionId);

                if (interaction != null){
                    interactions.add(interaction);
                }
            }

            CCParameters ccParameters = this.ccConverter.convertInteractionsIntoCCLines(interactions, results.getExportContext(), interactor.getKey());

            ccWriter.writeCCLine(ccParameters);
        }

        ccWriter.close();
    }*/

    public void exportGOLines(MiClusterScoreResults results, String GOFile) throws IOException {

        GOLineWriter goWriter = new GOLineWriterImpl(new FileWriter(GOFile));

        Map<Integer, EncoreInteraction> interactionMapping = results.getCluster().getEncoreInteractionCluster();

        for (Integer interactionId : interactionMapping.keySet()){
            EncoreInteraction interaction = interactionMapping.get(interactionId);

            GOParameters goParameters = this.goConverter.convertInteractionIntoGOParameters(interaction);

            goWriter.writeGOLine(goParameters);
        }

        goWriter.close();
    }

    public void exportDRLines(MiClusterScoreResults results, String DRFile) throws IOException {
        DRLineWriter drWriter = new DRLineWriterImpl(new FileWriter(DRFile));

        Set<String> interactors = new HashSet(results.getCluster().getInteractorCluster().keySet());
        Set<String> processedInteractors = new HashSet<String>();

        while (!interactors.isEmpty()){
            processedInteractors.clear();
            Iterator<String> interactorIterator = interactors.iterator();

            String interactorAc = interactorIterator.next();
            processedInteractors.add(interactorAc);

            String parent = interactorAc;
            if (interactorAc.contains("-")){
                parent = interactorAc.substring(0, interactorAc.indexOf("-"));
            }

            Collection<Integer> exportedInteractions = CollectionUtils.intersection(results.getInteractionsToExport(), results.getCluster().getInteractorCluster().get(interactorAc));
            int numberInteractions = exportedInteractions.size();

            while (interactorIterator.hasNext()){
                exportedInteractions.clear();

                String nextInteractor = interactorIterator.next();

                if (nextInteractor.startsWith(parent)){
                    processedInteractors.add(nextInteractor);

                    exportedInteractions = CollectionUtils.intersection(results.getInteractionsToExport(), results.getCluster().getInteractorCluster().get(nextInteractor));
                    numberInteractions += exportedInteractions.size();
                }
            }

            if (numberInteractions > 0){
                DRParameters parameter = this.drConverter.convertInteractorToDRLine(parent, numberInteractions);
                drWriter.writeDRLine(parameter);
                interactors.removeAll(processedInteractors);
            }
        }

        drWriter.close();
    }

    public void exportDRAndCCLines(MiClusterScoreResults results, String DRFile, String CCFile, boolean useNewFormat) throws IOException {
        DRLineWriter drWriter = new DRLineWriterImpl(new FileWriter(DRFile));

        Set<String> interactors = new HashSet(results.getCluster().getInteractorCluster().keySet());
        Set<String> processedInteractors = new HashSet<String>();

        CCLineWriter ccWriter;
        if(useNewFormat){
            ccWriter = new CCLineWriterImpl(new FileWriter(CCFile));
        }
        else {
            ccWriter = new OldCCLineWriterImpl(new FileWriter(CCFile));
        }
        List<EncoreInteraction> interactions = new ArrayList<EncoreInteraction>();

        while (!interactors.isEmpty()){
            processedInteractors.clear();
            interactions.clear();

            Iterator<String> interactorIterator = interactors.iterator();

            String interactorAc = interactorIterator.next();
            processedInteractors.add(interactorAc);

            String parent = interactorAc;
            if (interactorAc.contains("-")){
                parent = interactorAc.substring(0, interactorAc.indexOf("-"));
            }

            Collection<Integer> exportedInteractions = CollectionUtils.intersection(results.getInteractionsToExport(), results.getCluster().getInteractorCluster().get(interactorAc));
            int numberInteractions = exportedInteractions.size();

            Map<Integer, EncoreInteraction> interactionMapping = results.getCluster().getEncoreInteractionCluster();

            for (Integer interactionId : exportedInteractions){
                EncoreInteraction interaction = interactionMapping.get(interactionId);

                if (interaction != null){
                    interactions.add(interaction);
                }
            }

            while (interactorIterator.hasNext()){
                exportedInteractions.clear();

                String nextInteractor = interactorIterator.next();

                if (nextInteractor.startsWith(parent)){
                    processedInteractors.add(nextInteractor);

                    exportedInteractions = CollectionUtils.intersection(results.getInteractionsToExport(), results.getCluster().getInteractorCluster().get(nextInteractor));
                    numberInteractions += exportedInteractions.size();

                    for (Integer interactionId : exportedInteractions){
                        EncoreInteraction interaction = interactionMapping.get(interactionId);

                        if (interaction != null){
                            interactions.add(interaction);
                        }
                    }
                }
            }

            if (numberInteractions > 0){
                CCParameters ccParameters;
                if (useNewFormat){
                    ccParameters = this.ccConverter.convertInteractionsIntoCCLines(interactions, results.getExportContext(), parent);
                }
                else{
                    ccParameters = this.ccConverter.convertInteractionsIntoOldCCLines(interactions, results.getExportContext(), parent);
                }
                ccWriter.writeCCLine(ccParameters);

                DRParameters parameter = this.drConverter.convertInteractorToDRLine(parent, numberInteractions);
                drWriter.writeDRLine(parameter);
                interactors.removeAll(processedInteractors);
            }
        }

        ccWriter.close();
        drWriter.close();
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
