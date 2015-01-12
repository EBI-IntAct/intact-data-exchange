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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.enricher.CvTermEnricher;
import psidev.psi.mi.jami.enricher.FeatureEnricher;
import psidev.psi.mi.jami.enricher.OrganismEnricher;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.enricher.impl.CompositeInteractorEnricher;
import psidev.psi.mi.jami.enricher.impl.full.FullParticipantEvidenceEnricher;
import psidev.psi.mi.jami.enricher.listener.EntityEnricherListener;
import psidev.psi.mi.jami.enricher.listener.impl.log.ParticipantEvidenceEnricherLogger;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;

import javax.annotation.Resource;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component(value = "intactParticipantEvidenceEnricher")
@Lazy
@Scope( BeanDefinition.SCOPE_PROTOTYPE )
public class ComponentEnricher extends FullParticipantEvidenceEnricher<ParticipantEvidence>{

    @Autowired
    private EnricherContext enricherContext;

    @Resource(name = "intactParticipantEnricher")
    private psidev.psi.mi.jami.enricher.ParticipantEnricher intactParticipantEnricher;

    public ComponentEnricher() {
    }

    @Override
    public void enrich(ParticipantEvidence participantToEnrich) throws EnricherException {
        intactParticipantEnricher.enrich(participantToEnrich);
        // enrich other properties
        super.enrich(participantToEnrich);
    }

    @Override
    public void enrich(ParticipantEvidence objectToEnrich, ParticipantEvidence objectSource) throws EnricherException {
        // enrich full feature
        intactParticipantEnricher.enrich(objectToEnrich, objectSource);
        // enrich other properties
        super.enrich(objectToEnrich, objectSource);
    }

    @Override
    public void processOtherProperties(ParticipantEvidence participantEvidenceToEnrich) throws EnricherException {
        super.processOtherProperties(participantEvidenceToEnrich);
        processConfidences(participantEvidenceToEnrich, null);
        processParameters(participantEvidenceToEnrich, null);
    }

    @Override
    protected void processExperimentalPreparations(ParticipantEvidence participantEvidenceToEnrich) throws EnricherException {
        if (enricherContext.getConfig().isUpdateCvTerms()
                && !participantEvidenceToEnrich.getExperimentalPreparations().isEmpty()
                && getCvTermEnricher() != null){
            getCvTermEnricher().enrich(participantEvidenceToEnrich.getExperimentalPreparations());
        }
    }

    @Override
    protected void processParameters(ParticipantEvidence participantEvidenceToEnrich, ParticipantEvidence objectSource) throws EnricherException {
        if (objectSource != null){
            super.processParameters(participantEvidenceToEnrich, objectSource);
        }

        if (enricherContext.getConfig().isUpdateCvTerms() && getCvTermEnricher() != null){
            for (Parameter parameter : participantEvidenceToEnrich.getParameters()) {
                getCvTermEnricher().enrich(parameter.getType());
                if (parameter.getUnit() != null){
                    getCvTermEnricher().enrich(parameter.getUnit());
                }
            }
        }
    }

    @Override
    protected void processConfidences(ParticipantEvidence participantEvidenceToEnrich, ParticipantEvidence objectSource) throws EnricherException {
        if (objectSource != null){
            super.processConfidences(participantEvidenceToEnrich, objectSource);
        }

        if (enricherContext.getConfig().isUpdateCvTerms() && getCvTermEnricher() != null){
            for (Confidence confidence : participantEvidenceToEnrich.getConfidences()) {
                getCvTermEnricher().enrich(confidence.getType());
            }
        }
    }

    @Override
    protected void processExpressedInOrganism(ParticipantEvidence participantEvidenceToEnrich) throws EnricherException {
        if (enricherContext.getConfig().isUpdateOrganisms()
                && participantEvidenceToEnrich.getExpressedInOrganism() != null
                && getOrganismEnricher() != null){
            getOrganismEnricher().enrich(participantEvidenceToEnrich.getExpressedInOrganism());
        }
    }

    @Override
    protected void processIdentificationMethods(ParticipantEvidence participantEvidenceToEnrich) throws EnricherException {
        if (enricherContext.getConfig().isUpdateCvTerms()
                && !participantEvidenceToEnrich.getIdentificationMethods().isEmpty()){
            getCvTermEnricher().enrich(participantEvidenceToEnrich.getIdentificationMethods());
        }
    }

    @Override
    protected void processExperimentalRole(ParticipantEvidence participantEvidenceToEnrich) throws EnricherException {
        if (enricherContext.getConfig().isUpdateCvTerms()
                && participantEvidenceToEnrich.getExperimentalRole() != null){
            getCvTermEnricher().enrich(participantEvidenceToEnrich.getExperimentalRole());
        }
    }

    @Override
    public void processInteractor(ParticipantEvidence objectToEnrich, ParticipantEvidence objectSource) throws EnricherException {
        // nothing to do
    }

    @Override
    public void processBiologicalRole(ParticipantEvidence objectToEnrich, ParticipantEvidence objectSource) throws EnricherException {
        // nothing to do
    }

    @Override
    protected void processAliases(ParticipantEvidence objectToEnrich, ParticipantEvidence objectSource) {
        // nothing to do
    }

    @Override
    protected void processBiologicalRole(ParticipantEvidence participantToEnrich) throws EnricherException {
        // nothing to do
    }

    @Override
    public void processFeatures(ParticipantEvidence objectToEnrich, ParticipantEvidence objectSource) throws EnricherException {
        // nothing to do
    }

    @Override
    protected void processFeatures(ParticipantEvidence participantToEnrich) throws EnricherException {
        // nothing to do
    }

    @Override
    protected void processInteractor(ParticipantEvidence participantToEnrich) throws EnricherException {
        // nothing to do
    }

    @Override
    public OrganismEnricher getOrganismEnricher() {
        if (super.getOrganismEnricher() == null){
            super.setOrganismEnricher((OrganismEnricher) ApplicationContextProvider.getBean("intactBioSourceEnricher"));
        }
        return super.getOrganismEnricher();
    }

    @Override
    public psidev.psi.mi.jami.enricher.FeatureEnricher<FeatureEvidence> getFeatureEnricher() {
        if (super.getFeatureEnricher() == null){
            super.setFeatureEnricher((FeatureEvidenceEnricher) ApplicationContextProvider.getBean("intactFeatureEvidenceEnricher"));
            intactParticipantEnricher.setFeatureEnricher(super.getFeatureEnricher());
        }
        return super.getFeatureEnricher();
    }

    @Override
    public CvTermEnricher<CvTerm> getCvTermEnricher() {
        if (super.getCvTermEnricher() == null){
            super.setCvTermEnricher((CvTermEnricher<CvTerm>) ApplicationContextProvider.getBean("miCvObjectEnricher"));
            intactParticipantEnricher.setCvTermEnricher(super.getCvTermEnricher());
        }
        return super.getCvTermEnricher();
    }

    @Override
    public EntityEnricherListener getParticipantEnricherListener() {
        if (super.getParticipantEnricherListener() == null){
            super.setParticipantEnricherListener(new ParticipantEvidenceEnricherLogger());
            intactParticipantEnricher.setParticipantEnricherListener(super.getParticipantEnricherListener());
        }
        return super.getParticipantEnricherListener();
    }

    @Override
    public void setParticipantEnricherListener(EntityEnricherListener listener) {
        super.setParticipantEnricherListener(listener);
        intactParticipantEnricher.setParticipantEnricherListener(listener);
    }

    @Override
    public void setFeatureEnricher(FeatureEnricher<FeatureEvidence> featureEnricher) {
        super.setFeatureEnricher(featureEnricher);
        intactParticipantEnricher.setFeatureEnricher(featureEnricher);
    }

    @Override
    public void setInteractorEnricher(CompositeInteractorEnricher interactorEnricher) {
        super.setInteractorEnricher(interactorEnricher);
        intactParticipantEnricher.setInteractorEnricher(interactorEnricher);
    }

    @Override
    public void setCvTermEnricher(CvTermEnricher<CvTerm> cvTermEnricher) {
        super.setCvTermEnricher(cvTermEnricher);
        intactParticipantEnricher.setCvTermEnricher(cvTermEnricher);
    }

    @Override
    public CompositeInteractorEnricher getInteractorEnricher() {
        return super.getInteractorEnricher();
    }
}
