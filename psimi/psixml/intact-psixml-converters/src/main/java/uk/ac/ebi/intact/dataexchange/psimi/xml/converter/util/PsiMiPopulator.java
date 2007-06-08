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
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiMiPopulator {

    private Institution institution;
    private CvDatabase cvDatabase;
    private CvXrefQualifier cvXrefQualifier;

    public PsiMiPopulator() {
    }

    public <X extends Xref> void populateWithPsiMi(AnnotatedObject annotatedObject, String psiMi) {
        this.institution = annotatedObject.getOwner();

        Class<? extends Xref> xrefClass = AnnotatedObjectUtils.getXrefClassType(annotatedObject.getClass());

        X xref = (X) createPsiMiXref(xrefClass, psiMi);

        annotatedObject.getXrefs().add(xref);

    }

    private <X extends Xref> X createPsiMiXref(Class<X> xrefClass, String psiMi) {
        if (xrefClass == null) {
            throw new NullPointerException("xrefClass");
        }

        X xref;
        try {
            xref = xrefClass.newInstance();
        } catch (Exception e) {
            throw new IntactException("Problems instantiating Xref of type: " + xrefClass.getName());
        }
        xref.setOwner(institution);
        xref.setPrimaryId(psiMi);
        xref.setCvDatabase(createCvDatabase());
        xref.setCvXrefQualifier(createIdentityCvXrefQualifier());

        return xref;
    }

    private CvDatabase createCvDatabase() {
        if (cvDatabase != null) {
            return cvDatabase;
        }

        cvDatabase = new CvDatabase(institution, CvDatabase.PSI_MI);

        CvObjectXref xref = createPsiMiXref(CvObjectXref.class, CvDatabase.PSI_MI_MI_REF);
        cvDatabase.addXref(xref);

        return cvDatabase;

    }

    private CvXrefQualifier createIdentityCvXrefQualifier() {
        if (cvXrefQualifier != null) {
            return cvXrefQualifier;
        }

        cvXrefQualifier = new CvXrefQualifier(institution, CvXrefQualifier.IDENTITY);

        CvObjectXref xref = createPsiMiXref(CvObjectXref.class, CvXrefQualifier.IDENTITY_MI_REF);
        cvXrefQualifier.addXref(xref);

        return cvXrefQualifier;
    }

}