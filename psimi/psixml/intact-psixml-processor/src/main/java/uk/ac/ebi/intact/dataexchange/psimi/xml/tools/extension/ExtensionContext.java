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
package uk.ac.ebi.intact.dataexchange.psimi.xml.tools.extension;

import uk.ac.ebi.intact.annotation.util.AnnotationUtil;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.Phase;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.PsiProcessReport;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.extension.annotation.PsiExtension;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.extension.annotation.PsiExtensionContext;

import java.lang.reflect.Field;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExtensionContext<T> {

    private T element;
    private PsiProcessReport processReport;
    private Phase currentPhase;

    public ExtensionContext(T element, PsiProcessReport processReport, Phase currentPhase) {
        this.element = element;
        this.processReport = processReport;
        this.currentPhase = currentPhase;
    }

    public void injectIntoExtension(Object extension) throws ContextInjectionException {
        Class extensionClass = extension.getClass();

        if (!extensionClass.isAnnotationPresent(PsiExtension.class)) {
            throw new ContextInjectionException("Class " + extensionClass + " is not a valid PSI extension, since it does not have the @PsiExtension annotation");
        }

        List<Field> extensionContextFields = AnnotationUtil.declaredFieldsWithAnnotation(extensionClass, PsiExtensionContext.class);

        for (Field extensionContextField : extensionContextFields) {
            try {
                extensionContextField.setAccessible(true);
                extensionContextField.set(extension, this);
            } catch (IllegalAccessException e) {
                throw new ContextInjectionException(e, extension);
            }
        }
    }

    public Object getElement() {
        return element;
    }

    public void setElement(T element) {
        this.element = element;
    }

    public PsiProcessReport getProcessReport() {
        return processReport;
    }

    public void setProcessReport(PsiProcessReport processReport) {
        this.processReport = processReport;
    }

    public Phase getCurrentPhase()
    {
        return currentPhase;
    }
}