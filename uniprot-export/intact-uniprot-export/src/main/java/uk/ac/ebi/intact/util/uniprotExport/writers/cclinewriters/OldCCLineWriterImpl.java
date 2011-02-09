package uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters;

import uk.ac.ebi.intact.util.uniprotExport.event.CcLineCreatedEvent;
import uk.ac.ebi.intact.util.uniprotExport.event.CcLineEventListener;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.OldInteractionDetails;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCInteractor;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Writer of the old CC line format
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09/02/11</pre>
 */

public class OldCCLineWriterImpl implements CCLineWriter  {

    /**
     * The writer
     */
    private OutputStreamWriter writer;
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * Create a new CCLine writer with a fileName
     * @param outputStream : the outputStreamWriter
     * @throws IOException
     */
    public OldCCLineWriterImpl(OutputStreamWriter outputStream) throws IOException {
        if (outputStream == null){
            throw new IllegalArgumentException("You must give a non null OutputStream writer");
        }
        writer = outputStream;
    }

    @Override
    public void writeCCLine(CCParameters parameters) throws IOException {

        // if parameter not null, write it
        if (parameters != null){

            // write the title
            writeCCLineTitle(parameters.getFirstInteractor());

            // write the content
            writeCCLineParameters(parameters);

            writer.flush();
        }
    }

    @Override
    public void writeCCLines(List<CCParameters> CCLines) throws IOException {

        // write each CCParameter
        for (CCParameters parameter : CCLines){
            writeCCLine(parameter);
        }
    }

    /**
     * Write the content of the CC line
     * @param parameters : the parameters
     */
    public void writeCCLineParameters(CCParameters parameters) throws IOException {

        String firstUniprotAc = parameters.getFirstInteractor();
        String firstTaxId = parameters.getFirstTaxId();

        for (SecondCCInteractor secondInteractor : parameters.getSecondCCInteractors()){
            writer.write("CC       ");

            if (firstUniprotAc.equals(secondInteractor.getSecondInteractor())) {

                writer.write("Self");

            } else {

                writer.write(secondInteractor.getSecondInteractor());
                writer.write(':');
                writer.write(secondInteractor.getSecondGeneName());
            }

            // generated warning message if the two protein are from different organism
            if (!firstTaxId.equals(secondInteractor.getSecondTaxId())) {
                writer.write(' ');
                writer.write("(xeno)");
            }

            writer.write(';');
            writer.write(' ');
            writer.write("NbExp=");
            OldInteractionDetails oldDetails = (OldInteractionDetails) secondInteractor.getInteractionDetails().iterator().next();

            writer.write(oldDetails.getNumberOfExperiments());
            writer.write(';');
            writer.write(' ');
            writer.write("IntAct=");
            writer.write(secondInteractor.getFirstIntacAc());
            writer.write(',');
            writer.write(' ');
            writer.write(secondInteractor.getSecondInteractor());
            writer.write(';');

            writer.write(WriterUtils.NEW_LINE);
        }
        writer.write("//");
        writer.write(WriterUtils.NEW_LINE);
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }

    /**
     * Write the CC line title
     * @param uniprot1
     */
    private void writeCCLineTitle(String uniprot1) throws IOException {
        writer.write("AC   ");
        if (uniprot1.contains("-")){
            String uniprotParent = uniprot1.substring(0, uniprot1.indexOf("-"));
            writer.write(uniprotParent);
        }
        else{
            writer.write(uniprot1);
        }
        writer.write(WriterUtils.NEW_LINE);
        writer.write("CC   -!- INTERACTION:");
        writer.write(WriterUtils.NEW_LINE);
    }

    void fireCcLineCreatedEvent(CcLineCreatedEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == CcLineEventListener.class) {
                ((CcLineEventListener) listeners[i + 1]).ccLineCreated(evt);
            }
        }
    }

    public void addCcLineExportListener(CcLineEventListener eventListener) {
        listenerList.add(CcLineEventListener.class, eventListener);
    }

    // This methods allows classes to unregister for MyEvents
    public void removeCcLineExportListener(CcLineEventListener eventListener) {
        listenerList.remove(CcLineEventListener.class, eventListener);
    }
}
