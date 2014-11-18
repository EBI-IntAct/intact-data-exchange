package uk.ac.ebi.intact.dataexchange.dbimporter.writer;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.Complex;
import psidev.psi.mi.jami.model.Interaction;
import psidev.psi.mi.jami.model.InteractionEvidence;
import psidev.psi.mi.jami.model.ModelledInteraction;
import uk.ac.ebi.intact.jami.service.IntactService;

import java.util.List;
import java.util.logging.Logger;

/**
 * IntAct interaction importer which can deal with several types of interactions
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/11/14</pre>
 */

public class IntactInteractionMixDbImporter extends AbstractIntactDbImporter<Interaction>{

    @Autowired
    @Qualifier("interactionEvidenceService")
    private IntactService<InteractionEvidence> interactionEvidenceService;

    @Autowired
    @Qualifier("modelledInteractionService")
    private IntactService<ModelledInteraction> modelledInteractionService;

    @Autowired
    @Qualifier("complexService")
    private IntactService<Complex> complexService;

    private Logger log = Logger.getLogger(IntactInteractionMixDbImporter.class.getName());

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        super.open(executionContext);

        if (interactionEvidenceService == null){
            throw new IllegalStateException("The interaction evidence service must be provided. ");
        }
        if (modelledInteractionService == null){
            throw new IllegalStateException("The modelled interaction service must be provided. ");
        }
        if (complexService == null){
            throw new IllegalStateException("The complex service must be provided. ");
        }
    }

    @Override
    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    public void write(List<? extends Interaction> is) throws Exception {
        if (this.interactionEvidenceService == null){
            throw new IllegalStateException("The writer must have a non null interaction evidence service");
        }
        if (this.modelledInteractionService == null){
            throw new IllegalStateException("The writer must have a non null modelled interaction service");
        }
        if (this.complexService == null){
            throw new IllegalStateException("The writer must have a non null complex service");
        }

        if (is instanceof InteractionEvidence){
            this.interactionEvidenceService.saveOrUpdate((InteractionEvidence)is);
        }
        else if (is instanceof Complex){
            this.complexService.saveOrUpdate((Complex)is);
        }
        else if (is instanceof ModelledInteraction){
            this.modelledInteractionService.saveOrUpdate((ModelledInteraction)is);
        }
        else{
            log.severe("Did not recognize interaction type : "+is.getClass().getSimpleName()+" so ignored it.");
        }
    }

    public IntactService<InteractionEvidence> getInteractionEvidenceService() {
        return interactionEvidenceService;
    }

    public void setInteractionEvidenceService(IntactService<InteractionEvidence> interactionEvidenceService) {
        this.interactionEvidenceService = interactionEvidenceService;
    }

    public IntactService<ModelledInteraction> getModelledInteractionService() {
        return modelledInteractionService;
    }

    public void setModelledInteractionService(IntactService<ModelledInteraction> modelledInteractionService) {
        this.modelledInteractionService = modelledInteractionService;
    }

    public IntactService<Complex> getComplexService() {
        return complexService;
    }

    public void setComplexService(IntactService<Complex> complexService) {
        this.complexService = complexService;
    }
}
