package uk.ac.ebi.intact.util.uniprotExport.miscore;

import org.apache.commons.collections.CollectionUtils;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.miscore.converters.EncoreInteractionToCCLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.converters.EncoreInteractionToGoLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.converters.InteractorToDRLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.filter.InteractionFilter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.parameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.DRParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.GOParameters;
import uk.ac.ebi.intact.util.uniprotExport.writers.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class UniprotExportProcessor {

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

    public void runUniprotExport(String DRFile, String CCFile, String GOFile) throws UniprotExportException {

        MiScoreResults results = filter.exportInteractions();

        try {
            exportDRAndCCLines(results, DRFile, CCFile);
            exportGOLines(results, GOFile);

            results.getClusterScore().saveScoresForSpecificInteractions("uniprotExport.log", results.getInteractionsToExport());
        } catch (IOException e) {
            throw new UniprotExportException("Impossible to write the results of uniprot export in " + DRFile + " or " + CCFile + " or " + GOFile, e);
        }
    }

    /*public void exportCCLines(MiScoreResults results, String CCFile) throws IOException {

        CCLineWriter ccWriter = new CCLineWriterImpl(new FileWriter(CCFile));
        List<EncoreInteraction> interactions = new ArrayList<EncoreInteraction>();

        for (Map.Entry<String, List<Integer>> interactor : results.getClusterScore().getInteractorMapping().entrySet()){
            interactions.clear();

            for (Integer interactionId : interactor.getValue()){
                EncoreInteraction interaction = results.getClusterScore().getInteractionMapping().get(interactionId);

                if (interaction != null){
                    interactions.add(interaction);
                }
            }

            CCParameters ccParameters = this.ccConverter.convertInteractionsIntoCCLines(interactions, results.getClusterContext(), interactor.getKey());

            ccWriter.writeCCLine(ccParameters);
        }

        ccWriter.close();
    }*/

    public void exportGOLines(MiScoreResults results, String GOFile) throws IOException {

        GOLineWriter goWriter = new GOLineWriterImpl(new FileWriter(GOFile));

        for (Integer interactionId : results.getInteractionsToExport()){
            EncoreInteraction interaction = results.getClusterScore().getInteractionMapping().get(interactionId);

            GOParameters goParameters = this.goConverter.convertInteractionIntoGOParameters(interaction);

            goWriter.writeGOLine(goParameters);
        }

        goWriter.close();
    }

    public void exportDRLines(MiScoreResults results, String DRFile) throws IOException {
        DRLineWriter drWriter = new DRLineWriterImpl(new FileWriter(DRFile));

        Set<String> interactors = new HashSet(results.getClusterScore().getInteractorMapping().keySet());
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

            Collection<Integer> exportedInteractions = CollectionUtils.intersection(results.getInteractionsToExport(), results.getClusterScore().getInteractorMapping().get(interactorAc));
            int numberInteractions = exportedInteractions.size();

            while (interactorIterator.hasNext()){
                exportedInteractions.clear();

                String nextInteractor = interactorIterator.next();

                if (nextInteractor.startsWith(parent)){
                    processedInteractors.add(nextInteractor);

                    exportedInteractions = CollectionUtils.intersection(results.getInteractionsToExport(), results.getClusterScore().getInteractorMapping().get(nextInteractor));
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

    public void exportDRAndCCLines(MiScoreResults results, String DRFile, String CCFile) throws IOException {
        DRLineWriter drWriter = new DRLineWriterImpl(new FileWriter(DRFile));

        Set<String> interactors = new HashSet(results.getClusterScore().getInteractorMapping().keySet());
        Set<String> processedInteractors = new HashSet<String>();

        CCLineWriter ccWriter = new CCLineWriterImpl(new FileWriter(CCFile));
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

            Collection<Integer> exportedInteractions = CollectionUtils.intersection(results.getInteractionsToExport(), results.getClusterScore().getInteractorMapping().get(interactorAc));
            int numberInteractions = exportedInteractions.size();

            for (Integer interactionId : exportedInteractions){
                EncoreInteraction interaction = results.getClusterScore().getInteractionMapping().get(interactionId);

                if (interaction != null){
                    interactions.add(interaction);
                }
            }

            while (interactorIterator.hasNext()){
                exportedInteractions.clear();

                String nextInteractor = interactorIterator.next();

                if (nextInteractor.startsWith(parent)){
                    processedInteractors.add(nextInteractor);

                    exportedInteractions = CollectionUtils.intersection(results.getInteractionsToExport(), results.getClusterScore().getInteractorMapping().get(nextInteractor));
                    numberInteractions += exportedInteractions.size();

                    for (Integer interactionId : exportedInteractions){
                        EncoreInteraction interaction = results.getClusterScore().getInteractionMapping().get(interactionId);

                        if (interaction != null){
                            interactions.add(interaction);
                        }
                    }
                }
            }

            if (numberInteractions > 0){

                CCParameters ccParameters = this.ccConverter.convertInteractionsIntoCCLines(interactions, results.getClusterContext(), parent);
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
