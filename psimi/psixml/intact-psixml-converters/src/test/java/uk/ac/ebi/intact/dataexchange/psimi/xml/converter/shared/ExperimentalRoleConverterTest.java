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
import psidev.psi.mi.xml.model.*;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvExperimentalRole;
import uk.ac.ebi.intact.model.CvObjectXref;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentalRoleConverterTest extends IntactBasicTestCase {

    @Test
    public void psiToIntact_default() throws Exception {
        ExperimentalRole cvType = PsiMockFactory.createCvType(ExperimentalRole.class, CvExperimentalRole.BAIT_PSI_REF, CvExperimentalRole.BAIT);

        ExperimentalRoleConverter converter = new ExperimentalRoleConverter(getMockBuilder().getInstitution());
        CvExperimentalRole expRole = converter.psiToIntact(cvType);

        Assert.assertNotNull(expRole);
        Assert.assertEquals(CvExperimentalRole.BAIT_PSI_REF, expRole.getMiIdentifier());
        Assert.assertEquals(CvExperimentalRole.BAIT, expRole.getShortLabel());

        CvObjectXref identity = CvObjectUtils.getPsiMiIdentityXref(expRole);
        Assert.assertEquals(CvExperimentalRole.BAIT_PSI_REF, identity.getPrimaryId());

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
    }
}