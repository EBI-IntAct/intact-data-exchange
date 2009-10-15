/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.enricher.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EnricherCacheManager {

    private Map<String,EnricherCache> caches;

    public EnricherCacheManager() {
        caches = new HashMap<String,EnricherCache>(8);
    }

    public EnricherCache getCache(String name) {
        EnricherCache cache;

        if (caches.containsKey(name)) {
            return caches.get(name);
        } else {
            cache = new EnricherCache();
            caches.put(name, cache);
        }

        return cache;
    }

}
