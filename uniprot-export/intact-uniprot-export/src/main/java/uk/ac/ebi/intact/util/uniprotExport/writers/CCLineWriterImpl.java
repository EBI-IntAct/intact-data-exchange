package uk.ac.ebi.intact.util.uniprotExport.writers;

import uk.ac.ebi.intact.util.uniprotExport.CcLine;
import uk.ac.ebi.intact.util.uniprotExport.event.CcLineCreatedEvent;
import uk.ac.ebi.intact.util.uniprotExport.event.CcLineEventListener;

import javax.swing.event.EventListenerList;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public void writeCCLine(String uniprotAc1, List<CcLine> ccLines) throws IOException {

        writeCCLines(ccLines, uniprotAc1);
    }

    @Override
    public CcLine createCCline(String uniprot1, String geneName1, String taxId1, String uniprot2,
                               String geneName2, String taxId2, String organismName2,
                               Map<Map.Entry<String, String>, Set<String>> trueBinaryInteractionDetails,
                               Map<Map.Entry<String, String>, Set<String>> spokeExpandedInteractionDetails) {

        StringBuffer buffer = new StringBuffer(256); // average size is 160 char

        // write introduction
        writeInteractionIntroduction(true, uniprot1, uniprot2, buffer);

        // write first protein
        writeFirstProtein(uniprot1, geneName1, buffer);

        // write second protein
        writeSecondProtein(uniprot2, geneName2, taxId1, taxId2, organismName2, buffer);

        // write the deatils of the interaction
        writeInteractionDetails(buffer, trueBinaryInteractionDetails, spokeExpandedInteractionDetails);

        return new CcLine(buffer.toString(), geneName1, uniprot2);
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }

    private void writeCCLines(List<CcLine> ccLines, String uniprotAc) throws IOException {
        if (!ccLines.isEmpty()){
            Collections.sort(ccLines);

            StringBuffer sb = new StringBuffer(128 * ccLines.size());

            writeCCLineTitle(sb);

            for ( CcLine ccLine : ccLines ) {
                sb.append(ccLine.getCcLine());
            }

            sb.append("//");
            sb.append(WriterUtils.NEW_LINE);

            String ccs = sb.toString();

            // write the content in the output file.
            writer.write(ccs);
            writer.flush();
            // fire the event
            fireCcLineCreatedEvent(new CcLineCreatedEvent(this, uniprotAc, ccLines));
        }
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

    private void writeInteractionDetails(StringBuffer buffer, Map<Map.Entry<String, String>, Set<String>> trueBinaryInteractionDetails,
                               Map<Map.Entry<String, String>, Set<String>> spokeExpandedInteractionDetails) {

        // collect all pubmeds and spoke expanded information
        for (Map.Entry<Map.Entry<String, String>, Set<String>> detail : spokeExpandedInteractionDetails.entrySet()){
            String type = detail.getKey().getValue();
            String method = detail.getKey().getKey();

            writeSpokeExpandedInteractions(buffer, type, method, detail.getValue());
        }

        for (Map.Entry<Map.Entry<String, String>, Set<String>> detail : trueBinaryInteractionDetails.entrySet()){
            String type = detail.getKey().getValue();
            String method = detail.getKey().getKey();

            writeBinaryInteraction(buffer, type, method, detail.getValue());
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
