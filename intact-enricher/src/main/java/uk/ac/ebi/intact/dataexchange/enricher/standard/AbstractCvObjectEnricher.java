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
import psidev.psi.mi.jami.enricher.CvTermEnricher;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.enricher.impl.full.FullCvTermEnricher;
import psidev.psi.mi.jami.enricher.util.EnricherUtils;
import psidev.psi.mi.jami.model.Alias;
import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.AbstractCvObjectFetcher;

/**
 * CvObject enricher.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractCvObjectEnricher<T extends CvTerm> extends FullCvTermEnricher<T> {

    @Autowired
    private EnricherContext enricherContext;

    public AbstractCvObjectEnricher(AbstractCvObjectFetcher intactCvObjectFetcher) {
        super(intactCvObjectFetcher);
    }

    @Override
    protected void processAnnotations(T cvTermToEnrich, T termFetched) throws EnricherException{
        if (termFetched != null){
            super.processAnnotations(cvTermToEnrich, termFetched);
        }

        if (enricherContext.getConfig().isUpdateCvTerms() && getCvEnricher() != null){
            for (Object obj : cvTermToEnrich.getAnnotations()) {
                Annotation annotation = (Annotation)obj;
                getCvEnricher().enrich(annotation.getTopic());
            }
        }
    }

    @Override
    protected void processSynonyms(T objectToEnrich, T objectSource) throws EnricherException{
        if (objectSource != null){
            EnricherUtils.mergeAliases(objectToEnrich, objectToEnrich.getSynonyms(), objectSource.getSynonyms(), true,
                    getCvTermEnricherListener());
        }
        if (enricherContext.getConfig().isUpdateCvInXrefsAliasesAnnotations() && getCvEnricher() != null){
            for (Object obj : objectToEnrich.getSynonyms()) {
                Alias alias = (Alias)obj;
                if (alias.getType()!= null) {
                    getCvEnricher().enrich(alias.getType());
                }
            }
        }
    }

    @Override
    protected void processXrefs(T objectToEnrich, T objectSource) throws EnricherException{
        if (objectSource != null){
            EnricherUtils.mergeXrefs(objectToEnrich, objectToEnrich.getXrefs(), objectSource.getXrefs(), true, false,
                    getCvTermEnricherListener(), getCvTermEnricherListener());
        }

        if (enricherContext.getConfig().isUpdateCvInXrefsAliasesAnnotations() && getCvEnricher() != null){
            for (Object obj : objectToEnrich.getXrefs()) {
                Xref xref = (Xref)obj;
                if (xref.getQualifier()!= null) {
                    getCvEnricher().enrich(xref.getQualifier());
                }
                getCvEnricher().enrich(xref.getDatabase());
            }
        }
    }

    @Override
    protected void processIdentifiers(T objectToEnrich, T objectSource) throws EnricherException{
        if (objectSource != null){
            EnricherUtils.mergeXrefs(objectToEnrich, objectToEnrich.getIdentifiers(), objectSource.getIdentifiers(), true, true,
                    getCvTermEnricherListener(), getCvTermEnricherListener());
        }

        if (enricherContext.getConfig().isUpdateCvInXrefsAliasesAnnotations() && getCvEnricher() != null){
            for (Object obj : objectToEnrich.getIdentifiers()) {
                Xref xref = (Xref)obj;
                if (xref.getQualifier()!= null) {
                    getCvEnricher().enrich(xref.getQualifier());
                }
                getCvEnricher().enrich(xref.getDatabase());
            }
        }
    }

    @Override
    protected void processFullName(T cvTermToEnrich, T cvTermFetched) throws EnricherException{
        if((cvTermFetched.getFullName() != null && !cvTermFetched.getFullName().equals(cvTermToEnrich.getFullName()))
                || (cvTermFetched.getFullName() == null
                && cvTermToEnrich.getFullName() != null)){

            String oldValue = cvTermToEnrich.getFullName();
            cvTermToEnrich.setFullName(cvTermFetched.getFullName());
            if (getCvTermEnricherListener() != null)
                getCvTermEnricherListener().onFullNameUpdate(cvTermToEnrich, oldValue);
        }
    }

    @Override
    protected void processMinimalUpdates(T cvTermToEnrich, T termFetched) throws EnricherException {
        super.processMinimalUpdates(cvTermToEnrich, termFetched);

        // process shortlabel
        processShortName(cvTermToEnrich, termFetched);
    }

    protected void processShortName(CvTerm cvTermToEnrich, CvTerm cvTermFetched) {
        if(cvTermFetched.getShortName() != null
                && ! cvTermFetched.getShortName().equalsIgnoreCase(cvTermToEnrich.getShortName())){

            String oldValue = cvTermToEnrich.getShortName();
            cvTermToEnrich.setShortName(cvTermFetched.getShortName());
            if (getCvTermEnricherListener() != null)
                getCvTermEnricherListener().onShortNameUpdate(cvTermToEnrich, oldValue);
        }
    }

    @Override
    protected void onEnrichedVersionNotFound(T cvTermToEnrich) throws EnricherException{
        processIdentifiers(cvTermToEnrich, null);
        processXrefs(cvTermToEnrich, null);
        processSynonyms(cvTermToEnrich, null);
        processAnnotations(cvTermToEnrich, null);
        super.onEnrichedVersionNotFound(cvTermToEnrich);
    }

    protected abstract CvTermEnricher<CvTerm> getCvEnricher();
}
