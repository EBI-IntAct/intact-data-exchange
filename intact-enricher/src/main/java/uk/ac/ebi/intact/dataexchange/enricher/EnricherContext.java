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

import net.sf.ehcache.CacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.dataexchange.enricher.cache.EnricherCacheManager;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EnricherContext implements DisposableBean {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog(EnricherContext.class);

    @Autowired
    private EnricherCacheManager enricherCacheManager;

    private EnricherConfig config;

    public EnricherContext(EnricherConfig enricherConfig) {
        this.config = enricherConfig;
    }

    public EnricherConfig getConfig() {
        return config;
    }

    public void setConfig(EnricherConfig config) {
        this.config = config;
    }

    public EnricherCacheManager getCacheManager() {
        return enricherCacheManager;
    }

    public void close() {
        try {
            destroy();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot destroy enricher context", e);
        }
    }

    public void destroy() throws Exception {
        if (log.isDebugEnabled()) log.debug("Clearing all caches from CacheManager");
        CacheManager.getInstance().clearAll();
    }
}