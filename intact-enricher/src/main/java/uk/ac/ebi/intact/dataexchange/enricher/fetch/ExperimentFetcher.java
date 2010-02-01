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
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherException;
import uk.ac.ebi.intact.dataexchange.enricher.cache.EnricherCache;
import uk.ac.ebi.intact.util.cdb.ExperimentAutoFill;
import uk.ac.ebi.intact.util.cdb.PublicationNotFoundException;
import uk.ac.ebi.intact.util.cdb.UnexpectedException;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentFetcher {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog(BioSourceFetcher.class);

    @Autowired
    private EnricherContext enricherContext;

    public ExperimentFetcher() {
    }

    public ExperimentAutoFill fetchByPubmedId(String pubmedId) {
        EnricherCache experimentCache = enricherContext.getCacheManager().getCache("Experiment");

        ExperimentAutoFill autoFill = null;

        if (experimentCache.isKeyInCache(pubmedId)) {
            autoFill = (ExperimentAutoFill) experimentCache.get(pubmedId);
        }

        if (autoFill == null) {
            try {
                autoFill = new ExperimentAutoFill(pubmedId);
            } catch (PublicationNotFoundException e) {
                throw new EnricherException(e);
            } catch (UnexpectedException e) {
                throw new EnricherException(e);
            } finally {
                experimentCache.put(pubmedId, autoFill);
            }
        }

        return autoFill;
    }

}
