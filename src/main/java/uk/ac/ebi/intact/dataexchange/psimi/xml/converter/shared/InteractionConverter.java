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
import org.apache.commons.beanutils.ConversionException;
import psidev.psi.mi.xml.model.*;
import uk.ac.ebi.intact.dataexchange.cvutils.CvUtils;
import uk.ac.ebi.intact.dataexchange.cvutils.OboUtils;
import uk.ac.ebi.intact.dataexchange.cvutils.model.IntactOntology;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.*;
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
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionConverter extends AbstractAnnotatedObjectConverter<Interaction, psidev.psi.mi.xml.model.Interaction> {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(InteractionConverter.class);

    private static ThreadLocal<IntactOntology> ontology = new ThreadLocal<IntactOntology>();

    private static IntactOntology getCurrentOntology() {
        if (ontology.get() == null) {
            if (log.isDebugEnabled()) log.debug("Initializing Intact ontology lazily");
            try {
                ontology.set(OboUtils.createOntologyFromOboLatestPsiMi());
            } catch (Exception e) {
                throw new IllegalStateException("Could not load ontology");
            }
        }

        return ontology.get();
    }

    public InteractionConverter(Institution institution) {
        super(institution, InteractionImpl.class, psidev.psi.mi.xml.model.Interaction.class);
    }

    public Interaction psiToIntact(psidev.psi.mi.xml.model.Interaction psiObject) {
        Interaction interaction = super.psiToIntact(psiObject);

        if (!isNewIntactObjectCreated()) {
            return interaction;
        }

        psiStartConversion(psiObject);

        // This has to be before anything else (e.g. when creating xrefs)
        interaction.setOwner(getInstitution());

        String shortLabel = IntactConverterUtils.getShortLabelFromNames(psiObject.getNames());

        Collection<Experiment> experiments = getExperiments(psiObject);

        // only gets the first interaction type
        CvInteractionType interactionType = getInteractionType(psiObject);

        interaction.setShortLabel(shortLabel);
        interaction.setExperiments(experiments);
        interaction.setCvInteractionType(interactionType);

        // interactor type is always "interaction" for interactions
        CvInteractorType interactorType = CvObjectUtils.createCvObject(getInstitution(), CvInteractorType.class, CvInteractorType.INTERACTION_MI_REF, CvInteractorType.INTERACTION);
        interaction.setCvInteractorType(interactorType);

        IntactConverterUtils.populateNames(psiObject.getNames(), interaction);
        IntactConverterUtils.populateXref(psiObject.getXref(), interaction, new XrefConverter<InteractorXref>(getInstitution(), InteractorXref.class));
        IntactConverterUtils.populateAnnotations(psiObject, interaction, getInstitution());

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
        interaction.setComponents(components);

        ConfidenceConverter confConverter= new ConfidenceConverter( getInstitution());
        for (psidev.psi.mi.xml.model.Confidence psiConfidence :  psiObject.getConfidences()){
           Confidence confidence = confConverter.psiToIntact( psiConfidence );
            interaction.addConfidence( confidence);
        }

        // update experiment participant detection method if necessary
        updateExperimentParticipantDetectionMethod(interaction);

        psiEndConversion(psiObject);

        failIfInconsistentConversion(interaction, psiObject);

        return interaction;
    }

    protected InteractorXref createImexXref(Interaction interaction, String imexId) {
        CvDatabase cvImex = CvObjectUtils.createCvObject(interaction.getOwner(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        cvImex.setFullName(CvDatabase.IMEX);
        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(interaction.getOwner(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);

        return XrefUtils.createIdentityXref(interaction, imexId, imexPrimary, cvImex);
    }

    protected InteractorXref getImexXref(Interaction interaction) {
        for (InteractorXref xref : interaction.getXrefs()) {
            if (CvDatabase.IMEX_MI_REF.equals(xref.getCvDatabase().getMiIdentifier())) {
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

        // if the qualifier is identity, we will check if the owner identity MI is the same as the database MI
        for (InteractorXref xref : interaction.getXrefs()) {
            if (xref.getCvXrefQualifier() != null &&
                    getInstitutionPrimaryId() != null &&
                    getInstitutionPrimaryId().equals(xref.getCvDatabase().getMiIdentifier()) &&
                    !CvXrefQualifier.SOURCE_REFERENCE_MI_REF.equals(xref.getCvXrefQualifier().getMiIdentifier())) {

                xrefToFix = xref;
                break;
            }
        }

        if (xrefToFix != null) {
            log.warn("Interaction identity xref found pointing to the source database. It should be of type 'source-reference'. Will be fixed automatically: "+xrefToFix);
            CvXrefQualifier sourceReference = CvObjectUtils.createCvObject(interaction.getOwner(), CvXrefQualifier.class, CvXrefQualifier.SOURCE_REFERENCE_MI_REF, CvXrefQualifier.SOURCE_REFERENCE);
            xrefToFix.setCvXrefQualifier(sourceReference);

            addMessageToContext(MessageLevel.WARN, "Interaction identity xref found pointing to the source database. It should be of type 'source-reference'. Fixed.", true);
        }
        
    }

    public psidev.psi.mi.xml.model.Interaction intactToPsi(Interaction intactObject) {
        psidev.psi.mi.xml.model.Interaction interaction = super.intactToPsi(intactObject);

        if (!isNewPsiObjectCreated()) {
            return interaction;
        }

        intactStartConversation(intactObject);

        // imexId
        InteractorXref imexXref = getImexXref(intactObject);

        if (imexXref != null) {
            interaction.setImexId(imexXref.getPrimaryId());
        }

        ExperimentConverter experimentConverter = new ExperimentConverter(getInstitution());
        for (Experiment exp : intactObject.getExperiments()) {
            ExperimentDescription expDescription = experimentConverter.intactToPsi(exp);
            interaction.getExperiments().add(expDescription);
            interaction.getExperimentRefs().add(new ExperimentRef(expDescription.getId()));
        }

        ParticipantConverter participantConverter = new ParticipantConverter(getInstitution());
        for (Component comp : intactObject.getComponents()) {
            Participant participant = participantConverter.intactToPsi(comp);
            participant.setInteraction(interaction);
            participant.setInteractionRef(new InteractionRef(interaction.getId()));
            interaction.getParticipants().add(participant);
        }

        InteractionType interactionType = (InteractionType)
                PsiConverterUtils.toCvType(intactObject.getCvInteractionType(), new InteractionTypeConverter(getInstitution()));
        interaction.getInteractionTypes().add(interactionType);


        ConfidenceConverter confidenceConverter = new ConfidenceConverter( getInstitution());
        for (Confidence conf : intactObject.getConfidences()){
            psidev.psi.mi.xml.model.Confidence confidence = confidenceConverter.intactToPsi( conf);
            interaction.getConfidences().add( confidence);
        }

        intactEndConversion(intactObject);

        failIfInconsistentConversion(intactObject, interaction);

        return interaction;
    }

    private void updateExperimentParticipantDetectionMethod(Interaction interaction) {
        for (Experiment experiment : interaction.getExperiments()) {
            if (experiment.getCvIdentification() == null) {

                String partDetMethod = calculateParticipantDetMethod(interaction.getComponents());

                if (partDetMethod != null) {
                    final String message = "Experiment without participant detection method. One was calculated from the components: " + partDetMethod;
                    addMessageToContext(MessageLevel.INFO, message, true);

                    if (log.isWarnEnabled()) {
                        log.warn(message);
                    }

                    CvIdentification detMethod = CvObjectUtils.createCvObject(experiment.getOwner(), CvIdentification.class, partDetMethod, "undefined");
                    experiment.setCvIdentification(detMethod);
                }

                if (experiment.getCvIdentification() == null) {
                    throw new UnsupportedConversionException("Experiment without CvIdentification (participant detection method) and its participants do not have one either: Experiment '"+experiment.getShortLabel()+"', Interaction '"+interaction.getShortLabel()+"'");
                }
            }
        }
    }

    private String calculateParticipantDetMethod(Collection<Component> components) {
        Set<String> detMethodMis = new HashSet<String>();

         for (Component component : components) {
             for (CvIdentification partDetMethod : component.getParticipantDetectionMethods()) {
                 if (partDetMethod.getMiIdentifier() != null) {
                     detMethodMis.add(partDetMethod.getMiIdentifier());
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

        ExperimentConverter converter = new ExperimentConverter(getInstitution());

        for (ExperimentDescription expDesc : expDescriptions) {
            Experiment experiment = converter.psiToIntact(expDesc);
            experiments.add(experiment);
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

        return new InteractionTypeConverter(getInstitution()).psiToIntact(psiInteractionType);
    }

    protected Collection<Component> getComponents(Interaction interaction, psidev.psi.mi.xml.model.Interaction psiInteraction) {
        List<Component> components = new ArrayList<Component>(psiInteraction.getParticipants().size());

        for (Participant participant : psiInteraction.getParticipants()) {
            if (participant.getInteractor() == null) {
                throw new PsiConversionException("Participant without interactor found: "+participant+" in interaction: "+interaction);
            }

            Component component = ParticipantConverter.newComponent(interaction.getOwner(), participant, interaction);
            components.add(component);
        }

        return components;
    }
    
    protected void failIfInconsistentConversion(Interaction intact, psidev.psi.mi.xml.model.Interaction psi) {
        failIfInconsistentCollectionSize("experiment", intact.getExperiments(), psi.getExperiments());
        failIfInconsistentCollectionSize("participant", intact.getComponents(), psi.getParticipants());
        failIfInconsistentCollectionSize( "confidence", intact.getConfidences(), psi.getConfidences());
    }
}