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
import net.sf.ehcache.Element;
import uk.ac.ebi.intact.bridges.taxonomy.NewtTaxonomyService;
import uk.ac.ebi.intact.bridges.taxonomy.TaxonomyService;
import uk.ac.ebi.intact.bridges.taxonomy.TaxonomyServiceException;
import uk.ac.ebi.intact.bridges.taxonomy.TaxonomyTerm;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherException;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BioSourceFetcher {

    private static ThreadLocal<BioSourceFetcher> instance = new ThreadLocal<BioSourceFetcher>() {
        @Override
        protected BioSourceFetcher initialValue() {
            return new BioSourceFetcher();
        }
    };

    public static BioSourceFetcher getInstance() {
        return instance.get();
    }

    private TaxonomyService taxonomyService;

    public BioSourceFetcher() {
        this.taxonomyService = new NewtTaxonomyService();
    }

    public TaxonomyTerm fetchByTaxId(int taxId) {
        Cache bioSourceCache = EnricherContext.getInstance().getCache("BioSource");

        TaxonomyTerm term;

        if (bioSourceCache.isKeyInCache(taxId)) {
            term = (TaxonomyTerm) bioSourceCache.get(taxId).getObjectValue();
        } else {
            try {
                term = taxonomyService.getTaxonomyTerm(taxId);
            } catch (TaxonomyServiceException e) {
                throw new EnricherException(e);
            }
            bioSourceCache.put(new Element(taxId, term));
        }

        return term;
    }

}