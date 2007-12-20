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

import psidev.psi.mi.xml.model.*;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.PsiConversionException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.UnsupportedConversionException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Confidence;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.XrefUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

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

    public InteractionConverter(Institution institution) {
        super(institution, InteractionImpl.class, psidev.psi.mi.xml.model.Interaction.class);
    }

    public Interaction psiToIntact(psidev.psi.mi.xml.model.Interaction psiObject) {
        Interaction interaction = super.psiToIntact(psiObject);

        if (!isNewIntactObjectCreated()) {
            return interaction;
        }

        // This has to be before anything else (e.g. when creating xrefs)
        interaction.setOwner(getInstitution());

        String shortLabel = IntactConverterUtils.getShortLabelFromNames(psiObject.getNames());

        Collection<Experiment> experiments = getExperiments(psiObject);

        // imexId
        String imexId = psiObject.getImexId();
        if (imexId != null) {
            interaction.addXref(createImexXref(interaction, imexId));
        }

        // only gets the first interaction type
        CvInteractionType interactionType = getInteractionType(psiObject);

        interaction.setShortLabel(shortLabel);
        interaction.setExperiments(experiments);
        interaction.setCvInteractionType(interactionType);

        IntactConverterUtils.populateNames(psiObject.getNames(), interaction);
        IntactConverterUtils.populateXref(psiObject.getXref(), interaction, new XrefConverter<InteractorXref>(getInstitution(), InteractorXref.class));
        IntactConverterUtils.populateAnnotations(psiObject, interaction, getInstitution());

        // components, created after the interaction, as we need the interaction to create them
        Collection<Component> components = getComponents(interaction, psiObject);
        interaction.setComponents(components);

        //TODO:test      
        ConfidenceConverter confConverter= new ConfidenceConverter( getInstitution());
        for (psidev.psi.mi.xml.model.Confidence psiConfidence :  psiObject.getConfidences()){
           Confidence confidence = confConverter.psiToIntact( psiConfidence );
            interaction.addConfidence( confidence);
        }

        // update experiment participant detection method if necessary
        updateExperimentParticipantDetectionMethod(interaction);

        failIfInconsistentConversion(interaction, psiObject);

        return interaction;
    }

    private InteractorXref createImexXref(Interaction interaction, String imexId) {
        CvDatabase cvImex = CvObjectUtils.createCvObject(interaction.getOwner(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        cvImex.setFullName(CvDatabase.IMEX);

        return XrefUtils.createIdentityXref(interaction, imexId, null, cvImex);
    }

    public psidev.psi.mi.xml.model.Interaction intactToPsi(Interaction intactObject) {
        psidev.psi.mi.xml.model.Interaction interaction = super.intactToPsi(intactObject);

        if (!isNewPsiObjectCreated()) {
            return interaction;
        }

        // imexId
        InteractorXref imexXref = null;

        for (InteractorXref xref : intactObject.getXrefs()) {
            String primaryId = CvObjectUtils.getPsiMiIdentityXref(xref.getCvDatabase()).getPrimaryId();
            if (primaryId.equals(CvDatabase.IMEX_MI_REF)) {
                imexXref = xref;
                break;
            }
        }

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

        //TODO: test
        ConfidenceConverter confidenceConverter = new ConfidenceConverter( getInstitution());
        for (Confidence conf : intactObject.getConfidences()){
            psidev.psi.mi.xml.model.Confidence confidence = confidenceConverter.intactToPsi( conf);
            interaction.getConfidences().add( confidence);
        }

        failIfInconsistentConversion(intactObject, interaction);

        return interaction;
    }

    private void updateExperimentParticipantDetectionMethod(Interaction interaction) {
        for (Experiment experiment : interaction.getExperiments()) {
            if (experiment.getCvIdentification() == null) {
                for (Component component : interaction.getComponents() ) {
                    for (CvIdentification partDetMethod : component.getParticipantDetectionMethods()) {
                        if (experiment.getCvIdentification() != null && !experiment.getCvIdentification().getMiIdentifier().equals(partDetMethod.getMiIdentifier())) {
                            throw new UnsupportedConversionException("Cannot convert an experiment that does not have participant " +
                                  "detection method defined and its participants have different detection methods: Experiment '"+experiment.getShortLabel()+"', Interaction '"+interaction.getShortLabel()+"', Det. Methods: "+component.getParticipantDetectionMethods());
                        } else {
                            experiment.setCvIdentification(partDetMethod);
                        }
                    }
                }

                if (experiment.getCvIdentification() == null) {
                    throw new UnsupportedConversionException("Experiment without CvIdentification (participant detection method) and its participants do not have one either: Experiment '"+experiment.getShortLabel()+"', Interaction '"+interaction.getShortLabel()+"'");
                }
            }
        }
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