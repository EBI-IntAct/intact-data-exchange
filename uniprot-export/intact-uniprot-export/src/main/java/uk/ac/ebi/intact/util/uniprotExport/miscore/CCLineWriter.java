package uk.ac.ebi.intact.util.uniprotExport.miscore;

import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.CcLine;
import uk.ac.ebi.intact.util.uniprotExport.event.CcLineCreatedEvent;
import uk.ac.ebi.intact.util.uniprotExport.event.CcLineEventListener;
import uk.ac.ebi.intact.util.uniprotExport.miscore.extension.IntActInteractionClusterScore;

import javax.swing.event.EventListenerList;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Write CC lines
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27/01/11</pre>
 */

public class CCLineWriter {

    private IntActInteractionClusterScore clusterScore;
    private final static String PUBMED = "pubmed";
    private final static String UNIPROT = "uniprotkb";
    protected static final String NEW_LINE = System.getProperty("line.separator");
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * Use to out the CC lines in a file.
     */
    private Writer ccWriter;

    public CCLineWriter(IntActInteractionClusterScore clusterScore, String fileName) throws IOException {
        this.clusterScore = clusterScore;
        ccWriter = new FileWriter(fileName);
    }

    public IntActInteractionClusterScore getClusterScore() {
        return clusterScore;
    }

    public void setClusterScore(IntActInteractionClusterScore clusterScore) {
        this.clusterScore = clusterScore;
    }

    public void write() throws IOException {

        for (Map.Entry<String, List<Integer>> interactor : this.clusterScore.getInteractorMapping().entrySet()){
            String uniprotAc = interactor.getKey();
            List<Integer> interactions = interactor.getValue();

            List<CcLine> ccLines = new ArrayList<CcLine>();

            for (Integer interactionId : interactions){
                EncoreInteraction interaction = this.clusterScore.getInteractionMapping().get(interactionId);

                if (interaction != null){
                    CcLine line = createCCLine(interaction, uniprotAc);

                    if (line != null){
                        ccLines.add(line);
                    }
                }
            }

            if (!ccLines.isEmpty()){
                Collections.sort(ccLines);

                StringBuffer sb = new StringBuffer(128 * ccLines.size());

                sb.append("AC").append("   ").append(uniprotAc);
                sb.append(NEW_LINE);

                sb.append("CC   -!- INTERACTION:");
                sb.append(NEW_LINE);

                for ( CcLine ccLine : ccLines ) {
                    sb.append(ccLine.getCcLine());
                }

                sb.append("//");
                sb.append(NEW_LINE);

                String ccs = sb.toString();

                // write the content in the output file.
                ccWriter.write(ccs);
                ccWriter.flush();

                // fire the event
                fireCcLineCreatedEvent(new CcLineCreatedEvent(this, uniprotAc, ccLines));
            }
        }
    }

    /**
     * create the output of a CC line for a set of exportable interactions.
     *
     * @param interaction
     */
    private CcLine createCCLine( EncoreInteraction interaction, String firstInteractor) {

        String uniprot1 = interaction.getInteractorA(UNIPROT);
        String uniprot2 = interaction.getInteractorB(UNIPROT);

        if (uniprot1 != null && uniprot2 != null && firstInteractor != null){
            // produce the CC lines for the 1st protein
            CcLine cc1 = null;

            if (firstInteractor.equals(uniprot1)){
                formatCCLines(interaction, uniprot1, uniprot2);
            }
            else {
                formatCCLines(interaction, uniprot2, uniprot1);
            }

            return cc1;
        }

        return null;
    }

    private Set<String> extractPubmedIdentifiersFrom(EncoreInteraction interaction){
        List<CrossReference> publications = interaction.getPublicationIds();
        Set<String> pubmeds = new HashSet<String>(publications.size());

        for (CrossReference ref : publications){
            if (ref.getDatabase() != null && ref.getIdentifier() != null){
                if (ref.getDatabase().equalsIgnoreCase(PUBMED)){
                    pubmeds.add(ref.getIdentifier());
                }
            }
        }
        return pubmeds;
    }

    /**
     * Format introduced on July 29th 2009.
     *
     * Generate the CC line content based on the Interaction and its two interactor.
     * <br> protein1 is the entry in which that CC content will appear.
     * <p/>
     * <pre>
     *          <font color=gray>ID   rr44_HUMAN     STANDARD;      PRT;   123 AA.</font>
     *          <font color=gray>AC   P01232</font>
     *          <font color=gray>GN   rr44.</font>
     *          CC   -!- INTERACTION:
     *          CC       Interact=Yes (PubMed:12344567); Xref=IntAct:EBI-375446,EBI-389883;
     *          CC         Protein1=rr44 [P01232];
     *          CC         Protein2=tsr [P10981];
     * </pre>
     * @param interaction : the interaction to convert
     *
     * @return a CCLine
     */
    private CcLine formatCCLines(EncoreInteraction interaction, String uniprot1, String uniprot2) {

        StringBuffer buffer = new StringBuffer(256); // average size is 160 char

        buffer.append("CC       Interact=Yes ");

        // collect all pubmeds and format them
        Set<String> pubmeds = extractPubmedIdentifiersFrom(interaction);
        if(!pubmeds.isEmpty()) {
            buffer.append("(");
            for ( Iterator<String> iterator = pubmeds.iterator(); iterator.hasNext(); ) {
                String pubmed = iterator.next();
                buffer.append("PubMed:").append( pubmed );
                if(iterator.hasNext()) {
                    buffer.append( ", " );
                }
            }
            buffer.append(");");
        }

        buffer.append(" Xref=IntAct:").append( uniprot1 ).append(',').append(uniprot2).append(';');
        buffer.append(NEW_LINE);

        buffer.append("CC         Protein1=");
        String geneName1 = this.clusterScore.getGeneNames().get(uniprot1);
        buffer.append( geneName1 ).append(' ').append( '[' ).append( uniprot1 ).append( ']' ).append( ';' );
        buffer.append(NEW_LINE);

        buffer.append("CC         Protein2=");
        String geneName2 = this.clusterScore.getGeneNames().get(uniprot2);
        buffer.append( geneName2 ).append(' ').append( '[' ).append( uniprot2 ).append( ']' ).append( ';' );

        // handle protein originating from different organism
        String taxId1 = this.clusterScore.getOrganismTaxIds().get(uniprot1);
        String taxId2 = this.clusterScore.getOrganismTaxIds().get(uniprot2);

        String organism1 = this.clusterScore.getOrganismNames().get(uniprot1);
        String organism2 = this.clusterScore.getOrganismNames().get(uniprot2);

        if (!taxId1.equalsIgnoreCase(taxId2)) {
            buffer.append(" Organism=");
            buffer.append( organism2 ).append( " [NCBI_TaxID=" ).append( taxId2 ).append( "]" );
            buffer.append(';');
        }

        buffer.append(NEW_LINE);

        return new CcLine(buffer.toString(), geneName1, uniprot2);
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
