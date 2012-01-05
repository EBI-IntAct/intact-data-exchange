package uk.ac.ebi.intact.util.uniprotExport.writers.golinewriters;

import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.DefaultGOParameters2;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * writer of GO lines, version 2
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/01/12</pre>
 */

public class DefaultGOLineWriter2 implements GOLineWriter<DefaultGOParameters2> {

    /**
     * The writer
     */
    private OutputStreamWriter writer;

    public DefaultGOLineWriter2(OutputStreamWriter outputStream) throws IOException {
        if (outputStream == null){
            throw new IllegalArgumentException("You must give a non null OutputStream writer");
        }
        writer = outputStream;
        writeHeader();
    }

    private void writeHeader() throws IOException {
        writer.write("!gaf-version: 2.0");
        writer.write(WriterUtils.NEW_LINE);
        writer.flush();
    }

    @Override
    public void writeGOLine(DefaultGOParameters2 parameters) throws IOException {
        // if the parameter is not null, we can write the go line
        if (parameters != null){
            String uniprot1 = parameters.getFirstProtein();
            String uniprot2 = parameters.getSecondProtein();
            
            String master = parameters.getMasterProtein();

            boolean self = false;
            if (uniprot1.equals(uniprot2)){
                self = true;
            }

            // generate the line
            writeGOLine(master, uniprot1, uniprot2, self, parameters.getPubmedIds(), parameters.getComponentXrefs());

            writer.flush();
        }
    }

    @Override
    public void writeGOLines(List<DefaultGOParameters2> GOLines) throws IOException {
        for (DefaultGOParameters2 parameter : GOLines){
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
     * Write a list of go ref
     * @param goRefs
     * @throws IOException
     */
    private void writeGOAnnotationLine(Set<String> goRefs) throws IOException {

        if (!goRefs.isEmpty()) {
            // build a pipe separated list of pubmed IDs
            for (Iterator iterator = goRefs.iterator(); iterator.hasNext();) {
                String go = (String) iterator.next();
                writer.write("occurs_in(");
                writer.write(go);
                writer.write(")");
                if (iterator.hasNext()) {
                    writer.write('|');
                }
            }

            writer.write(WriterUtils.TABULATION);
        }
    }

    /**
     * Write the GO line
     * @param uniprot1
     * @param uniprot2
     * @param self
     * @param pubmedBuffer
     * @throws IOException
     */
    private void writeGOLine(String master1, String uniprot1, String uniprot2, boolean self, Set<String> pubmedBuffer, Set<String> goRefs) throws IOException {
        // first interactor is an isoform
        if (master1 != null && !master1.equalsIgnoreCase(uniprot1)){
            writeFirstInteractor(master1);

            // binding type
            writeBindingType(self);

            // write information
            writeGeneralLine(uniprot2, pubmedBuffer, uniprot1, goRefs);
        }
        // first interactor is master protein
        else {
            writeFirstInteractor(uniprot1);

            // binding type
            writeBindingType(self);

            // write information
            writeGeneralLine(uniprot2, pubmedBuffer, null, goRefs);
        }

        writer.flush();

    }

    /**
     * Write details of GO line
     * @param uniprot2
     * @param pubmedBuffer
     * @throws IOException
     */
    private void writeGeneralLine(String uniprot2, Set<String> pubmedBuffer, String isoform, Set<String> goRefs) throws IOException {
        writePubmedLine(pubmedBuffer);

        writer.write("IPI");
        writer.write(WriterUtils.TABULATION); // Evidence
        writer.write("UniProtKB:");
        writer.write(uniprot2);
        writer.write(WriterUtils.TABULATION); // with
        writer.write(WriterUtils.TABULATION); // Aspect
        writer.write(WriterUtils.TABULATION); // DB_Object_name
        writer.write(WriterUtils.TABULATION); // synonym
        writer.write(WriterUtils.TABULATION); // DB_object_type
        writer.write(WriterUtils.TABULATION); // Taxon_ID
        writer.write(WriterUtils.TABULATION); // Date
        writer.write("IntAct");   // Assigned By
        writer.write(WriterUtils.TABULATION); // Annotation extension

        if (isoform != null){
            String fixedIsoform = isoform.replace("-PRO_", ":PRO_");
            writer.write("UniProtKB:"); // Gene product form id
            writer.write(fixedIsoform);
        }
        else {
            writer.write(WriterUtils.TABULATION); //No Gene product form id
        }

        writeGOAnnotationLine(goRefs);
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
        writer.write("UniProtKB");
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
