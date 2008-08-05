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

import org.junit.Assert;
import org.junit.Test;
import psidev.psi.mi.xml.model.Parameter;
import psidev.psi.mi.xml.model.Participant;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.ComponentParameter;
import uk.ac.ebi.intact.model.CvExperimentalRole;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ComponentConverterTest extends IntactBasicTestCase {

    @Test
    public void psiToIntact_default() throws Exception {
        Participant participant = PsiMockFactory.createMockInteraction().getParticipants().iterator().next();
        participant.getExperimentalRoles().iterator().next().getXref().getPrimaryRef().setId(CvExperimentalRole.BAIT_PSI_REF);

        participant.getFeatures().clear();
        participant.getFeatures().add(PsiMockFactory.createFeature());

        ParticipantConverter participantConverter = new ParticipantConverter(getMockBuilder().getInstitution());
        Component component = participantConverter.psiToIntact(participant);

        Assert.assertNotNull(component);
        Assert.assertEquals(1, component.getExperimentalRoles().size());
        Assert.assertNull(component.getExpressedIn());

        CvExperimentalRole expRole = component.getExperimentalRoles().iterator().next();

        Assert.assertEquals(CvExperimentalRole.BAIT_PSI_REF, expRole.getIdentifier());
        Assert.assertEquals(1, component.getBindingDomains().size());
        
        Assert.assertEquals(1, component.getParameters().size());
        final ComponentParameter param = component.getParameters().iterator().next();
        Assert.assertEquals("temperature of inter", param.getCvParameterType().getShortLabel());
        Assert.assertEquals("MI:0836", param.getCvParameterType().getIdentifier());
        Assert.assertEquals("kelvin", param.getCvParameterUnit().getShortLabel());
        Assert.assertEquals("MI:0838", param.getCvParameterUnit().getIdentifier());
        Assert.assertEquals(275d, param.getFactor());
    }

    @Test
    public void intactToPsi_default() throws Exception {
        Component component = getMockBuilder().createInteractionRandomBinary().getComponents().iterator().next();
        component.setExpressedIn(getMockBuilder().createBioSourceRandom());

        ParticipantConverter participantConverter = new ParticipantConverter(getMockBuilder().getInstitution());
        Participant participant = participantConverter.intactToPsi(component);

        Assert.assertNotNull(participant);
        Assert.assertEquals(1, participant.getExperimentalRoles().size());
        Assert.assertEquals(1, participant.getHostOrganisms().size());

        Assert.assertEquals(1, participant.getParameters().size());
        Parameter param = participant.getParameters().iterator().next();
        Assert.assertEquals(302d, param.getFactor());
        Assert.assertEquals("temperature", param.getTerm());
        Assert.assertEquals("MI:0836", param.getTermAc());
        Assert.assertEquals("kelvin", param.getUnit());
        Assert.assertEquals("MI:0838", param.getUnitAc());
    }
}