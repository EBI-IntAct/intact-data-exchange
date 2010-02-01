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
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.bridges.taxonomy.OLSTaxonomyService;
import uk.ac.ebi.intact.bridges.taxonomy.TaxonomyService;
import uk.ac.ebi.intact.bridges.taxonomy.TaxonomyServiceException;
import uk.ac.ebi.intact.bridges.taxonomy.TaxonomyTerm;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherException;
import uk.ac.ebi.intact.dataexchange.enricher.cache.EnricherCache;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class BioSourceFetcher {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog(BioSourceFetcher.class);

    @Autowired
    private EnricherContext enricherContext;

    private TaxonomyService taxonomyService;

    public BioSourceFetcher() {
    }

    public TaxonomyTerm fetchByTaxId(int taxId) {
        if (taxonomyService == null) {
            taxonomyService = new OLSTaxonomyService();
        }
        
        EnricherCache bioSourceCache = enricherContext.getCacheManager().getCache("BioSource");

        TaxonomyTerm term = null;

        if (bioSourceCache.isKeyInCache(taxId)) {
            term = (TaxonomyTerm) bioSourceCache.get(taxId);
        }

        if (term == null) {
            try {
                term = taxonomyService.getTaxonomyTerm(taxId);
            } catch (TaxonomyServiceException e) {
                throw new EnricherException(e);
            }
            bioSourceCache.put(taxId, term);
        }

        return term;
    }

}
