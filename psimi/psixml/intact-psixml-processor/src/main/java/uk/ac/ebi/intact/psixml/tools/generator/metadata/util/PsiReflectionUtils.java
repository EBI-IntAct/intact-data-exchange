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
package uk.ac.ebi.intact.psixml.tools.generator.metadata.util;

import psidev.psi.mi.annotations.*;
import psidev.psi.mi.xml.model.Names;
import uk.ac.ebi.intact.annotation.util.AnnotationUtil;
import uk.ac.ebi.intact.psixml.tools.extension.annotation.PsiExtension;
import uk.ac.ebi.intact.psixml.tools.extension.annotation.PsiExtensionMethod;
import uk.ac.ebi.intact.psixml.tools.generator.SourceGeneratorContext;
import uk.ac.ebi.intact.psixml.tools.generator.metadata.ModelClassMetadata;
import uk.ac.ebi.intact.psixml.tools.generator.metadata.field.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiReflectionUtils {

    public static <M extends FieldMetadata> List<M> fieldsFrom(Class<M> metadataClass, Class fieldType,
                                                               Class<? extends Annotation> annotationClass,
                                                               ModelClassMetadata modelClassMetadata,
                                                               SourceGeneratorContext context
    ) {
        List<M> fields = new ArrayList<M>();

        for (Field field : fieldsOfType(modelClassMetadata, fieldType)) {
            M fieldMetadata = AnnotationFieldMetadataFactory.newFieldMetadata(metadataClass, annotationClass, field, context, modelClassMetadata);
            fields.add(fieldMetadata);
        }

        return fields;
    }

    public static List<BooleanFieldMetadata> booleanFieldsFrom(ModelClassMetadata modelClassMetadata) {
        return fieldsFrom(BooleanFieldMetadata.class, Boolean.class, PsiBooleanField.class, modelClassMetadata, null);
    }

    public static List<NamesFieldMetadata> namesFieldsFrom(ModelClassMetadata modelClassMetadata) {
        return fieldsFrom(NamesFieldMetadata.class, Names.class, PsiNamesField.class, modelClassMetadata, null);
    }

    public static List<NullValidationMetadata> nullValidationFieldsFrom(ModelClassMetadata modelClassMetadata) {
        return fieldsFrom(NullValidationMetadata.class, null, NullValidation.class, modelClassMetadata, null);
    }

    public static List<FieldMetadata> individualsFrom(SourceGeneratorContext context, ModelClassMetadata modelClassMetadata) {
        List<FieldMetadata> individuals = new ArrayList<FieldMetadata>();

        for (Field field : fieldsWithModelClasses(modelClassMetadata)) {
            FieldMetadata fm = AnnotationFieldMetadataFactory.newFieldMetadata(FieldMetadata.class, PsiField.class, field, context, modelClassMetadata);
            individuals.add(fm);
        }

        return individuals;
    }

    /**
     * Using reflection, gets the collections from the model class provided and create CollectionMetaData
     */
    public static List<CollectionFieldMetadata> collectionsFrom(SourceGeneratorContext context, ModelClassMetadata modelClassMetadata) {
        List<CollectionFieldMetadata> collections = new ArrayList<CollectionFieldMetadata>();

        for (Field field : fieldsOfType(modelClassMetadata, Collection.class)) {
            Type genType = field.getGenericType();

            if (genType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genType;

                Class typeOfCollection = (Class) pt.getActualTypeArguments()[0];

                if (typeOfCollection.isAnnotationPresent(PsiXmlElement.class)) {

                    CollectionFieldMetadata cm = AnnotationFieldMetadataFactory.newCollectionFieldMetadata(typeOfCollection, field, context, modelClassMetadata);
                    collections.add(cm);
                }
            }
        }

        return collections;
    }

    public static List<Method> discoverPsiExtensionMethodsForClass(Class modelClass, SourceGeneratorContext context) {

        Collection<Class> psiExtensionClasses = PsiExtensionsCache.getPsiExtensionClasses(context);

        // get the methods with the @PsiExtensionMethods
        List<Method> psiExtensionMethods = new ArrayList<Method>();

        for (Class psiExtensionClass : psiExtensionClasses) {

            PsiExtension psiExtensionAnnot = (PsiExtension) psiExtensionClass.getAnnotation(PsiExtension.class);

            if (psiExtensionAnnot.forClass().equals(modelClass)) {
                psiExtensionMethods.addAll(AnnotationUtil.methodsWithAnnotation(psiExtensionClass, PsiExtensionMethod.class));
            }
        }

        return psiExtensionMethods;
    }


    public static Method getReadMethodForProperty(Field field) {
        return getReadMethodForProperty(field.getName(), field.getDeclaringClass());
    }

    public static Method getReadMethodForProperty(String propName, Class beanClazz) {

        Field field;
        try {
            field = beanClazz.getDeclaredField(propName);
        } catch (NoSuchFieldException e) {
            throw new MetadataException("Field '" + propName + "' does not exist in class: " + beanClazz.getName(), e);
        }

        String methodName = "get" + capitalize(propName);


        if (field.getType().isAssignableFrom(Boolean.class)) {
            methodName = "is" + capitalize(propName);

            // special case: there are fields in the model with property field name "is...", the getter has the same name
            if (propName.startsWith("is")) {
                methodName = propName;
            }
        }

        Method readMethod = null;

        // check if exists
        try {
            readMethod = beanClazz.getMethod(methodName, null);
        } catch (NoSuchMethodException e) {
            throw new MetadataException("Read method for property '" + propName + "' was expected and does not exist: " + beanClazz.getName() + "." + methodName, e);
        }

        return readMethod;
    }

    /**
     * Creates a List with the Fields of a certain type.
     *
     * @param modelClassMetadata
     * @param type               if null is passes, all the fields will be retrieved
     *
     * @return
     */
    public static List<Field> fieldsOfType(ModelClassMetadata modelClassMetadata, Class type) {
        List<Field> fields = new ArrayList<Field>();

        for (Field field : modelClassMetadata.getModelClass().getDeclaredFields()) {
            if (type == null) {
                fields.add(field);
                continue;
            }

            if (type.equals(field.getType())) {
                fields.add(field);
            }
        }

        return fields;
    }

    public static List<Field> fieldsWithModelClasses(ModelClassMetadata modelClassMetadata) {
        List<Field> fields = new ArrayList<Field>();

        for (Field field : modelClassMetadata.getModelClass().getDeclaredFields()) {
            Class clazz = field.getType();

            if (clazz.isAnnotationPresent(PsiXmlElement.class)) {
                fields.add(field);
            }
        }

        return fields;
    }

    /**
     * Returns a String which capitalizes the first letter of the string.
     */
    public static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

}