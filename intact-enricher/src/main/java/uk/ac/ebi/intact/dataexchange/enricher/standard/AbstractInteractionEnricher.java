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
import psidev.psi.mi.jami.enricher.CvTermEnricher;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.enricher.impl.full.FullInteractionEnricher;
import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Interaction;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;

/**
 * Abstract class for extending interaction enrichers
 *
 */
public abstract class AbstractInteractionEnricher<T extends Interaction> extends FullInteractionEnricher<T> {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(AbstractInteractionEnricher.class);

    @Autowired
    private EnricherContext enricherContext;

    public AbstractInteractionEnricher() {
    }

    @Override
    protected void processOtherProperties(T interactionToEnrich) throws EnricherException {
        super.processOtherProperties(interactionToEnrich);

        processShortName(interactionToEnrich);
        processXrefs(interactionToEnrich, null);
        processIdentifiers(interactionToEnrich, null);
        processAnnotations(interactionToEnrich, null);
    }

    @Override
    protected void processXrefs(T objectToEnrich, T objectSource) throws EnricherException {
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
    protected void processAnnotations(T objectToEnrich, T objectSource) throws EnricherException {
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
    protected void processRigid(T interactionToEnrich) throws EnricherException {
        // nothing to do
    }

    @Override
    protected void processShortName(T objectToEnrich, T objectSource) throws EnricherException {
        super.processShortName(objectToEnrich, objectSource);

        processShortName(objectToEnrich);
    }

    protected void processShortName(T objectToEnrich) throws EnricherException {
        if (enricherContext.getConfig().isUpdateInteractionShortLabels()){
            objectToEnrich.setShortName(generateAutomaticShortlabel(objectToEnrich));
        }
    }

    protected abstract String generateAutomaticShortlabel(T objectToEnrich);

    @Override
    protected void processIdentifiers(T objectToEnrich, T objectSource) throws EnricherException {
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
    protected void processInteractionType(T interactionToEnrich) throws EnricherException {
        if( enricherContext.getConfig().isUpdateCvTerms()
                && getCvTermEnricher() != null &&
                interactionToEnrich.getInteractionType() != null)
            getCvTermEnricher().enrich(interactionToEnrich.getInteractionType());
    }

    @Override
    public CvTermEnricher<CvTerm> getCvTermEnricher() {
        if (super.getCvTermEnricher() == null){
            super.setCvTermEnricher((CvTermEnricher<CvTerm>) ApplicationContextProvider.getBean("miCvObjectEnricher"));
        }
        return super.getCvTermEnricher();
    }

    protected EnricherContext getEnricherContext() {
        return enricherContext;
    }
}
