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
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.intact.uniprot.model.UniprotProtein;
import net.sf.ehcache.CacheManager;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorFetcherTest {

    private InteractorFetcher fetcher;

    @Before
    public void before() throws Exception {
        fetcher = InteractorFetcher.getInstance();
    }

    @Test
    public void fetchFromUniprot() throws Exception {
        CacheManager.getInstance().getCache("Interactor").getStatistics().clearStatistics();
        
        UniprotProtein uniprotProtein = fetcher.fetchInteractorFromUniprot("MK01_HUMAN", 9606);
        Assert.assertNotNull(uniprotProtein);
        Assert.assertEquals("P28482", uniprotProtein.getPrimaryAc());
        
        uniprotProtein = fetcher.fetchInteractorFromUniprot("MK01_HUMAN", 9606);
        Assert.assertNotNull(uniprotProtein);
        Assert.assertEquals("P28482", uniprotProtein.getPrimaryAc());

        Assert.assertEquals(1, CacheManager.getInstance().getCache("Interactor").getStatistics().getInMemoryHits());
    }
}