/**
 * Copyright 2006 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.xml.generator;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.velocity.VelocityContext;
import uk.ac.ebi.intact.plugin.IntactAbstractMojo;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.generator.SourceGenerator;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.generator.SourceGeneratorContext;

import java.io.File;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Example mojo. This mojo is executed when the goal "mygoal" is called.
 * Change this comments and the goal name accordingly
 *
 * @goal generate-processors
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class PsiProcessorGeneratorMojo
        extends IntactAbstractMojo {

    private static final List<String> DEFAULT_EXCLUDED_GROUPIDS = Arrays.asList(
            new String[]{"commons-logging", "commons-collections",
                         "log4j", "org.hibernate", "xerces"});

    /**
     * Project instance
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * project-helper instance, used to make addition of resources
     * simpler.
     *
     * @component
     */
    private MavenProjectHelper helper;

    /**
     * Path where the classes will be generated
     *
     * @parameter default-value="${project.build.directory}/generated"
     * @required
     */
    private String targetPath;

    /**
     * Path where the classes will be generated
     *
     * @parameter default-value="uk.ac.ebi.intact.dataexchange.psimi.xml.generated"
     * @required
     */
    private String generatedPackage;

    /**
     * Path where the classes will be generated
     *
     * @parameter default-value="uk.ac.ebi.intact.dataexchange.psimi.xml.tools.generator.AnnotationSourceGenerator"
     * @required
     */
    private String sourceGeneratorClass;

    /**
     * Main execution method, which is called after hibernate has been initialized
     */
    public void execute()
            throws MojoExecutionException, MojoFailureException {
        File tempDir = new File(targetPath);

        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // create velocity context
        VelocityContext context = new VelocityContext();
        context.put("mojo", this);
        context.put("artifactId", project.getArtifactId());
        context.put("version", project.getVersion());

        // create the sourceGeneratorContext, using the dependencies of the project
        File[] depJars = getDependencyJars();
        ClassLoader classLoader = createClassLoaderWithJars(depJars);

        SourceGeneratorContext sbContext = new SourceGeneratorContext(context, generatedPackage, new File(targetPath));
        sbContext.setDependencyJars(depJars);
        sbContext.setDependencyClassLoader(classLoader);

        getLog().info("Going to look for model classes in " + sbContext.getDependencyJars().length + " jars");

        SourceGenerator generator = null;
        try {
            generator = newInstanceOfSourceGenerator(sourceGeneratorClass);
        } catch (Exception e) {
            throw new MojoExecutionException("Problem instantiating class for name: " + sourceGeneratorClass, e);
        }

        try {
            generator.generateClasses(sbContext, new MavenLogPrintStream(getLog()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Problem creating class from template", e);
        }

        if (targetPath != null) {
            // Adding the resources
            List includes = Collections.singletonList("*/**");
            List excludes = null;

            helper.addResource(project, targetPath, includes, excludes);
        }

    }

    private static SourceGenerator newInstanceOfSourceGenerator(String sourceBuilderClassName) throws Exception {
        Class sourceBuilderClass = Class.forName(sourceBuilderClassName);

        return (SourceGenerator) sourceBuilderClass.newInstance();
    }

    private File[] getDependencyJars() {
        Set<Artifact> artifacts = project.getArtifacts();

        List<File> dependencyJars = new ArrayList<File>();
        int i = 0;

        for (Artifact artifact : artifacts) {
            File artFile = artifact.getFile();

            if (artFile != null &&
                !DEFAULT_EXCLUDED_GROUPIDS.contains(artifact.getGroupId()) &&
                !artFile.getName().equals("tools.jar")) {
                dependencyJars.add(artFile);
            }
        }

        return dependencyJars.toArray(new File[dependencyJars.size()]);
    }

    private ClassLoader createClassLoaderWithJars(File[] jarsFiles) {
        URL[] jarsUrls = new URL[jarsFiles.length];

        for (int i = 0; i < jarsFiles.length; i++) {
            try {
                //jarsUrls[i] = new URL("jar:file://" + jarsFiles[i].toString() + "!/");
                jarsUrls[i] = jarsFiles[i].toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        PsiClassLoader urlClassLoader = new PsiClassLoader(jarsUrls, PsiProcessorGeneratorMojo.class.getClassLoader());

        return urlClassLoader;
    }

    /**
     * Implementation of abstract method from superclass
     */
    public MavenProject getProject() {
        return project;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getSourceGeneratorClass() {
        return sourceGeneratorClass;
    }

    private class MavenLogPrintStream extends PrintStream {

        private Log log;

        public MavenLogPrintStream(Log log) {
            super(System.out);
            this.log = log;
        }

        public void print(String str) {
            String lineSeparator = System.getProperty("line.separator");

            if (!str.equals(lineSeparator)) {
                log.info(str.replaceAll(lineSeparator, ""));
            }

        }

    }
}
