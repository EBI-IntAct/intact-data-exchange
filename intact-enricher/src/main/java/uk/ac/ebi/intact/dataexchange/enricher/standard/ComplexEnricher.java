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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.bridges.fetcher.InteractorFetcher;
import psidev.psi.mi.jami.enricher.CvTermEnricher;
import psidev.psi.mi.jami.enricher.OrganismEnricher;
import psidev.psi.mi.jami.enricher.SourceEnricher;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.enricher.listener.ComplexEnricherListener;
import psidev.psi.mi.jami.enricher.listener.InteractorEnricherListener;
import psidev.psi.mi.jami.enricher.listener.ModelledInteractionEnricherListener;
import psidev.psi.mi.jami.enricher.util.EnricherUtils;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.annotation.Resource;

/**
 * Intact enricher for complexes
 *
 */
@Component(value = "intactComplexEnricher")
@Lazy
@Scope( BeanDefinition.SCOPE_PROTOTYPE )
public class ComplexEnricher extends AbstractInteractionEnricher<Complex> implements psidev.psi.mi.jami.enricher.ComplexEnricher{

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(ComplexEnricher.class);

    @Resource(name = "intactCvObjectEnricher")
    private CvTermEnricher<CvTerm> intactCvObjectEnricher;

    private OrganismEnricher intactBioSourceEnricher;
    private SourceEnricher intactSourceEnricher;

    public ComplexEnricher() {
    }

    @Override
    protected String generateAutomaticShortlabel(Complex objectToEnrich) {
        return IntactUtils.generateAutomaticComplexShortlabelFor(objectToEnrich, IntactUtils.MAX_SHORT_LABEL_LEN);
    }

    @Override
    protected void processOtherProperties(Complex interactionToEnrich) throws EnricherException {
        super.processOtherProperties(interactionToEnrich);

        // process evidence type
        processEvidenceType(interactionToEnrich);
        // process organism
        processOrganism(interactionToEnrich);
        // process interactor type
        processInteractorType(interactionToEnrich);
        // process source
        processSource(interactionToEnrich);
        // process confidences
        processConfidences(interactionToEnrich, null);
        // process parameters
        processParameters(interactionToEnrich, null);
    }

    protected void processSource(Complex interactionToEnrich) throws EnricherException{
        if( interactionToEnrich.getSource() != null )
            getSourceEnricher().enrich(interactionToEnrich.getSource());
    }

    protected void processInteractorType(Complex interactionToEnrich) throws EnricherException {

        if( getEnricherContext().getConfig().isUpdateCvTerms()
                && getCvTermEnricher() != null &&
                interactionToEnrich.getInteractorType() != null)
            getCvTermEnricher().enrich(interactionToEnrich.getInteractorType());
    }

    protected void processOrganism(Complex interactionToEnrich) throws EnricherException {
        if( getEnricherContext().getConfig().isUpdateOrganisms()
                && getOrganismEnricher() != null &&
                interactionToEnrich.getOrganism() != null)
            getOrganismEnricher().enrich(interactionToEnrich.getOrganism());
    }

    protected void processEvidenceType(Complex interactionToEnrich) throws EnricherException {
        if( getEnricherContext().getConfig().isUpdateCvTerms()
                && intactCvObjectEnricher != null &&
                interactionToEnrich.getEvidenceType() != null)
            intactCvObjectEnricher.enrich(interactionToEnrich.getEvidenceType());
    }

    @Override
    protected void processOtherProperties(Complex objectToEnrich, Complex objectSource) throws EnricherException {
        super.processOtherProperties(objectToEnrich, objectSource);
        // process fullName
        processFullName(objectToEnrich, objectSource);
        // process evidence type
        processEvidenceType(objectToEnrich, objectSource);
        // process organism
        processOrganism(objectToEnrich, objectSource);
        // process interactor type
        processInteractorType(objectToEnrich, objectSource);
        // process source
        processSource(objectToEnrich, objectSource);
        // process confidences
        processConfidences(objectToEnrich, objectSource);
        // process parameters
        processParameters(objectToEnrich, objectSource);
    }

    protected void processSource(Complex objectToEnrich, Complex objectSource) throws EnricherException{
        if (objectSource.getSource() != null && objectToEnrich.getSource() == null){
            objectToEnrich.setSource(objectSource.getSource());
            if (getInteractionEnricherListener() instanceof ModelledInteractionEnricher){
                ((ModelledInteractionEnricherListener)getInteractionEnricherListener()).onSourceUpdate(objectToEnrich, null);
            }
        }

        processSource(objectToEnrich);
    }

    protected void processInteractorType(Complex objectToEnrich, Complex objectSource) throws EnricherException {
        if (objectSource != null && objectToEnrich.getInteractorType() == null && objectSource.getInteractorType() != null){
            objectToEnrich.setInteractorType(objectSource.getInteractorType());
            if(getInteractionEnricherListener() instanceof ComplexEnricherListener)
                ((ComplexEnricherListener)getInteractionEnricherListener()).onInteractorTypeUpdate(objectToEnrich , null);
        }

        processInteractorType(objectToEnrich);
    }

    protected void processOrganism(Complex objectToEnrich, Complex objectSource) throws EnricherException {
        if (objectSource != null && objectToEnrich.getOrganism() == null && objectSource.getOrganism() != null){
            objectToEnrich.setOrganism(objectSource.getOrganism());
            if(getInteractionEnricherListener() instanceof ComplexEnricherListener)
                ((ComplexEnricherListener)getInteractionEnricherListener()).onOrganismUpdate(objectToEnrich, null);
        }

        processOrganism(objectToEnrich);
    }

    protected void processEvidenceType(Complex objectToEnrich, Complex objectSource) throws EnricherException {
        if (objectSource != null && objectToEnrich.getEvidenceType() == null && objectSource.getEvidenceType() != null){
            objectToEnrich.setEvidenceType(objectSource.getEvidenceType());
            if(getInteractionEnricherListener() instanceof ComplexEnricherListener)
                ((ComplexEnricherListener)getInteractionEnricherListener()).onEvidenceTypeUpdate(objectToEnrich, null);
        }

        processEvidenceType(objectToEnrich);
    }

    protected void processFullName(Complex objectToEnrich, Complex objectSource) {
        if(objectToEnrich.getFullName() == null
                && objectSource.getFullName() != null){
            objectToEnrich.setFullName(objectSource.getFullName());
            if(getInteractionEnricherListener() instanceof ComplexEnricherListener)
                ((ComplexEnricherListener)getInteractionEnricherListener()).onFullNameUpdate(objectToEnrich , null);
        }
    }

    protected void processParameters(Complex objectToEnrich, Complex objectSource) throws EnricherException {
        if (objectSource != null){
            EnricherUtils.mergeParameters(objectToEnrich, objectToEnrich.getModelledParameters(), objectSource.getModelledParameters(), false,
                    (getInteractionEnricherListener() instanceof ModelledInteractionEnricherListener ? (ModelledInteractionEnricherListener)getInteractionEnricherListener():null));        }

        if (getEnricherContext().getConfig().isUpdateCvTerms() && getCvTermEnricher() != null){
            for (Parameter parameter : objectToEnrich.getModelledParameters()) {
                getCvTermEnricher().enrich(parameter.getType());
                if (parameter.getUnit() != null){
                    getCvTermEnricher().enrich(parameter.getUnit());
                }
            }
        }
    }

    protected void processConfidences(Complex objectToEnrich, Complex objectSource) throws EnricherException {
        if (objectSource != null){
            EnricherUtils.mergeConfidences(objectToEnrich, objectToEnrich.getModelledConfidences(), objectSource.getModelledConfidences(), false,
                    (getInteractionEnricherListener() instanceof ModelledInteractionEnricherListener ? (ModelledInteractionEnricherListener) getInteractionEnricherListener() : null));        }

        if (getEnricherContext().getConfig().isUpdateCvTerms() && getCvTermEnricher() != null){
            for (Confidence confidence : objectToEnrich.getModelledConfidences()) {
                getCvTermEnricher().enrich(confidence.getType());
            }
        }
    }

    @Override
    public InteractorFetcher<Complex> getInteractorFetcher() {
        return null;
    }

    @Override
    public InteractorEnricherListener<Complex> getListener() {
        return getInteractionEnricherListener() instanceof ComplexEnricherListener ? (ComplexEnricherListener)getInteractionEnricherListener():null;
    }

    @Override
    public OrganismEnricher getOrganismEnricher() {

        if (this.intactBioSourceEnricher == null){
            this.intactBioSourceEnricher = ApplicationContextProvider.getBean("intactBioSourceEnricher");
        }

        return intactBioSourceEnricher;
    }

    @Override
    public void setListener(InteractorEnricherListener<Complex> listener) {
        if ( listener instanceof ComplexEnricherListener){
            setInteractionEnricherListener((ComplexEnricherListener)listener);
        }
        else{
            setInteractionEnricherListener(null);
        }
    }

    @Override
    public SourceEnricher getSourceEnricher() {
        if (this.intactSourceEnricher == null){
            this.intactSourceEnricher = ApplicationContextProvider.getBean("intactInstitutionEnricher");
        }

        return intactSourceEnricher;
    }

    @Override
    public psidev.psi.mi.jami.enricher.ParticipantEnricher getParticipantEnricher() {
        if (super.getParticipantEnricher() == null){
            super.setParticipantEnricher((ParticipantEnricher) ApplicationContextProvider.getBean("intactModelledParticipantEnricher"));
        }
        return super.getParticipantEnricher();
    }

    public void setOrganismEnricher(OrganismEnricher intactBioSourceEnricher) {
        this.intactBioSourceEnricher = intactBioSourceEnricher;
    }

    public void setSourceEnricher(SourceEnricher intactSourceEnricher) {
        this.intactSourceEnricher = intactSourceEnricher;
    }

    public CvTermEnricher<CvTerm> getIntactCvObjectEnricher() {
        return intactCvObjectEnricher;
    }

    public void setIntactCvObjectEnricher(CvTermEnricher<CvTerm> intactCvObjectEnricher) {
        this.intactCvObjectEnricher = intactCvObjectEnricher;
    }
}
