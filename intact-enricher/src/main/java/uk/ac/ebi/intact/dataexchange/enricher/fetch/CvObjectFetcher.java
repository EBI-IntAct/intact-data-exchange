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
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvTerm;
import uk.ac.ebi.intact.dataexchange.cvutils.model.IntactOntology;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.model.CvObject;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvObjectFetcher {

    private static ThreadLocal<CvObjectFetcher> instance = new ThreadLocal<CvObjectFetcher>() {
        @Override
        protected CvObjectFetcher initialValue() {
            return new CvObjectFetcher();
        }
    };

    public static CvObjectFetcher getInstance() {
        return instance.get();
    }

    public CvObjectFetcher() {
    }

    public CvTerm fetchByTermId(String termId) {
        Cache cache = EnricherContext.getInstance().getCache("CvObject");

        CvTerm term;

        if (cache.isKeyInCache(termId)) {
            term = (CvTerm) cache.get(termId).getObjectValue();
        } else {
            IntactOntology ontology = EnricherContext.getInstance().getIntactOntology();
            term = ontology.search(termId);

            if (term != null) {
                cache.put(new Element(termId, term));
            }
        }

        return term;
    }

    public CvTerm fetchByShortLabel(Class<? extends CvObject> cvClass, String label) {
        if (cvClass == null || label == null) {
            return null;
        }
        
        Cache cache = EnricherContext.getInstance().getCache("CvObject");

        String key = cvClass+"_"+label;

        CvTerm term = null;

        if (cache.isKeyInCache(key)) {
            term = (CvTerm) cache.get(label).getObjectValue();
        } else {
            IntactOntology ontology = EnricherContext.getInstance().getIntactOntology();

            for (CvTerm candidateTerm : ontology.getCvTerms(cvClass)) {
                if (candidateTerm.getShortName().equals(label)) {
                    term = candidateTerm;
                }
            }

            if (term != null) {
                cache.put(new Element(key, term));
                cache.put(new Element(term.getId(), term));
            }
        }

        return term;
    }

}