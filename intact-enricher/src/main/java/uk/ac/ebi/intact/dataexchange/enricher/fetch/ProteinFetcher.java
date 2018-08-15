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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.uniprot.UniprotProteinFetcher;
import psidev.psi.mi.jami.model.Protein;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.cache.EnricherCache;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * Protein Fetcher.
 *
 * @version $Id$
 */
@Component(value = "intactProteinFetcher")
@Lazy
public class ProteinFetcher extends UniprotProteinFetcher{

    private static final Logger log = Logger.getLogger(ProteinFetcher.class.getName());

    @Autowired
    private EnricherContext enricherContext;

    public ProteinFetcher() {
        super();
    }

    @Override
    public Collection<Protein> fetchByIdentifier(String identifier) throws BridgeFailedException {
        EnricherCache proteinCache = enricherContext.getCacheManager().getCache("Protein");

        if (proteinCache.isKeyInCache(identifier)) {
            return (Collection<Protein>) proteinCache.get(identifier);
        }
        else{
            Collection<Protein> terms = super.fetchByIdentifier(identifier);
            proteinCache.put(identifier, terms);
            return terms;
        }
    }
}