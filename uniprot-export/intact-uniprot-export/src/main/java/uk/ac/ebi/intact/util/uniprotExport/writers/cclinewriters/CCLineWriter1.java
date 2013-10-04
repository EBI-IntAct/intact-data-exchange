package uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters;

import uk.ac.ebi.intact.util.uniprotExport.event.CcLineCreatedEvent;
import uk.ac.ebi.intact.util.uniprotExport.event.CcLineEventListener;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCParameters1;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.SortedSet;

/**
 * Default Writer of the CC line format (version 1)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09/02/11</pre>
 */

public class CCLineWriter1 implements CCLineWriter<CCParameters<SecondCCParameters1>> {

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
    public CCLineWriter1(OutputStreamWriter outputStream) throws IOException {
        if (outputStream == null){
            throw new IllegalArgumentException("You must give a non null OutputStream writer");
        }
        writer = outputStream;
    }

    @Override
    public void writeCCLine(CCParameters<SecondCCParameters1> parameters) throws IOException {

        // if parameter not null, write it
        if (parameters != null){

            // write the title
            writeCCLineTitle(parameters.getMasterUniprotAc());

            // write the content
            writeCCLineParameters(parameters);

            writer.flush();
        }
    }

    /**
     * Write the content of the CC line
     * @param parameters : the parameters
     */
    public void writeCCLineParameters(CCParameters<SecondCCParameters1> parameters) throws IOException {

        String firstUniprotAc = parameters.getMasterUniprotAc();
        String firstTaxId = parameters.getTaxId();

        SortedSet<SecondCCParameters1> secondParameters = parameters.getSecondCCParameters();

        for (SecondCCParameters1 secondInteractor : secondParameters){
            writer.write("CC       ");
            boolean isSelf = secondInteractor.getSecondUniprotAc().equals(firstUniprotAc);
            if (secondInteractor.getSecondUniprotAc().equals(firstUniprotAc)) {

                writer.write("Self");

            } else {

                writer.write(secondInteractor.getSecondUniprotAc());
                writer.write(':');
                writer.write(secondInteractor.getGeneName());
            }

            // generated warning message if the two protein are from different organism
            if (secondInteractor.getTaxId() != null && !firstTaxId.equals(secondInteractor.getTaxId())) {
                writer.write(' ');
                writer.write("(xeno)");
            }

            writer.write(';');
            writer.write(' ');
            writer.write("NbExp=");
            writer.write(Integer.toString(secondInteractor.getNumberOfInteractionEvidences()));
            writer.write(';');
            writer.write(' ');
            writer.write("IntAct=");
            writer.write(secondInteractor.getFirstIntactAc());
            writer.write(',');
            writer.write(' ');
            writer.write(secondInteractor.getSecondIntactAc());
            writer.write(';');

            writer.write(WriterUtils.NEW_LINE);
        }
        writer.write("//");
        writer.write(WriterUtils.NEW_LINE);
        writer.flush();
    }

    @Override
    public void writeCCLines(List<CCParameters<SecondCCParameters1>> CCLines) throws IOException {
        for (CCParameters<SecondCCParameters1> parameters : CCLines){
             writeCCLine(parameters);
        }
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
