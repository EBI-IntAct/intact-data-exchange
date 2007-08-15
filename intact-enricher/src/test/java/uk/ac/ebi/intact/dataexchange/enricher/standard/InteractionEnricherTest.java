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
package uk.ac.ebi.intact.dataexchange.enricher.standard;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Interactor;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionEnricherTest extends IntactBasicTestCase {

    private InteractionEnricher enricher;

    @Before
    public void beforeMethod() {
        enricher = InteractionEnricher.getInstance();
    }

    @After
    public void afterMethod() {
        enricher.close();
        enricher = null;
    }

    @Test
    public void enrich_default() {
        BioSource ecoli = getMockBuilder().createBioSource(83333, "lala");
        Interactor interactor1 = getMockBuilder().createProtein("P45531", "unk1", ecoli);
        Interactor interactor2 = getMockBuilder().createProtein("P45532", "unk2", ecoli);
        Experiment experiment = getMockBuilder().createExperimentEmpty("myExperiment");

        Interaction interaction = getMockBuilder().createInteraction("myInteraction", interactor1, interactor2, experiment);

        enricher.enrich(interaction);

        Assert.assertEquals("myInteraction", interaction.getShortLabel());
        Assert.assertEquals("83333", interactor1.getBioSource().getTaxId());
        Assert.assertEquals("ecoli", interactor1.getBioSource().getShortLabel());
        Assert.assertEquals("tusd_ecoli", interactor2.getShortLabel());
    }

    @Test
    public void enrich_updateLabel() {
        BioSource ecoli = getMockBuilder().createBioSource(83333, "lala");
        
        Interactor interactor1 = getMockBuilder().createProtein("P45531", "unk1", ecoli);
        Interactor interactor2 = getMockBuilder().createProtein("P45532", "unk2", ecoli);

        Experiment experiment = getMockBuilder().createExperimentEmpty("myExperiment");

        Interaction interaction = getMockBuilder().createInteraction("myInteraction", interactor1, interactor2, experiment);

        EnricherContext.getInstance().getConfig().setUpdateInteractionShortLabels(true);

        enricher.enrich(interaction);

        Assert.assertEquals("tusc-tusd", interaction.getShortLabel());
        Assert.assertEquals("83333", interactor2.getBioSource().getTaxId());
        Assert.assertEquals("ecoli", interactor2.getBioSource().getShortLabel());
        Assert.assertEquals("tusc_ecoli", interactor1.getShortLabel());
    }
}