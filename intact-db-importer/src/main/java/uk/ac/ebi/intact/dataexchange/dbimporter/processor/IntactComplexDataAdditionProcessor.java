package uk.ac.ebi.intact.dataexchange.dbimporter.processor;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import psidev.psi.mi.jami.model.Complex;
import uk.ac.ebi.intact.jami.model.extension.InteractorAnnotation;
import uk.ac.ebi.intact.jami.service.ComplexService;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by anjali on 16/04/20.
 */
public class IntactComplexDataAdditionProcessor implements ItemProcessor<Complex, Complex>, ItemStream {

    private Resource errorResource;
    private Writer errorWriter;
    private ComplexService complexService;
    private String importTag;

    public Complex process(Complex item) throws Exception {
        if (this.complexService == null) {
            throw new IllegalStateException("ComplexService must be provided.");
        }
        if (item == null) {
            return null;
        }

        // add complex complex ac xref
        try {
            item.assignComplexAc(complexService.retrieveNextComplexAc(), "1");
        } catch (Exception e) {
            errorWriter.write("Cannot add complex ac xref  ");
        }

        // add job id annotation
        if (getImportTag() != null) {
            item.getAnnotations().add(new InteractorAnnotation(IntactUtils.createMITopic("remark-internal", null),
                    getImportTag()));
        }else{
            errorWriter.write("Could not add Job ID annotation because Job ID could not be retrieved");
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

    public String getImportTag() {
        return importTag;
    }

    public void setImportTag(String importTag) {
        this.importTag = importTag;
    }


}
