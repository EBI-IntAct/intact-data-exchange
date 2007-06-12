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
package uk.ac.ebi.intact.dataexchange.psimi.xml.unitdataset;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.PersisterException;
import uk.ac.ebi.intact.plugin.IntactHibernateMojo;
import uk.ac.ebi.intact.plugin.cv.obo.OboImportMojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Generates a DBUnit-dataset from a set of PSI 2.5 xml files
 *
 * @goal dataset
 * @phase generate-sources
 * @requiresDependencyResolution generate-sources
 */
public class UnitDatasetGeneratorMojo
        extends IntactHibernateMojo {

    private static final String HIBERNATE_FILE = "/META-INF/dataset-hibernate.cfg.xml";

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
     * Dataset information
     *
     * @parameter
     * @required
     */
    private List<Dataset> datasets;

    /**
     * Cv configuration
     *
     * @parameter
     */
    private CvConfiguration cvConfiguration;


    /**
     * Main execution method, which is called after hibernate has been initialized
     */
    protected void executeIntactMojo() throws MojoExecutionException, MojoFailureException, IOException {

        getLog().info("Executing DBUnit dataset generator");

        if (datasets.isEmpty()) {
            throw new MojoFailureException("No datasets to import");
        }

        IntactContext context = IntactContext.getCurrentInstance();

        getLog().debug("Datasets to import ("+datasets.size()+"):");
        for (Dataset dataSet : datasets) {
            getLog().debug("\t"+dataSet.getId());
        }

        if (cvConfiguration != null) {
            getLog().debug("Importing CVs from OBO: "+cvConfiguration.getOboFile());

            File oboFile = cvConfiguration.getOboFile();
            File additionalFile = cvConfiguration.getAdditionalFile();
            File additionalAnnotationsFile = cvConfiguration.getAdditionalAnnotationsFile();

            checkFile(oboFile);

            OboImportMojo oboImportMojo = new OboImportMojo(project);
            oboImportMojo.setImportedOboFile(oboFile);

            if (additionalFile != null) {
                oboImportMojo.setAdditionalCsvFile(additionalFile);
            }

            if (additionalAnnotationsFile != null) {
                oboImportMojo.setAdditionalAnnotationsCsvFile(additionalAnnotationsFile);
            }

            oboImportMojo.executeIntactMojo();

            commitTransactionAndBegin();

        } else {
            getLog().info("No CV configuration found. CVs won't be imported");
        }

        getLog().info("Imported "+context.getDataContext().getDaoFactory().getCvObjectDao().countAll()+" CVs");

        getLog().debug("Starting to import Datasets...");

        for (Dataset dataset : datasets) {
            try {
                importDataset(dataset);
            } catch (PersisterException e) {
                throw new MojoExecutionException("Problem importing dataset: "+dataset.getId(), e);
            }
        }

        getLog().info("Imported "+context.getDataContext().getDaoFactory().getInteractionDao().countAll()+" Interactions");
    }

    private void commitTransactionAndBegin() throws MojoExecutionException {
        IntactContext context = IntactContext.getCurrentInstance();
         try {
                context.getDataContext().commitTransaction();
                context.getDataContext().beginTransaction();
            } catch (IntactTransactionException e) {
                throw new MojoExecutionException("Problem committing the transaction after importing CVs", e);
            }
    }

    private void importDataset(Dataset dataset) throws FileNotFoundException, PersisterException, MojoExecutionException {
        for (File psiFile : dataset.getFiles()) {
            checkFile(psiFile);
            PsiExchange.importIntoIntact(new FileInputStream(psiFile), false);

            commitTransactionAndBegin();
        }
    }

    private void checkFile(File file) throws MojoExecutionException {
        if (!file.exists()) {
            throw new MojoExecutionException("File does not exist: "+file);
        }

        if (file.isDirectory()) {
            throw new MojoExecutionException("File is a directory: "+file);
        }
    }

    /**
     * Implementation of abstract method from superclass
     */
    public MavenProject getProject() {
        return project;
    }


    public File getHibernateConfig() {
        return new File(UnitDatasetGeneratorMojo.class.getResource(HIBERNATE_FILE).getFile());
    }
}
