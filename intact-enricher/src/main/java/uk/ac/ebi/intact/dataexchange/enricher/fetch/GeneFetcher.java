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
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.uniprot.UniprotGeneFetcher;
import psidev.psi.mi.jami.model.Gene;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.cache.EnricherCache;

import java.util.Collection;

/**
 * Gene Fetcher.
 *
 * @version $Id$
 */
@Component(value = "intactGeneFetcher")
@Lazy
public class GeneFetcher extends UniprotGeneFetcher{

    private static final Log log = LogFactory.getLog(GeneFetcher.class);

    @Autowired
    private EnricherContext enricherContext;

    public GeneFetcher() {
        super();
    }

    @Override
    public Collection<Gene> fetchByIdentifier(String identifier, int taxID) throws BridgeFailedException {
        EnricherCache geneCache = enricherContext.getCacheManager().getCache("Gene");
        String key = identifier+"_"+taxID;
        if (geneCache.isKeyInCache(key)) {
            return (Collection<Gene>) geneCache.get(key);
        }
        else{
            Collection<Gene> terms = super.fetchByIdentifier(identifier, taxID);
            geneCache.put(key, terms);
            return terms;
        }
    }

    @Override
    public Collection<Gene> fetchByIdentifier(String identifier) throws BridgeFailedException {
        EnricherCache geneCache = enricherContext.getCacheManager().getCache("Gene");
        String key = identifier;
        if (geneCache.isKeyInCache(key)) {
            return (Collection<Gene>) geneCache.get(key);
        }
        else{
            Collection<Gene> terms = super.fetchByIdentifier(identifier);
            geneCache.put(key, terms);
            return terms;
        }
    }
}