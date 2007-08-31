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
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.ConversionCache;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Institution;

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
        A intactObject = (A) ConversionCache.getElement(psiElementKey(psiObject));
        
        if (intactObject != null) {
            newIntactObjectCreated = false;
            return intactObject;
        }

        intactObject = newIntactObjectInstance(psiObject);

        ConversionCache.putElement(psiElementKey(psiObject), intactObject);

        newIntactObjectCreated = true;

        return intactObject;
    }

    public T intactToPsi(A intactObject) {
        T psiObject = (T) ConversionCache.getElement(intactElementKey(intactObject));

        if (psiObject != null) {
            newPsiObjectCreated = false;
            return psiObject;
        }

        psiObject = newInstance(psiClass);
        PsiConverterUtils.populate(intactObject, psiObject);

        ConversionCache.putElement(intactElementKey(intactObject), psiObject);

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

    protected String intactElementKey(A intactObject) {
        return intactObject.getShortLabel() + "_" + intactObject.getClass();
    }

    protected abstract String psiElementKey(T psiObject);

    protected boolean isNewIntactObjectCreated() {
        return newIntactObjectCreated;
    }

    protected boolean isNewPsiObjectCreated() {
        return newPsiObjectCreated;
    }
}