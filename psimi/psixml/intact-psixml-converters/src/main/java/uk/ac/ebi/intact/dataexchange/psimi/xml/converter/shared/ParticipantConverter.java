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
import psidev.psi.mi.xml.model.BiologicalRole;
import psidev.psi.mi.xml.model.ExperimentalRole;
import psidev.psi.mi.xml.model.Participant;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;

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

        ExperimentalRole expRole = (ExperimentalRole)
                PsiConverterUtils.toCvType(intactObject.getCvExperimentalRole(), new ExperimentalRoleConverter(getInstitution()));
        participant.getExperimentalRoles().add(expRole);

        BiologicalRole bioRole = (BiologicalRole)
                PsiConverterUtils.toCvType(intactObject.getCvBiologicalRole(), new BiologicalRoleConverter(getInstitution()));
        participant.setBiologicalRole(bioRole);

        psidev.psi.mi.xml.model.Interactor interactor = new InteractorConverter(getInstitution()).intactToPsi(intactObject.getInteractor());
        participant.setInteractor(interactor);

        return participant;
    }

    /**
     * Only uses the first one
     */
    protected CvExperimentalRole getExperimentalRole(Participant psiObject) {
        ExperimentalRole role = psiObject.getExperimentalRoles().iterator().next();

        return new ExperimentalRoleConverter(getInstitution()).psiToIntact(role);
    }

    static Component newComponent(Institution institution, Participant participant, Interaction interaction) {
        Interactor interactor = new InteractorConverter(institution).psiToIntact(participant.getInteractor());

        BiologicalRole psiBioRole = participant.getBiologicalRole();
        if (psiBioRole == null) {
            psiBioRole = PsiConverterUtils.createUnspecifiedBiologicalRole();
        }
        CvBiologicalRole biologicalRole = new BiologicalRoleConverter(institution).psiToIntact(psiBioRole);

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

        return component;
    }


}