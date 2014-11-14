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
import psidev.psi.mi.jami.enricher.CvTermEnricher;
import psidev.psi.mi.jami.enricher.OrganismEnricher;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.enricher.impl.full.FullBioactiveEntityEnricher;
import psidev.psi.mi.jami.enricher.util.EnricherUtils;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.BioactiveEntityFetcher;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;

/**
 * This class enriches ie adds additional information to the Interactor by utilizing the webservices from UniProt and Chebi.
 *
 */
@Component(value = "intactBioactiveEntityEnricher")
@Lazy
public class BioActiveEntityEnricher extends FullBioactiveEntityEnricher {

    private static final Log log = LogFactory.getLog(BioActiveEntityEnricher.class);

    @Autowired
    private EnricherContext enricherContext;

    @Autowired
    public BioActiveEntityEnricher(@Qualifier("intactBioactiveEntityFetcher") BioactiveEntityFetcher intactBioactiveEntityFetcher) {
        super(intactBioactiveEntityFetcher);
    }

    @Override
    protected void onEnrichedVersionNotFound(BioactiveEntity objectToEnrich) throws EnricherException {

        objectToEnrich.setShortName(replaceLabelInvalidChars(objectToEnrich.getShortName()));

        processInteractorType(objectToEnrich);
        processOrganism(objectToEnrich);
        processXrefs(objectToEnrich, null);
        processAliases(objectToEnrich, null);
        processIdentifiers(objectToEnrich, null);
        processAnnotations(objectToEnrich, null);

        super.onEnrichedVersionNotFound(objectToEnrich);
    }

    @Override
    protected void processOrganism(BioactiveEntity entityToEnrich) throws EnricherException {
        if (enricherContext.getConfig().isUpdateOrganisms()
                && entityToEnrich.getOrganism() != null
                && getOrganismEnricher() != null){
            getOrganismEnricher().enrich(entityToEnrich.getOrganism());
        }
    }

    @Override
    protected void processInteractorType(BioactiveEntity entityToEnrich) throws EnricherException {
        if (enricherContext.getConfig().isUpdateCvTerms()
                && getCvTermEnricher() != null
                && entityToEnrich.getInteractorType() != null)
            getCvTermEnricher().enrich(entityToEnrich.getInteractorType());
    }

    @Override
    protected void processAnnotations(BioactiveEntity objectToEnrich, BioactiveEntity objectSource) throws EnricherException {
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
    protected void processShortLabel(BioactiveEntity objectToEnrich, BioactiveEntity fetched) {
        if(!fetched.getShortName().equalsIgnoreCase(objectToEnrich.getShortName())){
            String oldValue = objectToEnrich.getShortName();
            objectToEnrich.setShortName(fetched.getShortName());
            if(getListener() != null)
                getListener().onShortNameUpdate(objectToEnrich , oldValue);
        }

        objectToEnrich.setShortName(replaceLabelInvalidChars(objectToEnrich.getShortName()));
    }

    @Override
    public void processAliases(BioactiveEntity objectToEnrich, BioactiveEntity objectSource) throws EnricherException {
        if (objectSource != null){
            EnricherUtils.mergeAliases(objectToEnrich, objectToEnrich.getAliases(), objectSource.getAliases(), true,
                    getListener());
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
    protected void processIdentifiers(BioactiveEntity objectToEnrich, BioactiveEntity objectSource) throws EnricherException {
        if (objectSource != null){
            EnricherUtils.mergeXrefs(objectToEnrich, objectToEnrich.getIdentifiers(), objectSource.getIdentifiers(), true, true,
                    getListener(), getListener());
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
    public void processFullName(BioactiveEntity bioactiveEntityToEnrich, BioactiveEntity fetched) throws EnricherException {
        if((fetched.getFullName() != null && !fetched.getFullName().equalsIgnoreCase(bioactiveEntityToEnrich.getFullName())
                || (fetched.getFullName() == null && bioactiveEntityToEnrich.getFullName() != null))){
            String oldValue = bioactiveEntityToEnrich.getFullName();
            bioactiveEntityToEnrich.setFullName(fetched.getFullName());
            if(getListener() != null)
                getListener().onFullNameUpdate(bioactiveEntityToEnrich , oldValue);
        }
    }

    @Override
    protected void processChecksums(BioactiveEntity objectToEnrich, BioactiveEntity objectSource) throws EnricherException {
        if (objectSource != null){
            EnricherUtils.mergeChecksums(objectToEnrich, objectToEnrich.getChecksums(), objectSource.getChecksums(), true,
                    getListener());
        }

        if (enricherContext.getConfig().isUpdateCvInXrefsAliasesAnnotations() && getCvTermEnricher() != null){
            for (Object obj : objectToEnrich.getIdentifiers()) {
                Checksum check = (Checksum)obj;
                getCvTermEnricher().enrich(check.getMethod());
            }
        }
    }

    @Override
    protected void processXrefs(BioactiveEntity objectToEnrich, BioactiveEntity objectSource) throws EnricherException {
        if (objectSource != null){
            EnricherUtils.mergeXrefs(objectToEnrich, objectToEnrich.getXrefs(), objectSource.getXrefs(), true, false,
                    getListener(), getListener());
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

    protected String replaceLabelInvalidChars(String label) {
        if (label == null){
            return null;
        }
        label = label.replaceAll("-", "");
        return label;
    }

    @Override
    public OrganismEnricher getOrganismEnricher() {
        if (super.getOrganismEnricher() == null){
            super.setOrganismEnricher((OrganismEnricher) ApplicationContextProvider.getBean("intactBioSourceEnricher"));
        }
        return super.getOrganismEnricher();
    }

    @Override
    public CvTermEnricher<CvTerm> getCvTermEnricher() {
        if (super.getCvTermEnricher() == null){
            super.setCvTermEnricher((CvTermEnricher<CvTerm>) ApplicationContextProvider.getBean("miCvObjectEnricher"));
        }
        return super.getCvTermEnricher();
    }
}
