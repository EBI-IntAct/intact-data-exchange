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

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class FeatureConverterTest extends AbstractConverterTest {

    @Test
    public void psiToIntact_default() throws Exception {
        EntrySet entrySet = getIntactEntrySet();

        Entry entry = entrySet.getEntries().iterator().next();

        Participant participant = null;
        for (Interaction interaction : entry.getInteractions()) {
            if (interaction.getId() == 1) {
                for (Participant candidateParticipant : interaction.getParticipants()) {
                    if (candidateParticipant.getId() == 5) {
                        participant = candidateParticipant;
                    }
                }
            }
        }

        FeatureConverter featureConverter = new FeatureConverter(getMockInstitution());

        for (Feature psiFeature : participant.getFeatures()) {
            uk.ac.ebi.intact.model.Feature feature = featureConverter.psiToIntact(psiFeature);

            Assert.assertNotNull(feature);
            Assert.assertNotNull(feature.getOwner());
            Assert.assertEquals(psiFeature.getNames().getShortLabel(), feature.getShortLabel());
            Assert.assertEquals(psiFeature.getFeatureType().getNames().getShortLabel(), feature.getCvFeatureType().getShortLabel());
            Assert.assertEquals(psiFeature.getRanges().size(), feature.getRanges().size());
        }

    }
}