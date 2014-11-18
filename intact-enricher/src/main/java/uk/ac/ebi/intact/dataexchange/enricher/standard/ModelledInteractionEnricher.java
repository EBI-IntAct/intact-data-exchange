/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.enricher.standard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.enricher.ParticipantEnricher;
import psidev.psi.mi.jami.enricher.SourceEnricher;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.enricher.listener.InteractionEnricherListener;
import psidev.psi.mi.jami.enricher.listener.ModelledInteractionEnricherListener;
import psidev.psi.mi.jami.enricher.listener.impl.log.ModelledInteractionEnricherLogger;
import psidev.psi.mi.jami.model.ModelledInteraction;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

/**
 * Intact enricher for complexes
 *
 */
@Component(value = "intactModelledInteractionEnricher")
@Lazy
public class ModelledInteractionEnricher extends AbstractInteractionEnricher<ModelledInteraction> implements psidev.psi.mi.jami.enricher.ModelledInteractionEnricher<ModelledInteraction>{

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(ModelledInteractionEnricher.class);

    @Autowired
    @Qualifier("intactCvObjectEnricher")
    private CvObjectEnricher intactCvObjectEnricher;

    private SourceEnricher intactSourceEnricher;

    public ModelledInteractionEnricher() {
    }

    @Override
    protected String generateAutomaticShortlabel(ModelledInteraction objectToEnrich) {
        return IntactUtils.generateAutomaticShortlabelForModelledInteraction(objectToEnrich, IntactUtils.MAX_SHORT_LABEL_LEN);
    }

    @Override
    protected void processOtherProperties(ModelledInteraction interactionToEnrich) throws EnricherException {
        super.processOtherProperties(interactionToEnrich);

        // process source
        processSource(interactionToEnrich);
    }

    protected void processSource(ModelledInteraction interactionToEnrich) throws EnricherException{
        if( interactionToEnrich.getSource() != null )
            getSourceEnricher().enrich(interactionToEnrich.getSource());
    }

    @Override
    protected void processOtherProperties(ModelledInteraction objectToEnrich, ModelledInteraction objectSource) throws EnricherException {
        super.processOtherProperties(objectToEnrich, objectSource);
        // process source
        processSource(objectToEnrich, objectSource);
    }

    protected void processSource(ModelledInteraction objectToEnrich, ModelledInteraction objectSource) throws EnricherException{
        if (objectSource.getSource() != null && objectToEnrich.getSource() == null){
            objectToEnrich.setSource(objectSource.getSource());
            if (getInteractionEnricherListener() instanceof psidev.psi.mi.jami.enricher.ModelledInteractionEnricher){
                ((ModelledInteractionEnricherListener)getInteractionEnricherListener()).onSourceUpdate(objectToEnrich, null);
            }
        }

        processSource(objectToEnrich);
    }

    @Override
    public SourceEnricher getSourceEnricher() {
        if (this.intactSourceEnricher == null){
            this.intactSourceEnricher = ApplicationContextProvider.getBean("intactInstitutionEnricher");
        }

        return intactSourceEnricher;
    }

    @Override
    public ParticipantEnricher getParticipantEnricher() {
        if (super.getParticipantEnricher() == null){
            super.setParticipantEnricher((ParticipantEnricher) ApplicationContextProvider.getBean("intactModelledParticipantEnricher"));
        }
        return super.getParticipantEnricher();
    }

    @Override
    public InteractionEnricherListener<ModelledInteraction> getInteractionEnricherListener() {
        if (super.getInteractionEnricherListener() == null){
            super.setInteractionEnricherListener(new ModelledInteractionEnricherLogger<ModelledInteraction>());
        }
        return super.getInteractionEnricherListener();
    }
}
