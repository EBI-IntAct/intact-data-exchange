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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.persister.DefaultEntityStateCopier;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.CvObjectFetcher;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class CvObjectEnricher extends AnnotatedObjectEnricher<CvObject> {

    @Autowired
    private CvObjectFetcher cvObjectFetcher;
    
    public CvObjectEnricher() {
    }

    public void enrich(CvObject objectToEnrich) {
        String id = CvObjectUtils.getIdentity(objectToEnrich);

        CvObject referenceTerm;

        if (id != null) {
            referenceTerm = cvObjectFetcher.fetchByTermId(objectToEnrich.getClass(), id);
        } else {
            referenceTerm = cvObjectFetcher.fetchByShortLabel(objectToEnrich.getClass(), objectToEnrich.getShortLabel());
        }

        if (referenceTerm != null) {
            objectToEnrich.setShortLabel(referenceTerm.getShortLabel());
            objectToEnrich.setFullName(referenceTerm.getFullName());

            DefaultEntityStateCopier copier = new DefaultEntityStateCopier();
            copier.copy(referenceTerm, objectToEnrich);
        }

    }

}
