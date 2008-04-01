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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.Xref;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AnnotatedObjectEnricher<T extends AnnotatedObject<?,?>> implements Enricher<T> {

    private static final Log log = LogFactory.getLog( AnnotatedObjectEnricher.class );

    public void enrich(T objectToEnrich) {
        CvObjectEnricher cvObjectEnricher = CvObjectEnricher.getInstance();

        for (Xref xref : objectToEnrich.getXrefs()) {
            if (xref.getCvXrefQualifier() != null) {
                cvObjectEnricher.enrich(xref.getCvXrefQualifier());
            }
            cvObjectEnricher.enrich(xref.getCvDatabase());
        }

        if (!(objectToEnrich instanceof Institution)) {
            InstitutionEnricher.getInstance().enrich(objectToEnrich.getOwner());
        }
    }

    public void close() {
        if (log.isDebugEnabled()) log.debug("Closing "+getClass().getSimpleName());
        
        EnricherContext.getInstance().close();
    }

}