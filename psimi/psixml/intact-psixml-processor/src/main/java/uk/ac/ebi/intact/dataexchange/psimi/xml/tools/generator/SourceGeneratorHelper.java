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
package uk.ac.ebi.intact.dataexchange.psimi.xml.tools.generator;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id:SourceBuilderHelper.java 8272 2007-04-25 10:20:12Z baranda $
 */
public class SourceGeneratorHelper {

    private Map<Class, String> modelClassToProcessorName;
    private Map<Class, String> modelClassToProcessorFilename;

    private SourceGeneratorContext sgContext;

    SourceGeneratorHelper(Collection<Class> modelClasses, SourceGeneratorContext sgContext) {
        this.sgContext = sgContext;

        File outputDir = createOutputDir();

        modelClassToProcessorName = new HashMap(modelClasses.size());
        modelClassToProcessorFilename = new HashMap(modelClasses.size());

        for (Class modelClass : modelClasses) {
            String validatorClassName = validatorNameForClass(modelClass);
            String validatorClassFile = filenameForClass(modelClass);

            modelClassToProcessorName.put(modelClass, validatorClassName);
            modelClassToProcessorFilename.put(modelClass, validatorClassFile);
        }
    }

    public String getValidatorNameForClass(Class modelClass) {
        return modelClassToProcessorName.get(modelClass);
    }

    public String getValidatorFilenameForClass(Class modelClass) {
        return modelClassToProcessorFilename.get(modelClass);
    }

    public File getValidatorFileForClass(Class modelClass) {
        return new File(sgContext.getOutputDir(), getValidatorFilenameForClass(modelClass));
    }

    private String validatorNameForClass(Class modelClass) {
        return modelClass.getSimpleName() + "Processor";
    }

    private String filenameForClass(Class modelClass) {
        return validatorNameForClass(modelClass) + ".java";
    }

    private File createOutputDir() {
        sgContext.getOutputDir().mkdirs();

        return sgContext.getOutputDir();
    }
}