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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.enricher.*;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.enricher.impl.full.FullInteractionEnricher;
import psidev.psi.mi.jami.enricher.impl.full.FullInteractionEvidenceEnricher;
import psidev.psi.mi.jami.enricher.listener.InteractionEnricherListener;
import psidev.psi.mi.jami.enricher.listener.impl.log.InteractionEvidenceEnricherLogger;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

/**
 * IntAct enricher for interaction evidence
 *
 */
@Component(value = "intactInteractionEvidenceEnricher")
@Lazy
@Scope( BeanDefinition.SCOPE_PROTOTYPE )
public class InteractionEvidenceEnricher extends FullInteractionEvidenceEnricher {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(InteractionEvidenceEnricher.class);

    @Autowired
    private EnricherContext enricherContext;

    @Autowired
    public InteractionEvidenceEnricher(@Qualifier("intactInteractionEnricher") psidev.psi.mi.jami.enricher.InteractionEnricher<InteractionEvidence> interactionEnricher) {
        super((FullInteractionEnricher<InteractionEvidence>)interactionEnricher);
        getInteractionEnricher().setParticipantEnricher(getParticipantEnricher());
    }

    @Override
    protected void processConfidences(InteractionEvidence objectToEnrich, InteractionEvidence objectSource) throws EnricherException {
        if (objectSource != null){
            super.processConfidences(objectToEnrich, objectSource);
        }

        if (enricherContext.getConfig().isUpdateCvTerms() && getCvTermEnricher() != null){
            for (Confidence confidence : objectToEnrich.getConfidences()) {
                getCvTermEnricher().enrich(confidence.getType());
            }
        }
    }

    @Override
    protected void processParameters(InteractionEvidence objectToEnrich, InteractionEvidence objectSource) throws EnricherException {
        if (objectSource != null){
            super.processParameters(objectToEnrich, objectSource);
        }

        if (enricherContext.getConfig().isUpdateCvTerms() && getCvTermEnricher() != null){
            for (Parameter parameter : objectToEnrich.getParameters()) {
                getCvTermEnricher().enrich(parameter.getType());
                if (parameter.getUnit() != null){
                    getCvTermEnricher().enrich(parameter.getUnit());
                }
            }
        }
    }

    @Override
    protected void processInteractionType(InteractionEvidence objectToEnrich, InteractionEvidence objectSource) throws EnricherException {
        if( enricherContext.getConfig().isUpdateCvTerms()
                && getCvTermEnricher() != null &&
                objectToEnrich.getInteractionType() != null)
            getCvTermEnricher().enrich(objectToEnrich.getInteractionType());
    }

    @Override
    protected void processIdentifiers(InteractionEvidence objectToEnrich, InteractionEvidence objectSource) throws EnricherException {
        if (objectSource != null){
            super.processIdentifiers(objectToEnrich, objectSource);
        }

        if (enricherContext.getConfig().isUpdateCvInXrefsAliasesAnnotations() && getCvTermEnricher() != null){
            for (Object obj : objectToEnrich.getIdentifiers()) {
                Xref xref = (Xref)obj;
                if (xref.getQualifier()!= null) {
                    getCvTermEnricher().enrich(xref.getQualifier());
                }
                getCvTermEnricher().enrich(xref.getDatabase());
            }
        }
    }

    @Override
    protected void processShortName(InteractionEvidence objectToEnrich, InteractionEvidence objectSource) throws EnricherException {
        super.processShortName(objectToEnrich, objectSource);

        if (enricherContext.getConfig().isUpdateInteractionShortLabels()){
            objectToEnrich.setShortName(IntactUtils.generateAutomaticInteractionEvidenceShortlabelFor(objectToEnrich, IntactUtils.MAX_SHORT_LABEL_LEN));
        }
    }

    @Override
    protected void processOtherProperties(InteractionEvidence interactionToEnrich) throws EnricherException {
        super.processOtherProperties(interactionToEnrich);

        processConfidences(interactionToEnrich, null);
        processParameters(interactionToEnrich, null);
    }

    @Override
    public psidev.psi.mi.jami.enricher.ExperimentEnricher getExperimentEnricher() {
        if (super.getExperimentEnricher() == null){
            super.setExperimentEnricher((ExperimentEnricher) ApplicationContextProvider.getBean("intactExperimentEnricher"));
        }
        return super.getExperimentEnricher();
    }

    @Override
    public psidev.psi.mi.jami.enricher.ParticipantEnricher getParticipantEnricher() {
        if (super.getParticipantEnricher() == null){
            super.setParticipantEnricher((psidev.psi.mi.jami.enricher.ParticipantEnricher) ApplicationContextProvider.getBean("intactParticipantEvidenceEnricher"));
        }
        return super.getParticipantEnricher();
    }

    @Override
    public CvTermEnricher<CvTerm> getCvTermEnricher() {
        if (super.getCvTermEnricher() == null){
            super.setCvTermEnricher((CvTermEnricher<CvTerm>) ApplicationContextProvider.getBean("miCvObjectEnricher"));
        }
        return super.getCvTermEnricher();
    }

    @Override
    public InteractionEnricherListener<InteractionEvidence> getInteractionEnricherListener() {
        if (super.getInteractionEnricherListener() == null){
            super.setInteractionEnricherListener(new InteractionEvidenceEnricherLogger());
        }
        return super.getInteractionEnricherListener();
    }
}
