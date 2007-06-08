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
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import psidev.psi.mi.xml.model.DbReference;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiMiPopulator;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class XrefConverter<X extends Xref> extends AbstractIntactPsiConverter<X, DbReference> {

    private Class<X> xrefClass;

    public XrefConverter(Institution institution, Class<X> xrefType) {
        super(institution);
        this.xrefClass = xrefType;
    }

    public X psiToIntact(DbReference psiObject) {
        String primaryId = psiObject.getId();
        String secondaryId = psiObject.getSecondary();
        String dbRelease = psiObject.getVersion();

        PsiMiPopulator psiMiPopulator = new PsiMiPopulator();

        String db = psiObject.getDb();
        String dbAc = psiObject.getDbAc();

        CvDatabase cvDb = new CvDatabase(getInstitution(), db);

        if (dbAc != null && dbAc.startsWith("MI")) {
            psiMiPopulator.populateWithPsiMi(cvDb, dbAc);
        }

        String refType = psiObject.getRefType();
        String refTypeAc = psiObject.getRefTypeAc();

        CvXrefQualifier xrefQual = null;

        if (refType != null) {
            xrefQual = new CvXrefQualifier(getInstitution(), refType);

            if (refTypeAc != null) {
                psiMiPopulator.populateWithPsiMi(xrefQual, refTypeAc);
            }
        }


        X xref = newXrefInstance(xrefClass, cvDb, primaryId, secondaryId, dbRelease, xrefQual);

        return xref;
    }

    public DbReference intactToPsi(Xref intactObject) {
        DbReference dbRef = new DbReference();
        dbRef.setDb(intactObject.getCvDatabase().getShortLabel());
        dbRef.setId(intactObject.getPrimaryId());
        dbRef.setSecondary(intactObject.getSecondaryId());
        dbRef.setVersion(intactObject.getDbRelease());

        if (intactObject.getCvXrefQualifier() != null) {
            dbRef.setRefType(intactObject.getCvXrefQualifier().getShortLabel());
        }

        CvObjectXref cvDatabasePsiMiXref = CvObjectUtils.getPsiMiIdentityXref(intactObject.getCvDatabase());
        if (cvDatabasePsiMiXref != null) {
            dbRef.setDbAc(CvObjectUtils.getPsiMiIdentityXref(intactObject.getCvDatabase()).getPrimaryId());
        }

        if (intactObject.getCvXrefQualifier() != null) {
            CvObjectXref cvRefTypePsiMiXref = CvObjectUtils.getPsiMiIdentityXref(intactObject.getCvXrefQualifier());
            if (cvRefTypePsiMiXref != null) {
                dbRef.setRefTypeAc(cvRefTypePsiMiXref.getPrimaryId());
            }
        }

        return dbRef;
    }

    private static <X extends Xref> X newXrefInstance(Class<X> xrefClass, CvDatabase db, String primaryId, String secondaryId, String dbRelease, CvXrefQualifier cvXrefQual) {
        X xref = null;
        try {
            xref = xrefClass.newInstance();
            xref.setCvDatabase(db);
            xref.setPrimaryId(primaryId);
            xref.setSecondaryId(secondaryId);
            xref.setDbRelease(dbRelease);
            xref.setCvXrefQualifier(cvXrefQual);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return xref;
    }


}