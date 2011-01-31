package uk.ac.ebi.intact.util.uniprotExport.miscore;

import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.miscore.converters.EncoreInteractionToCCLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.converters.EncoreInteractionToGoLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.converters.InteractorToDRLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.extension.IntActInteractionClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.miscore.extractor.MiScoreFilterForUniprotExport;
import uk.ac.ebi.intact.util.uniprotExport.parameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.DRParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.GOParameters;
import uk.ac.ebi.intact.util.uniprotExport.writers.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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

    private MiScoreFilterForUniprotExport filter;

    public UniprotExportProcessor(){

        goConverter = new EncoreInteractionToGoLineConverter();
        ccConverter = new EncoreInteractionToCCLineConverter();
        drConverter = new InteractorToDRLineConverter();

        this.filter = new MiScoreFilterForUniprotExport();
    }

    public void runUniprotExport(String mitabFile, String DRFile, String CCFile, String GOFile) throws UniprotExportException {

        IntActInteractionClusterScore clusterScore = filter.exportInteractionsFrom(mitabFile);

        try {
            exportDRLines(clusterScore, DRFile);
            exportCCAndGOLines(clusterScore, CCFile, GOFile);
        } catch (IOException e) {
            throw new UniprotExportException("Impossible to write the results of uniprot export in " + DRFile + " or " + CCFile + " or " + GOFile, e);
        }
    }

    public void exportCCAndGOLines(IntActInteractionClusterScore clusterScore, String CCFile, String GOFile) throws IOException {

        CCLineWriter ccWriter = new CCLineWriterImpl(CCFile);
        GOLineWriter goWriter = new GOLineWriterImpl(GOFile);

        for (Map.Entry<Integer, EncoreInteraction> interaction : clusterScore.getInteractionMapping().entrySet()){

            CCParameters ccParameters = this.ccConverter.convertInteractionsIntoCCLines(interaction.getValue(), this.filter.getContext());

            ccWriter.writeCCLine(ccParameters);

            GOParameters goParameters = this.goConverter.convertInteractionIntoGOParameters(interaction.getValue());

            goWriter.writeGOLine(goParameters);
        }

        ccWriter.close();
        goWriter.close();
    }

    public void exportCCLines(IntActInteractionClusterScore clusterScore, String CCFile) throws IOException {

        CCLineWriter ccWriter = new CCLineWriterImpl(CCFile);

        for (Map.Entry<Integer, EncoreInteraction> interaction : clusterScore.getInteractionMapping().entrySet()){

            CCParameters ccParameters = this.ccConverter.convertInteractionsIntoCCLines(interaction.getValue(), this.filter.getContext());

            ccWriter.writeCCLine(ccParameters);
        }

        ccWriter.close();
    }

    public void exportGOLines(IntActInteractionClusterScore clusterScore, String GOFile) throws IOException {

        GOLineWriter goWriter = new GOLineWriterImpl(GOFile);

        for (Map.Entry<Integer, EncoreInteraction> interaction : clusterScore.getInteractionMapping().entrySet()){

            GOParameters goParameters = this.goConverter.convertInteractionIntoGOParameters(interaction.getValue());

            goWriter.writeGOLine(goParameters);
        }

        goWriter.close();
    }

    public void exportDRLines(IntActInteractionClusterScore clusterScore, String DRFile) throws IOException {
        DRLineWriter drWriter = new DRLineWriterImpl(DRFile);

        Set<String> interactors = new HashSet(clusterScore.getInteractorMapping().keySet());
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

            int numberInteractions = clusterScore.getInteractorMapping().get(interactorAc).size();

            while (interactorIterator.hasNext()){
                String nextInteractor = interactorIterator.next();

                if (nextInteractor.startsWith(parent)){
                    processedInteractors.add(nextInteractor);
                    numberInteractions += clusterScore.getInteractorMapping().get(nextInteractor).size();
                }
            }

            DRParameters parameter = this.drConverter.convertInteractorToDRLine(parent, numberInteractions);
            drWriter.writeDRLine(parameter);
            interactors.removeAll(processedInteractors);
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
}
