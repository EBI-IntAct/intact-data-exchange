package uk.ac.ebi.intact.util.uniprotExport.writers;

import uk.ac.ebi.intact.util.uniprotExport.parameters.DRParameters;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Default writer for DR lines
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public class DRLineWriterImpl implements DRLineWriter{

    /**
     * The current fileWriter
     */
    private FileWriter writer;

    public DRLineWriterImpl(String fileName) throws IOException {
        writer = new FileWriter(fileName);
    }

    @Override
    public void writeDRLine(DRParameters parameters) throws IOException {
        // if the parameter is not null, write the DR line
        if (parameters != null){
            StringBuffer sb = new StringBuffer();

            // write the title of a DR line
            writeDRTitle(sb);

            // write the content of a DR line
            writeDRLineParameters(parameters, sb);

            // write and flush
            writer.write(sb.toString());
            writer.flush();
        }
    }

    /**
     * Write the content of a DR line
     * @param parameters
     * @param sb
     */
    private void writeDRLineParameters(DRParameters parameters, StringBuffer sb) {
        sb.append(parameters.getUniprotAc()).append("; ");
        sb.append((parameters.getNumberOfInteractions() > 0 ? parameters.getNumberOfInteractions() : "-")+".");
        sb.append(WriterUtils.NEW_LINE);
    }

    /**
     * Write the title of a DR line
     * @param sb
     */
    private void writeDRTitle(StringBuffer sb) {
        sb.append("DR   ");
        sb.append("IntAct; ");
    }

    @Override
    public void writeDRLines(List<DRParameters> DRLines, String fileName) throws IOException {
        this.writer = new FileWriter(fileName, true);

        for (DRParameters parameter : DRLines){
            writeDRLine(parameter);
        }

        this.writer.close();
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }
}
