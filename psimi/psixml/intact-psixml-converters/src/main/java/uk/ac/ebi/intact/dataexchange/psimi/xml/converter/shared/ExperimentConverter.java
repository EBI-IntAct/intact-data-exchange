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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.xml.model.*;
import psidev.psi.mi.xml.model.Xref;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.MessageLevel;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.config.AnnotationConverterConfig;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.ConversionCache;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.PublicationUtils;

import java.util.*;

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

        experiment.setOwner(getInstitution());

        String shortLabel = IntactConverterUtils.getShortLabelFromNames(psiObject.getNames());
        if (shortLabel == null) {
            shortLabel = IntactConverterUtils.createExperimentTempShortLabel();
        }
        experiment.setShortLabel(shortLabel);

        if (psiObject.getNames() != null) {
            experiment.setFullName(psiObject.getNames().getFullName());
        }

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
            log.error("Experiment without host organism : " + experiment.getShortLabel());
        }

        // if more tan one host organism, we just convert the first organism and ignore the others
        if (psiObject.getHostOrganisms().size() > 1){
            log.error("Experiment with "+psiObject.getHostOrganisms().size()+" host organisms : " + experiment.getShortLabel() + ". Only the first host organism will be converted in Intact.");
        }

        // convert the interaction detection method
        InteractionDetectionMethod idm = psiObject.getInteractionDetectionMethod();
        if (idm != null){
            CvInteraction cvInteractionDetectionMethod = this.interactionDetectionMethodConverter.psiToIntact(idm);
            experiment.setCvInteraction(cvInteractionDetectionMethod);
        }
        else {
            log.error("Experiment without interaction detection method : " + experiment.getShortLabel());
        }

        // convert detection method
        ParticipantIdentificationMethod pim = psiObject.getParticipantIdentificationMethod();
        if (pim != null) {
            CvIdentification cvParticipantIdentification = this.participantIdentificationMethodConverter.psiToIntact(pim);
            experiment.setCvIdentification(cvParticipantIdentification);
        }
        else {
            log.error("Experiment without participant identification method : " + experiment.getShortLabel());
        }

        // fail if the primary reference does not point to Pubmed and primary-reference
        boolean hasValidPrimaryRef = true;
        Bibref bibref = psiObject.getBibref();
        DbReference validPubmedPrimaryRef = null;
        DbReference validDOIPrimaryRef = null;

        // we have a bibref xref
        if( bibref != null && bibref.getXref() != null ) {
            IntactConverterUtils.populateXref(psiObject.getBibref().getXref(), experiment, this.xrefConverter);

            for (DbReference ref : bibref.getXref().getAllDbReferences()){
                if (hasValidPubmedPrimaryRef( ref )){
                    validPubmedPrimaryRef = ref;
                    break;
                }
                else if (hasValidDOIPrimaryRef(ref)){
                    if (validDOIPrimaryRef != null){
                        validDOIPrimaryRef = ref;
                    }
                }
            }

            // for backward compatibility, check experiment xrefs for doi or pubmed
            if (validDOIPrimaryRef == null && validPubmedPrimaryRef == null){
                final String message = "Bibref in ExperimentDescription [PSI Id=" + psiObject.getId() + "] " +
                        "should have a primary-reference (refTypeAc=" + CvXrefQualifier.PRIMARY_REFERENCE_MI_REF + ") " +
                        "with reference to Pubmed (dbAc=" + CvDatabase.PUBMED_MI_REF + ") or a DOI (dbAc=" + CvDatabase.DOI_MI_REF + "): " + bibref.getXref().getPrimaryRef();
                log.warn(message);
                addMessageToContext(MessageLevel.WARN, message, true);

                // for backward compatibility, check experiment for pubmed
                if ( ConverterContext.getInstance().isCheckingExperimentForPrimaryRefs()
                      &&  validPubmedPrimaryRef == null && psiObject.getXref() != null){
                    for (DbReference ref : psiObject.getXref().getAllDbReferences()){
                        if (hasValidPubmedPrimaryRef( ref )){
                            validPubmedPrimaryRef = ref;
                            if (bibref.getXref().getPrimaryRef() == null){
                                bibref.getXref().setPrimaryRef(ref);
                            }
                            else {
                                bibref.getXref().getSecondaryRef().add(ref);
                            }
                            break;
                        }
                        else if (hasValidDOIPrimaryRef(ref)){
                            if (validDOIPrimaryRef != null){
                                validDOIPrimaryRef = ref;
                                if (bibref.getXref().getPrimaryRef() == null){
                                    bibref.getXref().setPrimaryRef(ref);
                                }
                                else {
                                    bibref.getXref().getSecondaryRef().add(ref);
                                }
                            }
                        }
                    }
                }

                hasValidPrimaryRef = (validDOIPrimaryRef != null || validPubmedPrimaryRef != null);
            }
        }
        // no bibref at all, have a look at experiment xrefs
        else if (bibref == null && psiObject.getXref() != null) {
            final String message = "No bibref defined in ExperimentDescription [PSI Id=" + psiObject.getId() + "]. " +
                    "It should have a primary-reference (refTypeAc=" + CvXrefQualifier.PRIMARY_REFERENCE_MI_REF + ") " +
                    "with reference to Pubmed (dbAc=" + CvDatabase.PUBMED_MI_REF + ") or a DOI (dbAc=" + CvDatabase.DOI_MI_REF + ")";
            log.warn(message);
            addMessageToContext(MessageLevel.WARN, message, true);

            if (ConverterContext.getInstance().isCheckingExperimentForPrimaryRefs()){
                for (DbReference ref : psiObject.getXref().getAllDbReferences()){
                    if (hasValidPubmedPrimaryRef( ref )){
                        if (bibref == null){
                            bibref = new Bibref(new Xref(ref));
                        }
                        else if (bibref.getXref() == null){
                            bibref.setXref(new Xref(ref));
                        }
                        else if (bibref.getXref().getPrimaryRef() == null) {
                            bibref.getXref().setPrimaryRef(ref);
                        }
                        else{
                            bibref.getXref().getSecondaryRef().add(ref);
                        }
                        validPubmedPrimaryRef = ref;
                        break;
                    }
                    else if (hasValidDOIPrimaryRef(ref)){
                        if (validDOIPrimaryRef != null){
                            if (bibref == null){
                                bibref = new Bibref(new Xref(ref));
                            }
                            else if (bibref.getXref() == null){
                                bibref.setXref(new Xref(ref));
                            }
                            else if (bibref.getXref().getPrimaryRef() == null) {
                                bibref.getXref().setPrimaryRef(ref);
                            }
                            else{
                                bibref.getXref().getSecondaryRef().add(ref);
                            }
                            validDOIPrimaryRef = ref;
                            bibref.getXref().getSecondaryRef().add(ref);
                        }
                    }
                }
            }

            hasValidPrimaryRef = (validDOIPrimaryRef != null || validPubmedPrimaryRef != null);
        }
        // one bibref but no xref objects or no bibref and no experiment xref
        else {
            hasValidPrimaryRef = false;
        }

        // create the publication
        Publication publication;
        // we have a pubmed or doi primary reference
        if( hasValidPrimaryRef ) {
            publication = createPublication(bibref, validPubmedPrimaryRef != null ? validPubmedPrimaryRef : validDOIPrimaryRef);
            experiment.setPublication(publication);
            publication.addExperiment(experiment);
        }
        // we need to create a unassigned publication
        else {
            publication = createUnassignedPublication(bibref, experiment);
        }

        publication.setFullName(experiment.getFullName());

        ExperimentXref imexPrimary=null;
        for (ExperimentXref refs : experiment.getXrefs()){
            if (refs.getCvDatabase() != null && CvDatabase.IMEX_MI_REF.equals(refs.getCvDatabase().getIdentifier())){
                 if (refs.getCvXrefQualifier() != null && CvXrefQualifier.IMEX_PRIMARY_MI_REF.equals(refs.getCvXrefQualifier().getIdentifier())){
                      imexPrimary = refs;
                     break;
                 }
            }
        }
        if (imexPrimary != null){
            PublicationXref pubImexPrimary = new PublicationXref(publication.getOwner(), imexPrimary.getCvDatabase(), imexPrimary.getPrimaryId(), imexPrimary.getCvXrefQualifier());
            publication.addXref(pubImexPrimary);
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

        // Set id, annotations, xrefs and aliases
        PsiConverterUtils.populateId(expDesc);
        PsiConverterUtils.populateNames(intactObject, expDesc, aliasConverter);
        PsiConverterUtils.populateXref(intactObject, expDesc, xrefConverter);
        PsiConverterUtils.populateAttributes(intactObject, expDesc, annotationConverter);

        // converts detection method
        if (intactObject.getCvInteraction() != null){
            InteractionDetectionMethod detMethod = this.interactionDetectionMethodConverter.intactToPsi(intactObject.getCvInteraction());
            expDesc.setInteractionDetectionMethod(detMethod);
        }
        else {
            log.error("Experiment without interaction detection method : " + intactObject.getShortLabel());
        }

        // converts participant identification method
        if (intactObject.getCvIdentification() != null) {
            ParticipantIdentificationMethod identMethod = participantIdentificationMethodConverter.intactToPsi(intactObject.getCvIdentification());
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
        boolean isBibRefSet = false;

        if (intactObject.getPublication() != null){
            expDesc.setBibref(bibref);
            PsiConverterUtils.populateXref(intactObject, bibref, xrefConverter);
            if (bibref.getXref() != null && bibref.getXref().getPrimaryRef() != null){
                isBibRefSet = true;
            }
            else if (ConverterContext.getInstance().isCheckingExperimentForPrimaryRefs()){
                isBibRefSet = extractPrimaryRefFromExperiment(intactObject, bibref);
            }

            Publication publication = intactObject.getPublication();

            // the shortlabel of the publication is the pubmed id by default
            if (!isBibRefSet && publication.getXrefs().isEmpty() && publication.getShortLabel() != null){
                Xref xref = new Xref();
                xref.setPrimaryRef(new DbReference(CvDatabase.PUBMED, CvDatabase.PUBMED_MI_REF, publication.getShortLabel(), CvXrefQualifier.PRIMARY_REFERENCE, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF));
                bibref.setXref(xref);
                isBibRefSet = true;
            }
            else if (!isBibRefSet && publication.getXrefs().isEmpty() && !publication.getAnnotations().isEmpty()){
                PsiConverterUtils.populateAttributes( publication, bibref, annotationConverter );
            }
            else if (!isBibRefSet && !publication.getXrefs().isEmpty()){
                PsiConverterUtils.populateXref(publication, bibref, publicationXrefConverter);
            }

            // add annotation/xrefs from publication not present in experiment
            extractPublicationAnnotationsAndXrefsAbsentFromExperiment(publication, expDesc, intactObject);
        }
        else {
            log.error("Experiment without publication : " + intactObject.getShortLabel());

            // we extract the primary ref from the experiment if possible
            isBibRefSet = extractPrimaryRefFromExperiment(intactObject, bibref);
        }

        // create bibref if possible
        if (!isBibRefSet){
            if (bibref.getXref() != null && bibref.getXref().getPrimaryRef() != null){
                expDesc.setBibref(bibref);
            }
            else if (bibref.hasAttributes()){
                expDesc.setBibref(bibref);
            }
            else {
                log.error("Experiment without primary reference or publicaton " + intactObject.getShortLabel());
            }
        }

        intactEndConversion(intactObject);

        return expDesc;
    }

    private boolean extractPrimaryRefFromExperiment(Experiment intactObject, Bibref bibref) {
        if (!intactObject.getXrefs().isEmpty()){
            Collection<uk.ac.ebi.intact.model.Xref> primaryRefs = searchXrefs(intactObject, CvDatabase.PUBMED_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF, isCheckInitializedCollections());

            if (primaryRefs.isEmpty()){
                primaryRefs = searchXrefs(intactObject, CvDatabase.DOI_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF, isCheckInitializedCollections());
            }

            if (!primaryRefs.isEmpty()){
                Iterator<uk.ac.ebi.intact.model.Xref> iterator = primaryRefs.iterator();
                uk.ac.ebi.intact.model.Xref primaryRef = iterator.next();

                Xref xref = new Xref();
                xref.setPrimaryRef(new DbReference(primaryRef.getCvDatabase().getShortLabel(), primaryRef.getCvDatabase().getIdentifier(), primaryRef.getPrimaryId(), primaryRef.getCvXrefQualifier().getShortLabel(), primaryRef.getCvXrefQualifier().getIdentifier()));
                bibref.setXref(xref);

                while (iterator.hasNext()){
                    primaryRef = iterator.next();
                    xref.getSecondaryRef().add(new DbReference(primaryRef.getCvDatabase().getShortLabel(), primaryRef.getCvDatabase().getIdentifier(), primaryRef.getPrimaryId(), primaryRef.getCvXrefQualifier().getShortLabel(), primaryRef.getCvXrefQualifier().getIdentifier()));
                }

                return true;
            }
        }

        return false;
    }

    private boolean hasValidPrimaryRef( DbReference primaryRef ) {
        if ( (primaryRef.getRefTypeAc() != null && !CvXrefQualifier.PRIMARY_REFERENCE_MI_REF.equals(primaryRef.getRefTypeAc())) ||
        (primaryRef.getRefTypeAc() == null && !CvXrefQualifier.PRIMARY_REFERENCE.equals(primaryRef.getRefType().toLowerCase())) ||
                (primaryRef.getDbAc() != null && !CvDatabase.PUBMED_MI_REF.equals(primaryRef.getDbAc())
                        && !CvDatabase.DOI_MI_REF.equals(primaryRef.getDbAc())) ||
                (primaryRef.getDbAc() == null && !CvDatabase.PUBMED.equals(primaryRef.getDb().toLowerCase())
                        && !CvDatabase.DOI.equals(primaryRef.getDb().toLowerCase()))
                ) {
            return false;
        }
        return true;
    }

    private boolean hasValidPubmedPrimaryRef( DbReference primaryRef ) {
        if ( (primaryRef.getRefTypeAc() != null && !CvXrefQualifier.PRIMARY_REFERENCE_MI_REF.equals(primaryRef.getRefTypeAc())) ||
                (primaryRef.getRefTypeAc() == null && !CvXrefQualifier.PRIMARY_REFERENCE.equals(primaryRef.getRefType().toLowerCase())) ||
                (primaryRef.getDbAc() != null && !CvDatabase.PUBMED_MI_REF.equals(primaryRef.getDbAc())) ||
                (primaryRef.getDbAc() == null && !CvDatabase.PUBMED.equals(primaryRef.getDb().toLowerCase()))
                ) {
            return false;
        }
        return true;
    }

    private boolean hasValidDOIPrimaryRef( DbReference primaryRef ) {
        if ( (primaryRef.getRefTypeAc() != null && !CvXrefQualifier.PRIMARY_REFERENCE_MI_REF.equals(primaryRef.getRefTypeAc())) ||
                (primaryRef.getRefTypeAc() == null && !CvXrefQualifier.PRIMARY_REFERENCE.equals(primaryRef.getRefType().toLowerCase())) ||
                (primaryRef.getDbAc() != null && !CvDatabase.DOI_MI_REF.equals(primaryRef.getDbAc())) ||
                (primaryRef.getDbAc() == null && !CvDatabase.DOI.equals(primaryRef.getDb().toLowerCase()))
                ) {
            return false;
        }
        return true;
    }

    private Publication createPublication(Bibref bibref, DbReference validPrimaryRef) {
        String pubId = validPrimaryRef.getId();

        Publication publication = (Publication) ConversionCache.getElement("pub:"+pubId);

        if (publication != null) {
            return publication;
        }

        publication = new Publication(getInstitution(), pubId);

        IntactConverterUtils.populateXref(bibref.getXref(), publication, this.publicationXrefConverter);
        IntactConverterUtils.populateAnnotations(bibref, publication, getInstitution(), this.annotationConverter);

        IntactContext.getCurrentInstance().getLifecycleManager().getStartStatus().create(publication, "created by xml import");

        ConversionCache.putElement("pub:"+pubId, publication);

        return publication;
    }

    private Publication createUnassignedPublication(Bibref bibRef, Experiment exp) {

        String pubId = PublicationUtils.nextUnassignedId(IntactContext.getCurrentInstance());

        Publication publication = (Publication) ConversionCache.getElement("pub:"+pubId);

        if (publication != null) {
            return publication;
        }

        publication = new Publication(getInstitution(), pubId);
        CvDatabase pubmed = CvObjectUtils.createCvObject(getInstitution(), CvDatabase.class, CvDatabase.PUBMED_MI_REF, CvDatabase.PUBMED);
        CvXrefQualifier primary = CvObjectUtils.createCvObject(getInstitution(), CvXrefQualifier.class, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE);
        // add unassigned as primary ref
        publication.addXref(new PublicationXref(getInstitution(), pubmed, pubId, primary));
        // add unassigned to exeriment
        exp.addXref(new ExperimentXref(getInstitution(), pubmed, pubId, primary));
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

        for (Attribute attr : expDesc.getAttributes()){
            if (attr.getNameAc() != null){
                if (publicationTopicsMi.contains(attr.getNameAc())){
                    attributes.add(attr);
                }
            }
            else if (attr.getName() != null){
                if (publicationTopics.contains(attr.getName().toLowerCase())){
                    attributes.add(attr);
                }
            }
        }

        return attributes;
    }

    public void extractPublicationAnnotationsAndXrefsAbsentFromExperiment(Publication pub, ExperimentDescription expDesc, Experiment exp){
        AnnotationConverterConfig configAnnotation = ConverterContext.getInstance().getAnnotationConfig();

        if (pub.getAnnotations().isEmpty() && pub.getXrefs().isEmpty()){
            return;
        }

        for (Annotation attr : pub.getAnnotations()){
            if (!configAnnotation.isExcluded(attr.getCvTopic())) {
                Attribute attribute = annotationConverter.intactToPsi(attr);

                if (!expDesc.getAttributes().contains(attribute)){
                    expDesc.getAttributes().add(attribute);
                }
            }
        }

        if (!pub.getXrefs().isEmpty()){
            Set<DbReference> convertedRefs = PsiConverterUtils.toDbReferences(pub.getXrefs(), publicationXrefConverter);

            if (expDesc.getXref() == null){
                Set<DbReference> existingDbRefs = new HashSet();

                if (expDesc.getBibref() != null && expDesc.getBibref().getXref() != null){
                    existingDbRefs.addAll(expDesc.getBibref().getXref().getAllDbReferences());
                }
                Collection<DbReference> disjunction = CollectionUtils.subtract(convertedRefs, existingDbRefs);
                if (!disjunction.isEmpty()){
                    Iterator<DbReference> iteratorDb = disjunction.iterator();

                    Xref expRef = new Xref(iteratorDb.next());

                    while(iteratorDb.hasNext()){
                        expRef.getSecondaryRef().add(iteratorDb.next());
                    }

                    expDesc.setXref(expRef);
                }
            }
            else {
                Xref expRef = expDesc.getXref();

                Set<DbReference> existingDbRefs = new HashSet(expRef.getAllDbReferences());

                if (expDesc.getBibref() != null && expDesc.getBibref().getXref() != null){
                    existingDbRefs.addAll(expDesc.getBibref().getXref().getAllDbReferences());
                }

                Collection<DbReference> disjunction = CollectionUtils.subtract(convertedRefs, existingDbRefs);

                if (!disjunction.isEmpty()){
                    if (expRef.getPrimaryRef() == null){
                        Iterator<DbReference> iteratorDb = disjunction.iterator();
                        expRef.setPrimaryRef(iteratorDb.next());

                        while(iteratorDb.hasNext()){
                            expRef.getSecondaryRef().add(iteratorDb.next());
                        }
                    }
                    else {
                        expRef.getSecondaryRef().addAll(disjunction);
                    }
                }
            }
        }
    }


    @Override
    public void setInstitution(Institution institution)
    {
        super.setInstitution(institution);
        organismConverter.setInstitution(institution, getInstitutionPrimaryId());
        interactionDetectionMethodConverter.setInstitution(institution, getInstitutionPrimaryId());
        participantIdentificationMethodConverter.setInstitution(institution, getInstitutionPrimaryId());
        publicationXrefConverter.setInstitution(institution, getInstitutionPrimaryId());
    }

    @Override
    public void setInstitution(Institution institution, String institId){
        super.setInstitution(institution, institId);
        organismConverter.setInstitution(institution, getInstitutionPrimaryId());
        interactionDetectionMethodConverter.setInstitution(institution, getInstitutionPrimaryId());
        participantIdentificationMethodConverter.setInstitution(institution, getInstitutionPrimaryId());
        publicationXrefConverter.setInstitution(institution, getInstitutionPrimaryId());
    }

    @Override
    public void setCheckInitializedCollections(boolean check){
        super.setCheckInitializedCollections(check);
        this.organismConverter.setCheckInitializedCollections(check);
        this.interactionDetectionMethodConverter.setCheckInitializedCollections(check);
        this.participantIdentificationMethodConverter.setCheckInitializedCollections(check);
        this.publicationXrefConverter.setCheckInitializedCollections(check);
    }
}