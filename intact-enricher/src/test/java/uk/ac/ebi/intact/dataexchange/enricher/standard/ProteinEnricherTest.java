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
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.model.Organism;
import psidev.psi.mi.jami.model.Protein;
import psidev.psi.mi.jami.model.impl.DefaultOrganism;
import psidev.psi.mi.jami.model.impl.DefaultProtein;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherBasicTestCase;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherConfig;

import javax.annotation.Resource;

/**
 * InteractorEnricher Tester.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */

public class ProteinEnricherTest extends EnricherBasicTestCase {

    @Resource(name = "intactProteinEnricher")
    private ProteinEnricher enricher;

    @Autowired
    private EnricherConfig config;

    @Test
    public void enrich_uniprot_enabled() throws EnricherException {
        Organism human = new DefaultOrganism(9606, "human");
        Protein protein = new DefaultProtein("unknownName", human);
        protein.setUniprotkb("P18850");

        enricher.enrich(protein);

        Assert.assertEquals("atf6a_human", protein.getShortName());
        Assert.assertEquals("Cyclic AMP-dependent transcription factor ATF-6 alpha", protein.getFullName());
        Assert.assertNotNull(protein.getSequence());
    }

    @Test
    public void enrich_uniprot_noXrefs() throws EnricherException {
        Organism human = new DefaultOrganism(9606, "human");
        Protein protein = new DefaultProtein("atf6a_human", human);

        enricher.enrich(protein);

        // don't enrich when no cross references

        Assert.assertEquals("atf6a_human", protein.getShortName());
        //Assert.assertEquals("Cyclic AMP-dependent transcription factor ATF-6 alpha", protein.getFullName());
        Assert.assertEquals(null, protein.getFullName());

        Assert.assertTrue(protein.getIdentifiers().isEmpty());
    }

    @Test
    public void enrich_alias() throws EnricherException {
        Organism ecoli = new DefaultOrganism(83333, "ecoli");
        Protein protein = new DefaultProtein("tusb_ecoli", ecoli);
        protein.setUniprotkb("P45530");

        enricher.enrich(protein);

        Assert.assertEquals("tusB", protein.getGeneName());
    }

    @Test
    public void enrich_invalidLabel() throws EnricherException {
        Organism human = new DefaultOrganism(9606, "human");
        Protein protein = new DefaultProtein("EBI-12345", human);

        enricher.enrich(protein);

        Assert.assertEquals("ebi12345", protein.getShortName());
    }

    @Test
    public void enrich_uniprot_biosource() throws EnricherException {
        Organism human = new DefaultOrganism(9606, "human");
        Protein protein = new DefaultProtein("unknownName", human);
        protein.setUniprotkb("P18850");

        enricher.enrich(protein);

        Assert.assertEquals("atf6a_human", protein.getShortName());
        Assert.assertEquals("Cyclic AMP-dependent transcription factor ATF-6 alpha", protein.getFullName());
        Assert.assertNotNull(protein.getSequence());
        Assert.assertEquals(9606, protein.getOrganism().getTaxId());
        Assert.assertEquals("human", protein.getOrganism().getCommonName());
    }
}
