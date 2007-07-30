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
package uk.ac.ebi.intact.dataexchange.enricher.standard;

import uk.ac.ebi.intact.dataexchange.cvutils.model.CvTerm;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.CvObjectFetcher;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.CvObjectXref;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvObjectEnricher extends AnnotatedObjectEnricher<CvObject> {

    private static ThreadLocal<CvObjectEnricher> instance = new ThreadLocal<CvObjectEnricher>() {
        @Override
        protected CvObjectEnricher initialValue() {
            return new CvObjectEnricher();
        }
    };

    public static CvObjectEnricher getInstance() {
        return instance.get();
    }

    protected CvObjectEnricher() {
    }

    public void enrich(CvObject objectToEnrich) {
        CvObjectXref identityXref = CvObjectUtils.getPsiMiIdentityXref(objectToEnrich);

        String mi = identityXref.getPrimaryId();

        CvTerm term = CvObjectFetcher.getInstance().fetchByTermId(mi);

        objectToEnrich.setShortLabel(term.getShortName());
        objectToEnrich.setFullName(term.getFullName());
    }

    public void close() {
    }

}