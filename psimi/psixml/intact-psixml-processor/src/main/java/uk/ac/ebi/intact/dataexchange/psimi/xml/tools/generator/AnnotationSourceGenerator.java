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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import psidev.psi.mi.annotations.PsiXmlElement;
import uk.ac.ebi.intact.annotation.util.AnnotationUtil;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.generator.metadata.ModelClassMetadata;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.generator.metadata.ModelClassMetadataFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Creates source validator files using the Annotations in the PSI XML model classes
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id:AnnotationSourceBuilder.java 8272 2007-04-25 10:20:12Z baranda $
 */
public class AnnotationSourceGenerator implements SourceGenerator {

    private PrintStream out;

    /**
     * Constructor
     */
    public AnnotationSourceGenerator() {
    }

    public void generateClasses(SourceGeneratorContext sgContext) throws Exception {
        generateClasses(sgContext, System.out);
    }

    public void generateClasses(SourceGeneratorContext sgContext, PrintStream out) throws Exception {
        this.out = out;

        Collection<Class> modelClasses = getModelClassesFromJars(sgContext.getDependencyJars());

        sgContext.createNewHelper(modelClasses);

        for (Class modelClass : modelClasses) {
            create(sgContext, modelClass);
        }

        if (modelClasses.isEmpty()) {
            out.println("No processors were generated");
        }
    }

    public void create(SourceGeneratorContext sgContext, Class modelClass) throws Exception {
        SourceGeneratorHelper helper = sgContext.getHelper();

        String processorClassName = helper.getValidatorNameForClass(modelClass);

        VelocityContext context = sgContext.getVelocityContext();

        context.put("packageName", sgContext.getGeneratedPackage());
        context.put("modelClass", modelClass);
        context.put("type", processorClassName);

        ModelClassMetadata modelClassMetadata = ModelClassMetadataFactory.createModelClassMetadata(sgContext, modelClass);
        context.put("mcm", modelClassMetadata);

        File outputFile = helper.getValidatorFileForClass(modelClass);

        Properties props = new Properties();
        props.setProperty("resource.loader", "class");
        props.setProperty("class." + VelocityEngine.RESOURCE_LOADER + ".class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        Velocity.init(props);

        Template template = Velocity.getTemplate("PsiProcessor.vm");

        // write the resulting file with velocity
        Writer writer = new FileWriter(outputFile);
        template.merge(context, writer);
        writer.close();

        out.println("Generated processor: " + processorClassName);
    }

    protected Collection<Class> getModelClassesFromJars(File[] jarFiles) {
        List<Class> modelClasses = new ArrayList<Class>();

        for (File jarFile : jarFiles) {
            Collection<Class> modelClassesInJar = getModelClassesFromJar(jarFile);

            if (!modelClassesInJar.isEmpty()) {
                out.println(modelClassesInJar.size() + " PsiXmlElemnt classes found in jar: " + jarFile);
            }

            modelClasses.addAll(modelClassesInJar);
        }

        return modelClasses;
    }

    protected Collection<Class> getModelClassesFromJar(File jarFile) {
        Collection<Class> modelClasses = null;

        try {
            // Looking for the annotation
            modelClasses = AnnotationUtil.getClassesWithAnnotationFromJar(PsiXmlElement.class, jarFile.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return modelClasses;
    }
}