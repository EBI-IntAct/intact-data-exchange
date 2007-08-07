/*
 * Copyright 2006 The European Bioinformatics Institute.
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
package uk.ac.ebi.intact.plugin.cv.obo;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import uk.ac.ebi.intact.dataexchange.cvutils.DownloadCVs;
import uk.ac.ebi.intact.plugin.IntactHibernateMojo;
import uk.ac.ebi.intact.plugin.MojoUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Export an OBO file from the provided database in the hibernateConfig file
 *
 * @goal obo-exp
 * 
 * @phase process-resources
 */
public class OboExportMojo
        extends IntactHibernateMojo
{
    /**
     * Project instance
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter default-value="${project.build.outputDirectory}/hibernate/config/hibernate.cfg.xml"
     * @required
     */
    private File hibernateConfig;

    /**
     * @parameter expression="${project.build.directory}/intact-exported.obo"
     * @required
     */
    private File exportedOboFile;

    /**
     * Main execution method, which is called after hibernate has been initialized
     */
    public void executeIntactMojo()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        if (isDryRun())
        {
            getLog().info("Running in dry-run mode");
        }

        MojoUtils.prepareFile(exportedOboFile, true);

        BufferedWriter out = new BufferedWriter(new FileWriter(exportedOboFile));

        DownloadCVs downloadCVs = new DownloadCVs();
        downloadCVs.download(out, isDryRun());


        out.flush();
        out.close();

        getLog().info("Closed " + exportedOboFile);
    }

    public MavenProject getProject()
    {
        return project;
    }

    public File getHibernateConfig()
    {
        return hibernateConfig;
    }

    public File getExportedOboFile()
    {
        return exportedOboFile;
    }

    protected Appender getLogAppender() throws IOException {
        Appender appender = super.getLogAppender();

        Category cat1 = Logger.getLogger("org.hibernate.SQL");
        cat1.setLevel(Level.DEBUG);
        cat1.addAppender(appender);

        Category cat2 = Logger.getLogger("org.hibernate.type");
        cat2.setLevel(Level.DEBUG);
        cat2.addAppender(appender);

        return appender;
    }
}
