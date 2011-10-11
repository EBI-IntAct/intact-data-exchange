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
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.xml.model.*;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.UnsupportedConversionException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.*;
import uk.ac.ebi.intact.model.Alias;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.Range;
import uk.ac.ebi.intact.model.Xref;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

/**
 * Intact Converter Utils.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactConverterUtils {

    private static final Log log = LogFactory.getLog(IntactConverterUtils.class);
    public static final String AUTHOR_SCORE = "author-score";
    public static final String AUTH_CONF_MI = "MI:0621";
    public static final String AUTH_CONF = "author-confidence";

    private IntactConverterUtils() {
    }
    public static void populateNames(Names names, AnnotatedObject<?, ?> annotatedObject) {
        String shortLabel = getShortLabelFromNames(names);

        if (names == null && (annotatedObject instanceof Experiment) ) {
            shortLabel = createExperimentTempShortLabel();
        }

        if ( ! ( annotatedObject instanceof Institution ) ) {
            if ( shortLabel != null ) {
                shortLabel = shortLabel.toLowerCase();
            }
        }

        annotatedObject.setShortLabel(shortLabel);

        if (names != null) {
            annotatedObject.setFullName(names.getFullName());

            Class<?> aliasClass = AnnotatedObjectUtils.getAliasClassType(annotatedObject.getClass());
            AliasConverter aliasConverter = new AliasConverter(getInstitution(annotatedObject), aliasClass);

            populateAliases(names.getAliases(), annotatedObject, aliasConverter);
        }
    }

    public static void populateNames(Names names, AnnotatedObject<?, ?> annotatedObject, AliasConverter aliasConverter) {
        String shortLabel = getShortLabelFromNames(names);

        if (names == null && (annotatedObject instanceof Experiment) ) {
            shortLabel = createExperimentTempShortLabel();
        }

        if ( ! ( annotatedObject instanceof Institution ) ) {
            if ( shortLabel != null ) {
                shortLabel = shortLabel.toLowerCase();
            }
        }

        annotatedObject.setShortLabel(shortLabel);

        if (names != null) {
            annotatedObject.setFullName(names.getFullName());

            if (aliasConverter == null){
                Class<?> aliasClass = AnnotatedObjectUtils.getAliasClassType(annotatedObject.getClass());
                aliasConverter = new AliasConverter(getInstitution(annotatedObject), aliasClass);
            }

            populateAliases(names.getAliases(), annotatedObject, aliasConverter);
        }
    }

    public static <X extends Xref> void populateXref(psidev.psi.mi.xml.model.Xref psiXref, AnnotatedObject<X, ?> annotatedObject, XrefConverter<X> xrefConverter) {
        if (psiXref == null) {
            return;
        }

        if (psiXref.getPrimaryRef() != null) {
            addXref(psiXref.getPrimaryRef(), annotatedObject, xrefConverter);
        }

        for (DbReference secondaryRef : psiXref.getSecondaryRef()) {
            addXref(secondaryRef, annotatedObject, xrefConverter);
        }
    }

    private static <X extends Xref> void addXref(DbReference dbReference, AnnotatedObject<X, ?> annotatedObject, XrefConverter<X> xrefConverter) {
        X xref = xrefConverter.psiToIntact(dbReference);
        annotatedObject.addXref(xref);

        if (annotatedObject instanceof Institution) {
            xref.setOwner((Institution)annotatedObject);
        } else if (xref instanceof CvObjectXref) {
            ((CvObjectXref)xref).prepareParentMi();
        }
    }

    public static <A extends Alias> void populateAliases(Collection<psidev.psi.mi.xml.model.Alias> psiAliases, AnnotatedObject<?, A> annotatedObject, AliasConverter<A> aliasConverter) {
        if (psiAliases == null) {
            return;
        }

        for (psidev.psi.mi.xml.model.Alias psiAlias : psiAliases) {
            if (psiAlias.hasValue()) {
                A alias = aliasConverter.psiToIntact(psiAlias);
                annotatedObject.addAlias(alias);

                if (annotatedObject instanceof Institution) {
                    alias.setOwner((Institution) annotatedObject);
                }
            } else {
                if (log.isWarnEnabled()) log.warn("Alias without value in location: "+ ConverterContext.getInstance().getLocation().getCurrentLocation().pathFromRootAsString());
            }
        }
    }

    public static void populateAnnotations(AttributeContainer attributeContainer, Annotated annotated, Institution institution, AnnotationConverter annotationConverter) {

        if (annotationConverter == null){
            annotationConverter = new AnnotationConverter(institution);
        }

        if (attributeContainer.hasAttributes()) {
            for (Attribute attribute : attributeContainer.getAttributes()) {
                Annotation annotation = annotationConverter.psiToIntact(attribute);
                annotation.setOwner(institution);

                if (!annotated.getAnnotations().contains(annotation)) {
                    annotated.getAnnotations().add(annotation);
                }
            }
        }
    }

    public static void populateAnnotations(Collection<Attribute> attributesToConvert, Annotated annotated, Institution institution, AnnotationConverter annotationConverter) {
        if (annotationConverter == null){
            annotationConverter = new AnnotationConverter(institution);
        }

        if (!attributesToConvert.isEmpty()) {
            for (Attribute attribute : attributesToConvert) {
                Annotation annotation = annotationConverter.psiToIntact(attribute);
                annotation.setOwner(institution);

                if (!annotated.getAnnotations().contains(annotation)) {
                    annotated.getAnnotations().add(annotation);
                }
            }
        }
    }

        public static void populateAnnotations(AttributeContainer attributeContainer, Annotated annotated, Institution institution) {
        AnnotationConverter annotationConverter = new AnnotationConverter(institution);

        if (attributeContainer.hasAttributes()) {
            for (Attribute attribute : attributeContainer.getAttributes()) {
                Annotation annotation = annotationConverter.psiToIntact(attribute);
                annotation.setOwner(institution);

                if (!annotated.getAnnotations().contains(annotation)) {
                    annotated.getAnnotations().add(annotation);
                }
            }
        }
    }

    public static void populateAnnotations(Collection<Attribute> attributesToConvert, Annotated annotated, Institution institution) {
        AnnotationConverter annotationConverter = new AnnotationConverter(institution);

        if (!attributesToConvert.isEmpty()) {
            for (Attribute attribute : attributesToConvert) {
                Annotation annotation = annotationConverter.psiToIntact(attribute);
                annotation.setOwner(institution);

                if (!annotated.getAnnotations().contains(annotation)) {
                    annotated.getAnnotations().add(annotation);
                }
            }
        }
    }

    public static CvXrefQualifier createCvXrefQualifier(Institution institution, DbReference dbReference) {
        String xrefType = dbReference.getRefType();
        CvXrefQualifier xrefQual = null;

        if (xrefType != null) {
            xrefQual = new CvXrefQualifier(institution, xrefType);
        }

        return xrefQual;
    }

    public static String getShortLabelFromNames(Names names) {
        if ( names == null || names.getShortLabel() == null ) {
            return IntactConverterUtils.createTempShortLabel();
        }

        String shortLabel = names.getShortLabel();
        String fullName = names.getFullName();

        // If the short label is null, but not the full name, use the full name as short label.
        // Truncate the full name if its length > SHORT_LABEL_LENGTH
        if (shortLabel == null) {
            if (fullName != null) {
                if (log.isWarnEnabled()) log.warn("Short label is null. Using full name as short label: " + fullName);
                shortLabel = fullName;
            } else {
                throw new NullPointerException("Both fullName and shortLabel are null");
            }
        }

        if (shortLabel.length() > AnnotatedObject.MAX_SHORT_LABEL_LEN) {
            shortLabel = shortLabel.substring(0, AnnotatedObject.MAX_SHORT_LABEL_LEN);

            if (log.isWarnEnabled()) {
                String msg = "\tFull name to short label truncated to: '" + shortLabel+"'";
                if (ConverterContext.getInstance().getLocation() != null && ConverterContext.getInstance().getLocation().getCurrentLocation() != null) {
                    msg = msg + " in location: "+ ConverterContext.getInstance().getLocation().getCurrentLocation().pathFromRootAsString();
                }
                log.warn(msg);
            }
        }

        return shortLabel;
    }

    public static String createTempShortLabel() {
        return AnnotatedObjectUtils.TEMP_LABEL_PREFIX + Math.abs(new Random().nextInt());
    }

    public static String createExperimentTempShortLabel() {
        return new IntactMockBuilder().randomString(5)+"-0000";
    }

    @Deprecated
    public static boolean isTempShortLabel(String label) {
        return AnnotatedObjectUtils.isTemporaryLabel( label );
    }

    protected static Institution getInstitution(AnnotatedObject ao) {
        if (ao instanceof Institution) {
            return (Institution)ao;
        } else {
            return ao.getOwner();
        }
    }

    public static Component newComponent(Institution institution, Participant participant, uk.ac.ebi.intact.model.Interaction interaction) {

        Interactor interactor = new InteractorConverter(institution).psiToIntact(participant.getInteractor());

        BiologicalRole psiBioRole = participant.getBiologicalRole();
        if (psiBioRole == null) {
            psiBioRole = PsiConverterUtils.createUnspecifiedBiologicalRole();
        }
        CvBiologicalRole biologicalRole = new BiologicalRoleConverter(institution).psiToIntact(psiBioRole);

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

        Collection<CvExperimentalRole> intactExpRoles = new ArrayList<CvExperimentalRole>(roles.size());

        for (ExperimentalRole role : roles) {
            CvExperimentalRole experimentalRole = new ExperimentalRoleConverter(institution).psiToIntact(role);
            intactExpRoles.add(experimentalRole);
        }

        Component component = new Component(institution, interaction, interactor, intactExpRoles.iterator().next(), biologicalRole);

        // author confidence annotations to migrate to componentConfidences later
        Collection<Attribute> annotationConfidencesToMigrate = extractAuthorConfidencesFrom(participant.getAttributes());

        // all other attributes will be converted into annotations
        Collection<Attribute> attributesToConvert = CollectionUtils.subtract(participant.getAttributes(), annotationConfidencesToMigrate);

        IntactConverterUtils.populateNames(participant.getNames(), component);
        IntactConverterUtils.populateXref(participant.getXref(), component, new XrefConverter<ComponentXref>(institution, ComponentXref.class));
        IntactConverterUtils.populateAnnotations(attributesToConvert, component, institution);

        component.setExperimentalRoles(intactExpRoles);

        FeatureConverter featureConverter = new FeatureConverter(institution);

        for (psidev.psi.mi.xml.model.Feature psiFeature : participant.getFeatures()) {
            Feature feature = featureConverter.psiToIntact(psiFeature);
            component.getBindingDomains().add(feature);
            feature.setComponent(component);

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

        for (ParticipantIdentificationMethod pim : participant.getParticipantIdentificationMethods()) {
            ParticipantIdentificationMethodConverter pimConverter = new ParticipantIdentificationMethodConverter(institution);
            CvIdentification cvIdentification = pimConverter.psiToIntact(pim);
            component.getParticipantDetectionMethods().add(cvIdentification);
        }

        for (ExperimentalPreparation expPrep : participant.getExperimentalPreparations()) {
            CvObjectConverter<CvExperimentalPreparation, ExperimentalPreparation> epConverter =
                    new CvObjectConverter<CvExperimentalPreparation, ExperimentalPreparation>(institution, CvExperimentalPreparation.class, ExperimentalPreparation.class);
            CvExperimentalPreparation cvExpPrep = epConverter.psiToIntact(expPrep);
            component.getExperimentalPreparations().add(cvExpPrep);
        }

        if (!participant.getHostOrganisms().isEmpty()) {
            HostOrganism hostOrganism = participant.getHostOrganisms().iterator().next();
            Organism organism = new Organism();
            organism.setNcbiTaxId(hostOrganism.getNcbiTaxId());
            organism.setNames(hostOrganism.getNames());
            organism.setCellType(hostOrganism.getCellType());
            organism.setCompartment(hostOrganism.getCompartment());
            organism.setTissue(hostOrganism.getTissue());

            BioSource bioSource = new OrganismConverter(institution).psiToIntact(organism);
            component.setExpressedIn(bioSource);
        }

        ParticipantConfidenceConverter confConverter= new ParticipantConfidenceConverter( institution);
        for (psidev.psi.mi.xml.model.Confidence psiConfidence :  participant.getConfidenceList()){
            ComponentConfidence confidence = confConverter.psiToIntact( psiConfidence );
            component.addConfidence( confidence);
        }
        for (Attribute authorConf : annotationConfidencesToMigrate){

            String value = authorConf.getValue();
            ComponentConfidence confidence = confConverter.newConfidenceInstance(value);

            CvConfidenceType cvConfType = new CvConfidenceType();
            cvConfType.setOwner(confConverter.getInstitution());
            cvConfType.setShortLabel(AUTHOR_SCORE);
            confidence.setCvConfidenceType( cvConfType);

            component.addConfidence( confidence);
        }

        ParticipantParameterConverter paramConverter= new ParticipantParameterConverter(institution);
        for (psidev.psi.mi.xml.model.Parameter psiParameter : participant.getParameters()){
            ComponentParameter parameter = paramConverter.psiToIntact( psiParameter );
            component.addParameter(parameter);
        }

//        ConfidenceConverter confConverter= new ConfidenceConverter( institution );
//        for (psidev.psi.mi.xml.model.Confidence psiConfidence :  participant.getConfidenceList()){
//           Confidence confidence = confConverter.psiToIntact( psiConfidence );
//            component.Confidence( confidence);
//        }

        return component;
    }

    public static Collection<Attribute> extractAuthorConfidencesFrom(Collection<Attribute> attributes){
        if (attributes != null && !attributes.isEmpty()){
            Collection<Attribute> attributesConf = new ArrayList<Attribute>(attributes.size());
            for (Attribute att : attributes){
                if (att.getNameAc() != null){
                    if (att.getNameAc().equals(AUTH_CONF_MI)){
                        attributesConf.add(att);
                    }
                }
                else if (att.getName() != null){
                    if (att.getName().equals(AUTH_CONF)){
                        attributesConf.add(att);
                    }
                }
            }

            return attributesConf;
        }

        return Collections.EMPTY_LIST;
    }

    public static Collection<Annotation> extractAuthorConfidencesAnnotationsFrom(Collection<Annotation> annotations){
        if (annotations != null && !annotations.isEmpty()){
            Collection<Annotation> attributesConf = new ArrayList<Annotation>(annotations.size());
            for (Annotation att : annotations){
                if (att.getCvTopic() != null){
                    if (AUTH_CONF_MI.equals(att.getCvTopic().getIdentifier())){
                        attributesConf.add(att);
                    }
                }
            }

            return attributesConf;
        }

        return Collections.EMPTY_LIST;
    }
}