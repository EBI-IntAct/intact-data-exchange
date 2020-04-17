package uk.ac.ebi.intact.dataexchange.dbimporter.processor;

import org.springframework.batch.item.ItemProcessor;
import psidev.psi.mi.jami.model.Complex;

/**
 * Created by anjali on 16/04/20.
 */
public class IntactComplexEnricherCompositeProcessor implements ItemProcessor<Complex, Complex> {

    private ItemProcessor<Complex, Complex> complexEnricherProcessor;
    private ItemProcessor<Complex, Complex> intactComplexDataAdditionProcessor;


    public Complex process(Complex item) throws Exception {
        if (this.complexEnricherProcessor == null || this.intactComplexDataAdditionProcessor == null) {
            throw new IllegalStateException("The IntactComplexEnricherCompositeProcessor needs all the processors instantiated.");
        }
        if (item == null) {
            return null;
        }

        // enrich complex
        Complex intactEnrichedItem = complexEnricherProcessor.process(item);

        // add more intact complex data
        Complex intactComplexDataEnrichedItem = intactComplexDataAdditionProcessor.process(intactEnrichedItem);
        return intactComplexDataEnrichedItem;
    }

    public ItemProcessor<Complex, Complex> getComplexEnricherProcessor() {
        return complexEnricherProcessor;
    }

    public void setComplexEnricherProcessor(ItemProcessor<Complex, Complex> complexEnricherProcessor) {
        this.complexEnricherProcessor = complexEnricherProcessor;
    }

    public ItemProcessor<Complex, Complex> getIntactComplexDataAdditionProcessor() {
        return intactComplexDataAdditionProcessor;
    }

    public void setIntactComplexDataAdditionProcessor(ItemProcessor<Complex, Complex> intactComplexDataAdditionProcessor) {
        this.intactComplexDataAdditionProcessor = intactComplexDataAdditionProcessor;
    }
}
