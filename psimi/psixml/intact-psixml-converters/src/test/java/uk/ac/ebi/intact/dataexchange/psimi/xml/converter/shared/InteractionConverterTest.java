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
import psidev.psi.mi.xml.model.Interaction;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.PsiConversionException;
import uk.ac.ebi.intact.model.Confidence;
import uk.ac.ebi.intact.model.Institution;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionConverterTest {

    @Test
    public void psiToIntact_default() throws Exception {
        Interaction psiInteraction = PsiMockFactory.createMockInteraction();

        InteractionConverter converter = new InteractionConverter(new Institution("testInstitution"));
        uk.ac.ebi.intact.model.Interaction interaction = converter.psiToIntact(psiInteraction);

        Assert.assertNotNull(interaction.getCvInteractionType());
        Assert.assertNull(interaction.getCvInteractorType());
        Assert.assertNotNull(interaction.getComponents().iterator().next().getInteractor().getOwner());
        Assert.assertEquals("testInstitution", interaction.getComponents().iterator().next().getInteractor().getOwner().getShortLabel());
        Assert.assertEquals( 1, interaction.getConfidences().size());

        Confidence conf = interaction.getConfidences().iterator().next();
        Assert.assertNotNull( conf.getCvConfidenceType());
        Assert.assertEquals("intact conf score", conf.getCvConfidenceType().getShortLabel());
        Assert.assertEquals( "0.8", conf.getValue());
        Assert.assertEquals( interaction, conf.getInteraction());
    }

    @Test (expected = PsiConversionException.class)
    public void psiToIntact_noInteractionType() throws Exception {
        Interaction psiInteraction = PsiMockFactory.createMockInteraction();
        psiInteraction.getInteractionTypes().clear();

        InteractionConverter converter = new InteractionConverter(new Institution("testInstitution"));
        uk.ac.ebi.intact.model.Interaction interaction = converter.psiToIntact(psiInteraction);
    }


    @Test
    public void intactTopsi_default() throws Exception {
        uk.ac.ebi.intact.model.Interaction intactInteraction = new IntactMockBuilder().createDeterministicInteraction();

        InteractionConverter converter = new InteractionConverter(new Institution("testInstitution"));
        Interaction psiInteraction = converter.intactToPsi( intactInteraction);

        Assert.assertEquals( 1, psiInteraction.getConfidences().size());
        Assert.assertNotNull( psiInteraction.getConfidences().iterator().next().getUnit());
        Assert.assertEquals( intactInteraction.getConfidences().iterator().next().getValue(),  psiInteraction.getConfidences().iterator().next().getValue());
        Assert.assertEquals( intactInteraction.getConfidences().iterator().next().getCvConfidenceType().getShortLabel(), psiInteraction.getConfidences().iterator().next().getUnit().getNames().getShortLabel());
    }
}