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
package uk.ac.ebi.intact.dataexchange.psimi.xml.tools.generator.metadata;

import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.generator.metadata.field.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id:ModelClassMetadata.java 8272 2007-04-25 10:20:12Z baranda $
 */
public class ModelClassMetadata {

    private Class modelClass;
    private List<CollectionFieldMetadata> collections;
    private Set<Class> importedClasses;
    private List<FieldMetadata> individuals;
    private List<BooleanFieldMetadata> booleansWithMetadata;
    private List<Method> extensions;
    private List<NamesFieldMetadata> names;
    private List<NullValidationMetadata> nullValidationFields;

    public ModelClassMetadata(Class modelClass) {
        this.modelClass = modelClass;

        this.collections = new ArrayList<CollectionFieldMetadata>();
        this.individuals = new ArrayList<FieldMetadata>();
        this.importedClasses = new HashSet<Class>();

        addImportedClass(modelClass);
    }

    public Class getModelClass() {
        return modelClass;
    }

    public List<CollectionFieldMetadata> getCollections() {
        return collections;
    }

    public void setCollections(List<CollectionFieldMetadata> collections) {
        this.collections = collections;

        for (CollectionFieldMetadata col : collections) {
            addImportedClass(col.getGenericType());
        }
    }

    public List<FieldMetadata> getIndividuals() {
        return individuals;
    }

    public void setIndividuals(List<FieldMetadata> individuals) {
        this.individuals = individuals;

        for (FieldMetadata ind : individuals) {
            addImportedClass(ind.getType());
        }
    }

    public Set<Class> getImportedClasses() {
        return importedClasses;
    }

    public boolean addImportedClass(Class o) {
        return importedClasses.add(o);
    }

    public List<BooleanFieldMetadata> getBooleansWithMetadata() {
        return booleansWithMetadata;
    }

    public void setBooleansWithMetadata(List<BooleanFieldMetadata> booleansWithMetadata) {
        this.booleansWithMetadata = booleansWithMetadata;
    }

    public List<Method> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<Method> extensions) {
        this.extensions = extensions;
    }

    public List<NamesFieldMetadata> getNames() {
        return names;
    }

    public void setNames(List<NamesFieldMetadata> names) {
        this.names = names;

        for (NamesFieldMetadata name : names) {
            addImportedClass(name.getField().getType());
        }
    }

    public List<NullValidationMetadata> getNullValidationFields() {
        return nullValidationFields;
    }

    public void setNullValidationFields(List<NullValidationMetadata> nullValidationFields) {
        this.nullValidationFields = nullValidationFields;
    }
}