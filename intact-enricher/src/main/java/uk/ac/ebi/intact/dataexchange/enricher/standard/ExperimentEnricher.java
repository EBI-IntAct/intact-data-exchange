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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.enricher.CvTermEnricher;
import psidev.psi.mi.jami.enricher.OrganismEnricher;
import psidev.psi.mi.jami.enricher.PublicationEnricher;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.enricher.impl.full.FullExperimentEnricher;
import psidev.psi.mi.jami.enricher.listener.ExperimentEnricherListener;
import psidev.psi.mi.jami.enricher.listener.impl.log.ExperimentEnricherLogger;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import psidev.psi.mi.jami.utils.XrefUtils;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component(value = "intactExperimentEnricher")
@Lazy
public class ExperimentEnricher extends FullExperimentEnricher {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(ExperimentEnricher.class);

    @Autowired
    private EnricherContext enricherContext;

    public ExperimentEnricher() {
        super();
    }

    @Override
    protected void processOtherProperties(Experiment experimentToEnrich) throws EnricherException {
        super.processOtherProperties(experimentToEnrich);

        processXrefs(experimentToEnrich, null);
        processAnnotations(experimentToEnrich, null);
        processVariableParameters(experimentToEnrich, null);
    }

    @Override
    protected void processOtherProperties(Experiment experimentToEnrich, Experiment objectSource) throws EnricherException {
        super.processOtherProperties(experimentToEnrich, objectSource);
    }

    @Override
    protected void processXrefs(Experiment objectToEnrich, Experiment objectSource) throws EnricherException {
        if (objectSource != null){
            super.processXrefs(objectToEnrich, objectSource);
        }

        // for backward compatibility, check publication primary ref
        if (objectToEnrich.getPublication() != null){
            Publication publication = objectToEnrich.getPublication();

            Collection<Xref> primaryRefs = XrefUtils.collectAllXrefsHavingQualifier(publication.getIdentifiers(), Xref.PRIMARY_MI, Xref.PRIMARY);

            for (Xref primary : primaryRefs){
                Collection<Xref> primaryExp = XrefUtils.collectAllXrefsHavingDatabaseAndQualifier(objectToEnrich.getXrefs(),
                        primary.getDatabase().getMIIdentifier(),
                        primary.getDatabase().getShortName(),
                        Xref.PRIMARY_MI, Xref.PRIMARY);
                objectToEnrich.getXrefs().removeAll(primaryExp);

                objectToEnrich.getXrefs().add(primary);
            }
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
    protected void processAnnotations(Experiment objectToEnrich, Experiment objectSource) throws EnricherException {
        if (objectSource != null){
            super.processAnnotations(objectToEnrich, objectSource);
        }

        // for backward compatibility, check publication annotations
        if (objectToEnrich.getPublication() != null){
            Publication publication = objectToEnrich.getPublication();
            if (publication.getPublicationDate() != null) {
                Annotation pubYear = AnnotationUtils.collectFirstAnnotationWithTopic(objectToEnrich.getAnnotations(),
                        Annotation.PUBLICATION_YEAR_MI, Annotation.PUBLICATION_YEAR);
                if (pubYear != null){
                    pubYear.setValue(IntactUtils.YEAR_FORMAT.format(publication.getPublicationDate()));
                }
                else{
                    pubYear = AnnotationUtils.createAnnotation(Annotation.PUBLICATION_YEAR, Annotation.PUBLICATION_YEAR_MI,
                            IntactUtils.YEAR_FORMAT.format(publication.getPublicationDate()));
                    objectToEnrich.getAnnotations().add(pubYear);
                }
            }

            if (!publication.getAuthors().isEmpty()) {
                Annotation pubAuthors = AnnotationUtils.collectFirstAnnotationWithTopic(objectToEnrich.getAnnotations(),
                        Annotation.AUTHOR_MI, Annotation.AUTHOR);
                if (pubAuthors != null){
                    pubAuthors.setValue(StringUtils.join(publication.getAuthors(), ", "));
                }
                else{
                    pubAuthors = AnnotationUtils.createAnnotation(Annotation.AUTHOR, Annotation.AUTHOR_MI,
                            StringUtils.join(publication.getAuthors(), ", "));
                    objectToEnrich.getAnnotations().add(pubAuthors);
                }
            }

            if (publication.getJournal() != null) {
                Annotation pubJournal = AnnotationUtils.collectFirstAnnotationWithTopic(objectToEnrich.getAnnotations(),
                        Annotation.PUBLICATION_JOURNAL_MI, Annotation.PUBLICATION_JOURNAL);
                if (pubJournal != null){
                    pubJournal.setValue(publication.getJournal());
                }
                else{
                    pubJournal = AnnotationUtils.createAnnotation(Annotation.PUBLICATION_JOURNAL, Annotation.PUBLICATION_JOURNAL_MI,
                            publication.getJournal());
                    objectToEnrich.getAnnotations().add(pubJournal);
                }
            }
        }

        if (enricherContext.getConfig().isUpdateCvTerms() && getCvTermEnricher() != null){
            for (Object obj : objectToEnrich.getAnnotations()) {
                Annotation annotation = (Annotation)obj;
                getCvTermEnricher().enrich(annotation.getTopic());
            }
        }
    }

    @Override
    protected void processVariableParameters(Experiment experimentToEnrich, Experiment objectSource) throws EnricherException {
        if (objectSource != null){
            super.processVariableParameters(experimentToEnrich, objectSource);
        }

        if (enricherContext.getConfig().isUpdateCvTerms() && getCvTermEnricher() != null){
            for (VariableParameter parameter : experimentToEnrich.getVariableParameters()) {
                if (parameter.getUnit() != null){
                    getCvTermEnricher().enrich(parameter.getUnit());
                }
            }
        }
    }

    @Override
    protected void processOrganism(Experiment experimentToEnrich) throws EnricherException {
        if( enricherContext.getConfig().isUpdateOrganisms()
                && getOrganismEnricher() != null
                && experimentToEnrich.getHostOrganism() != null )
            getOrganismEnricher().enrich(experimentToEnrich.getHostOrganism());
    }

    @Override
    protected void processInteractionDetectionMethod(Experiment experimentToEnrich) throws EnricherException {
        if( enricherContext.getConfig().isUpdateCvTerms()
                && getCvTermEnricher() != null
                && experimentToEnrich.getInteractionDetectionMethod() != null )
            getCvTermEnricher().enrich(experimentToEnrich.getInteractionDetectionMethod());
    }

    @Override
    protected void processPublication(Experiment experimentToEnrich) throws EnricherException {
        if( enricherContext.getConfig().isUpdateExperiments()
                && getPublicationEnricher() != null
                && experimentToEnrich.getPublication() != null )
            getPublicationEnricher().enrich(experimentToEnrich.getPublication());
    }

    @Override
    public CvTermEnricher getCvTermEnricher() {
        if (super.getCvTermEnricher() == null){
            super.setCvTermEnricher((CvTermEnricher<CvTerm>) ApplicationContextProvider.getBean("miCvObjectEnricher"));
        }
        return super.getCvTermEnricher();
    }

    @Override
    public PublicationEnricher getPublicationEnricher() {
        if (super.getPublicationEnricher() == null){
            super.setPublicationEnricher((PublicationEnricher) ApplicationContextProvider.getBean("intactPublicationEnricher"));
        }
        return super.getPublicationEnricher();
    }

    @Override
    public OrganismEnricher getOrganismEnricher() {
        if (super.getOrganismEnricher() == null){
            super.setOrganismEnricher((OrganismEnricher) ApplicationContextProvider.getBean("intactBioSourceEnricher"));
        }
        return super.getOrganismEnricher();
    }

    @Override
    public ExperimentEnricherListener getExperimentEnricherListener() {
        if (super.getExperimentEnricherListener() == null){
            super.setExperimentEnricherListener(new ExperimentEnricherLogger());
        }
        return super.getExperimentEnricherListener();
    }
}
