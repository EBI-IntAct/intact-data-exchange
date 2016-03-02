package uk.ac.ebi.intact.util.uniprotExport.writers.referencewriters;

import uk.ac.ebi.intact.util.uniprotExport.parameters.referencelineparameters.ReferenceParameters;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ReferenceLineWriter1 implements ReferenceLineWriter {


    /**
     * The writer
     */
    private OutputStreamWriter writer;

    public ReferenceLineWriter1(OutputStreamWriter outputStream) throws IOException {
        if (outputStream == null){
            throw new IllegalArgumentException("You must give a non null OutputStream writer");
        }
        writer = outputStream;
    }
    
    @Override
    public void writeReferenceLine(ReferenceParameters parameters) throws IOException {
        // if the parameter is not null, write the Reference line
        if (parameters != null){
            
            // write the content of a Reference line
            writeReferenceLineParameters(parameters);

            // write and flush
            writer.flush();
        }
    }

    /**
     * Write the content of a Reference line
     * @param parameters
     */
    private void writeReferenceLineParameters(ReferenceParameters parameters) throws IOException {
        writer.write(parameters.getUniProtAc());
        writer.write(WriterUtils.TABULATION);
        writer.write(parameters.getInstitution());
        writer.write(WriterUtils.TABULATION);
        writer.write(parameters.getPMID());
        writer.write(WriterUtils.TABULATION);
        writer.write(parameters.getPublicationAc());
        writer.write(WriterUtils.NEW_LINE);
    }

    @Override
    public void writeReferenceLines(List<ReferenceParameters> ReferenceLines) throws IOException {
        for (ReferenceParameters parameter : ReferenceLines){
            writeReferenceLine(parameter);
        }
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }
}
