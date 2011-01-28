package uk.ac.ebi.intact.util.uniprotExport.writers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * Default converters of GO lines
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public class GOLineWriterImpl implements GOLineWriter{

    private FileWriter writer;

    public GOLineWriterImpl(String fileName) throws IOException {
        writer = new FileWriter(fileName);
    }

    @Override
    public void writeGOLine(String uniprot1, String uniprot2, Set<String> pubmedIds) throws IOException {
        // build a pipe separated list of pubmed IDs
        StringBuffer pubmedBuffer = new StringBuffer();
        writePubmedLine(pubmedIds, pubmedBuffer);

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

    private void writePubmedLine(Set<String> pubmedIds, StringBuffer sb){

        if (pubmedIds.isEmpty()) {
            System.out.println("ERROR: No PubMed ID found in that set of experiments. ");
            return;
        }

        // build a pipe separated list of pubmed IDs
        for (Iterator iterator = pubmedIds.iterator(); iterator.hasNext();) {
            String pubmed = (String) iterator.next();
            sb.append("PMID:").append(pubmed);
            if (iterator.hasNext()) {
                sb.append('|');
            }
        }

        sb.append(WriterUtils.TABULATION);
    }

    private void writeGOLine(String uniprot1, String uniprot2, boolean self, StringBuffer pubmedBuffer, StringBuffer line){
        writeFirstInteractor(uniprot1, line);

        writeBindingType(uniprot1, uniprot2, line, self);

        writeGeneralLine(uniprot2, pubmedBuffer, line);

    }

    private void writeGeneralLine(String uniprot2, StringBuffer pubmedBuffer, StringBuffer line) {
        line.append(pubmedBuffer.toString()); // DB:Reference

        line.append("IPI").append(WriterUtils.TABULATION); // Evidence
        line.append("UniProt:").append(uniprot2).append(WriterUtils.TABULATION); // with
        line.append(WriterUtils.TABULATION); // Aspect
        line.append(WriterUtils.TABULATION); // DB_Object_name
        line.append(WriterUtils.TABULATION); // synonym
        line.append(WriterUtils.TABULATION); // DB_object_type
        line.append(WriterUtils.TABULATION); // Taxon_ID
        line.append(WriterUtils.TABULATION); // Date
        line.append("IntAct");   // Assigned By
        line.append(WriterUtils.NEW_LINE);
    }

    private void writeBindingType(String uniprot1, String uniprot2, StringBuffer line, boolean self) {
        if (self) {
            line.append("GO:0042802").append(WriterUtils.TABULATION); // GoId - protein self binding
            self = true;
        } else {
            line.append("GO:0005515").append(WriterUtils.TABULATION); // GoId - protein binding
        }
    }

    private void writeFirstInteractor(String uniprot1, StringBuffer sb) {
        sb.append("UniProt").append(WriterUtils.TABULATION); // DB
        sb.append(uniprot1).append(WriterUtils.TABULATION); // DB_object_ID
        sb.append(WriterUtils.TABULATION); // DB_Object_symbol
        sb.append(WriterUtils.TABULATION); // Qualifier
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }

}
