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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.uniprot.model.UniprotProtein;
import uk.ac.ebi.intact.uniprot.service.UniprotRemoteService;
import uk.ac.ebi.intact.uniprot.service.UniprotService;

import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorFetcher {

    private static final Log log = LogFactory.getLog(InteractorFetcher.class);

    private static ThreadLocal<InteractorFetcher> instance = new ThreadLocal<InteractorFetcher>() {
        @Override
        protected InteractorFetcher initialValue() {
            return new InteractorFetcher();
        }
    };

    public static InteractorFetcher getInstance() {
        return instance.get();
    }

    public InteractorFetcher() {
    }

    public UniprotProtein fetchInteractorFromUniprot(String uniprotId, int taxId) {
        if (uniprotId == null) {
            throw new NullPointerException("Trying to fetch a protein with null uniprotId");
        }

        Cache interactorCache = CacheManager.getInstance().getCache("Interactor");

        if (interactorCache == null) {
            throw new IllegalStateException("Interactor cache was not found, when fetching: "+uniprotId);
        }

        UniprotProtein uniprotProtein = null;

        String cacheKey = cacheKey(uniprotId, taxId);

        if (interactorCache.isKeyInCache(cacheKey)) {
            try {
                uniprotProtein = (UniprotProtein) interactorCache.get(cacheKey).getObjectValue();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        if (uniprotProtein == null) {
            if (log.isDebugEnabled()) log.debug("\t\tRemotely retrieving protein: "+uniprotId+" (taxid:"+taxId+")");

            UniprotService service = new UniprotRemoteService();
            Collection<UniprotProtein> uniprotProteins = service.retrieve(uniprotId);

            // if only one result, return it. If more, return the one that matches the tax id
            if (uniprotProteins.size() == 1) {
                uniprotProtein = uniprotProteins.iterator().next();
            } else {
                 for (UniprotProtein candidate : uniprotProteins) {
                    if (candidate.getOrganism().getTaxid() == taxId) {
                        uniprotProtein = candidate;
                        break;
                    }
                }
            }

            if (uniprotProtein != null) {
                interactorCache.put(new Element(cacheKey(uniprotId, taxId), uniprotProtein));
            }
        }

        return uniprotProtein;
    }

    private String cacheKey(String uniprotId, int taxId) {
        return uniprotId+"_"+taxId;
    }

}