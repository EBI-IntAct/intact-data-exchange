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
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.Interaction;

/**
 * Participant converter.
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

        Component component = IntactConverterUtils.newComponent(getInstitution(), psiObject, interaction);
        return component;
    }

    public Participant intactToPsi(Component intactObject) {
        Participant participant = new Participant();
        PsiConverterUtils.populate(intactObject, participant, this );
        participant.getNames().setShortLabel(intactObject.getInteractor().getShortLabel());

        for ( CvExperimentalRole experimentalRole : intactObject.getExperimentalRoles() ) {
            ExperimentalRole expRole = ( ExperimentalRole )
                    PsiConverterUtils.toCvType( experimentalRole,
                            new ExperimentalRoleConverter( getInstitution() ),
                            this );
            participant.getExperimentalRoles().add( expRole );
        }

        if (intactObject.getCvBiologicalRole() == null) {
            throw new IllegalStateException("Found component without biological role: "+intactObject.getAc());
        }

        BiologicalRole bioRole = (BiologicalRole)
                PsiConverterUtils.toCvType(intactObject.getCvBiologicalRole(),
                        new BiologicalRoleConverter(getInstitution()),
                        this);
        participant.setBiologicalRole(bioRole);

        psidev.psi.mi.xml.model.Interactor interactor = new InteractorConverter(getInstitution()).intactToPsi(intactObject.getInteractor());
        if( ConverterContext.getInstance().isGenerateExpandedXml() ) {
            participant.setInteractor(interactor);
        } else {
            participant.setInteractorRef(new InteractorRef(interactor.getId()));
        }

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

        ParticipantConfidenceConverter confidenceConverter = new ParticipantConfidenceConverter( getInstitution());
        for (ComponentConfidence conf : intactObject.getConfidences()){
            psidev.psi.mi.xml.model.Confidence confidence = confidenceConverter.intactToPsi( conf);
            participant.getConfidenceList().add( confidence);
        }

        ParticipantParameterConverter participantParameterConverter = new ParticipantParameterConverter( getInstitution());
        for (uk.ac.ebi.intact.model.ComponentParameter param : intactObject.getParameters()){
            psidev.psi.mi.xml.model.Parameter parameter = participantParameterConverter.intactToPsi(param);
            participant.getParameters().add(parameter);
        }

        if (intactObject.getStoichiometry() > 0.0f){
            Attribute attribute = new Attribute(CvTopic.COMMENT_MI_REF, CvTopic.COMMENT, "Stoichiometry: " + intactObject.getStoichiometry());

            if (!participant.getAttributes().contains( attribute )) {
                participant.getAttributes().add( attribute );
            }
        }

        return participant;
    }
}