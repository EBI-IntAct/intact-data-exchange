package uk.ac.ebi.intact.util.uniprotExport.writers.golinewriters;

import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters1;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Default converters of GO lines
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public class GOLineWriter1 implements GOLineWriter<GOParameters1>{

    /**
     * The writer
     */
    private OutputStreamWriter writer;

    public GOLineWriter1(OutputStreamWriter outputStream) throws IOException {
        if (outputStream == null){
             throw new IllegalArgumentException("You must give a non null OutputStream writer");
        }
        writer = outputStream;
    }

    @Override
    public void writeGOLine(GOParameters1 parameters) throws IOException {
        // if the parameter is not null, we can write the go line
        if (parameters != null){
            String uniprot1 = parameters.getFirstProtein();
            String uniprot2 = parameters.getSecondProtein();

            boolean self = false;
            if (uniprot1.equals(uniprot2)){
                self = true;
            }

            // generate the line
            writeGOLine(uniprot1, uniprot2, self, parameters.getPubmedIds());

            /*if (!self) {
                // write the reverse

                writeGOLine(uniprot2, uniprot1, self, parameters.getPubmedIds());
            }*/

            writer.flush();
        }
    }

    @Override
    public void writeGOLines(List<GOParameters1> GOLines) throws IOException {
        for (GOParameters1 parameter : GOLines){
            writeGOLine(parameter);
        }
    }

    /**
     * Write a list of pubmed ids
     * @param pubmedIds
     * @throws IOException
     */
    private void writePubmedLine(Set<String> pubmedIds) throws IOException {

        if (pubmedIds.isEmpty()) {
            System.out.println("ERROR: No PubMed ID found in that set of experiments. ");
            return;
        }

        // build a pipe separated list of pubmed IDs
        for (Iterator iterator = pubmedIds.iterator(); iterator.hasNext();) {
            String pubmed = (String) iterator.next();
            writer.write("PMID:");
            writer.write(pubmed);
            if (iterator.hasNext()) {
                writer.write('|');
            }
        }

        writer.write(WriterUtils.TABULATION);
    }

    /**
     * Write the GO line
     * @param uniprot1
     * @param uniprot2
     * @param self
     * @param pubmedBuffer
     * @throws IOException
     */
    private void writeGOLine(String uniprot1, String uniprot2, boolean self, Set<String> pubmedBuffer) throws IOException {
        // first interactor
        writeFirstInteractor(uniprot1);

        // binding type
        writeBindingType(self);

        // write information
        writeGeneralLine(uniprot2, pubmedBuffer);

        writer.flush();

    }

    /**
     * Write details of GO line
     * @param uniprot2
     * @param pubmedBuffer
     * @throws IOException
     */
    private void writeGeneralLine(String uniprot2, Set<String> pubmedBuffer) throws IOException {
        writePubmedLine(pubmedBuffer);

        writer.write("IPI");
        writer.write(WriterUtils.TABULATION); // Evidence
        writer.write("UniProt:");
        writer.write(uniprot2);
        writer.write(WriterUtils.TABULATION); // with
        writer.write(WriterUtils.TABULATION); // Aspect
        writer.write(WriterUtils.TABULATION); // DB_Object_name
        writer.write(WriterUtils.TABULATION); // synonym
        writer.write(WriterUtils.TABULATION); // DB_object_type
        writer.write(WriterUtils.TABULATION); // Taxon_ID
        writer.write(WriterUtils.TABULATION); // Date
        writer.write("IntAct");   // Assigned By
        writer.write(WriterUtils.NEW_LINE);
    }

    /**
     * Write binding type
     * @param self
     * @throws IOException
     */
    private void writeBindingType(boolean self) throws IOException {
        if (self) {
            writer.write("GO:0042802");
            writer.write(WriterUtils.TABULATION); // GoId - protein self binding
            self = true;
        } else {
            writer.write("GO:0005515");
            writer.write(WriterUtils.TABULATION); // GoId - protein binding
        }
    }

    /**
     * Write the first interactor
     * @param uniprot1
     * @throws IOException
     */
    private void writeFirstInteractor(String uniprot1) throws IOException {
        writer.write("UniProt");
        writer.write(WriterUtils.TABULATION); // DB
        writer.write(uniprot1);
        writer.write(WriterUtils.TABULATION); // DB_object_ID
        writer.write(WriterUtils.TABULATION); // DB_Object_symbol
        writer.write(WriterUtils.TABULATION); // Qualifier
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }

}
