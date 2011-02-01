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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
            exportDRLines(results, DRFile);
            exportCCAndGOLines(results, CCFile, GOFile);
        } catch (IOException e) {
            throw new UniprotExportException("Impossible to write the results of uniprot export in " + DRFile + " or " + CCFile + " or " + GOFile, e);
        }
    }

    public void exportCCAndGOLines(MiScoreResults results, String CCFile, String GOFile) throws IOException {

        CCLineWriter ccWriter = new CCLineWriterImpl(new FileWriter(CCFile));
        GOLineWriter goWriter = new GOLineWriterImpl(new FileWriter(GOFile));

        for (Integer interactionId : results.getInteractionsToExport()){
            EncoreInteraction interaction = results.getClusterScore().getInteractionMapping().get(interactionId);

            CCParameters ccParameters = this.ccConverter.convertInteractionsIntoCCLines(interaction, results.getClusterContext());

            ccWriter.writeCCLine(ccParameters);

            GOParameters goParameters = this.goConverter.convertInteractionIntoGOParameters(interaction);

            goWriter.writeGOLine(goParameters);
        }

        ccWriter.close();
        goWriter.close();
    }

    public void exportCCLines(MiScoreResults results, String CCFile) throws IOException {

        CCLineWriter ccWriter = new CCLineWriterImpl(new FileWriter(CCFile));

        for (Integer interactionId : results.getInteractionsToExport()){
            EncoreInteraction interaction = results.getClusterScore().getInteractionMapping().get(interactionId);

            CCParameters ccParameters = this.ccConverter.convertInteractionsIntoCCLines(interaction, results.getClusterContext());

            ccWriter.writeCCLine(ccParameters);
        }

        ccWriter.close();
    }

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
