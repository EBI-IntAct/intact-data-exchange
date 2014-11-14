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
import psidev.psi.mi.jami.bridges.europubmedcentral.EuroPubmedCentralFetcher;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.model.Publication;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.cache.EnricherCache;

/**
 * Intact extension of publication fetcher
 *
 */
@Component(value = "intactPublicationFetcher")
@Lazy
public class PublicationFetcher extends EuroPubmedCentralFetcher{

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog(BioSourceFetcher.class);

    @Autowired
    private EnricherContext enricherContext;

    public PublicationFetcher() throws BridgeFailedException {
        super();
    }

    @Override
    public Publication fetchByIdentifier(String id, String source) throws BridgeFailedException {
        EnricherCache publicationCache = enricherContext.getCacheManager().getCache("Publication");
        String key = id+"_"+source;
        if (publicationCache.isKeyInCache(key)) {
            return (Publication) publicationCache.get(key);
        }
        else{
            Publication term = super.fetchByIdentifier(id, source);
            publicationCache.put(key, term);
            return term;
        }
    }
}
