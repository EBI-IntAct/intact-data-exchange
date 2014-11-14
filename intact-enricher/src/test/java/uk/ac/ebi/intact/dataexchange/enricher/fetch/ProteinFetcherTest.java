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
package uk.ac.ebi.intact.dataexchange.enricher.fetch;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import psidev.psi.mi.jami.model.Protein;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherBasicTestCase;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.cache.EnricherCache;

import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ProteinFetcherTest extends EnricherBasicTestCase {

    @Autowired
    @Qualifier("intactProteinFetcher")
    private ProteinFetcher fetcher;

    @Autowired
    private EnricherContext enricherContext;

    @Test
    public void fetchFromUniprot() throws Exception {
        final EnricherCache cache = enricherContext.getCacheManager().getCache("Protein");
        cache.clearStatistics();

        Collection<Protein> uniprotProtein = fetcher.fetchByIdentifier("MK01_HUMAN");
        Assert.assertEquals(1, uniprotProtein.size());
        Assert.assertEquals("P28482", uniprotProtein.iterator().next().getUniprotkb());
        
        uniprotProtein = fetcher.fetchByIdentifier("MK01_HUMAN");
        Assert.assertEquals(1, uniprotProtein.size());
        Assert.assertEquals("P28482", uniprotProtein.iterator().next().getUniprotkb());

        Assert.assertEquals(1, cache.getInMemoryHits());
    }
}