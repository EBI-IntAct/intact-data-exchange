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
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Interaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionConverter extends AbstractAnnotatedObjectConverter<Interaction, psidev.psi.mi.xml.model.Interaction> {

    public InteractionConverter(Institution institution) {
        super(institution, InteractionImpl.class, psidev.psi.mi.xml.model.Interaction.class);
    }

    public Interaction psiToIntact(psidev.psi.mi.xml.model.Interaction psiObject) {
        Interaction interaction = super.psiToIntact(psiObject);

        String shortLabel = IntactConverterUtils.getShortLabelFromNames(psiObject.getNames());

        Collection<Experiment> experiments = getExperiments(psiObject);

        // only gets the first interaction type
        CvInteractionType interactionType = getInteractionType(psiObject);

        // only gets the first interactor type
        CvInteractorType interactorType = getInteractorType(psiObject);

        interaction.setShortLabel(shortLabel);
        interaction.setOwner(getInstitution());
        interaction.setCvInteractorType(interactorType);
        interaction.setExperiments(experiments);
        interaction.setCvInteractionType(interactionType);

        IntactConverterUtils.populateNames(psiObject.getNames(), interaction);
        IntactConverterUtils.populateXref(psiObject.getXref(), interaction, new XrefConverter<InteractorXref>(getInstitution(), InteractorXref.class));

        // components, created after the interaction, as we need the interaction to create them
        Collection<Component> components = getComponents(interaction, psiObject);
        interaction.setComponents(components);

        return interaction;
    }

    public psidev.psi.mi.xml.model.Interaction intactToPsi(Interaction intactObject) {
        psidev.psi.mi.xml.model.Interaction interaction = super.intactToPsi(intactObject);

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

        return interaction;
    }

    protected String psiElementKey(psidev.psi.mi.xml.model.Interaction psiObject) {
        return "i:"+psiObject.getId();
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
        InteractionType psiInteractionType = psiInteraction.getInteractionTypes().iterator().next();

        return new InteractionTypeConverter(getInstitution()).psiToIntact(psiInteractionType);
    }


    /**
     * Gets the interactor type from the first participant only
     */
    protected CvInteractorType getInteractorType(psidev.psi.mi.xml.model.Interaction psiInteraction) {
        InteractorType psiInteractorType = psiInteraction.getParticipants().iterator().next().getInteractor().getInteractorType();

        return new InteractorTypeConverter(getInstitution()).psiToIntact(psiInteractorType);
    }

    protected Collection<Component> getComponents(Interaction interaction, psidev.psi.mi.xml.model.Interaction psiInteraction) {
        List<Component> components = new ArrayList<Component>(psiInteraction.getParticipants().size());

        for (Participant participant : psiInteraction.getParticipants()) {
            Component component = ParticipantConverter.newComponent(getInstitution(), participant, interaction);
            components.add(component);
        }

        return components;
    }
}