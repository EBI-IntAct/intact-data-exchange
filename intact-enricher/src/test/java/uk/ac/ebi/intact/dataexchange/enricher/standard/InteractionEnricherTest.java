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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.model.impl.*;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherBasicTestCase;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionEnricherTest extends EnricherBasicTestCase {

    @Autowired
    @Qualifier("intactInteractionEvidenceEnricher")
    private InteractionEvidenceEnricher enricher;

    @Autowired
    private EnricherContext enricherContext;
    
    @Test
    public void enrich_default() throws EnricherException {
        enricherContext.getConfig().setUpdateInteractionShortLabels(false);

        Organism ecoli = new DefaultOrganism(83333, "lala");
        Protein interactor1 = new DefaultProtein("unk1", ecoli);
        interactor1.setUniprotkb("P45531");
        Protein interactor2 = new DefaultProtein("unk2", ecoli);
        interactor2.setUniprotkb("P45532");
        Experiment experiment = new DefaultExperiment(null);

        InteractionEvidence interaction = new DefaultInteractionEvidence("myInteraction");
        interaction.setExperiment(experiment);
        interaction.addParticipant(new DefaultParticipantEvidence(interactor1));
        interaction.addParticipant(new DefaultParticipantEvidence(interactor2));

        enricher.enrich(interaction);

        Assert.assertEquals("myInteraction", interaction.getShortName());
        Assert.assertEquals(83333, interactor1.getOrganism().getTaxId());
        Assert.assertEquals("strain k12", interactor1.getOrganism().getCommonName());
        Assert.assertEquals("tusd_ecoli", interactor2.getShortName());
    }

    @Test
    public void enrich_updateLabel() throws EnricherException {
        Organism ecoli = new DefaultOrganism(83333, "lala");

        Protein interactor1 = new DefaultProtein("unk1", ecoli);
        interactor1.setUniprotkb("P45531");
        Protein interactor2 = new DefaultProtein("unk2", ecoli);
        interactor2.setUniprotkb("P45532");

        Experiment experiment = new DefaultExperiment(null);

        InteractionEvidence interaction = new DefaultInteractionEvidence("myInteraction");
        interaction.setExperiment(experiment);
        interaction.addParticipant(new DefaultParticipantEvidence(interactor1));
        interaction.addParticipant(new DefaultParticipantEvidence(interactor2));

        enricherContext.getConfig().setUpdateInteractionShortLabels(true);

        enricher.enrich(interaction);

        Assert.assertEquals("tusc-tusd", interaction.getShortName());
        Assert.assertEquals(83333, interactor2.getOrganism().getTaxId());
        Assert.assertEquals("strain k12", interactor2.getOrganism().getCommonName());
        Assert.assertEquals("tusc_ecoli", interactor1.getShortName());
    }

    @Test
    public void enrich_updateLabel2() throws EnricherException {
        Organism ecoli = new DefaultOrganism(83333, "lala");

        Protein interactor1 = new DefaultProtein("unk1", ecoli);
        interactor1.setUniprotkb("P45531");
        Interactor interactor2 = new DefaultProtein("EBI-12345", ecoli);

        Experiment experiment = new DefaultExperiment(null);

        InteractionEvidence interaction = new DefaultInteractionEvidence("myInteraction");
        interaction.setExperiment(experiment);
        interaction.addParticipant(new DefaultParticipantEvidence(interactor1));
        interaction.addParticipant(new DefaultParticipantEvidence(interactor2));

        enricherContext.getConfig().setUpdateInteractionShortLabels(true);

        enricher.enrich(interaction);

        Assert.assertEquals("tusc-ebi_12345", interaction.getShortName());
        Assert.assertEquals(83333, interactor2.getOrganism().getTaxId());
        Assert.assertEquals("strain k12", interactor2.getOrganism().getCommonName());
        Assert.assertEquals("tusc_ecoli", interactor1.getShortName());
    }
}