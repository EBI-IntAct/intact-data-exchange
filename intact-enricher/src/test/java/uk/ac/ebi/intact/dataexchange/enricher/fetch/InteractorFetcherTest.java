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
import uk.ac.ebi.chebi.webapps.chebiWS.model.Entity;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherBasicTestCase;
import uk.ac.ebi.intact.dataexchange.enricher.cache.EnricherCache;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.uniprot.model.UniprotProtein;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorFetcherTest extends EnricherBasicTestCase {

    @Autowired
    private InteractorFetcher fetcher;

    @Autowired
    private EnricherContext enricherContext;

    @Test
    public void fetchFromUniprot() throws Exception {
        final EnricherCache cache = enricherContext.getCacheManager().getCache("Interactor");
        cache.clearStatistics();

        UniprotProtein uniprotProtein = fetcher.fetchInteractorFromUniprot("MK01_HUMAN", 9606);
        Assert.assertNotNull(uniprotProtein);
        Assert.assertEquals("P28482", uniprotProtein.getPrimaryAc());
        
        uniprotProtein = fetcher.fetchInteractorFromUniprot("MK01_HUMAN", 9606);
        Assert.assertNotNull(uniprotProtein);
        Assert.assertEquals("P28482", uniprotProtein.getPrimaryAc());

        Assert.assertEquals(1, cache.getInMemoryHits());
    }

    @Test
    public void fetchFromChebi() throws Exception {
        final EnricherCache cache = enricherContext.getCacheManager().getCache("Interactor");
        cache.clearStatistics();

        Entity smallMoleculeEntity = fetcher.fetchInteractorFromChebi( "CHEBI:16851" );
        Assert.assertNotNull( smallMoleculeEntity );
        Assert.assertEquals( "CHEBI:16851", smallMoleculeEntity.getChebiId() );
        Assert.assertEquals( "1-phosphatidyl-1D-myo-inositol 3,5-bisphosphate", smallMoleculeEntity.getChebiAsciiName() );

        smallMoleculeEntity = fetcher.fetchInteractorFromChebi( "CHEBI:16851" );
        Assert.assertEquals( "CHEBI:16851", smallMoleculeEntity.getChebiId() );
        Assert.assertEquals( "1-phosphatidyl-1D-myo-inositol 3,5-bisphosphate", smallMoleculeEntity.getChebiAsciiName() );


        Assert.assertEquals( 1, cache.getInMemoryHits() );

    }
}