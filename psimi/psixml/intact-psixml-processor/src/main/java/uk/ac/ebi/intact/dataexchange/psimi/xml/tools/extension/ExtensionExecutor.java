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

import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.Phase;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.PsiProcessReport;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.extension.annotation.PsiExtensionMethod;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.validator.ValidationReport;

import java.lang.reflect.Method;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExtensionExecutor {

    public static void execute(ExtensionContext context, Object extension, String methodName) throws ExtensionExecutionException {
        Method method = null;
        try {
            method = extension.getClass().getMethod(methodName, new Class[0]);
        } catch (NoSuchMethodException e) {
            throw new ExtensionExecutionException(e);
        }
        execute(context, extension, method);
    }

    public static void execute(ExtensionContext context, Object extension, Method method) throws ExtensionExecutionException {
        PsiExtensionMethod methodAnnot = method.getAnnotation(PsiExtensionMethod.class);

        // check if this method has to be executed in this phase, if not, just return
        Phase currentPhase = context.getCurrentPhase();
        if (methodAnnot.phase() != currentPhase)
        {
            return;
        }

        PsiProcessReport report = context.getProcessReport();
        ValidationReport validationReport = report.getValidationReport();

        // check "onlyExecuteIfValid"
        if (methodAnnot.onlyExecuteIfValid()) {
            if (validationReport.isValid()) {
                invokeMethod(extension, method);
            }
        } else {
            invokeMethod(extension, method);
        }
    }

    private static void invokeMethod(Object extension, Method method) throws ExtensionExecutionException {
        try {
            method.invoke(extension, null);
        } catch (Exception e) {
            throw new ExtensionExecutionException(e);
        }
    }
}