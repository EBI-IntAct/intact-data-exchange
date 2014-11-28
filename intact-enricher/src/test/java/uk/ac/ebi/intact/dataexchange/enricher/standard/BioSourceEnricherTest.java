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
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Organism;
import psidev.psi.mi.jami.model.impl.DefaultCvTerm;
import psidev.psi.mi.jami.model.impl.DefaultOrganism;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherBasicTestCase;

import javax.annotation.Resource;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BioSourceEnricherTest extends EnricherBasicTestCase {

    @Resource(name = "intactBioSourceEnricher")
    private BioSourceEnricher enricher;

    @Test
    public void enrich_default() throws Exception {
        Organism human = new DefaultOrganism(9606, "unknown");

        enricher.enrich(human);

        Assert.assertEquals("human", human.getCommonName());
    }

    @Test
    public void enrich_noCommonName() throws Exception {
        Organism unculturedBacterium = new DefaultOrganism(77133, "unknown");

        enricher.enrich(unculturedBacterium);

        Assert.assertEquals("uncultured bacterium", unculturedBacterium.getCommonName());
    }

    @Test
    public void enrich_longShortLabel() throws Exception {
        Organism organism = new DefaultOrganism(224325, "Unknown");
        enricher.enrich(organism);

        Assert.assertEquals("arcfu", organism.getCommonName());
        Assert.assertEquals("Archaeoglobus fulgidus (strain ATCC 49558 / VC-16 / DSM 4304 / JCM 9628 / NBRC 100126)", organism.getScientificName());
    }

    @Test
    public void enrich_withCellType() throws Exception {
        Organism human = new DefaultOrganism(9606, "human_custom");
        CvTerm cellType = new DefaultCvTerm("customId", "custom_cellType", (String)null);
        human.setCellType(cellType);

        enricher.enrich(human);

        Assert.assertEquals("human-customid", human.getCommonName());
    }
}