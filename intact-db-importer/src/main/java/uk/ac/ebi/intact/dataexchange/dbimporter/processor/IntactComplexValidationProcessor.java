package uk.ac.ebi.intact.dataexchange.dbimporter.processor;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import psidev.psi.mi.jami.model.Complex;
import uk.ac.ebi.intact.jami.service.ComplexService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

public class IntactComplexValidationProcessor implements ItemProcessor<Complex, Complex>, ItemStream {

    private Resource errorResource;
    private Writer errorWriter;
    private ComplexService complexService;

    public Complex process(Complex item) throws Exception {
        if (this.complexService == null) {
            throw new IllegalStateException("ComplexService must be provided.");
        }
        if (item == null) {
            return null;
        }

        // add validations
        Collection<String> complexAlreadyExistsAcs = null;
        try {
            // for duplication check
            complexAlreadyExistsAcs = complexService.getIntactDao().getSynchronizerContext().getComplexSynchronizer().findAllMatchingAcs(item);
        } catch (Exception e) {
            errorWriter.write("Could not check for duplication");
            throw new Exception("Could not check for duplication, aborting job");
        }
        if (!complexAlreadyExistsAcs.isEmpty()) {
            errorWriter.write("This complex already exists in intact, acs of duplicate complexes found :" + complexAlreadyExistsAcs);
            throw new DuplicateEntityException("This complex already exists in intact, acs of duplicate complexes found :" + complexAlreadyExistsAcs);
        }

        return item;
    }

    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        if (errorResource == null) {
            throw new IllegalStateException("Error resource must be provided. ");
        }

        if (complexService == null) {
            throw new IllegalStateException("ComplexService must be provided. ");
        }

        File fileToRead = null;
        try {
            fileToRead = errorResource.getFile();

            this.errorWriter = new FileWriter(fileToRead);
        } catch (IOException e) {
            throw new ItemStreamException("Error resource must be writable: "
                    + errorResource, e);
        }
    }

    public void update(ExecutionContext executionContext) throws ItemStreamException {
        try {
            errorWriter.flush();
        } catch (IOException e) {
            throw new ItemStreamException("Cannot flush Error resource: "
                    + errorResource, e);
        }
    }

    public void close() throws ItemStreamException {
        if (errorWriter != null) {
            try {
                errorWriter.close();
            } catch (IOException e) {
                throw new ItemStreamException("Error resource cannot be closed: "
                        + errorResource, e);
            }
        }
    }

    public ComplexService getComplexService() {
        return complexService;
    }

    public void setComplexService(ComplexService complexService) {
        this.complexService = complexService;
    }

    public Resource getErrorResource() {
        return errorResource;
    }

    public void setErrorResource(Resource errorResource) {
        this.errorResource = errorResource;
    }

    public Writer getErrorWriter() {
        return errorWriter;
    }

    public void setErrorWriter(Writer errorWriter) {
        this.errorWriter = errorWriter;
    }
}
