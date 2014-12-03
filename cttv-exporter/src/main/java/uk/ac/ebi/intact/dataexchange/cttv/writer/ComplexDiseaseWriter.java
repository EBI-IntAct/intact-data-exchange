package uk.ac.ebi.intact.dataexchange.cttv.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import psidev.psi.mi.jami.datasource.InteractionWriter;
import psidev.psi.mi.jami.exception.MIIOException;
import psidev.psi.mi.jami.factory.options.InteractionWriterOptions;
import psidev.psi.mi.jami.model.Complex;
import uk.ac.ebi.intact.dataexchange.cttv.converter.ComplexCttvConverter;
import uk.ac.ebi.intact.dataexchange.cttv.converter.DefaultComplexCttvConverter;

import java.io.*;
import java.util.*;

/**
 * Created by maitesin on 13/11/2014.
 */
public class ComplexDiseaseWriter implements InteractionWriter<Complex> {

    private ObjectMapper mapper = null;
    private Writer writer = null;
    private Boolean isInitialised = false;
    private Boolean first = true;
    private ComplexCttvConverter converter = null;

    public ComplexDiseaseWriter(Map<String, Object> options) {
        this.mapper = new ObjectMapper();
        initialiseContext(options);
    }

    @Override
    public void initialiseContext(Map<String, Object> options) {
        if (options == null && !isInitialised) {
            throw new IllegalArgumentException("The options provided should not be null");
        }
        else if (options == null) {
            return;
        }
        else if (options.containsKey(InteractionWriterOptions.OUTPUT_OPTION_KEY) && options.containsKey(ComplexDiseaseWriterOptions.COMPLEX_CTTV_CONVERTER)) {
            Object output = options.get(InteractionWriterOptions.OUTPUT_OPTION_KEY);

            if (output instanceof File) {
                try {
                    initialiseFile((File) output);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Impossible to open and uk.ac.ebi.intact.dataexchange.cttv.write in output file " + ((File) output).getName(), e);
                }
            } else if (output instanceof OutputStream) {
                initialiseOutputStream((OutputStream) output);
            } else if (output instanceof Writer) {
                initialiseWriter((Writer) output);
            }
            // suspect a file path
            else if (output instanceof String) {
                try {
                    initialiseFile(new File((String) output));
                } catch (IOException e) {
                    throw new IllegalArgumentException("Impossible to open and uk.ac.ebi.intact.dataexchange.cttv.write in output file " + output, e);
                }
            } else {
                throw new IllegalArgumentException("Impossible to uk.ac.ebi.intact.dataexchange.cttv.write in the provided output " + output.getClass().getName() + ", a File, OutputStream, Writer or file path was expected.");
            }
            this.converter = (ComplexCttvConverter) options.get(ComplexDiseaseWriterOptions.COMPLEX_CTTV_CONVERTER);
        }
        else if (!isInitialised){
            throw new IllegalArgumentException("The options for the json writer should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY + " to know where to uk.ac.ebi.intact.dataexchange.cttv.write the complexes and "+ ComplexDiseaseWriterOptions.COMPLEX_CTTV_CONVERTER+" to know which uk.ac.ebi.intact.dataexchange.cttv.converter to use.");
        }
        this.isInitialised = true;
    }

    private void initialiseWriter(Writer output) {
        this.writer = output;
    }

    private void initialiseOutputStream(OutputStream output) {
        this.writer = new OutputStreamWriter(output);
    }

    private void initialiseFile(File output) throws IOException{
        this.writer = new FileWriter(output);
    }

    @Override
    public void start() throws MIIOException {
        if (!isInitialised){
            throw new IllegalArgumentException("The options for the json writer should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY + " to know where to uk.ac.ebi.intact.dataexchange.cttv.write the complexes and "+ ComplexDiseaseWriterOptions.COMPLEX_CTTV_CONVERTER+" to know which uk.ac.ebi.intact.dataexchange.cttv.converter to use.");
        }
        try {
            writeStart();
        } catch (IOException e) {
            throw new MIIOException("Impossible to uk.ac.ebi.intact.dataexchange.cttv.write start of JSON file", e);
        }
    }

    private void writeStart() throws IOException {
        this.writer.write("[");
    }

    @Override
    public void end() throws MIIOException {
        if (!isInitialised) {
            throw new IllegalArgumentException("The options for the json writer should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY + " to know where to uk.ac.ebi.intact.dataexchange.cttv.write the complexes and "+ ComplexDiseaseWriterOptions.COMPLEX_CTTV_CONVERTER+" to know which uk.ac.ebi.intact.dataexchange.cttv.converter to use.");
        }
        try {
            writeEnd();
        } catch (IOException e) {
            throw new MIIOException("Impossible to uk.ac.ebi.intact.dataexchange.cttv.write end of JSON file", e);
        }
    }

    private void writeEnd() throws IOException {
        this.writer.write("]");
    }

    @Override
    public void write(Complex complex) throws MIIOException {
        if (!isInitialised) {
            throw new IllegalArgumentException("The options for the json writer should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY + " to know where to uk.ac.ebi.intact.dataexchange.cttv.write the complexes and "+ ComplexDiseaseWriterOptions.COMPLEX_CTTV_CONVERTER+" to know which uk.ac.ebi.intact.dataexchange.cttv.converter to use.");
        }
        try {
            if (first) {
                this.writer.write(this.mapper.writeValueAsString(this.converter.convertToEvidenceStringFromComplex(complex)));
                first = false;
            }
            else {
                this.writer.write("," + this.mapper.writeValueAsString(this.converter.convertToEvidenceStringFromComplex(complex)));
            }
        } catch (IOException e) {
            throw new MIIOException("Impossible to uk.ac.ebi.intact.dataexchange.cttv.write an object in the JSON file", e);
        }
    }

    @Override
    public void write(Collection<? extends Complex> complexes) throws MIIOException {
        if (!isInitialised) {
            throw new IllegalArgumentException("The options for the json writer should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY + " to know where to uk.ac.ebi.intact.dataexchange.cttv.write the complexes and "+ ComplexDiseaseWriterOptions.COMPLEX_CTTV_CONVERTER+" to know which uk.ac.ebi.intact.dataexchange.cttv.converter to use.");
        }
        for (Complex complex : complexes)
            this.write(complex);
    }

    @Override
    public void write(Iterator<? extends Complex> iterator) throws MIIOException {
        if (!isInitialised) {
            throw new IllegalArgumentException("The options for the json writer should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY + " to know where to uk.ac.ebi.intact.dataexchange.cttv.write the complexes and "+ ComplexDiseaseWriterOptions.COMPLEX_CTTV_CONVERTER+" to know which uk.ac.ebi.intact.dataexchange.cttv.converter to use.");
        }
        while(iterator.hasNext())
            this.write(iterator.next());
    }

    @Override
    public void flush() throws MIIOException {
        if (!isInitialised) {
            throw new IllegalArgumentException("The options for the json writer should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY + " to know where to uk.ac.ebi.intact.dataexchange.cttv.write the complexes and "+ ComplexDiseaseWriterOptions.COMPLEX_CTTV_CONVERTER+" to know which uk.ac.ebi.intact.dataexchange.cttv.converter to use.");
        }
        try {
            this.writer.flush();
        } catch (IOException e) {
            throw new MIIOException("Impossible to flush the JSON file", e);
        }
    }

    @Override
    public void close() throws MIIOException {
        if (!isInitialised) {
            throw new IllegalArgumentException("The options for the json writer should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY + " to know where to uk.ac.ebi.intact.dataexchange.cttv.write the complexes and "+ ComplexDiseaseWriterOptions.COMPLEX_CTTV_CONVERTER+" to know which uk.ac.ebi.intact.dataexchange.cttv.converter to use.");
        }
        try {
            this.writer.close();
        } catch (IOException e) {
            throw new MIIOException("Impossible to close the JSON file", e);
        }
    }

    @Override
    public void reset() throws MIIOException {
        if (!isInitialised) {
            throw new IllegalArgumentException("The options for the json writer should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY + " to know where to uk.ac.ebi.intact.dataexchange.cttv.write the complexes and "+ ComplexDiseaseWriterOptions.COMPLEX_CTTV_CONVERTER+" to know which uk.ac.ebi.intact.dataexchange.cttv.converter to use.");
        }
        try {
            this.writer.flush();
        } catch (IOException e) {
            throw new MIIOException("Impossible to flush the JSON file", e);
        }
        finally {
            this.isInitialised = false;
            this.converter = null;
            this.writer = null;
            this.first = true;
        }
    }

}
