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
package uk.ac.ebi.intact.psixml.tools.generator.metadata.field;

import uk.ac.ebi.intact.psixml.tools.generator.metadata.util.PsiReflectionUtils;

import java.lang.reflect.Field;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id:CollectionMetadata.java 8272 2007-04-25 10:20:12Z baranda $
 */
public class FieldMetadata {

    private String processorClassName;
    private Field field;

    public FieldMetadata(Field field) {
        this.field = field;
    }

    public FieldMetadata(Field field, String processorClassName) {
        this.processorClassName = processorClassName;
        this.field = field;
    }

    public String getProcessorClassName() {
        return processorClassName;
    }

    public void setProcessorClassName(String processorClassName) {
        this.processorClassName = processorClassName;
    }

    public Field getField() {
        return field;
    }

    public String getGetterMethodName() {
        return PsiReflectionUtils.getReadMethodForProperty(field).getName();
    }

    public Class getType() {
        return field.getDeclaringClass();
    }
}