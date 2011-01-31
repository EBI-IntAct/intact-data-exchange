package uk.ac.ebi.intact.util.uniprotExport.writers;

import uk.ac.ebi.intact.util.uniprotExport.parameters.DRParameter;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Default converters for DR lines
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public class DRLineWriterImpl implements DRLineWriter{

    private FileWriter writer;

    public DRLineWriterImpl(String fileName) throws IOException {
        writer = new FileWriter(fileName);
    }

    @Override
    public void writeDRLine(DRParameter parameters) throws IOException {
        if (parameters != null && parameters.getUniprotAc() != null){
            StringBuffer sb = new StringBuffer();

            sb.append("DR   ");
            sb.append("IntAct; ");
            sb.append(parameters.getUniprotAc()).append("; ");
            sb.append((parameters.getNumberOfInteractions() > 0 ? parameters.getNumberOfInteractions() : "-")+".");
            sb.append(WriterUtils.NEW_LINE);

            writer.write(sb.toString());
            writer.flush();
        }
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }
}
