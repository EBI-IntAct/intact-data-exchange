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
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.bridges.citexplore.exceptions.InvalidPubmedException;
import uk.ac.ebi.intact.bridges.citexplore.exceptions.PublicationNotFoundException;
import uk.ac.ebi.intact.bridges.citexplore.exceptions.UnexpectedException;
import uk.ac.ebi.intact.bridges.citexplore.util.ExperimentAutoFill;
import uk.ac.ebi.intact.dataexchange.cvutils.CvUtils;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.CvObjectFetcher;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.ExperimentFetcher;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.ExperimentUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class ExperimentEnricher extends AnnotatedObjectEnricher<Experiment> {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(ExperimentEnricher.class);

    @Autowired
    private CvObjectFetcher cvObjectFetcher;

    @Autowired
    private ExperimentFetcher experimentFetcher;

    @Autowired
    private CvObjectEnricher cvObjectEnricher;

    @Autowired
    private BioSourceEnricher bioSourceEnricher;

    public ExperimentEnricher() {
    }

    public void enrich(Experiment objectToEnrich) {
        if (getEnricherContext().getConfig().isUpdateOrganisms()){
            bioSourceEnricher.enrich(objectToEnrich.getBioSource());
        }

        if (getEnricherContext().getConfig().isUpdateCvTerms()) {
            if (objectToEnrich.getCvInteraction() != null){
                cvObjectEnricher.enrich(objectToEnrich.getCvInteraction());
            }

            // add the participant detection method to the experiment if missing
            if (objectToEnrich.getCvIdentification() == null) {
                String detMethodMi = calculateParticipantDetMethod(objectToEnrich);

                if (detMethodMi != null) {
                    CvIdentification detMethod = cvObjectFetcher.fetchByTermId(CvIdentification.class, detMethodMi);
                    objectToEnrich.setCvIdentification(detMethod);
                }
            }

            if (objectToEnrich.getCvIdentification() != null) {
                cvObjectEnricher.enrich(objectToEnrich.getCvIdentification());
            }
        }

        if (objectToEnrich.getPublication() != null){
            final Collection<PublicationXref> primaryRefs =
                    AnnotatedObjectUtils.searchXrefsByQualifier( objectToEnrich.getPublication(), CvXrefQualifier.PRIMARY_REFERENCE_MI_REF );

            if( primaryRefs.isEmpty() ){
                // fix wrong xref qualifiers in pubmed xrefs if necessary
                fixPubmedXrefIfNecessary(objectToEnrich.getPublication());
            }
        }

        final Collection<ExperimentXref> primaryRefs =
                AnnotatedObjectUtils.searchXrefsByQualifier( objectToEnrich, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF );

        if( primaryRefs.isEmpty() ){
            // fix wrong xref qualifiers in pubmed xrefs if necessary
            fixPubmedXrefIfNecessary(objectToEnrich);
        }
        
        // populate the experiment using the pubmed id
        if (getEnricherContext().getConfig().isUpdateExperiments()) {
            String pubmedId = ExperimentUtils.getPubmedId(objectToEnrich);
            // enrich
            if (pubmedId != null){
                try {
                    Long.parseLong(pubmedId);
                    populateExperiment(objectToEnrich, pubmedId);

                } catch (InvalidPubmedException pe) {
                    log.error("Experiment with invalid pubmed id cannot be enriched from citeXplore: "+pubmedId);
                    populateExperiment(objectToEnrich, null);
                } catch (NumberFormatException nfe) {
                    log.error("Experiment with invalid pubmed (not a number) cannot be enriched from citeXplore: "+pubmedId);
                    populateExperiment(objectToEnrich, null);
                } catch (Exception e) {
                    log.error("An error occured while enriching experiment with PMID: "+pubmedId, e);
                    populateExperiment(objectToEnrich, null);
                }
            }
            // check author-list and publication date
            else {
                populateExperiment(objectToEnrich, null);
            }
        }

        super.enrich(objectToEnrich);
    }

    protected void populateExperiment(Experiment experiment, String pubmedId) {
        ExperimentAutoFill autoFill = null;
        String shortLabel = null;
        if (pubmedId != null){
            autoFill = experimentFetcher.fetchByPubmedId(pubmedId);
            try {
                shortLabel = AnnotatedObjectUtils.prepareShortLabel(autoFill.getShortlabel());
            } catch (UnexpectedException e) {
                log.error(e);
            } catch (PublicationNotFoundException e) {
                log.error(e);
            }
        }

        if (autoFill != null) {
            if (experiment.getShortLabel() == null || !ExperimentUtils.matchesSyncedLabel(experiment.getShortLabel())) {
                experiment.setShortLabel(shortLabel);
            }

            String authorList = autoFill.getAuthorList();
            String pubYear = String.valueOf(autoFill.getYear());
            String contactEmail = autoFill.getAuthorEmail();
            String journal = autoFill.getJournal();

            fillPublicationDetails(experiment, authorList, pubYear, contactEmail, journal, autoFill.getFullname());
            if (experiment.getPublication() != null){
                fillPublicationDetails(experiment.getPublication(), authorList, pubYear, contactEmail, journal, autoFill.getFullname());
            }
        } else {
            if (log.isWarnEnabled()) log.warn("Couldn't use ExperimentAutoFill for experiment with pubmed: "+pubmedId+". Will look at existing author list and publication date");

            String authorList = null;
            String pubYear = null;

            for (Annotation annot : experiment.getAnnotations()) {
                if (CvTopic.AUTHOR_LIST_MI_REF.equals(annot.getCvTopic().getIdentifier())) {
                    if (authorList == null){
                        authorList = annot.getAnnotationText();
                    }
                }
                else if (CvTopic.PUBLICATION_YEAR_MI_REF.equals(annot.getCvTopic().getIdentifier())){
                    if (pubYear == null){
                        pubYear = annot.getAnnotationText();
                    }
                }
            }

            if (authorList != null && pubYear != null){
                String authorLastName = null;
                if (authorList.contains(",")){
                    String[] authors = authorList.split(",");
                    String lastAuthor = authors[0];
                    if (lastAuthor.contains(" ")){
                        authorLastName = lastAuthor.substring(0,lastAuthor.indexOf(" ")).toLowerCase().trim();
                    }
                    else{
                        authorLastName = lastAuthor.toLowerCase().trim();
                    }
                }
                else if (authorList.contains(" ")){
                    authorLastName = authorList.substring(0,authorList.indexOf(" ")).toLowerCase().trim();
                }
                else{
                    authorLastName = authorList.toLowerCase().trim();
                }

                shortLabel = AnnotatedObjectUtils.prepareShortLabel(authorLastName+"-"+pubYear);
            }
            else {
                shortLabel = AnnotatedObjectUtils.prepareShortLabel(experiment.getShortLabel());
            }

            if (experiment.getShortLabel() == null || !ExperimentUtils.matchesSyncedLabel(experiment.getShortLabel())) {
                experiment.setShortLabel(shortLabel);
            }
        }
    }

    private void fillPublicationDetails(AnnotatedObject object, String authorList, String pubYear, String contactEmail, String journal, String title) {
        if (title != null){
            object.setFullName(title);
        }

        boolean hasAuthor = false;
        boolean hasPublicationYear = false;
        boolean hasContactEmail = false;
        boolean hasJournal = false;

        // fill experiments annotations
        for (Annotation annot : object.getAnnotations()) {
            if (CvTopic.AUTHOR_LIST_MI_REF.equals(annot.getCvTopic().getIdentifier())) {
                hasAuthor = true;
                if (annot.getAnnotationText() == null || (annot.getAnnotationText() != null && authorList != null && !authorList.equalsIgnoreCase(annot.getAnnotationText()))){
                    annot.setAnnotationText(authorList);
                }
            }
            else if (CvTopic.PUBLICATION_YEAR_MI_REF.equals(annot.getCvTopic().getIdentifier())){
                hasPublicationYear = true;
                if (annot.getAnnotationText() == null || (annot.getAnnotationText() != null && pubYear != null && !pubYear.equalsIgnoreCase(annot.getAnnotationText()))){
                    annot.setAnnotationText(pubYear);
                }
            }
            else if (CvTopic.CONTACT_EMAIL_MI_REF.equals(annot.getCvTopic().getIdentifier())){
                hasContactEmail = true;
                if (annot.getAnnotationText() == null || (annot.getAnnotationText() != null && contactEmail != null && !contactEmail.equalsIgnoreCase(annot.getAnnotationText()))){
                    annot.setAnnotationText(contactEmail);
                }
            }
            else if (CvTopic.JOURNAL_MI_REF.equals(annot.getCvTopic().getIdentifier())){
                hasJournal = true;
                if (annot.getAnnotationText() == null || (annot.getAnnotationText() != null && journal != null && !journal.equalsIgnoreCase(annot.getAnnotationText()))){
                    annot.setAnnotationText(journal);
                }
            }
        }

        if (!hasPublicationYear && pubYear != null) {
            CvTopic publicationYearTopic = CvObjectUtils.createCvObject(object.getOwner(), CvTopic.class, CvTopic.PUBLICATION_YEAR_MI_REF, CvTopic.PUBLICATION_YEAR);
            object.addAnnotation(new Annotation(object.getOwner(), publicationYearTopic, pubYear));
        }

        if (!hasAuthor && authorList != null) {
            CvTopic authorListTopic = CvObjectUtils.createCvObject(object.getOwner(), CvTopic.class, CvTopic.AUTHOR_LIST_MI_REF, CvTopic.AUTHOR_LIST);
            object.addAnnotation(new Annotation(object.getOwner(), authorListTopic, authorList));
        }

        if (!hasContactEmail && contactEmail != null) {
            CvTopic authorEmail = CvObjectUtils.createCvObject(object.getOwner(), CvTopic.class, CvTopic.CONTACT_EMAIL_MI_REF, CvTopic.CONTACT_EMAIL);
            object.addAnnotation(new Annotation(object.getOwner(), authorEmail, contactEmail));
        }

        if (!hasJournal && journal != null) {
            CvTopic journalTopic = CvObjectUtils.createCvObject(object.getOwner(), CvTopic.class, CvTopic.JOURNAL_MI_REF, CvTopic.JOURNAL);
            object.addAnnotation(new Annotation(object.getOwner(), journalTopic, journal));
        }
    }

    private boolean experimentAnnotationsContainTopic(Experiment experiment, String miIdentifier) {
        for (Annotation annot : experiment.getAnnotations()) {
            if (miIdentifier.equals(annot.getCvTopic().getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    private String calculateParticipantDetMethod(Experiment experiment) {
        Set<String> detMethodMis = new HashSet<String>();

        if (experiment.getCvIdentification() != null && experiment.getCvIdentification().getIdentifier() != null){
           detMethodMis.add(experiment.getCvIdentification().getIdentifier());
        }

        for (Interaction interaction : experiment.getInteractions()) {
            for (Component component : interaction.getComponents()) {
                for (CvIdentification partDetMethod : component.getParticipantDetectionMethods()) {
                    if (partDetMethod.getIdentifier() != null) {
                        detMethodMis.add(partDetMethod.getIdentifier());
                    }
                }
            }
        }

        if (detMethodMis.size() == 1) {
            return detMethodMis.iterator().next();
        } else if (detMethodMis.size() > 1) {
            List<CvDagObject> ontology = getEnricherContext().getIntactOntology();

            return CvUtils.findLowestCommonAncestor(ontology, detMethodMis.toArray(new String[detMethodMis.size()]));
        }

        log.error("No participant detection methods found for components in experiment");

        return null;
    }

    protected void fixPubmedXrefIfNecessary(AnnotatedObject<? extends Xref, ? extends Alias> annotatedObject) {
        for (Xref xref : annotatedObject.getXrefs()) {
            if (CvDatabase.PUBMED_MI_REF.equals(xref.getCvDatabase().getIdentifier())) {
                if ( xref.getCvXrefQualifier() == null ) {
                    log.warn( "Fixing pubmed xref with no xref qualifier: "+xref.getPrimaryId() );

                    CvXrefQualifier primaryRef = CvObjectUtils.createCvObject(annotatedObject.getOwner(),
                                                                              CvXrefQualifier.class,
                                                                              CvXrefQualifier.PRIMARY_REFERENCE_MI_REF,
                                                                              CvXrefQualifier.PRIMARY_REFERENCE);
                    cvObjectEnricher.enrich(primaryRef);
                    xref.setCvXrefQualifier(primaryRef);
                }
            }
        }
    }
}
