package uk.ac.ebi.intact.util.uniprotExport.writers;

import uk.ac.ebi.intact.util.uniprotExport.event.CcLineCreatedEvent;
import uk.ac.ebi.intact.util.uniprotExport.event.CcLineEventListener;
import uk.ac.ebi.intact.util.uniprotExport.parameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.InteractionDetails;

import javax.swing.event.EventListenerList;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.SortedSet;

/**
 * Default converters for CCLines
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public class CCLineWriterImpl implements CCLineWriter{

    private FileWriter writer;
    protected EventListenerList listenerList = new EventListenerList();

    public CCLineWriterImpl(String fileName) throws IOException {
        writer = new FileWriter(fileName);
    }

    @Override
    public void writeCCLine(CCParameters parameters) throws IOException {

        if (parameters != null){

            StringBuffer sb = new StringBuffer();

            writeCCLineTitle(sb);

            writeCCLineParameters(parameters, sb);

            sb.append("//");
            sb.append(WriterUtils.NEW_LINE);

            String ccs = sb.toString();

            // write the content in the output file.
            writer.write(ccs);
            writer.flush();
        }
    }

    public void writeCCLineParameters(CCParameters parameters, StringBuffer sb) {

        // write introduction
        writeInteractionIntroduction(true, parameters.getFirstInteractor(), parameters.getSecondInteractor(), sb);

        // write first protein
        writeFirstProtein(parameters.getFirstInteractor(), parameters.getFirstGeneName(), sb);

        // write second protein
        writeSecondProtein(parameters.getSecondInteractor(), parameters.getSecondGeneName(),
                parameters.getFirstTaxId(), parameters.getSecondTaxId(), parameters.getSecondOrganismName(), sb);

        // write the deatils of the interaction
        writeInteractionDetails(sb, parameters.getInteractionDetails());
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }

    private void writeCCLineTitle(StringBuffer sb){
        sb.append("CC   -!- INTERACTION:");
        sb.append(WriterUtils.NEW_LINE);
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

    private void writeInteractionIntroduction(boolean doesInteract, String uniprot1, String uniprot2, StringBuffer buffer) {
        buffer.append("CC       Interact="+ (doesInteract ? "yes" : "no") +"; ");

        buffer.append(" Xref=IntAct:").append( uniprot1 ).append(',').append(uniprot2).append(';');
        buffer.append(WriterUtils.NEW_LINE);
    }

    private void writeFirstProtein(String uniprot1, String geneName1, StringBuffer buffer) {
        buffer.append("CC         Protein1=");
        buffer.append( geneName1 ).append(' ').append( '[' ).append( uniprot1 ).append( ']' ).append( ';' );
        buffer.append(WriterUtils.NEW_LINE);
    }

    private void writeSecondProtein(String uniprot2, String geneName2, String taxId1, String taxId2, String organism2, StringBuffer buffer) {
        buffer.append("CC         Protein2=");
        buffer.append( geneName2 ).append(' ').append( '[' ).append( uniprot2 ).append( ']' ).append( ';' );

        if (!taxId1.equalsIgnoreCase(taxId2)) {
            buffer.append(" Organism=");
            buffer.append( organism2 ).append( " [NCBI_TaxID=" ).append( taxId2 ).append( "]" );
            buffer.append(';');
        }

        buffer.append(WriterUtils.NEW_LINE);
    }

    private void writeInteractionDetails(StringBuffer buffer, SortedSet<InteractionDetails> interactionDetails) {

        // collect all pubmeds and spoke expanded information
        for (InteractionDetails details : interactionDetails){
            String type = details.getInteractionType();
            String method = details.getDetectionMethod();

            if (details.isSpokeExpanded()){
                writeSpokeExpandedInteractions(buffer, type, method, details.getPubmedIds());
            }
            else{
                writeBinaryInteraction(buffer, type, method, details.getPubmedIds());
            }
        }
    }

    private void writeSpokeExpandedInteractions(StringBuffer buffer, String type, String method, Set<String> spokeExpandedPubmeds) {
        buffer.append("CC         InteractionType="+type+"; Method="+method+"; Expansion=Spoke; Source=");

        for (String pid : spokeExpandedPubmeds){
            buffer.append("Pubmed:"+pid+", ");
        }

        buffer.deleteCharAt(buffer.length() - 1);
        buffer.deleteCharAt(buffer.length() - 1);
        buffer.append(";");
        buffer.append(WriterUtils.NEW_LINE);
    }

    private void writeBinaryInteraction(StringBuffer buffer, String type, String method, Set<String> binaryInteractions) {
        buffer.append("CC         InteractionType="+type+"; Method="+method+"; Source=");

        for (String pid : binaryInteractions){
            buffer.append("Pubmed:"+pid+", ");
        }
        buffer.deleteCharAt(buffer.length() - 1);
        buffer.deleteCharAt(buffer.length() - 1);
        buffer.append(";");
        buffer.append(WriterUtils.NEW_LINE);
    }
}
