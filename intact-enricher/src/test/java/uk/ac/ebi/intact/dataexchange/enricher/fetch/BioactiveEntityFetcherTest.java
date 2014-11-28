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
import psidev.psi.mi.jami.model.BioactiveEntity;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherBasicTestCase;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.cache.EnricherCache;

import javax.annotation.Resource;
import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BioactiveEntityFetcherTest extends EnricherBasicTestCase {

    @Resource(name = "intactBioactiveEntityFetcher")
    private BioactiveEntityFetcher fetcher;

    @Autowired
    private EnricherContext enricherContext;

    @Test
    public void fetchFromChebi() throws Exception {
        final EnricherCache cache = enricherContext.getCacheManager().getCache("BioactiveEntity");
        cache.clearStatistics();

        Collection<BioactiveEntity> smallMoleculeEntity = fetcher.fetchByIdentifier("CHEBI:16851");
        Assert.assertEquals(1, smallMoleculeEntity.size() );
        Assert.assertEquals( "CHEBI:16851", smallMoleculeEntity.iterator().next().getChebi() );
        Assert.assertEquals( "1-phosphatidyl-1D-myo-inositol 3,5-bisphosphate", smallMoleculeEntity.iterator().next().getShortName()  );

        smallMoleculeEntity = fetcher.fetchByIdentifier( "CHEBI:16851" );
        Assert.assertEquals(1, smallMoleculeEntity.size() );
        Assert.assertEquals( "CHEBI:16851", smallMoleculeEntity.iterator().next().getChebi()   );
        Assert.assertEquals( "1-phosphatidyl-1D-myo-inositol 3,5-bisphosphate", smallMoleculeEntity.iterator().next().getShortName()   );


        Assert.assertEquals( 1, cache.getInMemoryHits() );

    }
}