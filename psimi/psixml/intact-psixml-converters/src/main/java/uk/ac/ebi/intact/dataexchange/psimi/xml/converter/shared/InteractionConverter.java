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
import org.obo.datamodel.OBOSession;
import psidev.psi.mi.xml.model.*;
import psidev.psi.mi.xml.model.Feature;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.dataexchange.cvutils.CvUtils;
import uk.ac.ebi.intact.dataexchange.cvutils.OboUtils;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvObjectOntologyBuilder;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.InconsistentConversionException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.MessageLevel;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.PsiConversionException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.ConversionCache;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Confidence;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Xref;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.XrefUtils;

import java.util.*;

/**
 * Interaction Converter.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionConverter extends AbstractAnnotatedObjectConverter<Interaction, psidev.psi.mi.xml.model.Interaction> {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(InteractionConverter.class);

    private static ThreadLocal<List<CvDagObject>> ontology = new ThreadLocal<List<CvDagObject>>();
    public static final String MODELLED = "modelled";
    public static final String INTRA_MOLECULAR = "intra-molecular";
    public static final String NEGATIVE = "negative";
    protected static final String PUTATIVE_SELF_PSI_REF = "MI:0898";


    private static final String TRUE = "true";

    private ConfidenceConverter confConverter;
    private InteractionParameterConverter paramConverter;
    private ExperimentConverter experimentConverter;
    private ParticipantConverter participantConverter;
    private InteractionTypeConverter interactionTypeConverter;

    private static List<CvDagObject> getCurrentOntology() {
        if (ontology.get() == null) {
            if (log.isDebugEnabled()) log.debug("Initializing Intact ontology lazily");
            try {
                final OBOSession oboSession = OboUtils.createOBOSessionFromLatestMi();
                CvObjectOntologyBuilder builder = new CvObjectOntologyBuilder(oboSession);
                ontology.set(new ArrayList<CvDagObject>(builder.getAllValidCvs()));
            } catch (Exception e) {
                throw new IllegalStateException("Could not load ontology");
            }
        }

        return ontology.get();
    }

    public InteractionConverter(Institution institution) {
        super(institution, InteractionImpl.class, psidev.psi.mi.xml.model.Interaction.class);
        confConverter= new ConfidenceConverter( institution);
        experimentConverter = new ExperimentConverter(institution);
        paramConverter= new InteractionParameterConverter( institution, experimentConverter);
        participantConverter = new ParticipantConverter(institution, this, experimentConverter);
        interactionTypeConverter = new InteractionTypeConverter(institution);
    }

    public InteractionConverter(Institution institution, ExperimentConverter expConverter) {
        super(institution, InteractionImpl.class, psidev.psi.mi.xml.model.Interaction.class);
        confConverter= new ConfidenceConverter( institution);
        if (expConverter != null){
            experimentConverter = expConverter;
        }
        else {
            experimentConverter = new ExperimentConverter(institution);
        }
        paramConverter= new InteractionParameterConverter( institution, experimentConverter);
        participantConverter = new ParticipantConverter(institution, this, experimentConverter);
        interactionTypeConverter = new InteractionTypeConverter(institution);
    }

    public InteractionConverter(Institution institution, ParticipantConverter partConveter) {
        super(institution, InteractionImpl.class, psidev.psi.mi.xml.model.Interaction.class);
        confConverter= new ConfidenceConverter( institution);
        experimentConverter = new ExperimentConverter(institution);
        paramConverter= new InteractionParameterConverter( institution, experimentConverter);
        if (partConveter != null){
            participantConverter = partConveter;
        }
        else {
            participantConverter = new ParticipantConverter(institution, this, experimentConverter);
        }
        interactionTypeConverter = new InteractionTypeConverter(institution);
    }

    public InteractionConverter(Institution institution, ExperimentConverter expConverter, ParticipantConverter partConveter) {
        super(institution, InteractionImpl.class, psidev.psi.mi.xml.model.Interaction.class);
        confConverter= new ConfidenceConverter( institution);
        if (expConverter != null){
            experimentConverter = expConverter;
        }
        else {
            experimentConverter = new ExperimentConverter(institution);
        }
        paramConverter= new InteractionParameterConverter( institution, experimentConverter);
        if (partConveter != null){
            participantConverter = partConveter;
        }
        else {
            participantConverter = new ParticipantConverter(institution, this, experimentConverter);
        }
        interactionTypeConverter = new InteractionTypeConverter(institution);
    }

    public Interaction psiToIntact(psidev.psi.mi.xml.model.Interaction psiObject) {
        Interaction interaction = super.psiToIntact(psiObject);

        if (!isNewIntactObjectCreated()) {
            return interaction;
        }

        psiStartConversion(psiObject);

        int numberOfAuthoConfToConvert = 0;

        try {

            // This has to be before anything else (e.g. when creating xrefs)
            interaction.setOwner(getInstitution());

            // set the names, xrefs and annotations
            IntactConverterUtils.populateNames(psiObject.getNames(), interaction, aliasConverter);
            IntactConverterUtils.populateXref(psiObject.getXref(), interaction, xrefConverter);
            IntactConverterUtils.populateAnnotations(psiObject, interaction, getInstitution(), annotationConverter);

            // collect experiments
            Collection<Experiment> experiments = getExperiments(psiObject);
            interaction.getExperiments().addAll(experiments);

            // only gets the first interaction type
            CvInteractionType interactionType = getInteractionType(psiObject);
            interaction.setCvInteractionType(interactionType);

            // interactor type is always "interaction" for interactions
            CvInteractorType interactorType = CvObjectUtils.createCvObject(getInstitution(), CvInteractorType.class, CvInteractorType.INTERACTION_MI_REF, CvInteractorType.INTERACTION);
            interaction.setCvInteractorType(interactorType);

            // imexId
            String imexId = psiObject.getImexId();
            if (imexId != null && !alreadyContainsImexXref(interaction)) {
                final InteractorXref imexXref = createImexXref(interaction, imexId);
                interaction.addXref(imexXref);
            }

            // note: with the first IMEx conversions, the source-database xref was wrongly
            // marked as an "identity" xref instead of source-database. This is meant to fix that on the fly
            if (ConverterContext.getInstance().getInteractionConfig().isAutoFixSourceReferences()) {
                fixSourceReferenceXrefsIfNecessary(interaction);
            }

            // components, created after the interaction, as we need the interaction to create them
            Collection<Component> components = getComponents(interaction, psiObject);
            if (components.isEmpty()){
                log.error("Interaction without any participants : " + interaction.getShortLabel());
            }
            interaction.getComponents().addAll(components);

            // convert confidences
            for (psidev.psi.mi.xml.model.Confidence psiConfidence :  psiObject.getConfidences()){
                Confidence confidence = confConverter.psiToIntact( psiConfidence );

                interaction.addConfidence( confidence);
            }

            // parameter conversion
            for (psidev.psi.mi.xml.model.Parameter psiParameter :  psiObject.getParameters()){
                InteractionParameter parameter = paramConverter.psiToIntact( psiParameter );
                interaction.addParameter(parameter);
            }

            // update experiment participant detection method if necessary
            updateExperimentParticipantDetectionMethod(interaction);

            // negative
            if( psiObject.isNegative() ) {
                interaction.addAnnotation( new Annotation( getInstitution(),
                        new CvTopic( getInstitution(), NEGATIVE ),
                        TRUE ) );
            }

            if( psiObject.isIntraMolecular() ) {
                interaction.addAnnotation( new Annotation( getInstitution(),
                        new CvTopic( getInstitution(), INTRA_MOLECULAR ),
                        TRUE ) );
            }

            if( psiObject.isModelled() ) {
                interaction.addAnnotation( new Annotation( getInstitution(),
                        new CvTopic( getInstitution(), MODELLED ),
                        TRUE ) );
            }
        } catch (Throwable t) {
            throw new PsiConversionException("Problem converting PSI interaction to Intact: "+psiObject.getNames(), t);
        }

        psiEndConversion(psiObject);


        failIfInconsistentConversion(interaction, psiObject, numberOfAuthoConfToConvert);

        return interaction;
    }

    protected InteractorXref createImexXref(Interaction interaction, String imexId) {
        CvDatabase cvImex = CvObjectUtils.createCvObject(interaction.getOwner(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        cvImex.setFullName(CvDatabase.IMEX);
        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(interaction.getOwner(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);

        return XrefUtils.createIdentityXref(interaction, imexId, imexPrimary, cvImex);
    }

    protected Xref getImexXref(Interaction interaction) {
        Collection<? extends Xref> refs;
        if (isCheckInitializedCollections()){
            refs = IntactCore.ensureInitializedXrefs(interaction);
        }
        else {
            refs = interaction.getXrefs();
        }
        for (Xref xref : refs) {
            if (CvDatabase.IMEX_MI_REF.equals(xref.getCvDatabase().getIdentifier())) {
                return xref;
            }
        }

        return null;
    }

    private boolean alreadyContainsImexXref(Interaction interaction) {
        return getImexXref(interaction) != null;
    }

    protected void fixSourceReferenceXrefsIfNecessary(Interaction interaction) {
        InteractorXref xrefToFix = null;

        if( ConverterContext.getInstance().isAutoFixInteractionSourceReference() ) {

            // Look up source reference xref and only try to fix identity if there is no source ref present.
            // if the qualifier is identity, we will check if the owner identity MI is the same as the database MI
            for (InteractorXref xref : interaction.getXrefs()) {
                if (xref.getCvXrefQualifier() != null &&
                        getInstitutionPrimaryId() != null &&
                        getInstitutionPrimaryId().equals( xref.getPrimaryId() ) &&
                        !CvXrefQualifier.SOURCE_REFERENCE_MI_REF.equals(xref.getCvXrefQualifier().getIdentifier())) {

                    xrefToFix = xref;
                    break;
                }
            }

            if ( xrefToFix != null ) {
                log.warn("Interaction identity xref found pointing to the source database. It should be of type 'source-reference'. Will be fixed automatically: "+xrefToFix);
                CvXrefQualifier sourceReference = CvObjectUtils.createCvObject(interaction.getOwner(), CvXrefQualifier.class, CvXrefQualifier.SOURCE_REFERENCE_MI_REF, CvXrefQualifier.SOURCE_REFERENCE);
                xrefToFix.setCvXrefQualifier(sourceReference);

                addMessageToContext(MessageLevel.WARN, "Interaction identity xref found pointing to the source database. It should be of type 'source-reference'. Fixed.", true);
            }
        }
    }

    public psidev.psi.mi.xml.model.Interaction intactToPsi(Interaction intactObject) {

        psidev.psi.mi.xml.model.Interaction interaction = super.intactToPsi(intactObject);

        if (!isNewPsiObjectCreated()) {
            return interaction;
        }

        intactStartConversation(intactObject);

        // Set id, annotations, xrefs and aliases
        PsiConverterUtils.populateId(interaction);
        PsiConverterUtils.populateNames(intactObject, interaction, aliasConverter);
        PsiConverterUtils.populateXref(intactObject, interaction, xrefConverter);
        PsiConverterUtils.populateAttributes(intactObject, interaction, annotationConverter);

        Collection<Experiment> experiments;
        Collection<Component> components;
        Collection<Confidence> confidences;
        Collection<uk.ac.ebi.intact.model.InteractionParameter> parameters;
        Collection<Annotation> annotations;

        if (isCheckInitializedCollections()){
            experiments = IntactCore.ensureInitializedExperiments(intactObject);
            components = IntactCore.ensureInitializedParticipants(intactObject);
            confidences = IntactCore.ensureInitializedConfidences(intactObject);
            parameters = IntactCore.ensureInitializedInteractionParameters(intactObject);
            annotations = IntactCore.ensureInitializedAnnotations(intactObject);
        }
        else {
            experiments = intactObject.getExperiments();
            components = intactObject.getComponents();
            confidences = intactObject.getConfidences();
            parameters = intactObject.getParameters();
            annotations = intactObject.getAnnotations();
        }

        participantConverter.getFeatureMap().clear();

        // imexId
        Xref imexXref = getImexXref(intactObject);

        if (imexXref != null) {
            interaction.setImexId(imexXref.getPrimaryId());
        }

        // converts experiments

        if (experiments.size() == 0){
            log.error("Interaction without any experiments : " + intactObject.getShortLabel());
        }
        else if (experiments.size() > 1){
            log.error("Interaction with "+experiments.size()+" experiments : " + intactObject.getShortLabel()+ ". On;y one experiment per interactions is expected in IntAct");
        }

        for (Experiment exp : experiments) {
            ExperimentDescription expDescription = experimentConverter.intactToPsi(exp);
            if( ConverterContext.getInstance().isGenerateExpandedXml() ) {
                interaction.getExperiments().add(expDescription);
            } else {
                interaction.getExperimentRefs().add(new ExperimentRef(expDescription.getId()));
            }
        }

        // converts participants and inferred interactions

        if (components.isEmpty()){
            log.error("Interaction without any participants : " + intactObject.getShortLabel());
        }
        for (Component comp : components) {
            Participant participant = participantConverter.intactToPsi(comp);
            interaction.getParticipants().add(participant);

            // process features
            Collection<uk.ac.ebi.intact.model.Feature> features;
            if (isCheckInitializedCollections()){
                features = IntactCore.ensureInitializedFeatures(comp);
            }
            else {
                features = comp.getFeatures();
            }

            for(uk.ac.ebi.intact.model.Feature feature : features){
                if(feature.getBoundDomain() != null){

                    uk.ac.ebi.intact.model.Feature boundFeature = feature.getBoundDomain();

                    Feature psiFeature;
                    Feature boundPsiFeature;

                    if(feature.getAc() != null){
                        psiFeature = participantConverter.getFeatureMap().get(feature.getAc());
                    }else{
                        psiFeature = (Feature) ConversionCache.getElement(feature);
                    }

                    if(boundFeature.getAc() != null){
                        boundPsiFeature = participantConverter.getFeatureMap().get(boundFeature.getAc());
                    }else{
                        boundPsiFeature = (Feature) ConversionCache.getElement(boundFeature);
                    }

                    // we have an inferred interaction
                    if(psiFeature != null && boundPsiFeature != null){
                        InferredInteractionParticipant iParticipantFirst = new InferredInteractionParticipant(psiFeature);
                        InferredInteractionParticipant iParticipantSecond = new InferredInteractionParticipant(boundPsiFeature);

                        InferredInteraction iInteraction = new InferredInteraction(Arrays.asList(iParticipantFirst, iParticipantSecond));
                        InferredInteraction iTestInteraction = new InferredInteraction(Arrays.asList(iParticipantSecond, iParticipantFirst));

                        if(!(interaction.getInferredInteractions().contains(iInteraction) ||
                                interaction.getInferredInteractions().contains(iTestInteraction))){
                            interaction.getInferredInteractions().add(iInteraction);
                        }
                    }
                }
            }
        }

        // converts interaction type
        if (intactObject.getCvInteractionType() != null){
            InteractionType interactionType = this.interactionTypeConverter.intactToPsi(intactObject.getCvInteractionType());
            interaction.getInteractionTypes().add(interactionType);
        }
        else {
            log.error("Interaction without interaction type : " + intactObject.getShortLabel());
        }

        // converts confidences (and add author-confidence when necessary for retrocompatibility)
        for (Confidence conf : confidences) {
            psidev.psi.mi.xml.model.Confidence confidence = confConverter.intactToPsi(conf);
            interaction.getConfidences().add( confidence);

            // in case of author-score and for retro-compatibility, we add an author-confidence annotation
            if (conf.getCvConfidenceType() != null && IntactConverterUtils.AUTHOR_SCORE_MI.equalsIgnoreCase(conf.getCvConfidenceType().getIdentifier())){
                Attribute authConf = new Attribute(IntactConverterUtils.AUTH_CONF_MI, IntactConverterUtils.AUTH_CONF, conf.getValue());

                if (!interaction.getAttributes().contains(authConf)){
                    interaction.getAttributes().add(authConf);
                }
            }
        }

        // collect annotations which have a special meaning for psi xml
        processSpecializedAnnotations(annotations, interaction);

        // converts interaction parameters
        for (uk.ac.ebi.intact.model.InteractionParameter param : parameters){
            psidev.psi.mi.xml.model.Parameter parameter = paramConverter.intactToPsi(param);
            interaction.getParameters().add(parameter);
        }

        // check if intra molecular
        if (intactObject.getComponents().size() == 1){
            Component c = intactObject.getComponents().iterator().next();
            if (c.getStoichiometry() == 1 || (c.getStoichiometry() == 0 && containsRole(c.getExperimentalRoles(), new String[]{CvExperimentalRole.SELF_PSI_REF, PUTATIVE_SELF_PSI_REF}))){
                interaction.setIntraMolecular(true);
            }
        }

        intactEndConversion(intactObject);

        failIfInconsistentPsiConversion(intactObject, interaction);

        return interaction;
    }

    protected boolean containsRole(Collection<CvExperimentalRole> experimentalRoles, String[] rolesToFind) {
        if (experimentalRoles != null) {
            for (CvExperimentalRole expRole : experimentalRoles) {
                for (String roleToFind : rolesToFind) {
                    if (roleToFind.equals(expRole.getIdentifier())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void processSpecializedAnnotations( Collection<Annotation> annotations, psidev.psi.mi.xml.model.Interaction interaction) {
        // set boolean values

        final Iterator<Annotation> it = annotations.iterator();

        int numberOfAuthConf = 0;

        while ( it.hasNext() ) {
            Annotation annotation = it.next();
            if ( annotation.getCvTopic().getShortLabel().equals( NEGATIVE ) ) {
                interaction.setNegative( true );
            }
            else if (annotation.getCvTopic().getShortLabel().equals( MODELLED )){
                interaction.setModelled( true );
            }
            else if (annotation.getCvTopic().getShortLabel().equals( INTRA_MOLECULAR )){
                interaction.setIntraMolecular( true );
            }
        }
    }

    private void updateExperimentParticipantDetectionMethod(Interaction interaction) {
        for (Experiment experiment : interaction.getExperiments()) {
            if (experiment.getCvIdentification() == null) {

                String partDetMethod = calculateParticipantDetMethod(interaction.getComponents());

                if (partDetMethod != null) {
                    final String message = "Experiment ("+ experiment.getShortLabel() +") without participant detection method. One was calculated from the components: " + partDetMethod;
                    addMessageToContext(MessageLevel.INFO, message, true);

                    if (log.isWarnEnabled()) {
                        log.warn(message);
                    }

                    CvIdentification detMethod = CvObjectUtils.createCvObject(experiment.getOwner(), CvIdentification.class, partDetMethod, "undefined");
                    experiment.setCvIdentification(detMethod);
                } else {

                    final String message = "Neither the Experiment nor its participants have CvIdentification (participant detection method). Using the term \"experimental particp\" (MI:0661).";
                    if (log.isWarnEnabled()) log.warn(": Experiment '"+experiment.getShortLabel()+
                            "', Interaction '"+interaction.getShortLabel()+"' - Location: "+ConverterContext.getInstance().getLocation().getCurrentLocation().pathFromRootAsString());
                    addMessageToContext(MessageLevel.WARN, message, true);

                    CvIdentification detMethod = CvObjectUtils.createCvObject(experiment.getOwner(), CvIdentification.class, "MI:0661", "experimental particp");
                    experiment.setCvIdentification(detMethod);
                }
            }
        }
    }

    private String calculateParticipantDetMethod(Collection<Component> components) {
        Set<String> detMethodMis = new HashSet<String>();

        for (Component component : components) {
            for (CvIdentification partDetMethod : component.getParticipantDetectionMethods()) {
                if (partDetMethod.getIdentifier() != null) {
                    detMethodMis.add(partDetMethod.getIdentifier());
                }
            }
        }

        if (detMethodMis.size() == 1) {
            return detMethodMis.iterator().next();
        } else if (detMethodMis.size() > 1) {
            return CvUtils.findLowestCommonAncestor(getCurrentOntology(), detMethodMis.toArray(new String[detMethodMis.size()]));
        }

        log.error("No participant detection methods found for components in experiment");

        return null;
    }

    protected Collection<Experiment> getExperiments(psidev.psi.mi.xml.model.Interaction psiInteraction) {
        Collection<ExperimentDescription> expDescriptions = psiInteraction.getExperiments();

        List<Experiment> experiments = new ArrayList<Experiment>(expDescriptions.size());

        for (ExperimentDescription expDesc : expDescriptions) {
            Experiment experiment = experimentConverter.psiToIntact(expDesc);
            experiments.add(experiment);
        }

        if (expDescriptions.size() > 1){
            log.error("Interaction having more than one experiment not valid in IntAct : " + psiInteraction);
        }
        else if (expDescriptions.isEmpty()){
            log.error("Interaction without experiments not valid in IntAct : " + psiInteraction);
        }

        return experiments;
    }

    /**
     * Get the first interaction type only
     */
    protected CvInteractionType getInteractionType(psidev.psi.mi.xml.model.Interaction psiInteraction) {
        final Collection<InteractionType> interactionTypes = psiInteraction.getInteractionTypes();

        if (interactionTypes == null || interactionTypes.isEmpty()) {
            throw new PsiConversionException("Interaction without Interaction Type: "+psiInteraction);
        }

        if (interactionTypes.size() > 1) {
            throw new PsiConversionException("Interaction with more than one Interaction Type: "+psiInteraction);
        }

        InteractionType psiInteractionType = interactionTypes.iterator().next();
        return this.interactionTypeConverter.psiToIntact(psiInteractionType);
    }

    protected Collection<Component> getComponents(Interaction interaction, psidev.psi.mi.xml.model.Interaction psiInteraction) {
        List<Component> components = new ArrayList<Component>(psiInteraction.getParticipants().size());

        for (Participant participant : psiInteraction.getParticipants()) {
            if (participant.getInteractor() == null) {
                throw new PsiConversionException("Participant without interactor found: "+participant+" in interaction: "+interaction);
            }

            Component component = participantConverter.psiToIntact(participant);
            component.setInteraction(interaction);

            components.add(component);
        }

        // does not look at the experimentRef of inferredInteraction because we suppose in IntAct that one interaction has only one experiment
        for( InferredInteraction inferredInteraction : psiInteraction.getInferredInteractions()){
            if(inferredInteraction.getParticipant().size() == 2){
                Iterator<InferredInteractionParticipant> iterator = inferredInteraction.getParticipant().iterator();

                // we suspect that one inferredInteraction contains only two participants
                InferredInteractionParticipant firstParticipant = iterator.next();
                InferredInteractionParticipant secParticipant = iterator.next();

                uk.ac.ebi.intact.model.Feature firstFeature = null;
                uk.ac.ebi.intact.model.Feature secFeature = null;

                if(firstParticipant.hasFeature()){
                    firstFeature = (uk.ac.ebi.intact.model.Feature) ConversionCache.getElement(firstParticipant.getFeature());
                }else if (firstParticipant.hasFeatureRef()){
                    // TODO featureRef?
                }
                else if (firstParticipant.hasParticipant() || firstParticipant.hasParticipantRef()){
                    log.warn("Inferred interaction with a participant or participant ref cannot be converted in IntAct yet.");
                }

                if(secParticipant.hasFeature()){
                    secFeature = (uk.ac.ebi.intact.model.Feature) ConversionCache.getElement(secParticipant.getFeature());
                }else if(secParticipant.hasFeatureRef()){
                    // TODO featureRef?
                }
                else if (secParticipant.hasParticipant() || secParticipant.hasParticipantRef()){
                    log.warn("Inferred interaction with a participant or participant ref cannot be converted in IntAct yet.");
                }

                if(firstFeature != null && secFeature != null){
                    firstFeature.setBoundDomain(secFeature);
                    secFeature.setBoundDomain(firstFeature);
                }else{
                    // TODO think about cases where features are linked with participants
                }

            }else{
                // TODO think about cases with more than two participant
                if (inferredInteraction.getParticipant().isEmpty()){
                    log.error("Inferred interaction without any participants which is not valid.");
                }
                else {
                    log.warn("Inferred interaction with " + inferredInteraction.getParticipant().size() + " participants. In IntAct, we cannot convert such inferred interactions.");
                }
            }
        }

        return components;
    }

    protected void failIfInconsistentConversion(Interaction intact, psidev.psi.mi.xml.model.Interaction psi) {
        Collection<Experiment> experiments;
        Collection<Component> participants;
        Collection<Confidence> confidences;
        if (isCheckInitializedCollections()){
            experiments = IntactCore.ensureInitializedExperiments(intact);
            participants = IntactCore.ensureInitializedParticipants(intact);
            confidences = IntactCore.ensureInitializedConfidences(intact);
        }
        else {
            experiments = intact.getExperiments();
            participants = intact.getComponents();
            confidences = intact.getConfidences();
        }

        failIfInconsistentCollectionSize("experiment", experiments, psi.getExperiments());
        failIfInconsistentCollectionSize("participant", participants, psi.getParticipants());
        failIfInconsistentCollectionSize( "confidence", confidences, psi.getConfidences());
    }

    protected void failIfInconsistentConversion(Interaction intact, psidev.psi.mi.xml.model.Interaction psi, int numberOfAuthorConfAttributes) {
        Collection<Experiment> experiments;
        Collection<Component> participants;
        Collection<Confidence> confidences;
        if (isCheckInitializedCollections()){
            experiments = IntactCore.ensureInitializedExperiments(intact);
            participants = IntactCore.ensureInitializedParticipants(intact);
            confidences = IntactCore.ensureInitializedConfidences(intact);
        }
        else {
            experiments = intact.getExperiments();
            participants = intact.getComponents();
            confidences = intact.getConfidences();
        }

        failIfInconsistentCollectionSize("experiment", experiments, psi.getExperiments());
        failIfInconsistentCollectionSize("participant", participants, psi.getParticipants());

        Collection<Confidence> confs = confidences;
        if (confs.size() > 0 && psi.getConfidences().size() + numberOfAuthorConfAttributes > 0 && confs.size() != (psi.getConfidences().size() + numberOfAuthorConfAttributes)) {
            throw new InconsistentConversionException("Confidence", confs.size(), psi.getConfidences().size() + numberOfAuthorConfAttributes);
        }
    }

    protected void failIfInconsistentPsiConversion(Interaction intact, psidev.psi.mi.xml.model.Interaction psi) {
        Collection<Experiment> experiments;
        Collection<Component> participants;
        Collection<Confidence> confidences;
        if (isCheckInitializedCollections()){
            experiments = IntactCore.ensureInitializedExperiments(intact);
            participants = IntactCore.ensureInitializedParticipants(intact);
            confidences = IntactCore.ensureInitializedConfidences(intact);
        }
        else {
            experiments = intact.getExperiments();
            participants = intact.getComponents();
            confidences = intact.getConfidences();
        }

        failIfInconsistentCollectionSize("experiment", experiments, psi.getExperiments());
        failIfInconsistentCollectionSize("participant", participants, psi.getParticipants());

        Collection<Confidence> confs = confidences;
        if (confs.size() > 0 && psi.getConfidences().size() > 0 && (confs.size()) != psi.getConfidences().size()) {
            throw new InconsistentConversionException("Confidence", confs.size(), psi.getConfidences().size());
        }
    }

    @Override
    public void setInstitution(Institution institution)
    {
        super.setInstitution(institution);
        confConverter.setInstitution( institution, getInstitutionPrimaryId());
        experimentConverter.setInstitution(institution, getInstitutionPrimaryId());
        paramConverter.setInstitution( institution, getInstitutionPrimaryId());
        participantConverter.setInstitution(institution, true, false, getInstitutionPrimaryId());
        interactionTypeConverter.setInstitution(institution, getInstitutionPrimaryId());
    }

    public void setInstitution(Institution institution, boolean setExperimentInstitution, boolean setParticipantInstitution)
    {
        super.setInstitution(institution);
        confConverter.setInstitution( institution, getInstitutionPrimaryId());
        if (setExperimentInstitution){
            experimentConverter.setInstitution(institution, getInstitutionPrimaryId());
        }
        paramConverter.setInstitution( institution, false, getInstitutionPrimaryId());

        if (setParticipantInstitution){
            participantConverter.setInstitution(institution, false, false, getInstitutionPrimaryId());
        }
        interactionTypeConverter.setInstitution(institution, getInstitutionPrimaryId());
    }

    public void setInstitution(Institution institution, boolean setExperimentInstitution, boolean setParticipantInstitution, String institutionPrimaryId)
    {
        super.setInstitution(institution, institutionPrimaryId);
        confConverter.setInstitution( institution, getInstitutionPrimaryId());
        if (setExperimentInstitution){
            experimentConverter.setInstitution(institution, getInstitutionPrimaryId());
        }
        paramConverter.setInstitution( institution, false, getInstitutionPrimaryId());

        if (setParticipantInstitution){
            participantConverter.setInstitution(institution, false, false, getInstitutionPrimaryId());
        }
        interactionTypeConverter.setInstitution(institution, getInstitutionPrimaryId());
    }

    @Override
    public void setInstitution(Institution institution, String institId){
        super.setInstitution(institution, institId);
        confConverter.setInstitution( institution, getInstitutionPrimaryId());
        experimentConverter.setInstitution(institution, getInstitutionPrimaryId());
        paramConverter.setInstitution( institution, getInstitutionPrimaryId());
        participantConverter.setInstitution(institution, true, false, getInstitutionPrimaryId());
        interactionTypeConverter.setInstitution(institution, getInstitutionPrimaryId());
    }

    @Override
    public void setCheckInitializedCollections(boolean check){
        super.setCheckInitializedCollections(check);
        this.confConverter.setCheckInitializedCollections(check);
        this.experimentConverter.setCheckInitializedCollections(check);
        this.paramConverter.setCheckInitializedCollections(check);
        this.participantConverter.setCheckInitializedCollections(check);
        this.interactionTypeConverter.setCheckInitializedCollections(check);
    }

    public void setCheckInitializedCollections(boolean check, boolean initializeExperiment, boolean initializeParticipant){
        super.setCheckInitializedCollections(check);
        this.confConverter.setCheckInitializedCollections(check);
        if (initializeExperiment){
            this.experimentConverter.setCheckInitializedCollections(check);
        }
        this.paramConverter.setCheckInitializedCollections(check, false);
        if (initializeParticipant){
            this.participantConverter.setCheckInitializedCollections(check, false, false);
        }
        this.interactionTypeConverter.setCheckInitializedCollections(check);
    }
}