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
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.CvObjectXref;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.ConversionCache;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AbstractCvConverter<C extends CvObject, T extends CvType> extends AbstractIntactPsiConverter<C, T> {

    private Class<C> intactCvClass;
    private Class<T> psiCvClass;

    public AbstractCvConverter(Institution institution, Class<C> intactCvClass, Class<T> psiCvClass) {
        super(institution);
        this.intactCvClass = intactCvClass;
        this.psiCvClass = psiCvClass;
    }

    public C psiToIntact(T psiObject) {
        C cv = newCvInstance(intactCvClass);
        cv.setOwner(getInstitution());
        IntactConverterUtils.populateNames(psiObject.getNames(), cv);
        IntactConverterUtils.populateXref(psiObject.getXref(), cv, new XrefConverter<CvObjectXref>(getInstitution(), CvObjectXref.class));

        return cv;
    }

    public T intactToPsi(C intactObject) {
        T cvType = (T) ConversionCache.getElement(elementKey(intactObject));

        if (cvType != null) {
            return cvType;
        }

        cvType = newCvInstance(psiCvClass);
        PsiConverterUtils.populate(intactObject, cvType);

        ConversionCache.putElement(elementKey(intactObject), cvType);

        return cvType;
    }

    private static <C> C newCvInstance(Class<C> cvClass) {
        C cv = null;
        try {
            cv = cvClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return cv;
    }

    private String elementKey(C intactObject) {
        return intactObject.getShortLabel() + "_" + intactObject.getClass();
    }
}