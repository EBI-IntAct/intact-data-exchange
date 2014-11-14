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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.jami.enricher.CvTermEnricher;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.enricher.impl.CompositeInteractorEnricher;
import psidev.psi.mi.jami.enricher.impl.full.FullParticipantEnricher;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;

/**
 * Modelled participant enricher
 *
 * @version $Id: ComponentEnricher.java 19461 2013-08-21 10:31:21Z mdumousseau@yahoo.com $
 */
@Controller(value = "intactParticipantEnricher")
@Lazy
public class ParticipantEnricher<P extends Participant, F extends Feature> extends FullParticipantEnricher<P, F>{

    @Autowired
    private EnricherContext enricherContext;

    public ParticipantEnricher() {
    }

    @Override
    protected void processCausalRelationships(P objectToEnrich, P objectSource) throws EnricherException {
        if (objectSource != null){
            super.processCausalRelationships(objectToEnrich, objectSource);
        }

        if (enricherContext.getConfig().isUpdateCvTerms() && getCvTermEnricher() != null){
            for (Object obj : objectToEnrich.getCausalRelationships()) {
                CausalRelationship parameter = (CausalRelationship)obj;
                getCvTermEnricher().enrich(parameter.getRelationType());
            }
        }
    }

    @Override
    protected void processXrefs(P objectToEnrich, P objectSource) throws EnricherException{
        if (objectSource != null){
            super.processXrefs(objectToEnrich, objectSource);
        }

        if (enricherContext.getConfig().isUpdateCvInXrefsAliasesAnnotations() && getCvTermEnricher() != null){
            for (Object obj : objectToEnrich.getXrefs()) {
                Xref xref = (Xref)obj;
                if (xref.getQualifier()!= null) {
                    getCvTermEnricher().enrich(xref.getQualifier());
                }
                getCvTermEnricher().enrich(xref.getDatabase());
            }
        }
    }

    @Override
    protected void processAnnotations(P objectToEnrich, P objectSource) throws EnricherException{
        if (objectSource != null){
            super.processAnnotations(objectToEnrich, objectSource);
        }

        if (enricherContext.getConfig().isUpdateCvTerms() && getCvTermEnricher() != null){
            for (Object obj : objectToEnrich.getAnnotations()) {
                Annotation annotation = (Annotation)obj;
                getCvTermEnricher().enrich(annotation.getTopic());
            }
        }
    }

    @Override
    protected void processAliases(P objectToEnrich, P objectSource) throws EnricherException{
        if (objectSource != null){
            super.processAliases(objectToEnrich, objectSource);
        }
        if (enricherContext.getConfig().isUpdateCvInXrefsAliasesAnnotations() && getCvTermEnricher() != null){
            for (Object obj : objectToEnrich.getAliases()) {
                Alias alias = (Alias)obj;
                if (alias.getType()!= null) {
                    getCvTermEnricher().enrich(alias.getType());
                }
            }
        }
    }

    @Override
    protected void processBiologicalRole(P participantToEnrich) throws EnricherException {
        if (enricherContext.getConfig().isUpdateCvTerms()
                && getCvTermEnricher() != null
                && participantToEnrich.getBiologicalRole() != null)
            getCvTermEnricher().enrich(participantToEnrich.getBiologicalRole());
    }

    @Override
    public void processOtherProperties(P participantToEnrich) throws EnricherException {
        super.processOtherProperties(participantToEnrich);
        // process aliases
        processAliases(participantToEnrich, null);
        // process xrefs
        processXrefs(participantToEnrich, null);
        // process annotations
        processAnnotations(participantToEnrich, null);
        // process causal relationships
        processCausalRelationships(participantToEnrich, null);
    }

    @Override
    public CvTermEnricher<CvTerm> getCvTermEnricher() {
        if (super.getCvTermEnricher() == null){
            super.setCvTermEnricher((CvTermEnricher<CvTerm>) ApplicationContextProvider.getBean("miCvObjectEnricher"));
        }
        return super.getCvTermEnricher();
    }

    @Override
    public CompositeInteractorEnricher getInteractorEnricher() {
        if (super.getInteractorEnricher() == null){
            super.setInteractorEnricher((CompositeInteractorEnricher) ApplicationContextProvider.getBean("intactCompositeInteractorEnricher"));
        }
        return super.getInteractorEnricher();
    }
}
