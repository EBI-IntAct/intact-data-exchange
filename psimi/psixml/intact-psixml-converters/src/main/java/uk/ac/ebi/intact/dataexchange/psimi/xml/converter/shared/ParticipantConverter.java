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
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.UnsupportedConversionException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.Range;

import java.util.*;

/**
 * Participant converter.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ParticipantConverter extends AbstractAnnotatedObjectConverter<Component, psidev.psi.mi.xml.model.Participant>{

    private static final Log log = LogFactory.getLog(ParticipantConverter.class);
    // TODO: fix ConversionCache or lazy initialization (featureMap is only necessary because of this)
    Map<String, psidev.psi.mi.xml.model.Feature> featureMap = new HashMap<String, psidev.psi.mi.xml.model.Feature>();

    private InteractionConverter interactionConverter;
    private ExperimentalRoleConverter experimentalRoleConverter;
    private BiologicalRoleConverter biologicalRoleConverter;
    private InteractorConverter interactorConverter;
    private ParticipantIdentificationMethodConverter pimConverter;
    private CvObjectConverter<CvExperimentalPreparation, ExperimentalPreparation> epConverter;
    private FeatureConverter featureConverter;
    private OrganismConverter organismConverter;
    private ParticipantConfidenceConverter confidenceConverter;
    private ParticipantParameterConverter participantParameterConverter;
    private static final String STOICHIOMETRY_PREFIX = "Stoichiometry: ";

    public ParticipantConverter(Institution institution) {
        super(institution, Component.class, psidev.psi.mi.xml.model.Participant.class);
        ExperimentConverter expConverter = new ExperimentConverter(institution);

        interactionConverter = new InteractionConverter(institution, expConverter, this);
        this.experimentalRoleConverter = new ExperimentalRoleConverter( institution );
        this.biologicalRoleConverter = new BiologicalRoleConverter(institution);
        this.interactorConverter = new InteractorConverter(institution);
        pimConverter = new ParticipantIdentificationMethodConverter(institution);
        epConverter = new CvObjectConverter<CvExperimentalPreparation, ExperimentalPreparation>(institution, CvExperimentalPreparation.class, ExperimentalPreparation.class);
        featureConverter = new FeatureConverter(institution);
        organismConverter = new OrganismConverter(institution);
        confidenceConverter = new ParticipantConfidenceConverter( institution);
        participantParameterConverter = new ParticipantParameterConverter( institution, expConverter);
    }

    public ParticipantConverter(Institution institution, InteractionConverter interactionConverter) {
        super(institution, Component.class, psidev.psi.mi.xml.model.Participant.class);
        ExperimentConverter expConverter = new ExperimentConverter(institution);

        if (interactionConverter != null){
            this.interactionConverter = interactionConverter;
        }
        else {
            this.interactionConverter = new InteractionConverter(institution, expConverter, this);
        }

        this.participantParameterConverter = new ParticipantParameterConverter(institution, expConverter);
        this.experimentalRoleConverter = new ExperimentalRoleConverter( institution );
        this.biologicalRoleConverter = new BiologicalRoleConverter(institution);
        this.interactorConverter = new InteractorConverter(institution);
        pimConverter = new ParticipantIdentificationMethodConverter(institution);
        epConverter = new CvObjectConverter<CvExperimentalPreparation, ExperimentalPreparation>(institution, CvExperimentalPreparation.class, ExperimentalPreparation.class);
        featureConverter = new FeatureConverter(institution);
        organismConverter = new OrganismConverter(institution);
        confidenceConverter = new ParticipantConfidenceConverter( institution);
    }

    public ParticipantConverter(Institution institution, InteractionConverter interactionConverter, ExperimentConverter expConverter) {
        super(institution, Component.class, psidev.psi.mi.xml.model.Participant.class);

        if (interactionConverter != null){
            this.interactionConverter = interactionConverter;
        }
        else {
            this.interactionConverter = new InteractionConverter(institution, expConverter, this);
        }

        this.participantParameterConverter = new ParticipantParameterConverter(institution, expConverter);
        this.experimentalRoleConverter = new ExperimentalRoleConverter( institution );
        this.biologicalRoleConverter = new BiologicalRoleConverter(institution);
        this.interactorConverter = new InteractorConverter(institution);
        pimConverter = new ParticipantIdentificationMethodConverter(institution);
        epConverter = new CvObjectConverter<CvExperimentalPreparation, ExperimentalPreparation>(institution, CvExperimentalPreparation.class, ExperimentalPreparation.class);
        featureConverter = new FeatureConverter(institution);
        organismConverter = new OrganismConverter(institution);
        confidenceConverter = new ParticipantConfidenceConverter( institution);
    }

    public Component psiToIntact(Participant psiObject) {

        // the interaction in the participant is a complex interactor, not an 'interaction'...
        // TODO being able to convert interactions as participants
        //Interaction interaction = interactionConverter.psiToIntact(psiObject.getInteraction());
        psiStartConversion(psiObject);
        Component component = newComponent(psiObject);

        psiEndConversion(psiObject);
        return component;
    }

    public Participant intactToPsi(Component intactObject) {
        Participant participant = super.intactToPsi(intactObject);

        intactStartConversation(intactObject);

        Collection<CvExperimentalRole> experimentalRoles;
        Collection<CvIdentification> partIdentMethods;
        Collection<CvExperimentalPreparation> expPreparations;
        Collection<Feature> features;
        Collection<ComponentConfidence> confs;
        Collection<uk.ac.ebi.intact.model.ComponentParameter> params;

        if (isCheckInitializedCollections()){
            experimentalRoles = IntactCore.ensureInitializedExperimentalRoles(intactObject);
            partIdentMethods = IntactCore.ensureInitializedParticipantIdentificationMethods(intactObject);
            expPreparations = IntactCore.ensureInitializedExperimentalPreparations(intactObject);
            features = IntactCore.ensureInitializedFeatures(intactObject);
            confs = IntactCore.ensureInitializedComponentConfidences(intactObject);
            params = IntactCore.ensureInitializedComponentParameters(intactObject);
        }
        else {
            experimentalRoles = intactObject.getExperimentalRoles();
            partIdentMethods = intactObject.getParticipantDetectionMethods();
            expPreparations = intactObject.getExperimentalPreparations();
            features = intactObject.getFeatures();
            confs = intactObject.getConfidences();
            params = intactObject.getParameters();
        }

        // Set id, annotations, xrefs and aliases
        PsiConverterUtils.populateId(participant);
        PsiConverterUtils.populateNames(intactObject, participant, aliasConverter);
        PsiConverterUtils.populateXref(intactObject, participant, xrefConverter);
        PsiConverterUtils.populateAttributes(intactObject, participant, annotationConverter);

        // experimental roles

        if (experimentalRoles.isEmpty()){
            log.error("Component without experimental roles : " + intactObject.getShortLabel());
        }
        else if (experimentalRoles.size() > 1){
            log.error("Component with "+experimentalRoles.size()+" experimental roles : " + intactObject.getShortLabel());
        }
        for ( CvExperimentalRole experimentalRole : experimentalRoles) {
            ExperimentalRole expRole = this.experimentalRoleConverter.intactToPsi(experimentalRole);
            participant.getExperimentalRoles().add( expRole );
        }

        // biological role
        if (intactObject.getCvBiologicalRole() == null) {
            throw new IllegalStateException("Found component without biological role: "+intactObject.getAc());
        }

        BiologicalRole bioRole = this.biologicalRoleConverter.intactToPsi(intactObject.getCvBiologicalRole());
        participant.setBiologicalRole(bioRole);

        // interactor converter

        // interactor is interaction
        if (intactObject.getInteractor() != null && intactObject.getInteractor() instanceof InteractionImpl){
            psidev.psi.mi.xml.model.Interaction interactor = interactionConverter.intactToPsi((InteractionImpl) intactObject.getInteractor());
            if( ConverterContext.getInstance().isGenerateExpandedXml() ) {
                participant.setInteraction(interactor);
            } else {
                participant.setInteractionRef(new InteractionRef(interactor.getId()));
            }
        }
        // normal interactor
        else if (intactObject.getInteractor() != null){
            psidev.psi.mi.xml.model.Interactor interactor = interactorConverter.intactToPsi(intactObject.getInteractor());
            if( ConverterContext.getInstance().isGenerateExpandedXml() ) {
                participant.setInteractor(interactor);
            } else {
                participant.setInteractorRef(new InteractorRef(interactor.getId()));
            }
        }
        else {
            log.error("Component without interactor " + intactObject.getShortLabel());
        }

        // participant identification methods

        for (CvIdentification participantDetectionMethod : partIdentMethods) {

            ParticipantIdentificationMethod participantIdentificationMethod = pimConverter.intactToPsi(participantDetectionMethod);

            participant.getParticipantIdentificationMethods().add(participantIdentificationMethod);
        }

        // experimental preparations

        for (CvExperimentalPreparation experimentalPreparation : expPreparations) {
            ExperimentalPreparation expPrep = epConverter.intactToPsi(experimentalPreparation);

            participant.getExperimentalPreparations().add(expPrep);
        }

        // features

        if (!features.isEmpty()) {
            for (Feature feature : features) {
                psidev.psi.mi.xml.model.Feature psiFeature = featureConverter.intactToPsi(feature);
                if(feature.getAc() != null){
                    featureMap.put(feature.getAc(), psiFeature);
                }
                participant.getFeatures().add(psiFeature);
            }
        }

        // expressed in
        if (intactObject.getExpressedIn() != null) {
            Organism organism = organismConverter.intactToPsi(intactObject.getExpressedIn());
            if (organism != null) {
                HostOrganism hostOrganism = new HostOrganism();
                hostOrganism.setNcbiTaxId(organism.getNcbiTaxId());
                hostOrganism.setNames(organism.getNames());
                hostOrganism.setCellType(organism.getCellType());
                hostOrganism.setCompartment(organism.getCompartment());
                hostOrganism.setTissue(organism.getTissue());

                participant.getHostOrganisms().add(hostOrganism);
            }
        }

        // confidences

        for (ComponentConfidence conf : confs){
            psidev.psi.mi.xml.model.Confidence confidence = confidenceConverter.intactToPsi( conf);
            participant.getConfidenceList().add( confidence);

            // in case of author-score and for retro-compatibility, we add an author-confidence annotation
            if (conf.getCvConfidenceType() != null && IntactConverterUtils.AUTHOR_SCORE.equalsIgnoreCase(conf.getCvConfidenceType().getShortLabel())){
                Attribute authConf = new Attribute(IntactConverterUtils.AUTH_CONF_MI, IntactConverterUtils.AUTH_CONF, conf.getValue());

                if (!participant.getAttributes().contains(authConf)){
                    participant.getAttributes().add(authConf);
                }
            }
        }

        // if we had author-conf annotations, we convert them in confidences also
        Collection<Annotation> annotationsToConvertInConfidence = IntactConverterUtils.extractAuthorConfidencesAnnotationsFrom(intactObject.getAnnotations());
        for (Annotation ann : annotationsToConvertInConfidence){
            if (ann.getAnnotationText() != null){
                psidev.psi.mi.xml.model.Confidence confidence = new psidev.psi.mi.xml.model.Confidence();
                confidence.setValue(ann.getAnnotationText());

                Names names = new Names();
                names.setFullName(IntactConverterUtils.AUTH_CONF);
                names.setShortLabel(IntactConverterUtils.AUTH_CONF_MI);

                psidev.psi.mi.xml.model.Xref xref = new psidev.psi.mi.xml.model.Xref();
                xref.setPrimaryRef(new DbReference(CvDatabase.PSI_MI, CvDatabase.PSI_MI_MI_REF, IntactConverterUtils.AUTH_CONF_MI, CvXrefQualifier.IDENTITY, CvXrefQualifier.IDENTITY_MI_REF));
                Unit unit = new Unit();
                unit.setNames(names);
                unit.setXref(xref);

                confidence.setUnit(unit);

                if (!participant.getConfidenceList().contains(confidence)){
                    participant.getConfidenceList().add( confidence);
                }
            }
        }

        // component parameters

        for (uk.ac.ebi.intact.model.ComponentParameter param : params){
            psidev.psi.mi.xml.model.Parameter parameter = participantParameterConverter.intactToPsi(param);
            participant.getParameters().add(parameter);
        }

        // stoichiometry
        if (intactObject.getStoichiometry() > 0.0f){
            Attribute attribute = new Attribute(CvTopic.COMMENT_MI_REF, CvTopic.COMMENT, STOICHIOMETRY_PREFIX + intactObject.getStoichiometry());

            if (!participant.getAttributes().contains( attribute )) {
                participant.getAttributes().add( attribute );
            }
        }

        intactEndConversion(intactObject);
        return participant;
    }

    private Component newComponent(Participant participant) {

        Component component = new Component();

        // author confidence annotations to migrate to componentConfidences later
        Collection<Attribute> annotationConfidencesToMigrate = IntactConverterUtils.extractAuthorConfidencesFrom(participant.getAttributes());

        // all other attributes will be converted into annotations
        Collection<Attribute> attributesToConvert = CollectionUtils.subtract(participant.getAttributes(), annotationConfidencesToMigrate);

        // populates names, xrefs and attributes
        IntactConverterUtils.populateNames(participant.getNames(), component);
        IntactConverterUtils.populateXref(participant.getXref(), component, new XrefConverter<ComponentXref>(getInstitution(), ComponentXref.class));
        IntactConverterUtils.populateAnnotations(attributesToConvert, component, getInstitution());

        component.setOwner(getInstitution());

        Interactor interactor = null;
        // interactor is participant
        if (participant.getInteractor() != null){
            interactor = this.interactorConverter.psiToIntact(participant.getInteractor());
        }
        // interaction is participant
        else if (participant.getInteraction() != null){
            interactor = this.interactionConverter.psiToIntact(participant.getInteraction());
        }
        else {
            log.error("Participant without interactor : " + component.getShortLabel());
        }
        component.setInteractor(interactor);

        // biological role
        BiologicalRole psiBioRole = participant.getBiologicalRole();
        if (psiBioRole == null) {
            psiBioRole = PsiConverterUtils.createUnspecifiedBiologicalRole();
        }
        CvBiologicalRole biologicalRole = this.biologicalRoleConverter.psiToIntact(psiBioRole);
        component.setCvBiologicalRole(biologicalRole);

        // we have more than one experimental role
        if (participant.getExperimentalRoles().size() > 1) {
            throw new UnsupportedConversionException("Cannot convert participants with more than one experimental role: "+participant);
        }

        // only the first experimental role
        Collection<ExperimentalRole> roles = new ArrayList<ExperimentalRole>(2);

        if (participant.getExperimentalRoles().isEmpty()) {
            if (log.isWarnEnabled()) log.warn("Participant without experimental roles: " + participant);

            roles.add(PsiConverterUtils.createUnspecifiedExperimentalRole());
        } else {
            roles = participant.getExperimentalRoles();
        }

        for (ExperimentalRole role : roles) {
            CvExperimentalRole experimentalRole = this.experimentalRoleConverter.psiToIntact(role);
            component.getExperimentalRoles().add(experimentalRole);
        }

        // converts features
        for (psidev.psi.mi.xml.model.Feature psiFeature : participant.getFeatures()) {
            Feature feature = featureConverter.psiToIntact(psiFeature);
            component.addFeature(feature);

            if (interactor instanceof Polymer){
                Polymer polymer = (Polymer) interactor;
                String sequence = polymer.getSequence();

                if (sequence != null){
                    for (Range r : feature.getRanges()){

                        r.prepareSequence(polymer.getSequence());
                    }
                }
            }
        }

        // converts participant identification methods
        for (ParticipantIdentificationMethod pim : participant.getParticipantIdentificationMethods()) {
            CvIdentification cvIdentification = pimConverter.psiToIntact(pim);
            component.getParticipantDetectionMethods().add(cvIdentification);
        }

        // converts experimental preparations
        for (ExperimentalPreparation expPrep : participant.getExperimentalPreparations()) {
            CvExperimentalPreparation cvExpPrep = epConverter.psiToIntact(expPrep);
            component.getExperimentalPreparations().add(cvExpPrep);
        }

        // converts host organism
        if (!participant.getHostOrganisms().isEmpty()) {
            HostOrganism hostOrganism = participant.getHostOrganisms().iterator().next();
            Organism organism = new Organism();
            organism.setNcbiTaxId(hostOrganism.getNcbiTaxId());
            organism.setNames(hostOrganism.getNames());
            organism.setCellType(hostOrganism.getCellType());
            organism.setCompartment(hostOrganism.getCompartment());
            organism.setTissue(hostOrganism.getTissue());

            BioSource bioSource = organismConverter.psiToIntact(organism);
            component.setExpressedIn(bioSource);
        }

        // converts confidences
        for (psidev.psi.mi.xml.model.Confidence psiConfidence :  participant.getConfidenceList()){
            ComponentConfidence confidence = confidenceConverter.psiToIntact( psiConfidence );
            component.addConfidence( confidence);
        }
        // converts author-confidences
        for (Attribute authorConf : annotationConfidencesToMigrate){

            String value = authorConf.getValue();
            ComponentConfidence confidence = confidenceConverter.newConfidenceInstance(value);

            CvConfidenceType cvConfType = new CvConfidenceType();
            cvConfType.setOwner(confidenceConverter.getInstitution());
            cvConfType.setShortLabel(IntactConverterUtils.AUTHOR_SCORE);
            confidence.setCvConfidenceType( cvConfType);

            if (!component.getConfidences().contains(confidence)){
                component.addConfidence( confidence);
            }
        }

        // converts parameters
        for (psidev.psi.mi.xml.model.Parameter psiParameter : participant.getParameters()){
            ComponentParameter parameter = participantParameterConverter.psiToIntact( psiParameter );
            component.addParameter(parameter);
        }

        if (!participant.getExperimentalInteractors().isEmpty()){
            log.warn("Participant with " + participant.getExperimentalInteractors().size() + " experimental interactors : " + component.getShortLabel() + ". We do not export them in Intact");
        }

        // stoichiometry
        if (participant.hasAttributes()){
            for (Attribute a : participant.getAttributes()){
                if (a.getNameAc() != null){
                    if (a.getNameAc().equals(CvTopic.COMMENT_MI_REF) && a.getValue() != null){
                        if (a.getValue().startsWith(STOICHIOMETRY_PREFIX)){
                            try {
                                float stoichio = Float.parseFloat(a.getValue().substring(a.getValue().indexOf(STOICHIOMETRY_PREFIX) + STOICHIOMETRY_PREFIX.length()));
                                component.setStoichiometry(stoichio);
                            }
                            catch (NumberFormatException e) {
                                log.error(e);
                            }
                        }
                    }
                }
            }
        }

//        ConfidenceConverter confConverter= new ConfidenceConverter( institution );
//        for (psidev.psi.mi.xml.model.Confidence psiConfidence :  participant.getConfidenceList()){
//           Confidence confidence = confConverter.psiToIntact( psiConfidence );
//            component.Confidence( confidence);
//        }

        return component;
    }

    public Map<String, psidev.psi.mi.xml.model.Feature> getFeatureMap() {
        return featureMap;
    }

    @Override
    public void setInstitution(Institution institution)
    {
        super.setInstitution(institution);

        this.interactionConverter.setInstitution(institution, true, false, getInstitutionPrimaryId());

        this.experimentalRoleConverter.setInstitution(institution, getInstitutionPrimaryId());
        this.biologicalRoleConverter.setInstitution(institution, getInstitutionPrimaryId());
        this.interactorConverter.setInstitution(institution, getInstitutionPrimaryId());
        pimConverter.setInstitution(institution, getInstitutionPrimaryId());
        epConverter.setInstitution(institution, getInstitutionPrimaryId());
        featureConverter.setInstitution(institution, getInstitutionPrimaryId());
        organismConverter.setInstitution(institution, getInstitutionPrimaryId());
        confidenceConverter.setInstitution(institution, getInstitutionPrimaryId());
        participantParameterConverter.setInstitution(institution, getInstitutionPrimaryId());
    }

    public void setInstitution(Institution institution, boolean setExperimentInstitution, boolean setInteractionInstitution)
    {
        super.setInstitution(institution);

        if (setInteractionInstitution){
            this.interactionConverter.setInstitution(institution, true, false, getInstitutionPrimaryId());
        }

        this.experimentalRoleConverter.setInstitution(institution, getInstitutionPrimaryId());
        this.biologicalRoleConverter.setInstitution(institution, getInstitutionPrimaryId());
        this.interactorConverter.setInstitution(institution, getInstitutionPrimaryId());
        pimConverter.setInstitution(institution, getInstitutionPrimaryId());
        epConverter.setInstitution(institution, getInstitutionPrimaryId());
        featureConverter.setInstitution(institution, getInstitutionPrimaryId());
        organismConverter.setInstitution(institution, getInstitutionPrimaryId());
        confidenceConverter.setInstitution(institution, getInstitutionPrimaryId());
        participantParameterConverter.setInstitution(institution, setExperimentInstitution, getInstitutionPrimaryId());
    }

    public void setInstitution(Institution institution, boolean setExperimentInstitution, boolean setInteractionInstitution, String institutionPrimaryId)
    {
        super.setInstitution(institution, institutionPrimaryId);

        if (setInteractionInstitution){
            this.interactionConverter.setInstitution(institution, setExperimentInstitution, false, getInstitutionPrimaryId());
        }

        this.experimentalRoleConverter.setInstitution(institution, getInstitutionPrimaryId());
        this.biologicalRoleConverter.setInstitution(institution, getInstitutionPrimaryId());
        this.interactorConverter.setInstitution(institution, getInstitutionPrimaryId());
        pimConverter.setInstitution(institution, getInstitutionPrimaryId());
        epConverter.setInstitution(institution, getInstitutionPrimaryId());
        featureConverter.setInstitution(institution, getInstitutionPrimaryId());
        organismConverter.setInstitution(institution, getInstitutionPrimaryId());
        confidenceConverter.setInstitution(institution, getInstitutionPrimaryId());
        participantParameterConverter.setInstitution(institution, setExperimentInstitution, getInstitutionPrimaryId());
    }

    @Override
    public void setInstitution(Institution institution, String institId){
        super.setInstitution(institution, institId);

        this.interactionConverter.setInstitution(institution, true, false, institId);

        this.experimentalRoleConverter.setInstitution(institution, institId);
        this.biologicalRoleConverter.setInstitution(institution, institId);
        this.interactorConverter.setInstitution(institution, institId);
        pimConverter.setInstitution(institution, institId);
        epConverter.setInstitution(institution, institId);
        featureConverter.setInstitution(institution, institId);
        organismConverter.setInstitution(institution, institId);
        confidenceConverter.setInstitution(institution, institId);
        participantParameterConverter.setInstitution(institution, institId);

    }

    @Override
    public void setCheckInitializedCollections(boolean check){
        super.setCheckInitializedCollections(check);
        this.interactionConverter.setCheckInitializedCollections(check);
        this.experimentalRoleConverter.setCheckInitializedCollections(check);
        this.interactorConverter.setCheckInitializedCollections(check);
        this.pimConverter.setCheckInitializedCollections(check);
        this.epConverter.setCheckInitializedCollections(check);
        this.featureConverter.setCheckInitializedCollections(check);
        this.organismConverter.setCheckInitializedCollections(check);
        this.confidenceConverter.setCheckInitializedCollections(check);
        this.biologicalRoleConverter.setCheckInitializedCollections(check);
        this.participantParameterConverter.setCheckInitializedCollections(check);
    }

    public void setCheckInitializedCollections(boolean check, boolean initializeExperiment, boolean initialiseInteraction){
        super.setCheckInitializedCollections(check);
        if (initialiseInteraction){
            this.interactionConverter.setCheckInitializedCollections(check, initializeExperiment, false);
        }
        this.experimentalRoleConverter.setCheckInitializedCollections(check);
        this.interactorConverter.setCheckInitializedCollections(check);
        this.pimConverter.setCheckInitializedCollections(check);
        this.epConverter.setCheckInitializedCollections(check);
        this.featureConverter.setCheckInitializedCollections(check);
        this.organismConverter.setCheckInitializedCollections(check);
        this.confidenceConverter.setCheckInitializedCollections(check);
        this.biologicalRoleConverter.setCheckInitializedCollections(check);
        this.participantParameterConverter.setCheckInitializedCollections(check, initializeExperiment);
    }

        private int processSpecializedAnnotations( Collection<Annotation> annotations, psidev.psi.mi.xml.model.Participant participant) {
        // set boolean values

        final Iterator<Annotation> it = annotations.iterator();

        if (!it.hasNext()){
            return 0;
        }

        int numberOfAuthConf = 0;

        while ( it.hasNext() ) {
            Annotation annotation = it.next();
            if (IntactConverterUtils.AUTH_CONF_MI.equals(annotation.getCvTopic().getIdentifier())){
                if (annotation.getAnnotationText() != null){
                    psidev.psi.mi.xml.model.Confidence confidence = new psidev.psi.mi.xml.model.Confidence();
                    confidence.setValue(annotation.getAnnotationText());

                    Names names = new Names();
                    names.setFullName(IntactConverterUtils.AUTH_CONF);
                    names.setShortLabel(IntactConverterUtils.AUTH_CONF_MI);

                    psidev.psi.mi.xml.model.Xref xref = new psidev.psi.mi.xml.model.Xref();
                    xref.setPrimaryRef(new DbReference(CvDatabase.PSI_MI, CvDatabase.PSI_MI_MI_REF, IntactConverterUtils.AUTH_CONF_MI, CvXrefQualifier.IDENTITY, CvXrefQualifier.IDENTITY_MI_REF));
                    Unit unit = new Unit();
                    unit.setNames(names);
                    unit.setXref(xref);

                    confidence.setUnit(unit);

                    if (!participant.getConfidenceList().contains(confidence)){
                        participant.getConfidenceList().add( confidence);
                        numberOfAuthConf ++;
                    }
                }
            }
        }

        return numberOfAuthConf;
    }
}