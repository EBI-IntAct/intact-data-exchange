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
import org.obo.datamodel.OBOSession;
import org.obo.dataadapter.OBOParseException;
import uk.ac.ebi.intact.dataexchange.cvutils.PSILoader;
import uk.ac.ebi.intact.dataexchange.cvutils.PsiLoaderException;
import uk.ac.ebi.intact.dataexchange.cvutils.OboUtils;
import uk.ac.ebi.intact.dataexchange.cvutils.model.IntactOntology;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvObjectOntologyBuilder;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.CvDagObject;

import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EnricherContext {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog(EnricherContext.class);

    private EnricherConfig config;
    private List<CvDagObject> ontology;

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

        InputStream ehcacheConfig = EnricherContext.class.getResourceAsStream("/META-INF/ehcache-enricher.xml");
        CacheManager.create(ehcacheConfig);
    }

    public EnricherConfig getConfig() {
        return config;
    }

    public void setConfig(EnricherConfig config) {
        this.config = config;
    }

    public Cache getCache(String name) {
        Cache cache = CacheManager.getInstance().getCache(name);

        if (cache == null) {
            throw new EnricherException("Cache not found: "+name);
        }

        return cache;
    }


    public List<CvDagObject> getIntactOntology() {
        if (ontology == null) {
            try {
                ontology = loadOntology();
            } catch (PsiLoaderException e) {
                throw new EnricherException(e);
            }
        }

        return ontology;
    }

    private List<CvDagObject> loadOntology() throws PsiLoaderException {
        final URL url;
        try {
            url = new URL(getConfig().getOboUrl());
        } catch (MalformedURLException e) {
            throw new EnricherException(e);
        }

        OBOSession oboSession = null;
        try {
            oboSession = OboUtils.createOBOSession(url);
        } catch (IOException e) {
            throw new EnricherException("Problem reading OBO file from URL: "+url, e);
        } catch (OBOParseException e) {
            throw new EnricherException("Problem parsing OBO file: "+url);
        }

        CvObjectOntologyBuilder builder = new CvObjectOntologyBuilder(oboSession);


        return builder.getAllCvs();
    }

    public void close() {
        if (log.isDebugEnabled()) log.debug("Clearing all caches from CacheManager");
        CacheManager.getInstance().clearAll();
    }

}