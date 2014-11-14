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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.bridges.chebi.ChebiFetcher;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.model.BioactiveEntity;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.cache.EnricherCache;

import java.util.Collection;

/**
 * Bioactive entity Fetcher.
 *
 * @version $Id: InteractorFetcher.java 16883 2011-07-28 13:22:38Z mdumousseau@yahoo.com $
 */
@Component(value = "intactBioactiveEntityFetcher")
@Lazy
public class BioactiveEntityFetcher extends ChebiFetcher{

    private static final Log log = LogFactory.getLog(BioactiveEntityFetcher.class);

    @Autowired
    private EnricherContext enricherContext;

    public BioactiveEntityFetcher() {
        super();
    }

    @Override
    public Collection<BioactiveEntity> fetchByIdentifier(String identifier) throws BridgeFailedException {
        EnricherCache entityCache = enricherContext.getCacheManager().getCache("BioactiveEntity");

        if (entityCache.isKeyInCache(identifier)) {
            return (Collection<BioactiveEntity>) entityCache.get(identifier);
        }
        else{
            Collection<BioactiveEntity> terms = super.fetchByIdentifier(identifier);
            entityCache.put(identifier, terms);
            return terms;
        }
    }
}