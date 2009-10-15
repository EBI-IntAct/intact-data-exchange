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

import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EnricherCache {

    private Map<Object,Object> map = new WeakHashMap(512);

    long inMemoryHits = 0;

    public boolean isKeyInCache(Object key) {
        return map.containsKey(key);
    }

    public Object put(Object key, Object value) {
        return map.put(key, value);
    }

    public Object get(Object key) {
        inMemoryHits++;
        return map.get(key);
    }

    public long getInMemoryHits() {
        return inMemoryHits;
    }

    public void clearStatistics() {
        inMemoryHits = 0;
    }
}
