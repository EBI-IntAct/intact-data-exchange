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
import uk.ac.ebi.intact.persistence.util.CgLibUtil;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvObjectKey extends AnnotatedObjectKey<CvObject> {

    private CvObject annotatedObject;
    private String shortLabel;
    private String primaryId;
    private Class cvClass;

    public CvObjectKey(CvObject annotatedObject) {
        super(annotatedObject);
        init(annotatedObject);
    }

    private void init(CvObject annotatedObject) {
       this.shortLabel = annotatedObject.getShortLabel();
        this.cvClass = CgLibUtil.removeCglibEnhanced(annotatedObject.getClass());

        if (cvClass == null) {
            throw new NullPointerException("cvClass");
        }

        Xref xref = CvObjectUtils.getPsiMiIdentityXref(annotatedObject);
        if (xref != null) {
            this.primaryId = xref.getPrimaryId();
        }
    }

    @Override
    protected String generateKey(CvObject annotatedObject) {
        init(annotatedObject);
        return toString();
    }

    public Class getCvClass() {
        return cvClass;
    }

    public String getPrimaryId() {
        return primaryId;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    @Override
    public String toString() {
        if (primaryId != null) {
            return primaryId + "_" + cvClass.getSimpleName();
        }

       return shortLabel + "_" + cvClass.getSimpleName();
    }
}