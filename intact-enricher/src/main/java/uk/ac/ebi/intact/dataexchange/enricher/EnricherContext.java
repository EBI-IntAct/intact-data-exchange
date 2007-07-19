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
package uk.ac.ebi.intact.dataexchange.enricher;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EnricherContext {

    private EnricherConfig config;
    private CacheManager cacheManager;

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog(EnricherContext.class);

    private static ThreadLocal<EnricherContext> instance = new ThreadLocal<EnricherContext>() {
        @Override
        protected EnricherContext initialValue() {
            return new EnricherContext();
        }
    };

    public static EnricherContext getInstance() {
        return instance.get();
    }

    private EnricherContext() {
        this.config = new EnricherConfig();


        InputStream ehcacheConfig = EnricherContext.class.getResourceAsStream("/ehcache-enricher.xml");
        this.cacheManager = CacheManager.create(ehcacheConfig);
    }

    public EnricherConfig getConfig() {
        return config;
    }

    public Cache getCache(String name) {
        return cacheManager.getCache(name);
    }

    public void loadCvOntology() {

    }

}