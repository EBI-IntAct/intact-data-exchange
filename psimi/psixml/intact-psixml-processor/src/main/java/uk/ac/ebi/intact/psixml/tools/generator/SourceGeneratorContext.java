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
package uk.ac.ebi.intact.psixml.tools.generator;

import org.apache.velocity.VelocityContext;

import java.io.File;
import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id:SourceBuilderContext.java 8272 2007-04-25 10:20:12Z baranda $
 */
public class SourceGeneratorContext {

    private File[] dependencyJars;
    private ClassLoader dependencyClassLoader;

    private String generatedPackage;
    private File targetPath;
    private VelocityContext velocityContext;
    private SourceGeneratorHelper helper;

    public SourceGeneratorContext(VelocityContext context, String generatedPackage, File targetPath) {
        this.velocityContext = context;
        this.generatedPackage = generatedPackage;
        this.targetPath = targetPath;
    }

    public void createNewHelper(Collection<Class> modelClasses) {
        this.helper = new SourceGeneratorHelper(modelClasses, this);
    }

    public File getOutputDir() {
        String packageDir = generatedPackage.replaceAll("\\.", "/");
        File outputDir = new File(targetPath, packageDir);

        return outputDir;
    }

    public File[] getDependencyJars() {
        return dependencyJars;
    }

    public void setDependencyJars(File... dependencyJars) {
        this.dependencyJars = dependencyJars;
    }

    public String getGeneratedPackage() {
        return generatedPackage;
    }

    public File getTargetPath() {
        return targetPath;
    }

    public VelocityContext getVelocityContext() {
        return velocityContext;
    }

    public SourceGeneratorHelper getHelper() {
        if (helper == null) {
            throw new RuntimeException("SourceGeneratorHElper has not been created. Call createNewHelper first.");
        }
        return helper;
    }

    public void setHelper(SourceGeneratorHelper helper) {
        this.helper = helper;
    }

    public ClassLoader getDependencyClassLoader() {
        if (dependencyClassLoader == null) {
            return Thread.currentThread().getContextClassLoader();
        }
        return dependencyClassLoader;
    }

    public void setDependencyClassLoader(ClassLoader dependencyClassLoader) {
        this.dependencyClassLoader = dependencyClassLoader;
    }
}