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

import psidev.psi.mi.xml.model.CvType;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.ConversionCache;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvObjectConverter<C extends CvObject, T extends CvType> extends AbstractIntactPsiConverter<C, T> {

    protected Class<C> intactCvClass;
    protected Class<T> psiCvClass;
    protected AliasConverter aliasConverter;
    protected XrefConverter xrefConverter;

    public CvObjectConverter(Institution institution, Class<C> intactCvClass, Class<T> psiCvClass) {
        super(institution);
        this.intactCvClass = intactCvClass;
        this.psiCvClass = psiCvClass;
        Class<?> aliasClass = AnnotatedObjectUtils.getAliasClassType(intactCvClass);
        Class<?> xrefClass = AnnotatedObjectUtils.getXrefClassType(intactCvClass);

        this.aliasConverter = new AliasConverter(institution, aliasClass);
        this.xrefConverter = new XrefConverter(institution, xrefClass);
    }

    public C psiToIntact(T psiObject) {
        psiStartConversion(psiObject);

        C cv = newCvInstance(intactCvClass);
        cv.setOwner(getInstitution());
        IntactConverterUtils.populateNames(psiObject.getNames(), cv, aliasConverter);
        IntactConverterUtils.populateXref(psiObject.getXref(), cv, xrefConverter);

        psiEndConversion(psiObject);

        return cv;
    }

    public T intactToPsi(C intactObject) {
        intactStartConversation(intactObject);

        T cvType = (T) ConversionCache.getElement(elementKey(intactObject));

        if (cvType != null) {
            return cvType;
        }

        cvType = newCvInstance(psiCvClass);

        // Set id, annotations, xrefs and aliases
        PsiConverterUtils.populateNames(intactObject, cvType, aliasConverter);
        PsiConverterUtils.populateXref(intactObject, cvType, xrefConverter);

        ConversionCache.putElement(elementKey(intactObject), cvType);

        intactEndConversion(intactObject);

        return cvType;
    }


    protected static <C> C newCvInstance(Class<C> cvClass) {
        C cv = null;
        try {
            cv = cvClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return cv;
    }

    protected String elementKey(C intactObject) {
        return intactObject.getIdentifier() + "_" + intactObject.getClass();
    }

    @Override
    public void setInstitution(Institution institution){
        super.setInstitution(institution);
        this.aliasConverter.setInstitution(institution, getInstitutionPrimaryId());
        this.xrefConverter.setInstitution(institution, getInstitutionPrimaryId());
    }

    @Override
    public void setInstitution(Institution institution, String institId){
        super.setInstitution(institution, institId);
        this.aliasConverter.setInstitution(institution, institId);
        this.xrefConverter.setInstitution(institution, institId);

    }

    @Override
    public void setCheckInitializedCollections(boolean check){
        super.setCheckInitializedCollections(check);
        this.aliasConverter.setCheckInitializedCollections(check);
        this.xrefConverter.setCheckInitializedCollections(check);
    }
}