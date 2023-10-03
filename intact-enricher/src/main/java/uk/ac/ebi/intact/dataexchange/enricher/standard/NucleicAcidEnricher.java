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
import psidev.psi.mi.jami.enricher.CvTermEnricher;
import psidev.psi.mi.jami.enricher.OrganismEnricher;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.enricher.impl.full.FullNucleicAcidEnricher;
import psidev.psi.mi.jami.enricher.listener.NucleicAcidEnricherListener;
import psidev.psi.mi.jami.enricher.util.EnricherUtils;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.EnsemblNucleicAcidFetcher;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.NucleicAcidFetcher;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;

/**
 *
 */
@Component(value = "intactNucleicAcidEnricher")
@Lazy
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NucleicAcidEnricher extends FullNucleicAcidEnricher {

    private static final Log log = LogFactory.getLog(NucleicAcidEnricher.class);

    @Autowired
    private EnricherContext enricherContext;

    @Autowired
    public NucleicAcidEnricher(@Qualifier("intactNucleicAcidFetcher") NucleicAcidFetcher fetcher,
                               @Qualifier("intactEnsemblNucleicAcidFetcher") EnsemblNucleicAcidFetcher ensemblNucleicAcidFetcherFetcher) {
        super(fetcher);
        this.setEnsemblFetcher(ensemblNucleicAcidFetcherFetcher);
    }

    @Override
    protected void onEnrichedVersionNotFound(NucleicAcid objectToEnrich) throws EnricherException {

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
    protected void processOrganism(NucleicAcid entityToEnrich) throws EnricherException {
        if (enricherContext.getConfig().isUpdateOrganisms()
                && entityToEnrich.getOrganism() != null
                && getOrganismEnricher() != null) {
            getOrganismEnricher().enrich(entityToEnrich.getOrganism());
        }
    }

    @Override
    protected void processInteractorType(NucleicAcid entityToEnrich) throws EnricherException {
        if (enricherContext.getConfig().isUpdateCvTerms()
                && getCvTermEnricher() != null
                && entityToEnrich.getInteractorType() != null)
            getCvTermEnricher().enrich(entityToEnrich.getInteractorType());
    }

    @Override
    protected void processAnnotations(NucleicAcid objectToEnrich, NucleicAcid objectSource) throws EnricherException {
        if (objectSource != null) {
            super.processAnnotations(objectToEnrich, objectSource);
        }

        if (enricherContext.getConfig().isUpdateCvTerms() && getCvTermEnricher() != null) {
            for (Object obj : objectToEnrich.getAnnotations()) {
                Annotation annotation = (Annotation) obj;
                getCvTermEnricher().enrich(annotation.getTopic());
            }
        }
    }

    @Override
    protected void processShortLabel(NucleicAcid objectToEnrich, NucleicAcid fetched) {
        if (!fetched.getShortName().equalsIgnoreCase(objectToEnrich.getShortName())) {
            String oldValue = objectToEnrich.getShortName();
            objectToEnrich.setShortName(fetched.getShortName());
            if (getListener() != null)
                getListener().onShortNameUpdate(objectToEnrich, oldValue);
        }

        objectToEnrich.setShortName(replaceLabelInvalidChars(objectToEnrich.getShortName()));
    }

    @Override
    public void processAliases(NucleicAcid objectToEnrich, NucleicAcid objectSource) throws EnricherException {
        if (objectSource != null) {
            EnricherUtils.mergeAliases(objectToEnrich, objectToEnrich.getAliases(), objectSource.getAliases(), true,
                    getListener());
        }
        if (enricherContext.getConfig().isUpdateCvInXrefsAliasesAnnotations() && getCvTermEnricher() != null) {
            for (Object obj : objectToEnrich.getAliases()) {
                Alias alias = (Alias) obj;
                if (alias.getType() != null) {
                    getCvTermEnricher().enrich(alias.getType());
                }
            }
        }
    }

    @Override
    protected void processIdentifiers(NucleicAcid objectToEnrich, NucleicAcid objectSource) throws EnricherException {
        if (objectSource != null) {
            EnricherUtils.mergeXrefs(objectToEnrich, objectToEnrich.getIdentifiers(), objectSource.getIdentifiers(), true, true,
                    getListener(), getListener());
        }

        if (enricherContext.getConfig().isUpdateCvInXrefsAliasesAnnotations() && getCvTermEnricher() != null) {
            for (Object obj : objectToEnrich.getIdentifiers()) {
                Xref xref = (Xref) obj;
                if (xref.getQualifier() != null) {
                    getCvTermEnricher().enrich(xref.getQualifier());
                }
                getCvTermEnricher().enrich(xref.getDatabase());
            }
        }
    }

    @Override
    public void processFullName(NucleicAcid bioactiveEntityToEnrich, NucleicAcid fetched) throws EnricherException {
        if ((fetched.getFullName() != null && !fetched.getFullName().equalsIgnoreCase(bioactiveEntityToEnrich.getFullName())
                || (fetched.getFullName() == null && bioactiveEntityToEnrich.getFullName() != null))) {
            String oldValue = bioactiveEntityToEnrich.getFullName();
            bioactiveEntityToEnrich.setFullName(fetched.getFullName());
            if (getListener() != null)
                getListener().onFullNameUpdate(bioactiveEntityToEnrich, oldValue);
        }
    }

    @Override
    protected void processOtherProperties(NucleicAcid toEnrich, NucleicAcid fetched) {
        // sequence
        if ((fetched.getSequence() != null && !fetched.getSequence().equalsIgnoreCase(toEnrich.getSequence())
                || (fetched.getSequence() == null && toEnrich.getSequence() != null))) {
            String oldSeq = toEnrich.getSequence();
            toEnrich.setSequence(fetched.getSequence());
            if (getListener() instanceof NucleicAcidEnricherListener) {
                ((NucleicAcidEnricherListener) getListener()).onSequenceUpdate(toEnrich, oldSeq);
            }
        }
    }


    @Override
    protected void processChecksums(NucleicAcid bioactiveEntityToEnrich, NucleicAcid fetched) throws EnricherException {
        // nothing to do here
    }

    @Override
    protected void processXrefs(NucleicAcid objectToEnrich, NucleicAcid objectSource) throws EnricherException {
        if (objectSource != null) {
            EnricherUtils.mergeXrefs(objectToEnrich, objectToEnrich.getXrefs(), objectSource.getXrefs(), true, false,
                    getListener(), getListener());
        }

        if (enricherContext.getConfig().isUpdateCvInXrefsAliasesAnnotations() && getCvTermEnricher() != null) {
            for (Object obj : objectToEnrich.getXrefs()) {
                Xref xref = (Xref) obj;
                if (xref.getQualifier() != null) {
                    getCvTermEnricher().enrich(xref.getQualifier());
                }
                getCvTermEnricher().enrich(xref.getDatabase());
            }
        }
    }

    protected String replaceLabelInvalidChars(String label) {
        if (label == null) {
            return null;
        }
        label = label.replaceAll("-", "").toLowerCase();
        return label;
    }

    @Override
    public OrganismEnricher getOrganismEnricher() {
        if (super.getOrganismEnricher() == null) {
            super.setOrganismEnricher((OrganismEnricher) ApplicationContextProvider.getBean("intactBioSourceEnricher"));
        }
        return super.getOrganismEnricher();
    }

    @Override
    public CvTermEnricher<CvTerm> getCvTermEnricher() {
        if (super.getCvTermEnricher() == null) {
            super.setCvTermEnricher((CvTermEnricher<CvTerm>) ApplicationContextProvider.getBean("miCvObjectEnricher"));
        }
        return super.getCvTermEnricher();
    }
}
