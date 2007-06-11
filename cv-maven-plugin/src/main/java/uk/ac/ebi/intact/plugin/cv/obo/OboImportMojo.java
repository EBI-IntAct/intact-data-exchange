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
package uk.ac.ebi.intact.plugin.cv.obo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.h2.tools.Csv;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import uk.ac.ebi.intact.plugin.IntactHibernateMojo;
import uk.ac.ebi.intact.plugin.MojoUtils;
import uk.ac.ebi.intact.dbutil.cv.UpdateCVs;
import uk.ac.ebi.intact.dbutil.cv.PsiLoaderException;
import uk.ac.ebi.intact.dbutil.cv.UpdateCVsReport;
import uk.ac.ebi.intact.dbutil.cv.UpdateCVsConfig;
import uk.ac.ebi.intact.dbutil.cv.model.CvTerm;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.context.CvContext;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.ook.model.implementation.TermBean;

/**
 * Export an OBO file from the provided database in the hibernateConfig file
 *
 * @goal obo-imp
 *
 * @phase process-resources
 */
public class OboImportMojo
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
     * @parameter expression="${project.build.directory}/hibernate/config/hibernate.cfg.xml"
     * @required
     */
    private File hibernateConfig;

    /**
     * Contains the CVs to upload in OBO format
     * @parameter expression="${intact.obo}"
     * @required
     */
    private File importedOboFile;

    /**
     * Use this CSV formatted file to upload additional CVs.
     * The file should follow the format "class","shortlabel","fullname"
     * @parameter expression="${intact.additional}
     */
    private File additionalCsvFile;

    /**
     * @parameter expression="${project.build.directory}/updatedTerms.txt"
     */
    private File updatedTermsFile;

    /**
     * @parameter expression="${project.build.directory}/createdTerms.txt"
     */
    private File createdTermsFile;

    /**
     * @parameter expression="${project.build.directory}/obsoleteTerms.txt"
     */
    private File obsoleteTermsFile;

    /**
     * @parameter expression="${project.build.directory}/invalidTerms.txt"
     */
    private File invalidTermsFile;

    /**
     * @parameter expression="${project.build.directory}/orphanTerms.txt"
     */
    private File orphanTermsFile;

    /**
     * @parameter expression="${project.build.directory}/ontology.txt"
     */
    private File ontologyFile;

    /**
     * File with additional CVs, in CSV format (objclass,shortlabel,fullname)
     * @parameter expression="${project.build.directory}/created-additional.txt"
     */
    private File additionalCreatedFile;

    /**
     * File with additional annotations, in CSV format (objclass,shortlabel,topic_shortlabel,description)
     * @parameter expression="${intact.additional.annotations}"
     */
    private File additionalAnnotationsCsvFile;

    /**
     * If true, don't obsolete the cv term "obsolete"
     * @parameter expression="${intact.ignoreObsoletionOfObsolete}"
     */
    private boolean ignoreObsoletionOfObsolete;

    /**
     * Main execution method, which is called after hibernate has been initialized
     */
    public void executeIntactMojo()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        if (!importedOboFile.exists())
        {
            throw new MojoExecutionException("OBO file to import does not exist: "+importedOboFile);
        }

        getLog().info("Importing CVs from OBO: "+importedOboFile);
        UpdateCVsReport report = null;

        getLog().info("Ignore Obsoletion of the Obsolete CV: "+ignoreObsoletionOfObsolete);

        UpdateCVsConfig config = new UpdateCVsConfig();
        config.setIgnoreObsoletionOfObsolete(ignoreObsoletionOfObsolete);

        PrintStream output = getOutputPrintStream();

        try
        {
            report = UpdateCVs.load(importedOboFile, output, config);
        }
        catch (PsiLoaderException e)
        {
            throw new MojoExecutionException("Problem importing OBO file", e);
        }

        try {
            IntactContext.getCurrentInstance().getDataContext().commitTransaction();
        } catch (IntactTransactionException e) {
            e.printStackTrace();
        }

        IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        writeUpdatedTermsFile(report);
        writeCreatedTermsFile(report);
        writeObsoleteTermsFile(report);
        writeOrphanTermsFile(report);
        writeOntologyFile(report);
        writeInvalidTermsFile(report);

        if (additionalCsvFile != null)
        {
            getLog().info("Reading additional CVs from file: "+additionalCsvFile);

            try
            {
                importAdditionalCVs();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new MojoExecutionException("Exception importing additional CVs", e);
            }
        }

        try {
            IntactContext.getCurrentInstance().getDataContext().commitTransaction();
        } catch (IntactTransactionException e) {
            e.printStackTrace();
        }

        IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        if (additionalAnnotationsCsvFile != null)
        {
             getLog().info("Reading additional annotations from file: "+additionalAnnotationsCsvFile);

            try
            {
                importAdditionalAnnotations();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new MojoExecutionException("Exception importing additional annotations", e);
            }
        }

        try {
            IntactContext.getCurrentInstance().getDataContext().commitTransaction();
        } catch (IntactTransactionException e) {
            e.printStackTrace();
        }
    }

    private void importAdditionalCVs() throws IOException, SQLException
    {
        MojoUtils.prepareFile(additionalCreatedFile);
        MojoUtils.writeStandardHeaderToFile("Additional terms created", "Terms created from CSV file: "+additionalCsvFile,
                getProject(), additionalCreatedFile);
        FileWriter writer = new FileWriter(additionalCreatedFile, true);

        int created = 0;

        ResultSet rs = Csv.getInstance().read(additionalCsvFile.toString(), null, "utf-8");

        List<CvObject> allCvs = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getCvObjectDao().getAll();

        CvXrefQualifier identityXrefQual = IntactContext.getCurrentInstance().getCvContext().getByMiRef(CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF);
        CvDatabase intactDb = IntactContext.getCurrentInstance().getCvContext().getByMiRef(CvDatabase.class, CvDatabase.INTACT_MI_REF);

        while (rs.next())
        {
            String objclass = rs.getString(1);
            String shortLabel = rs.getString(2);
            String fullName = rs.getString(3);

            if (!containsCv(allCvs, objclass, shortLabel))
            {
                CvObject cv = null;
                try
                {
                    cv = (CvObject) Class.forName(objclass).newInstance();
                }
                catch (Exception e)
                {
                    getLog().error("Couldn't create an instance of: "+objclass+ ", with label '"+shortLabel+"'");
                    continue;
                }

                cv.setShortLabel(shortLabel);
                cv.setFullName(fullName);
                cv.setOwner(IntactContext.getCurrentInstance().getConfig().getInstitution());

                CvObjectXref xref = new CvObjectXref(IntactContext.getCurrentInstance().getConfig().getInstitution(),
                                                     intactDb,
                                                     "IAX:"+System.currentTimeMillis(),
                                                      identityXrefQual);

                cv.addXref(xref);

                IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                        .getCvObjectDao().persist(cv);

                writer.write("[CREATED]\t"+objclass+"\t"+shortLabel+NEW_LINE);
                created++;
            }
            else
            {
                writer.write("[IGNORED]\t"+objclass+"\t"+shortLabel+NEW_LINE);
            }
        }

        writer.write(NEW_LINE);
        writer.write("# Total created: "+created+NEW_LINE);
        writer.close();

    }

    private void importAdditionalAnnotations() throws IOException, SQLException
    {
        ResultSet rs = Csv.getInstance().read(additionalAnnotationsCsvFile.toString(), null, "utf-8");

        CvContext cvContext = IntactContext.getCurrentInstance().getCvContext();

        while (rs.next())
        {
            String objclass = rs.getString(1);
            String shortLabel = rs.getString(2);
            String topicLabel = rs.getString(3);
            String description = rs.getString(4);

            Class cvType = null;
            try
            {
                cvType = Class.forName(objclass);
            }
            catch (Exception e)
            {
                getLog().error("Couldn't find: " + objclass + " in classpath");
                continue;
            }

            CvObject cv = cvContext.getByLabel(cvType, shortLabel);

            if (cv == null)
            {
                getLog().error("CVObject not found: " + objclass + ", with label '" + cvType + "'");
                continue;
            }

            CvTopic cvTopic = cvContext.getByLabel(CvTopic.class, topicLabel);

            Annotation annot = new Annotation(IntactContext.getCurrentInstance().getConfig().getInstitution(),
                                            cvTopic, description);

            cv.addAnnotation(annot);

            DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();

            daoFactory.getAnnotationDao().persist(annot);
            daoFactory.getCvObjectDao().update(cv);
        }
    }

    private boolean containsCv(List<CvObject> cvList, String objclass, String shortlabel)
    {
        for (CvObject existingCv : cvList)
        {
            if (objclass.equals(existingCv.getObjClass())
                    && shortlabel.equals(existingCv.getShortLabel()))
            {
                return true;
            }
        }

        return false;
    }

    private void writeUpdatedTermsFile(UpdateCVsReport report) throws IOException
    {
        MojoUtils.prepareFile(updatedTermsFile);
        MojoUtils.writeStandardHeaderToFile("Updated terms", "CvObjects updated", getProject(), updatedTermsFile);

        Writer writer = new FileWriter(updatedTermsFile, true);

        writer.write("# Terms: "+report.getUpdatedTerms().size()+NEW_LINE+NEW_LINE);

        for (CvObject cv : report.getUpdatedTerms())
        {
            writer.write(cv.getAc()+"\t"+cv.getShortLabel()+NEW_LINE);
        }

        writer.close();
    }

    private void writeCreatedTermsFile(UpdateCVsReport report) throws IOException
    {
        MojoUtils.prepareFile(createdTermsFile);
        MojoUtils.writeStandardHeaderToFile("Created terms", "New CvObjects from the OBO file that has been created",
                getProject(), createdTermsFile);

        Writer writer = new FileWriter(createdTermsFile, true);

        writer.write("# Terms: "+report.getCreatedTerms().size()+NEW_LINE+NEW_LINE);

        for (CvObject cv : report.getCreatedTerms())
        {
            writer.write(cv.getAc()+"\t"+cv.getShortLabel()+NEW_LINE);
        }

        writer.close();
    }

    private void writeObsoleteTermsFile(UpdateCVsReport report) throws IOException
    {
        MojoUtils.prepareFile(obsoleteTermsFile);
        MojoUtils.writeStandardHeaderToFile("Obsolete terms", "Obsolete terms", getProject(), obsoleteTermsFile);

        Writer writer = new FileWriter(obsoleteTermsFile, true);

        writer.write("# Terms: "+report.getObsoleteTerms().size()+NEW_LINE+NEW_LINE);

        for (CvTerm cv : report.getObsoleteTerms())
        {
            writer.write(cv.getId()+"\t"+cv.getShortName()+NEW_LINE);
        }

        writer.close();
    }

    private void writeInvalidTermsFile(UpdateCVsReport report) throws IOException
    {
        MojoUtils.prepareFile(invalidTermsFile);
        MojoUtils.writeStandardHeaderToFile("Invalid terms", "Invalid terms\"", getProject(), invalidTermsFile);

        Writer writer = new FileWriter(invalidTermsFile, true);

        writer.write("# Terms: "+report.getOntology().getInvalidTerms().size()+NEW_LINE+NEW_LINE);

        for (TermBean term : report.getOntology().getInvalidTerms())
        {
            writer.write(term.getIdentifier()+"\t"+term.getName()+NEW_LINE);
        }

        writer.close();
    }
    
    private void writeOrphanTermsFile(UpdateCVsReport report) throws IOException
    {
        MojoUtils.prepareFile(orphanTermsFile);
        MojoUtils.writeStandardHeaderToFile("Orphan terms", "The list of terms below could not be added to your IntAct node. " +
                "These terms are obsolete in PSI-MI and the ontology doesn't keep track of the root of obsolete terms." +
                " Solution: if you really want to add these terms into IntAct, you will have to do it manually and make " +
                "sure that they get their MI:xxxx.", getProject(), orphanTermsFile);

        Writer writer = new FileWriter(orphanTermsFile, true);

        writer.write("# Terms: "+report.getOrphanTerms().size()+NEW_LINE+NEW_LINE);

        for (CvTerm cv : report.getOrphanTerms())
        {
            writer.write(cv.getId()+"\t"+cv.getShortName()+NEW_LINE);
        }

        writer.close();
    }

    private void writeOntologyFile(UpdateCVsReport report) throws IOException
    {
        MojoUtils.prepareFile(ontologyFile);
        MojoUtils.writeStandardHeaderToFile("Ontology", "List of terms from the OBO file: "+importedOboFile,
                getProject(), ontologyFile);

        PrintStream ps = new PrintStream(ontologyFile);
        report.getOntology().print(ps);
    }

    public File getImportedOboFile()
    {
        return importedOboFile;
    }

    public void setImportedOboFile(File importedOboFile)
    {
        this.importedOboFile = importedOboFile;
    }

    public MavenProject getProject()
    {
        return project;
    }

    public File getHibernateConfig()
    {
        return hibernateConfig;
    }

    public void setHibernateConfig(File hibernateConfig) {
        this.hibernateConfig = hibernateConfig;
    }

    public File getUpdatedTermsFile()
    {
        return updatedTermsFile;
    }

    public File getCreatedTermsFile()
    {
        return createdTermsFile;
    }

    public File getObsoleteTermsFile()
    {
        return obsoleteTermsFile;
    }

    public File getOrphanTermsFile()
    {
        return orphanTermsFile;
    }

    public File getOntologyFile()
    {
        return ontologyFile;
    }

    public File getAdditionalCsvFile()
    {
        return additionalCsvFile;
    }

    public void setAdditionalCsvFile(File additionalCsvFile)
    {
        this.additionalCsvFile = additionalCsvFile;
    }

    public File getInvalidTermsFile()
    {
        return invalidTermsFile;
    }

    public void setInvalidTermsFile(File invalidTermsFile)
    {
        this.invalidTermsFile = invalidTermsFile;
    }

    public File getAdditionalCreatedFile()
    {
        return additionalCreatedFile;
    }

    public void setAdditionalCreatedFile(File additionalCreatedFile)
    {
        this.additionalCreatedFile = additionalCreatedFile;
    }

    public File getAdditionalAnnotationsCsvFile() {
        return additionalAnnotationsCsvFile;
    }

    public void setAdditionalAnnotationsCsvFile(File additionalAnnotationsCsvFile) {
        this.additionalAnnotationsCsvFile = additionalAnnotationsCsvFile;
    }

    public boolean isIgnoreObsoletionOfObsolete() {
        return ignoreObsoletionOfObsolete;
    }

    public void setIgnoreObsoletionOfObsolete(boolean ignoreObsoletionOfObsolete) {
        this.ignoreObsoletionOfObsolete = ignoreObsoletionOfObsolete;
    }
}
