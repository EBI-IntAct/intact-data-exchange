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
import org.junit.Ignore;
import org.junit.Test;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.model.impl.*;
import psidev.psi.mi.jami.utils.CvTermUtils;
import psidev.psi.mi.jami.utils.InteractorUtils;
import psidev.psi.mi.jami.utils.RangeUtils;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherBasicTestCase;

import javax.annotation.Resource;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ComponentEnricherTest extends EnricherBasicTestCase {

    @Resource(name = "intactParticipantEvidenceEnricher")
    private ComponentEnricher enricher;

    @Test
    public void enrich_default() throws Exception {
        ParticipantEvidence comp = new DefaultParticipantEvidence(InteractorUtils.createUnknownBasicInteractor());
        comp.setExpressedInOrganism(null);

        enricher.enrich(comp);

        Assert.assertNull(comp.getExpressedInOrganism());
    }

    @Test
    public void enrich_expressedIn() throws Exception {
        Organism human = new DefaultOrganism(9606, "unknown");
        ParticipantEvidence comp = new DefaultParticipantEvidence(InteractorUtils.createUnknownBasicInteractor());
        comp.setExpressedInOrganism(human);

        enricher.enrich(comp);

        Assert.assertEquals("human", comp.getExpressedInOrganism().getCommonName());
    }

    @Test
    public void enrich_cvs() throws Exception {
        ParticipantEvidence comp = new DefaultParticipantEvidence(InteractorUtils.createUnknownBasicInteractor());

        CvTerm cvExperimentalPrep = new DefaultCvTerm("nothing", "MI:0350");
        cvExperimentalPrep.setFullName("nothing");
        comp.getExperimentalPreparations().add(cvExperimentalPrep);

        enricher.enrich(comp);

        CvTerm enrichedExperimentalPreparation = comp.getExperimentalPreparations().iterator().next();
        Assert.assertEquals("purified", enrichedExperimentalPreparation.getShortName());
    }

    @Test
    @Ignore
    public void enrich_range_nTerminal() throws Exception {
        Organism human = new DefaultOrganism(9606, "unknown");
        Protein protein = new DefaultProtein("unknownName", human);
        protein.setUniprotkb("P18850");

        ParticipantEvidence comp = new DefaultParticipantEvidence(protein);

        FeatureEvidence feature = new DefaultFeatureEvidence("aFeature", null);
        feature.setType(CvTermUtils.createMICvTerm("experimental feature detection","MI:0659"));
        feature.getRanges().add(RangeUtils.createNTerminusRange());

        enricher.enrich(comp);

        Assert.assertEquals(1, feature.getRanges().iterator().next().getStart().getStart());
        Assert.assertEquals(1, feature.getRanges().iterator().next().getEnd().getEnd());
    }

    @Test
    @Ignore
    public void enrich_range_cTerminal() throws Exception {
        Organism human = new DefaultOrganism(9606, "unknown");
        Protein protein = new DefaultProtein("unknownName", human);
        protein.setUniprotkb("P18850");

        ParticipantEvidence comp = new DefaultParticipantEvidence(protein);

        FeatureEvidence feature = new DefaultFeatureEvidence("aFeature", null);
        feature.setType(CvTermUtils.createMICvTerm("experimental feature detection","MI:0659"));
        feature.getRanges().add(RangeUtils.createCTerminusRange(50));

        enricher.enrich(comp);

        Assert.assertEquals(670, feature.getRanges().iterator().next().getStart().getStart());
        Assert.assertEquals(670, feature.getRanges().iterator().next().getEnd().getEnd());
    }
}