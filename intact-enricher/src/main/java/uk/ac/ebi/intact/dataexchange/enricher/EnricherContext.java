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
import org.obo.dataadapter.OBOParseException;
import org.obo.datamodel.OBOSession;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.dataexchange.cvutils.OboUtils;
import uk.ac.ebi.intact.dataexchange.cvutils.PsiLoaderException;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvObjectOntologyBuilder;
import uk.ac.ebi.intact.dataexchange.enricher.cache.EnricherCacheManager;
import uk.ac.ebi.intact.model.CvDagObject;

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
public class EnricherContext implements DisposableBean {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog(EnricherContext.class);

    @Autowired
    private EnricherCacheManager enricherCacheManager;

    private EnricherConfig config;
    private List<CvDagObject> ontology;

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
        try {
            destroy();
        } catch (Exception e) {
            throw new EnricherException(e);
        }
    }

    public void destroy() throws Exception {
        if (log.isDebugEnabled()) log.debug("Clearing all caches from CacheManager");
        CacheManager.getInstance().clearAll();
    }
}