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
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.*;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiMiPopulator;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

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

        if (primaryId.length() == 0) {
            throw new PsiConversionException("Id in DbReference is empty: "+psiObject);
        }

        fixPubmedReferenceAsIdentityToPrimaryRef(psiObject);

        PsiMiPopulator psiMiPopulator = new PsiMiPopulator(getInstitution());

        String db = psiObject.getDb();
        String dbAc = psiObject.getDbAc();

        CvDatabase cvDb = new CvDatabase(getInstitution(), db);
        cvDb.setMiIdentifier(dbAc);

        if (dbAc != null) {
            psiMiPopulator.populateWithPsiMi(cvDb, dbAc);
        }

        String refType = psiObject.getRefType();
        String refTypeAc = psiObject.getRefTypeAc();

        CvXrefQualifier xrefQual = null;

        if (refType != null) {
            xrefQual = new CvXrefQualifier(getInstitution(), refType);
            xrefQual.setMiIdentifier(refTypeAc);

            if (refTypeAc != null) {
                psiMiPopulator.populateWithPsiMi(xrefQual, refTypeAc);
            }
        }

        X xref = newXrefInstance(xrefClass, cvDb, primaryId, secondaryId, dbRelease, xrefQual);
        xref.setOwner(getInstitution());

        return xref;
    }

    public DbReference intactToPsi(Xref intactObject) {
        fixPubmedReferenceAsIdentityToPrimaryRef(intactObject);

        DbReference dbRef = new DbReference();
        dbRef.setDb(intactObject.getCvDatabase().getShortLabel());
        dbRef.setId(intactObject.getPrimaryId());
        dbRef.setSecondary(intactObject.getSecondaryId());
        dbRef.setVersion(intactObject.getDbRelease());

        if (intactObject.getCvXrefQualifier() != null) {
            dbRef.setRefType(intactObject.getCvXrefQualifier().getShortLabel());
            dbRef.setRefTypeAc(intactObject.getCvXrefQualifier().getMiIdentifier());
        }

        if (intactObject.getCvDatabase() != null) {
            dbRef.setDbAc(intactObject.getCvDatabase().getMiIdentifier());
            dbRef.setDb(intactObject.getCvDatabase().getShortLabel());
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
            throw new PsiConversionException(e);
        }

        return xref;
    }

     protected void fixPubmedReferenceAsIdentityToPrimaryRef(Xref xref) {
         if (CvDatabase.PUBMED_MI_REF.equals(xref.getCvDatabase().getMiIdentifier())
                 && CvXrefQualifier.IDENTITY_MI_REF.equals(xref.getCvXrefQualifier().getMiIdentifier())) {
             CvXrefQualifier primaryRef = CvObjectUtils.createCvObject(xref.getOwner(), CvXrefQualifier.class,
                     CvXrefQualifier.PRIMARY_REFERENCE_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE);
             xref.setCvXrefQualifier(primaryRef);

            final ConverterMessage converterMessage = new ConverterMessage(MessageLevel.WARN, "Incorrect cross refernece to Pubmed that had qualifier 'identity'. Changed to 'primary-reference",
                    ConverterContext.getInstance().getLocation().getCurrentLocation());
            converterMessage.setAutoFixed(true);
            ConverterContext.getInstance().getReport().getMessages().add(converterMessage);
         }
     }

    protected void fixPubmedReferenceAsIdentityToPrimaryRef(DbReference dbRef) {
         if (CvDatabase.PUBMED_MI_REF.equals(dbRef.getDbAc())
                 && CvXrefQualifier.IDENTITY_MI_REF.equals(dbRef.getRefTypeAc())) {
             dbRef.setRefTypeAc(CvXrefQualifier.PRIMARY_REFERENCE_MI_REF);
             dbRef.setRefType(CvXrefQualifier.PRIMARY_REFERENCE);

            final ConverterMessage converterMessage = new ConverterMessage(MessageLevel.WARN, "Incorrect cross refernece to Pubmed that had qualifier 'identity'. Changed to 'primary-reference",
                    ConverterContext.getInstance().getLocation().getCurrentLocation());
            converterMessage.setAutoFixed(true);
            ConverterContext.getInstance().getReport().getMessages().add(converterMessage);
         }
     }

}