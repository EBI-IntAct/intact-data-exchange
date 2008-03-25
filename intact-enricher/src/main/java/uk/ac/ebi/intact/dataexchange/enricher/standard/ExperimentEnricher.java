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

import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.ExperimentUtils;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.CvObjectBuilder;
import uk.ac.ebi.intact.util.cdb.ExperimentAutoFill;
import uk.ac.ebi.intact.util.cdb.InvalidPubmedException;
import uk.ac.ebi.intact.dataexchange.cvutils.model.IntactOntology;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvTerm;
import uk.ac.ebi.intact.dataexchange.cvutils.CvUtils;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.CvObjectFetcher;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.ExperimentFetcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentEnricher extends AnnotatedObjectEnricher<Experiment> {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(ExperimentEnricher.class);

     private static ThreadLocal<ExperimentEnricher> instance = new ThreadLocal<ExperimentEnricher>() {
        @Override
        protected ExperimentEnricher initialValue() {
            return new ExperimentEnricher();
        }
    };

    public static ExperimentEnricher getInstance() {
        return instance.get();
    }

    protected ExperimentEnricher() {
    }

    public void enrich(Experiment objectToEnrich) {
        
        BioSourceEnricher bioSourceEnricher = BioSourceEnricher.getInstance();
        bioSourceEnricher.enrich(objectToEnrich.getBioSource());

        CvObjectEnricher cvObjectEnricher = CvObjectEnricher.getInstance();

        if (objectToEnrich.getCvInteraction() != null) {
            cvObjectEnricher.enrich(objectToEnrich.getCvInteraction());
        }

        // fix wrong xref qualifiers in pubmed xrefs if necessary
        fixPubmedXrefIfNecessary(objectToEnrich);

        
        // populate the experiment using the pubmed id
        if (EnricherContext.getInstance().getConfig().isUpdateExperiments()) {
            String pubmedId = ExperimentUtils.getPubmedId(objectToEnrich);
            try {
                Long.parseLong(pubmedId);

                populateExperiment(objectToEnrich, pubmedId);
            } catch (InvalidPubmedException pe) {
               log.error("Experiment with invalid pubmed cannot be enriched from citeXplore: "+pubmedId);
            } catch (NumberFormatException nfe) {
                log.error("Experiment with invalid pubmed (it is not a number) cannot be enriched from citeXplore: "+pubmedId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // add the participant detection method to the experiment if missing
        if (objectToEnrich.getCvIdentification() == null) {
            String detMethodMi = calculateParticipantDetMethod(objectToEnrich);

            if (detMethodMi != null) {
                CvTerm detMethodTerm = CvObjectFetcher.getInstance().fetchByTermId(detMethodMi);
                CvIdentification detMethod = CvObjectUtils.createCvObject(objectToEnrich.getOwner(), CvIdentification.class, detMethodMi, detMethodTerm.getShortName());
                objectToEnrich.setCvIdentification(detMethod);
            }
        }

        if (objectToEnrich.getCvIdentification() != null) {
            cvObjectEnricher.enrich(objectToEnrich.getCvIdentification());
        }



        super.enrich(objectToEnrich);
    }

    public void close() {
    }

    protected void populateExperiment(Experiment experiment, String pubmedId) throws Exception {
        ExperimentAutoFill autoFill = ExperimentFetcher.getInstance().fetchByPubmedId(pubmedId);

        if (autoFill != null) {
            experiment.setShortLabel(AnnotatedObjectUtils.prepareShortLabel(autoFill.getShortlabel(false)));
            experiment.setFullName(autoFill.getFullname());

            if (!experimentAnnotationsContainTopic(experiment, CvTopic.AUTHOR_LIST_MI_REF)) {
                CvTopic publicationYearTopic = CvObjectUtils.createCvObject(experiment.getOwner(), CvTopic.class, CvTopic.PUBLICATION_YEAR_MI_REF, CvTopic.PUBLICATION_YEAR);
                experiment.addAnnotation(new Annotation(experiment.getOwner(), publicationYearTopic, String.valueOf(autoFill.getYear())));
            }

            if (autoFill.getAuthorList() != null && !experimentAnnotationsContainTopic(experiment, CvTopic.AUTHOR_LIST_MI_REF)) {
                CvTopic authorListTopic = CvObjectUtils.createCvObject(experiment.getOwner(), CvTopic.class, CvTopic.AUTHOR_LIST_MI_REF, CvTopic.AUTHOR_LIST);
                experiment.addAnnotation(new Annotation(experiment.getOwner(), authorListTopic, autoFill.getAuthorList()));
            }

            if (autoFill.getAuthorEmail() != null && !experimentAnnotationsContainTopic(experiment, CvTopic.CONTACT_EMAIL_MI_REF)) {
                CvTopic authorEmail = CvObjectUtils.createCvObject(experiment.getOwner(), CvTopic.class, CvTopic.CONTACT_EMAIL_MI_REF, CvTopic.CONTACT_EMAIL);
                experiment.addAnnotation(new Annotation(experiment.getOwner(), authorEmail, autoFill.getAuthorEmail()));
            }
        } else {
            if (log.isWarnEnabled()) log.warn("Couldn't use ExperimentAutoFill for experiment with pubmed: "+pubmedId);
        }
    }

    private boolean experimentAnnotationsContainTopic(Experiment experiment, String miIdentifier) {
        for (Annotation annot : experiment.getAnnotations()) {
            if (miIdentifier.equals(annot.getCvTopic().getMiIdentifier())) {
                return true;
            }
        }
        return false;
    }

    private String calculateParticipantDetMethod(Experiment experiment) {
        Set<String> detMethodMis = new HashSet<String>();

        for (Interaction interaction : experiment.getInteractions()) {
            for (Component component : interaction.getComponents()) {
                for (CvIdentification partDetMethod : component.getParticipantDetectionMethods()) {
                    if (partDetMethod.getMiIdentifier() != null) {
                        detMethodMis.add(partDetMethod.getMiIdentifier());
                    }
                }
            }
        }

        if (detMethodMis.size() == 1) {
            return detMethodMis.iterator().next();
        } else if (detMethodMis.size() > 1) {
            IntactOntology ontology = EnricherContext.getInstance().getIntactOntology();

            return CvUtils.findLowestCommonAncestor(ontology, detMethodMis.toArray(new String[detMethodMis.size()]));
        }

        log.error("No participant detection methods found for components in experiment");

        return null;
    }

    protected void fixPubmedXrefIfNecessary(Experiment experiment) {
        for (ExperimentXref xref : experiment.getXrefs()) {
            if (CvDatabase.PUBMED_MI_REF.equals(xref.getCvDatabase().getMiIdentifier())) {
                if (!CvXrefQualifier.PRIMARY_REFERENCE_MI_REF.equals(xref.getCvXrefQualifier().getMiIdentifier())) {
                    log.warn("Fixing wrong xref qualifier for xref to pubmed: "+xref.getCvXrefQualifier().getShortLabel());

                    CvXrefQualifier primaryRef = CvObjectUtils.createCvObject(experiment.getOwner(), CvXrefQualifier.class, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE);
                    CvObjectEnricher.getInstance().enrich(primaryRef);

                    xref.setCvXrefQualifier(primaryRef);
                }
            }
        }
    }
}
