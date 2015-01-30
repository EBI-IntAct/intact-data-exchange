package uk.ac.ebi.intact.dataexchange.dbimporter.writer;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.Complex;
import psidev.psi.mi.jami.model.Interaction;
import psidev.psi.mi.jami.model.InteractionEvidence;
import psidev.psi.mi.jami.model.ModelledInteraction;
import uk.ac.ebi.intact.jami.service.IntactService;

import javax.annotation.Resource;
import java.util.ArrayList;
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

    @Resource(name = "interactionEvidenceService")
    private IntactService<InteractionEvidence> interactionEvidenceService;

    @Resource(name = "modelledInteractionService")
    private IntactService<ModelledInteraction> modelledInteractionService;

    @Resource(name = "complexService")
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

        this.interactionEvidenceService.getIntactDao().getSynchronizerContext().initialiseDbSynchronizerListener(getSynchronizerListener());
        this.modelledInteractionService.getIntactDao().getSynchronizerContext().initialiseDbSynchronizerListener(getSynchronizerListener());
        this.complexService.getIntactDao().getSynchronizerContext().initialiseDbSynchronizerListener(getSynchronizerListener());
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
        List<InteractionEvidence> evidences = new ArrayList<InteractionEvidence>(is.size());
        List<ModelledInteraction> modelledInteractions = new ArrayList<ModelledInteraction>(is.size());
        List<Complex> complexes = new ArrayList<Complex>(is.size());
        for (Interaction i : is){
            if (i instanceof InteractionEvidence){
                evidences.add((InteractionEvidence)i);
            }
            else if (i instanceof Complex){
                complexes.add((Complex)i);
            }
            else if (i instanceof ModelledInteraction){
                modelledInteractions.add((ModelledInteraction)i);
            }
            else{
                log.severe("Did not recognize interaction type : "+is.getClass().getSimpleName()+" so ignored it.");
            }
        }

        if (!evidences.isEmpty()){
            this.interactionEvidenceService.saveOrUpdate(evidences);
        }
        if (!modelledInteractions.isEmpty()){
            this.modelledInteractionService.saveOrUpdate(modelledInteractions);
        }
        if (!complexes.isEmpty()){
            this.complexService.saveOrUpdate(complexes);
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
