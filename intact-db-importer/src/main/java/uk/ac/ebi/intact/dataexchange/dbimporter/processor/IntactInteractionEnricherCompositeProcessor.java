package uk.ac.ebi.intact.dataexchange.dbimporter.processor;

import org.springframework.batch.item.ItemProcessor;
import psidev.psi.mi.jami.model.Complex;
import psidev.psi.mi.jami.model.Interaction;
import psidev.psi.mi.jami.model.InteractionEvidence;
import psidev.psi.mi.jami.model.ModelledInteraction;

/**
 * Spring batch processor that enriches the interactions
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23/07/13</pre>
 */

public class IntactInteractionEnricherCompositeProcessor implements ItemProcessor<Interaction, Interaction> {

    private IntactEnricherProcessor<InteractionEvidence> interactionEvidenceProcessor;
    private IntactEnricherProcessor<Complex> complexProcessor;
    private IntactEnricherProcessor<ModelledInteraction> modelledInteractionProcessor;
    private IntactEnricherProcessor<Interaction> interactionProcessor;

    public Interaction process(Interaction item) throws Exception {
        if (this.interactionEvidenceProcessor == null && this.complexProcessor == null && this.modelledInteractionProcessor == null
                && this.interactionProcessor == null){
            throw new IllegalStateException("The IntactInteractionEnricherCompositeProcessor needs at least one non null InteractionProcessor.");
        }
        if (item == null){
            return null;
        }

        // enrich interaction
        if (interactionEvidenceProcessor != null && item instanceof InteractionEvidence){
            return interactionEvidenceProcessor.process((InteractionEvidence)item);
        }
        else if (complexProcessor != null && item instanceof Complex){
            return complexProcessor.process((Complex)item);
        }
        else if (modelledInteractionProcessor != null && item instanceof ModelledInteraction){
            return modelledInteractionProcessor.process((ModelledInteraction)item);
        }
        else if (interactionProcessor != null){
            return interactionProcessor.process(item);
        }

        return null;
    }

    public IntactEnricherProcessor<InteractionEvidence> getInteractionEvidenceProcessor() {
        return interactionEvidenceProcessor;
    }

    public void setInteractionEvidenceProcessor(IntactEnricherProcessor<InteractionEvidence> interactionEvidenceProcessor) {
        this.interactionEvidenceProcessor = interactionEvidenceProcessor;
    }

    public IntactEnricherProcessor<Complex> getComplexProcessor() {
        return complexProcessor;
    }

    public void setComplexProcessor(IntactEnricherProcessor<Complex> complexProcessor) {
        this.complexProcessor = complexProcessor;
    }

    public IntactEnricherProcessor<ModelledInteraction> getModelledInteractionProcessor() {
        return modelledInteractionProcessor;
    }

    public void setModelledInteractionProcessor(IntactEnricherProcessor<ModelledInteraction> modelledInteractionProcessor) {
        this.modelledInteractionProcessor = modelledInteractionProcessor;
    }

    public IntactEnricherProcessor<Interaction> getInteractionProcessor() {
        return interactionProcessor;
    }

    public void setInteractionProcessor(IntactEnricherProcessor<Interaction> interactionProcessor) {
        this.interactionProcessor = interactionProcessor;
    }
}
