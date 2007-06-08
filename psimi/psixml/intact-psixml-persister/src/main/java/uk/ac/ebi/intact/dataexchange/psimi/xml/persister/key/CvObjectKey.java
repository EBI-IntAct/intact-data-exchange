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
package uk.ac.ebi.intact.dataexchange.psimi.xml.persister.key;

import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.Xref;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvObjectKey extends AnnotatedObjectKey<CvObject> {

    public CvObjectKey(CvObject annotatedObject) {
        super(annotatedObject);
    }

    @Override
    protected String generateKey(CvObject annotatedObject) {
        Xref xref = CvObjectUtils.getPsiMiIdentityXref(annotatedObject);

        String key;

        if (xref != null) {
            key = xref.getPrimaryId() + "_" + annotatedObject.getClass().getSimpleName();
        } else {
            key = annotatedObject.getShortLabel() + "_" + annotatedObject.getClass().getSimpleName();
        }

        return key;
    }
}