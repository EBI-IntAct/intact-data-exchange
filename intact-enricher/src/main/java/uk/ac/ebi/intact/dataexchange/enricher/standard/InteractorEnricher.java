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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.enricher.CvTermEnricher;
import psidev.psi.mi.jami.enricher.OrganismEnricher;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.enricher.impl.full.FullInteractorBaseEnricher;
import psidev.psi.mi.jami.enricher.listener.InteractorEnricherListener;
import psidev.psi.mi.jami.enricher.listener.impl.log.InteractorEnricherLogger;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;

/**
 * This class enriches ie adds additional information to the Interactor by utilizing the webservices from UniProt and Chebi.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component(value = "intactInteractorEnricher")
@Lazy
public class InteractorEnricher extends FullInteractorBaseEnricher<Interactor> {

    private static final Log log = LogFactory.getLog(InteractorEnricher.class);

    @Autowired
    private EnricherContext enricherContext;

    public InteractorEnricher() {
        super();
    }

    @Override
    protected void processOrganism(Interactor entityToEnrich) throws EnricherException {
        if (enricherContext.getConfig().isUpdateOrganisms()
                && entityToEnrich.getOrganism() != null
                && getOrganismEnricher() != null){
            getOrganismEnricher().enrich(entityToEnrich.getOrganism());
        }
    }

    @Override
    protected void processInteractorType(Interactor entityToEnrich) throws EnricherException {
        if (enricherContext.getConfig().isUpdateCvTerms()
                && getCvTermEnricher() != null
                && entityToEnrich.getInteractorType() != null)
            getCvTermEnricher().enrich(entityToEnrich.getInteractorType());
    }

    @Override
    protected void processAnnotations(Interactor objectToEnrich, Interactor objectSource) throws EnricherException {
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
    protected void processShortLabel(Interactor objectToEnrich, Interactor fetched) {
        if(!fetched.getShortName().equalsIgnoreCase(objectToEnrich.getShortName())){
            String oldValue = objectToEnrich.getShortName();
            objectToEnrich.setShortName(fetched.getShortName());
            if(getListener() != null)
                getListener().onShortNameUpdate(objectToEnrich , oldValue);
        }

        objectToEnrich.setShortName(replaceLabelInvalidChars(objectToEnrich.getShortName()));
    }

    @Override
    public void processAliases(Interactor objectToEnrich, Interactor objectSource) throws EnricherException {
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
    protected void processIdentifiers(Interactor objectToEnrich, Interactor objectSource) throws EnricherException {
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
    public void processFullName(Interactor bioactiveEntityToEnrich, Interactor fetched) throws EnricherException {
        if((fetched.getFullName() != null && !fetched.getFullName().equalsIgnoreCase(bioactiveEntityToEnrich.getFullName())
                || (fetched.getFullName() == null && bioactiveEntityToEnrich.getFullName() != null))){
            String oldValue = bioactiveEntityToEnrich.getFullName();
            bioactiveEntityToEnrich.setFullName(fetched.getFullName());
            if(getListener() != null)
                getListener().onFullNameUpdate(bioactiveEntityToEnrich , oldValue);
        }
    }

    @Override
    protected void processChecksums(Interactor bioactiveEntityToEnrich, Interactor fetched) throws EnricherException {
        // nothing to do here
    }

    @Override
    protected void processXrefs(Interactor objectToEnrich, Interactor objectSource) throws EnricherException {
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

    protected String replaceLabelInvalidChars(String label) {
        if (label == null){
            return null;
        }
        label = label.replaceAll("-", "").toLowerCase();
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

    @Override
    public InteractorEnricherListener<Interactor> getListener() {
        if (super.getListener() == null){
            super.setListener(new InteractorEnricherLogger<Interactor>());
        }
        return super.getListener();
    }
}
