package uk.ac.ebi.intact.util.uniprotExport.writers.golinewriters;

import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters2;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils.NEW_LINE;
import static uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils.TABULATION;

/**
 * writer of GO lines, version 2
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/01/12</pre>
 */

public class GOLineWriter2 implements GOLineWriter<GOParameters2> {

    /**
     * The writer
     */
    private static final Format DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    private OutputStreamWriter writer;

    public GOLineWriter2(OutputStreamWriter outputStream) throws IOException {
        if (outputStream == null){
            throw new IllegalArgumentException("You must give a non null OutputStream writer");
        }
        writer = outputStream;
        writeHeader();
    }

    private void writeHeader() throws IOException {
        writer.write("!gaf-version: 2.2");
        writer.write(NEW_LINE);
        writer.write("!generated-by: IntAct");
        writer.write(NEW_LINE);
        writer.write("!date-generated: " + DATE_FORMATTER.format(new Date()));
        writer.write(NEW_LINE);
        writer.flush();
    }

    @Override
    public void writeGOLine(GOParameters2 parameters) throws IOException {
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
            writeGOLine(master, uniprot1, uniprot2, self, parameters.getPubmedId(), parameters.getComponentXrefs());

            writer.flush();
        }
    }

    @Override
    public void writeGOLines(List<GOParameters2> GOLines) throws IOException {
        for (GOParameters2 parameter : GOLines){
            writeGOLine(parameter);
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
    private void writeGOLine(String master1, String uniprot1, String uniprot2, boolean self, String pubmedBuffer, Set<String> goRefs) throws IOException {
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
     * Write the first interactor
     * @param uniprot1
     * @throws IOException
     */
    private void writeFirstInteractor(String uniprot1) throws IOException {

        /* 1 DB e.g. UniProtKB - required */
        writer.write("UniProtKB");
        writer.write(TABULATION);

        /* 2 DB Object ID e.g. P12345 - required */
        writer.write(uniprot1);
        writer.write(TABULATION);

        /* 3 DB Object Symbol e.g. PHO3 - required */
        // TODO So far Uniprot GOA is taking care of it
        writer.write(TABULATION);

        /* 4 Qualifier e.g. NOT|involved_in - required */
        writer.write("enables");
        writer.write(TABULATION);
    }

    /**
     * Write binding type
     * @param self
     * @throws IOException
     */
    private void writeBindingType(boolean self) throws IOException {

        /* 5 GO ID e.g. GO:0003993 - required */
        if (self) {
            writer.write("GO:0042802");
            writer.write(TABULATION); // protein self binding
        } else {
            writer.write("GO:0005515");
            writer.write(TABULATION); // protein binding
        }
    }

    /**
     * Write a pubmed id
     * @param pubmedIds
     * @throws IOException
     */
    private void writePubmedLine(String pubmedIds) throws IOException {

        if (pubmedIds == null) {
            System.out.println("ERROR: No PubMed ID found in that set of experiments. ");
            return;
        }
        /* 6 DB:Reference (|DB:Reference) e.g. PMID:2676709 - required, cardinality 1 or greater */
        writer.write("PMID:");
        writer.write(pubmedIds);
        writer.write(TABULATION);
    }

    /**
     * Write details of GO line
     * @param uniprot2
     * @param pubmedBuffer
     * @throws IOException
     */
    private void writeGeneralLine(String uniprot2, String pubmedBuffer, String isoform, Set<String> goRefs) throws IOException {
        writePubmedLine(pubmedBuffer);
        String fixedSecondInteractor = uniprot2.replace("-PRO_", ":PRO_");

        /* 7 Evidence Code e.g. IPI - required */
        writer.write("IPI");
        writer.write(TABULATION);

        /* 8 With (or) From e.g. GO:0000346 - optional */
        writer.write("UniProtKB:");
        writer.write(fixedSecondInteractor);
        writer.write(TABULATION);

        /* 9 Aspect e.g. F P or C from column 5 - required */
        writer.write("F"); // Protein Binding events are molecular functions
        writer.write(TABULATION);

        /* 10 DB Object Name e.g. Toll-like receptor 4 - optional */
        // Maybe we cab add it in the future?
        writer.write(TABULATION);

        /* 11 DB Object Synonym (|Synonym) e.g. hToll - optional, cardinality 0 or greater */
        // Maybe we cab add it in the future?
        writer.write(TABULATION);

        /* 12 DB Object Type e.g. protein - required */
        // Note: For the moment we only expose protein protein binding -> protein
        // so either column 2 or 17 are proteins
        writer.write("protein");
        writer.write(TABULATION);

        /* 13 Taxon(|taxon)	e.g. taxon:9606 - required 1 or 2 */
        // TODO So far Uniprot GOA is taking care of it Only one taxon and the reverse
        // interaction with the other
        writer.write(TABULATION);

        /* 14 Date on which the annotation was made, format is YYYYMMDD - required */
        // TODO It is not passed from thhe cluster data. Improvement
        writer.write(TABULATION);

        /* 15 Assigned By e.g. IntAct - required */
        writer.write("IntAct");
        writer.write(TABULATION);

        /* 16 Annotation Extension e.g. part_of(CL:0000576) - optional */
        writeGOAnnotationLine(goRefs);
        writer.write(TABULATION);

        /* 17 Gene Product Form ID e.g. UniProtKB:P12345-2 - optional */
        if (isoform != null){
            String fixedIsoform = isoform.replace("-PRO_", ":PRO_");
            writer.write("UniProtKB:"); // Gene product form id
            writer.write(fixedIsoform);
        }
        writer.write(NEW_LINE);
    }

    /**
     * Write a list of go ref
     * @param goRefs
     * @throws IOException
     */
    private void writeGOAnnotationLine(Set<String> goRefs) throws IOException {

        if (!goRefs.isEmpty()) {
            // build a pipe separated list of GO Refs IDs
            for (Iterator iterator = goRefs.iterator(); iterator.hasNext();) {
                String go = (String) iterator.next();
                writer.write("occurs_in(");
                writer.write(go);
                writer.write(")");
                if (iterator.hasNext()) {
                    writer.write('|');
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }
}
