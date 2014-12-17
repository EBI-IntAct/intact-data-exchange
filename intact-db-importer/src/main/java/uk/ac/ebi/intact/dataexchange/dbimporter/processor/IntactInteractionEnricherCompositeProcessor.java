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

    private ItemProcessor<InteractionEvidence,InteractionEvidence> interactionEvidenceProcessor;
    private ItemProcessor<Complex,Complex> complexProcessor;
    private ItemProcessor<ModelledInteraction,ModelledInteraction> modelledInteractionProcessor;
    private ItemProcessor<Interaction,InteractionEvidence> interactionProcessor;

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

    public ItemProcessor<InteractionEvidence,InteractionEvidence> getInteractionEvidenceProcessor() {
        return interactionEvidenceProcessor;
    }

    public void setInteractionEvidenceProcessor(ItemProcessor<InteractionEvidence,InteractionEvidence> interactionEvidenceProcessor) {
        this.interactionEvidenceProcessor = interactionEvidenceProcessor;
    }

    public ItemProcessor<Complex, Complex> getComplexProcessor() {
        return complexProcessor;
    }

    public void setComplexProcessor(ItemProcessor<Complex, Complex> complexProcessor) {
        this.complexProcessor = complexProcessor;
    }

    public ItemProcessor<ModelledInteraction, ModelledInteraction> getModelledInteractionProcessor() {
        return modelledInteractionProcessor;
    }

    public void setModelledInteractionProcessor(ItemProcessor<ModelledInteraction, ModelledInteraction> modelledInteractionProcessor) {
        this.modelledInteractionProcessor = modelledInteractionProcessor;
    }

    public ItemProcessor<Interaction, InteractionEvidence> getInteractionProcessor() {
        return interactionProcessor;
    }

    public void setInteractionProcessor(ItemProcessor<Interaction, InteractionEvidence> interactionProcessor) {
        this.interactionProcessor = interactionProcessor;
    }
}
