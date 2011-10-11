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
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.xml.model.*;
import psidev.psi.mi.xml.model.Xref;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.MessageLevel;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.ConversionCache;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.PublicationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Experiment converter.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentConverter extends AbstractAnnotatedObjectConverter<Experiment, ExperimentDescription> {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(ExperimentConverter.class);
    private OrganismConverter organismConverter;
    private InteractionDetectionMethodConverter interactionDetectionMethodConverter;
    private ParticipantIdentificationMethodConverter participantIdentificationMethodConverter;
    private XrefConverter<PublicationXref> publicationXrefConverter;

    private Collection<String> publicationTopicsMi = new ArrayList<String>();
    private Collection<String> publicationTopics = new ArrayList<String>();

    public ExperimentConverter(Institution institution) {

        super(institution, Experiment.class, ExperimentDescription.class);
        organismConverter = new OrganismConverter(institution);
        interactionDetectionMethodConverter = new InteractionDetectionMethodConverter(institution);
        participantIdentificationMethodConverter = new ParticipantIdentificationMethodConverter(institution);
        publicationXrefConverter = new XrefConverter<PublicationXref>(institution, PublicationXref.class);

        initializePublicationTopics();
    }

    private void initializePublicationTopics(){
        publicationTopics.add("author-list");
        publicationTopicsMi.add("MI:0636");

        publicationTopics.add("contact-comment");
        publicationTopicsMi.add("MI:0635");

        publicationTopics.add("contact-email");
        publicationTopicsMi.add("MI:0634");

        publicationTopics.add("curation request");
        publicationTopicsMi.add("MI:0873");

        publicationTopics.add("dataset");
        publicationTopicsMi.add("MI:0875");

        publicationTopics.add("journal");
        publicationTopicsMi.add("MI:0885");

        publicationTopics.add("publication title");
        publicationTopicsMi.add("MI:1091");

        publicationTopics.add("publication year");
        publicationTopicsMi.add("MI:0886");

        publicationTopics.add("imex-exported");
        publicationTopics.add("imex-range-assigned");
        publicationTopics.add("imex-range-requested");
        publicationTopics.add("on-hold");
        publicationTopics.add("accepted");
        publicationTopics.add("curation depth");
    }

    public Experiment psiToIntact(ExperimentDescription psiObject) {
        Experiment experiment = super.psiToIntact(psiObject);

        if (!isNewIntactObjectCreated()) {
            return experiment;
        }

        psiStartConversion(psiObject);

        String shortLabel;

        if (psiObject.getNames() != null) {
            shortLabel = IntactConverterUtils.getShortLabelFromNames(psiObject.getNames());
        } else {
            shortLabel = IntactConverterUtils.createExperimentTempShortLabel();
        }

        experiment.setOwner(getInstitution());
        experiment.setShortLabel(shortLabel);

        IntactConverterUtils.populateNames(psiObject.getNames(), experiment, this.aliasConverter);
        IntactConverterUtils.populateXref(psiObject.getXref(), experiment, this.xrefConverter);
        IntactConverterUtils.populateAnnotations(psiObject, experiment, getInstitution(), this.annotationConverter);

        BioSource bioSource = null;

        // in Intact only one host organism per experiment
        if (psiObject.getHostOrganisms() != null && !psiObject.getHostOrganisms().isEmpty()) {
            Organism hostOrganism = psiObject.getHostOrganisms().iterator().next();

            bioSource = organismConverter.psiToIntact(hostOrganism);
            experiment.setBioSource(bioSource);
        }
        else {
            log.error("Experiment without host organism : " + shortLabel);
        }

        // if more tan one host organism, we just convert the first organism and ignore the others
        if (psiObject.getHostOrganisms().size() > 1){
            log.error("Experiment with "+psiObject.getHostOrganisms().size()+" host organisms : " + shortLabel + ". Only the first host organism will be converted in Intact.");
        }

        // convert the interaction detection method
        InteractionDetectionMethod idm = psiObject.getInteractionDetectionMethod();
        if (idm != null){
            CvInteraction cvInteractionDetectionMethod = this.interactionDetectionMethodConverter.psiToIntact(idm);
            experiment.setCvInteraction(cvInteractionDetectionMethod);
        }
        else {
            log.error("Experiment without interaction detection method : " + shortLabel);
        }

        // convert detection method
        ParticipantIdentificationMethod pim = psiObject.getParticipantIdentificationMethod();
        if (pim != null) {
            CvIdentification cvParticipantIdentification = this.participantIdentificationMethodConverter.psiToIntact(pim);
            experiment.setCvIdentification(cvParticipantIdentification);
        }
        else {
            log.error("Experiment without participant identification method : " + shortLabel);
        }

        // fail if the primary reference does not point to Pubmed and primary-reference
        boolean hasValidPrimaryRef = true;
        final Bibref bibref = psiObject.getBibref();

        // we have a bibref xref
        if( bibref != null && bibref.getXref() != null ) {
            IntactConverterUtils.populateXref(psiObject.getBibref().getXref(), experiment, this.xrefConverter);

            final DbReference primaryRef = bibref.getXref().getPrimaryRef();
            if ( ! hasValidPrimaryRef( primaryRef ) ) {
                final String message = "Bibref in ExperimentDescription [PSI Id=" + psiObject.getId() + "] " +
                        "should have a primary-reference (refTypeAc=" + CvXrefQualifier.PRIMARY_REFERENCE_MI_REF + ") " +
                        "with reference to Pubmed (dbAc=" + CvDatabase.PUBMED_MI_REF + ") or a DOI (dbAc=" + CvDatabase.DOI_MI_REF + "): " + primaryRef;
                log.warn(message);
                addMessageToContext(MessageLevel.WARN, message, true);

                hasValidPrimaryRef = false;
            }
        }
        // no bibref at all
        else if (bibref == null) {
            final String message = "No bibref defined in ExperimentDescription [PSI Id=" + psiObject.getId() + "]. " +
                    "It should have a primary-reference (refTypeAc=" + CvXrefQualifier.PRIMARY_REFERENCE_MI_REF + ") " +
                    "with reference to Pubmed (dbAc=" + CvDatabase.PUBMED_MI_REF + ") or a DOI (dbAc=" + CvDatabase.DOI_MI_REF + ")";
            log.warn(message);
            addMessageToContext(MessageLevel.WARN, message, true);

            hasValidPrimaryRef = false;
        }
        // one bibref but no xref objects
        else {
            hasValidPrimaryRef = false;
        }

        // create the publication
        Publication publication;
        // we have a pubmed or doi primary reference
        if( hasValidPrimaryRef ) {
            publication = createPublication(bibref);
            experiment.setPublication(publication);
            publication.addExperiment(experiment);
        }
        // we need to create a unassigned publication
        else {
            publication = createUnassignedPublication(bibref);
        }

        experiment.setPublication(publication);
        publication.addExperiment(experiment);

        // populates common attributes for the publication
        Collection<Attribute> publicationAttributes = extractPublicationAttributesFromExperiment(psiObject);
        IntactConverterUtils.populateAnnotations(publicationAttributes, publication, getInstitution(), this.annotationConverter);

        // CONFIDENCES and FEATURE DETECTION METHODS are not converted from the experiment

        psiEndConversion(psiObject);

        return experiment;
    }

    public ExperimentDescription intactToPsi(Experiment intactObject) {
        ExperimentDescription expDesc = super.intactToPsi(intactObject);

        if (!isNewPsiObjectCreated()) {
            return expDesc;
        }

        intactStartConversation(intactObject);

        // converts detection method
        if (intactObject.getCvInteraction() != null){
            InteractionDetectionMethod detMethod = (InteractionDetectionMethod) PsiConverterUtils.toCvType(intactObject.getCvInteraction(), this.interactionDetectionMethodConverter, this);
            expDesc.setInteractionDetectionMethod(detMethod);
        }
        else {
            log.error("Experiment without interaction detection method : " + intactObject.getShortLabel());
        }

        // converts participant identification method
        if (intactObject.getCvIdentification() != null) {
            ParticipantIdentificationMethod identMethod = (ParticipantIdentificationMethod)
                    PsiConverterUtils.toCvType(intactObject.getCvIdentification(),
                            this.participantIdentificationMethodConverter,
                            this );
            expDesc.setParticipantIdentificationMethod(identMethod);
        }
        else {
            log.error("Experiment without participant identification method : " + intactObject.getShortLabel());
        }

        // converts bioSource
        if (intactObject.getBioSource() != null) {
            Organism organism = this.organismConverter.intactToPsi(intactObject.getBioSource());
            expDesc.getHostOrganisms().add(organism);
        }
        else {
            log.error("Experiment without host organism : " + intactObject.getShortLabel());
        }

        Bibref bibref = new Bibref();

        if (intactObject.getPublication() != null){
            Publication publication = intactObject.getPublication();

            // the shortlabel of the publication is the pubmed id by default
            if (publication.getXrefs().isEmpty() && publication.getShortLabel() != null){
                Xref xref = new Xref();
                xref.setPrimaryRef(new DbReference(CvDatabase.PUBMED, CvDatabase.PUBMED_MI_REF, publication.getShortLabel(), CvXrefQualifier.PRIMARY_REFERENCE, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF));
                bibref.setXref(xref);
            }
            else if (publication.getXrefs().isEmpty() && !publication.getAnnotations().isEmpty()){
                PsiConverterUtils.populateAttributes( publication, bibref, annotationConverter );
            }
            else if (!publication.getXrefs().isEmpty()){
                PsiConverterUtils.populateXref( publication, bibref, publicationXrefConverter );
            }

            // add annotation from publication not present in experiment
            extractPublicationAnnotationsAbsentFromExperiment(publication, expDesc);
        }
        else {
            log.error("Experiment without publication : " + intactObject.getShortLabel());

            // we extract the primary ref from the experiment if possible
            if (!intactObject.getXrefs().isEmpty()){
                Collection<ExperimentXref> primaryRefs = AnnotatedObjectUtils.searchXrefs(intactObject, CvDatabase.PUBMED_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF);

                if (primaryRefs.isEmpty()){
                    primaryRefs = AnnotatedObjectUtils.searchXrefs(intactObject, CvDatabase.DOI_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF);
                }

                if (!primaryRefs.isEmpty()){
                    Iterator<ExperimentXref> iterator = primaryRefs.iterator();
                    ExperimentXref primaryRef = iterator.next();

                    Xref xref = new Xref();
                    xref.setPrimaryRef(new DbReference(primaryRef.getCvDatabase().getShortLabel(), primaryRef.getCvDatabase().getIdentifier(), primaryRef.getPrimaryId(), primaryRef.getCvXrefQualifier().getShortLabel(), primaryRef.getCvXrefQualifier().getIdentifier()));
                    bibref.setXref(xref);

                    while (iterator.hasNext()){
                        primaryRef = iterator.next();
                        xref.getSecondaryRef().add(new DbReference(primaryRef.getCvDatabase().getShortLabel(), primaryRef.getCvDatabase().getIdentifier(), primaryRef.getPrimaryId(), primaryRef.getCvXrefQualifier().getShortLabel(), primaryRef.getCvXrefQualifier().getIdentifier()));
                    }
                }
            }
        }

        // create bibref if possible
        if (bibref.getXref() != null && bibref.getXref().getPrimaryRef() != null){
            expDesc.setBibref(bibref);
        }
        else if (bibref.hasAttributes()){
            expDesc.setBibref(bibref);
        }
        else {
            log.error("Experiment without primary reference or publicaton " + intactObject.getShortLabel());
        }

        intactEndConversion(intactObject);

        return expDesc;
    }

    private boolean hasValidPrimaryRef( DbReference primaryRef ) {
        if (!CvXrefQualifier.PRIMARY_REFERENCE_MI_REF.equals(primaryRef.getRefTypeAc()) ||
                (!CvDatabase.PUBMED_MI_REF.equals(primaryRef.getDbAc())
                        && !CvDatabase.DOI_MI_REF.equals(primaryRef.getDbAc()))
                ) {
            return false;
        }
        return true;
    }

    private Publication createPublication(Bibref bibref) {
        final DbReference primaryRef = bibref.getXref().getPrimaryRef();

        String pubId = primaryRef.getId();

        Publication publication = (Publication) ConversionCache.getElement("pub:"+pubId);

        if (publication != null) {
            return publication;
        }

        publication = new Publication(getInstitution(), pubId);
        publication.setPublicationId(pubId);

        IntactConverterUtils.populateXref(bibref.getXref(), publication, this.publicationXrefConverter);
        IntactConverterUtils.populateAnnotations(bibref, publication, getInstitution(), this.annotationConverter);

        IntactContext.getCurrentInstance().getLifecycleManager().getStartStatus().create(publication, "created by xml import");

        ConversionCache.putElement("pub:"+pubId, publication);

        return publication;
    }

    private Publication createUnassignedPublication(Bibref bibRef) {

        String pubId = PublicationUtils.nextUnassignedId(IntactContext.getCurrentInstance());

        Publication publication = (Publication) ConversionCache.getElement("pub:"+pubId);

        if (publication != null) {
            return publication;
        }

        publication = new Publication(getInstitution(), pubId);
        publication.setPublicationId(pubId);

        if (bibRef != null){
            IntactConverterUtils.populateXref(bibRef.getXref(), publication, this.publicationXrefConverter);
            IntactConverterUtils.populateAnnotations(bibRef, publication, getInstitution(), this.annotationConverter);
        }

        IntactContext.getCurrentInstance().getLifecycleManager().getStartStatus().create(publication, "created by xml import");
        ConversionCache.putElement("pub:"+pubId, publication);

        return publication;
    }

    public Collection<Attribute> extractPublicationAttributesFromExperiment(ExperimentDescription expDesc){

        if (!expDesc.hasAttributes()){
            return Collections.EMPTY_LIST;
        }

        Collection<Attribute> attributes = new ArrayList<Attribute>(expDesc.getAttributes().size());

        for (Attribute attr : attributes){
            if (attr.getNameAc() != null){
                if (publicationTopicsMi.contains(attr.getNameAc())){
                    attributes.add(attr);
                }
            }
            else if (attr.getName() != null){
                if (publicationTopics.contains(attr.getNameAc().toLowerCase())){
                    attributes.add(attr);
                }
            }
        }

        return attributes;
    }

    public void extractPublicationAnnotationsAbsentFromExperiment(Publication pub, ExperimentDescription expDesc){

        if (pub.getAnnotations().isEmpty()){
            return;
        }

        for (Annotation attr : pub.getAnnotations()){
            Attribute attribute = annotationConverter.intactToPsi(attr);

            if (!expDesc.getAttributes().contains(attribute)){
                expDesc.getAttributes().add(attribute);
            }
        }
    }


    @Override
    public void setInstitution(Institution institution)
    {
        super.setInstitution(institution);
        organismConverter.setInstitution(institution);
        interactionDetectionMethodConverter.setInstitution(institution);
        participantIdentificationMethodConverter.setInstitution(institution);
        publicationXrefConverter.setInstitution(institution);
    }
}