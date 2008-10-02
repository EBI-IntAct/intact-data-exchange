/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.psimitab;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import uk.ac.ebi.intact.util.ols.OlsClient;
import uk.ac.ebi.ook.web.services.Query;

import java.io.InputStream;
import java.rmi.RemoteException;

/**
 * The InterproNameHandler gets the Information from a OLS-Service or a Map.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public class GoTermHandler {

    private Query query;

    private Cache cache;

    public GoTermHandler() {
        query = new OlsClient().getOntologyQuery();

        InputStream ehcacheConfig = GoTermHandler.class.getResourceAsStream("/META-INF/ehcache-go.xml");
        CacheManager cacheManager = CacheManager.create(ehcacheConfig);

        final String cacheName = "go";
        cache = cacheManager.getCache(cacheName);

        if (cache == null) {
            throw new IllegalStateException("Cache not found: "+ cacheName);
        }

    }

    public String getNameById( String goTerm ) throws RemoteException {
        String result;

        if (cache.isKeyInCache(goTerm) && cache.get(goTerm) != null) {
            result = (String) cache.get(goTerm).getObjectValue();
        } else {
            result = query.getTermById( goTerm, "GO" );

            if (result != null) {
                cache.put(new Element(goTerm, result));
            }
        }

        return result;
    }
}
