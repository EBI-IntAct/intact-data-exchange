package uk.ac.ebi.intact.util.uniprotExport.miscore.writer;

import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.miscore.extension.IntActInteractionClusterScore;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Writer of GO Lines
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public class GOLineWriter extends AbstractWriter{

    public GOLineWriter(IntActInteractionClusterScore clusterScore, String fileName) throws IOException {
        super(clusterScore, fileName);
    }

    private Set<String> extractPubmedIdsFrom(EncoreInteraction interaction){
        Set<String> pubmedIds = new HashSet<String>(interaction.getPublicationIds().size());

        for (CrossReference ref : interaction.getPublicationIds()){
            if (PUBMED.equalsIgnoreCase(ref.getDatabase())){
                pubmedIds.add(ref.getIdentifier());
            }
        }

        return pubmedIds;
    }

    private void writePubmedLine(EncoreInteraction interaction, StringBuffer sb){

        Set pubmeds = extractPubmedIdsFrom(interaction);
        if (pubmeds.isEmpty()) {
            System.out.println("ERROR: No PubMed ID found in that set of experiments. ");
            return;
        }

        // build a pipe separated list of pubmed IDs
        for (Iterator iterator = pubmeds.iterator(); iterator.hasNext();) {
            String pubmed = (String) iterator.next();
            sb.append("PMID:").append(pubmed);
            if (iterator.hasNext()) {
                sb.append('|');
            }
        }

        sb.append(TABULATION);
    }

    private void convertInteractionIntoGOLines(EncoreInteraction interaction) throws IOException {
        String uniprot1 = interaction.getInteractorA(UNIPROT);
        String uniprot2 = interaction.getInteractorB(UNIPROT);

        if (uniprot1 != null && uniprot2 != null){
            // build a pipe separated list of pubmed IDs
            StringBuffer pubmedBuffer = new StringBuffer();
            writePubmedLine(interaction, pubmedBuffer);

            boolean self = false;
            if (uniprot1.equals(uniprot2)){
                self = true;
            }

            // generate the line
            StringBuffer line = new StringBuffer();

            writeGOLine(uniprot1, uniprot2, self, pubmedBuffer, line);

            if (!self) {
                // write the reverse

                writeGOLine(uniprot2, uniprot1, self, pubmedBuffer, line);
            }

            // write into the GO file
            writer.write(line.toString());
            writer.flush();
        }
    }

    private void writeGeneralLine(String uniprot2, StringBuffer pubmedBuffer, StringBuffer line) {
        line.append(pubmedBuffer.toString()); // DB:Reference

        line.append("IPI").append(TABULATION); // Evidence
        line.append("UniProt:").append(uniprot2).append(TABULATION); // with
        line.append(TABULATION); // Aspect
        line.append(TABULATION); // DB_Object_name
        line.append(TABULATION); // synonym
        line.append(TABULATION); // DB_object_type
        line.append(TABULATION); // Taxon_ID
        line.append(TABULATION); // Date
        line.append("IntAct");   // Assigned By
        line.append(NEW_LINE);
    }

    private void writeBindingType(String uniprot1, String uniprot2, StringBuffer line, boolean self) {
        if (self) {
            line.append("GO:0042802").append(TABULATION); // GoId - protein self binding
            self = true;
        } else {
            line.append("GO:0005515").append(TABULATION); // GoId - protein binding
        }
    }

    private void writeFirstInteractor(String uniprot1, StringBuffer sb) {
        sb.append("UniProt").append(TABULATION); // DB
        sb.append(uniprot1).append(TABULATION); // DB_object_ID
        sb.append(TABULATION); // DB_Object_symbol
        sb.append(TABULATION); // Qualifier
    }

    private void writeGOLine(String uniprot1, String uniprot2, boolean self, StringBuffer pubmedBuffer, StringBuffer line){
        writeFirstInteractor(uniprot1, line);

        writeBindingType(uniprot1, uniprot2, line, self);

        writeGeneralLine(uniprot2, pubmedBuffer, line);

    }

    public void write() throws IOException {

        for (Map.Entry<Integer, EncoreInteraction> interaction : this.clusterScore.getInteractionMapping().entrySet()){

            convertInteractionIntoGOLines(interaction.getValue());
        }
    }
}
