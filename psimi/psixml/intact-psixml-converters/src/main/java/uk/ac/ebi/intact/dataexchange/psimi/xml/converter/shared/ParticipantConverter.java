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
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.PsiConversionException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Interactor;

import java.util.Iterator;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ParticipantConverter extends AbstractIntactPsiConverter<Component, Participant> {

    private static final Log log = LogFactory.getLog(ParticipantConverter.class);

    public ParticipantConverter(Institution institution) {
        super(institution);
    }

    public Component psiToIntact(Participant psiObject) {
        Interaction interaction = new InteractionConverter(getInstitution()).psiToIntact(psiObject.getInteraction());

        Component component = newComponent(getInstitution(), psiObject, interaction);
        return component;
    }

    public Participant intactToPsi(Component intactObject) {
        Participant participant = new Participant();
        PsiConverterUtils.populate(intactObject, participant);
        participant.getNames().setShortLabel(intactObject.getInteractor().getShortLabel());

        ExperimentalRole expRole = (ExperimentalRole)
                PsiConverterUtils.toCvType(intactObject.getCvExperimentalRole(), new ExperimentalRoleConverter(getInstitution()));
        participant.getExperimentalRoles().add(expRole);

        BiologicalRole bioRole = (BiologicalRole)
                PsiConverterUtils.toCvType(intactObject.getCvBiologicalRole(), new BiologicalRoleConverter(getInstitution()));
        participant.setBiologicalRole(bioRole);

        psidev.psi.mi.xml.model.Interactor interactor = new InteractorConverter(getInstitution()).intactToPsi(intactObject.getInteractor());
        participant.setInteractor(interactor);
        participant.setInteractorRef(new InteractorRef(interactor.getId()));

        for (CvIdentification participantDetectionMethod : intactObject.getParticipantDetectionMethods()) {
            ParticipantIdentificationMethodConverter pimConverter = new ParticipantIdentificationMethodConverter(getInstitution());
            ParticipantIdentificationMethod participantIdentificationMethod = pimConverter.intactToPsi(participantDetectionMethod);

            participant.getParticipantIdentificationMethods().add(participantIdentificationMethod);
        }

        for (CvExperimentalPreparation experimentalPreparation : intactObject.getExperimentalPreparations()) {
            CvObjectConverter<CvExperimentalPreparation, ExperimentalPreparation> epConverter =
                    new CvObjectConverter<CvExperimentalPreparation, ExperimentalPreparation>(getInstitution(), CvExperimentalPreparation.class, ExperimentalPreparation.class);
            ExperimentalPreparation expPrep = epConverter.intactToPsi(experimentalPreparation);

            participant.getExperimentalPreparations().add(expPrep);
        }

        if (!intactObject.getBindingDomains().isEmpty()) {
            FeatureConverter featureConverter = new FeatureConverter(getInstitution());

            for (Feature feature : intactObject.getBindingDomains()) {
                participant.getFeatures().add(featureConverter.intactToPsi(feature));
            }
        }

        if (intactObject.getExpressedIn() != null) {
            Organism organism = new OrganismConverter(getInstitution()).intactToPsi(intactObject.getExpressedIn());
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

        return participant;
    }

    protected static Component newComponent(Institution institution, Participant participant, Interaction interaction) {
        Interactor interactor = new InteractorConverter(institution).psiToIntact(participant.getInteractor());

        BiologicalRole psiBioRole = participant.getBiologicalRole();
        if (psiBioRole == null) {
            psiBioRole = PsiConverterUtils.createUnspecifiedBiologicalRole();
        }
        CvBiologicalRole biologicalRole = new BiologicalRoleConverter(institution).psiToIntact(psiBioRole);

        if (participant.getExperimentalRoles().size() > 1) {
            throw new PsiConversionException("Cannot convert participants with more than one experimental role: "+participant);
        }

        // only the first experimental role
        ExperimentalRole role;

        Iterator<ExperimentalRole> expRoleIterator = participant.getExperimentalRoles().iterator();
        if (expRoleIterator.hasNext()) {
            role = expRoleIterator.next();
        } else {
            if (log.isWarnEnabled()) log.warn("Participant without experimental role: " + participant);

            role = PsiConverterUtils.createUnspecifiedExperimentalRole();
        }

        CvExperimentalRole experimentalRole = new ExperimentalRoleConverter(institution).psiToIntact(role);

        Component component = new Component(institution, interaction, interactor, experimentalRole, biologicalRole);

        IntactConverterUtils.populateAnnotations(participant, component, institution);

        FeatureConverter featureConverter = new FeatureConverter(institution);

        for (psidev.psi.mi.xml.model.Feature psiFeature : participant.getFeatures()) {
            Feature feature = featureConverter.psiToIntact(psiFeature);
            component.getBindingDomains().add(feature);
            feature.setComponent(component);
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

        return component;
    }


}