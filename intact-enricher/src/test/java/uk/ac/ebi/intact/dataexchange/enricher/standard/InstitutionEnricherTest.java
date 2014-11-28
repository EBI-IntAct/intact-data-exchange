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
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.model.Source;
import psidev.psi.mi.jami.model.impl.DefaultSource;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherBasicTestCase;

import javax.annotation.Resource;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */

public class InstitutionEnricherTest extends EnricherBasicTestCase {

    @Resource(name = "intactInstitutionEnricher")
    private InstitutionEnricher enricher;

    @Test
    public void enrich_intact() throws EnricherException {
        Source ebi = new DefaultSource("intact");

        enricher.enrich(ebi);

        Assert.assertEquals("intact", ebi.getShortName());
        Assert.assertEquals("MI:0469", ebi.getMIIdentifier());
    }

    @Test
    public void enrich_mint() throws EnricherException {
        Source mint = new DefaultSource("mint");

        enricher.enrich(mint);

        Assert.assertEquals("mint", mint.getShortName());
        Assert.assertEquals("MI:0471", mint.getMIIdentifier());
    }
    
    @Test
    public void enrich_dip() throws EnricherException {
        Source dip = new DefaultSource("dip");

        enricher.enrich(dip);

        Assert.assertEquals("dip", dip.getShortName());
        Assert.assertEquals("MI:0465", dip.getMIIdentifier());
    }

}