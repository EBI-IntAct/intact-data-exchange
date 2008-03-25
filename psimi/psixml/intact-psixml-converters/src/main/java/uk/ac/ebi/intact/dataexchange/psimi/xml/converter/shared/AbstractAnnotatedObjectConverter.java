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

import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.ConversionCache;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.XrefUtils;

import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractAnnotatedObjectConverter<A extends AnnotatedObject, T> extends AbstractIntactPsiConverter<A, T> {

    private Class<? extends A> intactClass;
    private Class<T> psiClass;
    private boolean newIntactObjectCreated;
    private boolean newPsiObjectCreated;

    public AbstractAnnotatedObjectConverter(Institution institution, Class<? extends A> intactClass, Class<T> psiClass) {
        super(institution);
        this.intactClass = intactClass;
        this.psiClass = psiClass;
    }


    public A psiToIntact(T psiObject) {
        A intactObject = (A) ConversionCache.getElement(psiObject);
        
        if (intactObject != null) {
            newIntactObjectCreated = false;
            return intactObject;
        }

        intactObject = newIntactObjectInstance(psiObject);

        if (!(intactObject instanceof Institution)) {
            intactObject.setOwner(getInstitution());
        }

        ConversionCache.putElement(psiObject, intactObject);

        newIntactObjectCreated = true;

        return intactObject;
    }


    public T intactToPsi(A intactObject) {
        T psiObject = (T) ConversionCache.getElement(intactObject);

        if (psiObject != null) {
            newPsiObjectCreated = false;
            return psiObject;
        }

        // ac - create a xref to the institution db
        String ac = intactObject.getAc();
        if (ac != null)  {
            boolean containsAcXref = false;
            for (Xref xref : (Collection<Xref>) intactObject.getXrefs()) {
                if (intactObject.getAc().equals(xref.getPrimaryId())) {
                    containsAcXref = true;
                    break;
                }
            }

            if (!containsAcXref) {
                String dbMi = null;
                String db = null;

                // calculate the owner of the interaction, based on the AC prefix first,
                // then in the defaultInstitutionForACs if passed to the ConverterContext or,
                // finally to the Institution in the source section of the PSI-XML
                if (ac.startsWith("EBI")) {
                    dbMi = Institution.INTACT_REF;
                    db = Institution.INTACT.toLowerCase();
                } else if (ac.startsWith("MINT")) {
                    dbMi = Institution.MINT_REF;
                    db = Institution.MINT.toLowerCase();
                } else if (ConverterContext.getInstance().getDefaultInstitutionForAcs() != null){
                    Institution defaultInstitution = ConverterContext.getInstance().getDefaultInstitutionForAcs();
                    dbMi = calculateInstitutionPrimaryId(defaultInstitution);
                    db = defaultInstitution.getShortLabel().toLowerCase();
                } else {
                    dbMi = getInstitutionPrimaryId();
                    db = getInstitution().getShortLabel().toLowerCase();
                }

                CvXrefQualifier sourceRef = CvObjectUtils.createCvObject(getInstitution(), CvXrefQualifier.class,
                        CvXrefQualifier.SOURCE_REFERENCE_MI_REF, CvXrefQualifier.SOURCE_REFERENCE);
                CvDatabase cvDb = CvObjectUtils.createCvObject(getInstitution(), CvDatabase.class,
                        dbMi, db);

                Xref xref = XrefUtils.newXrefInstanceFor(intactClass);
                xref.setCvXrefQualifier(sourceRef);
                xref.setCvDatabase(cvDb);
                xref.setPrimaryId(intactObject.getAc());
                xref.setSecondaryId(intactObject.getShortLabel());
                intactObject.addXref(xref);
            }
        }

        psiObject = newInstance(psiClass);
        PsiConverterUtils.populate(intactObject, psiObject);


        ConversionCache.putElement(intactObject, psiObject);

        newPsiObjectCreated = true;

        return psiObject;
    }

    protected A newIntactObjectInstance(T psiObject) {
        return newInstance(intactClass);
    }

    private static <T> T newInstance(Class<T> clazz) {
        T instance = null;
        try {
            instance = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return instance;
    }

    protected boolean isNewIntactObjectCreated() {
        return newIntactObjectCreated;
    }

    protected boolean isNewPsiObjectCreated() {
        return newPsiObjectCreated;
    }
}