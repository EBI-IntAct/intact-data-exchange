package uk.ac.ebi.intact.dataexchange.dbimporter.processor;

import org.apache.commons.lang.StringUtils;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import psidev.psi.mi.jami.listener.comparator.ComplexComparatorListener;
import psidev.psi.mi.jami.listener.comparator.analyzer.ComplexComparatorListenerAnalyzer;
import psidev.psi.mi.jami.listener.comparator.event.ComplexComparisonEvent;
import psidev.psi.mi.jami.listener.comparator.impl.ComplexComparatorListenerImpl;
import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.Complex;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import uk.ac.ebi.intact.jami.service.ComplexService;
import uk.ac.ebi.intact.jami.synchronizer.impl.ComplexSynchronizer;

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
        Collection<String> complexesDiffOnlyByStoichiometry = null;
        ComplexComparatorListener complexComparatorListener = new ComplexComparatorListenerImpl();
        try {
            // for duplication check
            ComplexSynchronizer complexSynchronizer = (ComplexSynchronizer) complexService.getIntactDao().getSynchronizerContext().getComplexSynchronizer();
            complexSynchronizer.setComplexComparatorListener(complexComparatorListener);
            complexAlreadyExistsAcs = complexService.getIntactDao().getSynchronizerContext().getComplexSynchronizer().findAllMatchingAcs(item);
        } catch (Exception e) {
            errorWriter.write("Could not check for duplication");
            throw new Exception("Could not check for duplication, aborting job");
        }
        if (!complexAlreadyExistsAcs.isEmpty()) {
            errorWriter.write("This complex already exists in intact, acs of duplicate complexes found :" + complexAlreadyExistsAcs);
            throw new DuplicateEntityException("This complex already exists in intact, acs of duplicate complexes found :" + complexAlreadyExistsAcs);
        } else {
            complexesDiffOnlyByStoichiometry = ComplexComparatorListenerAnalyzer.getComplexAcsDifferentOnlyByStoichiometry(complexComparatorListener);
            if (!complexesDiffOnlyByStoichiometry.isEmpty()) {
                Annotation annotation = AnnotationUtils.createCaution("This complex is almost duplicate (" + ComplexComparisonEvent.EventType.ONLY_STOICHIOMETRY_DIFFERENT.getMessage() + ") with complex acs:: " + StringUtils.join(complexesDiffOnlyByStoichiometry, ", "));
                item.getAnnotations().add(annotation);
            }
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
        ((ComplexSynchronizer) complexService.getIntactDao().getSynchronizerContext().getComplexSynchronizer()).setComplexComparatorListener(null);
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
