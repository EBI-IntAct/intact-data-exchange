package uk.ac.ebi.intact.task.pmid;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.tab.PsimiTabReader;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.task.mitab.BinaryInteractionItemProcessor;
import uk.ac.ebi.intact.task.mitab.InteractionExpansionCompositeProcessor;
import uk.ac.ebi.intact.task.mitab.index.OntologyEnricherItemProcessor;
import uk.ac.ebi.intact.task.mitab.pmid.PublicationFileEntry;
import uk.ac.ebi.intact.task.mitab.pmid.PublicationMitabItemProcessor;
import uk.ac.ebi.intact.task.mitab.pmid.PublicationMitabItemWriter;
import uk.ac.ebi.intact.task.util.FileNameGenerator;

import javax.annotation.Resource;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Unit tester of PublicationMitabItemWriter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>20/08/12</pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/META-INF/mitab-creation.spring.xml", "/META-INF/job-tests.spring.xml"})
@Transactional(propagation = Propagation.NEVER)
public class PublicationMitabItemWriterTest extends IntactBasicTestCase{

    @Resource(name = "publicationMitabWriterTest")
    private PublicationMitabItemWriter writer;

    private PublicationMitabItemProcessor mitabProcessor;
    private PsimiTabReader mitabReader = new PsimiTabReader();

    @Before
    public void initializeProcessor (){
        mitabProcessor = new PublicationMitabItemProcessor();

        FileNameGenerator fileNameGenerator = new FileNameGenerator();
        fileNameGenerator.setNegativeTag("negative");
        fileNameGenerator.setSeparator("_");

        mitabProcessor.setPublicationNameGenerator(fileNameGenerator);

        InteractionExpansionCompositeProcessor processor = new InteractionExpansionCompositeProcessor(false, false);
        OntologyEnricherItemProcessor ontologyEnricher = new OntologyEnricherItemProcessor();
        ontologyEnricher.setOntologiesSolrUrl("http://localhost:18080/solr/core_ontology_pub");

        processor.setBinaryItemProcessors(Arrays.asList((BinaryInteractionItemProcessor) ontologyEnricher));

        mitabProcessor.setCompositeProcessor(processor);

    }

    public StepExecution getStepExecution() {
        StepExecution execution = MetaDataInstanceFactory.createStepExecution();
        execution.getExecutionContext().putString("input.data", "foo,bar,spam");
        return execution;
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.NEVER)
    public void test_write_publications() throws Exception {
        File pmid = new File(writer.getParentFolderPaths());
        if (pmid.exists()){
            FileUtils.deleteDirectory(pmid);
        }
        TransactionStatus status = getDataContext().beginTransaction();

        // contains small scale experiment
        Publication pubWithNoPublicationDate = getMockBuilder().createPublicationRandom();
        pubWithNoPublicationDate.setShortLabel("12345");
        Experiment smallScale = getMockBuilder().createExperimentRandom(7);
        smallScale.setPublication(pubWithNoPublicationDate);
        pubWithNoPublicationDate.addExperiment(smallScale);
        getCorePersister().saveOrUpdate(pubWithNoPublicationDate);

        getDataContext().commitTransaction(status);

        TransactionStatus status2 = getDataContext().beginTransaction();

        // contains small scale experiment
        String publicationYearStr = "2020";
        Publication pubWithPublicationDate = getMockBuilder().createPublicationRandom();
        pubWithPublicationDate.setShortLabel("98765");
        List<Annotation> annotations = new ArrayList();
        annotations.add(getMockBuilder().createAnnotation(
                publicationYearStr,
                getMockBuilder().createCvObject(CvTopic.class, "MI:0886", "publication year")));
        pubWithPublicationDate.setAnnotations(annotations);
        Experiment smallestScale = getMockBuilder().createExperimentRandom(1);
        smallestScale.setPublication(pubWithPublicationDate);
        pubWithPublicationDate.addExperiment(smallestScale);
        getCorePersister().saveOrUpdate(pubWithPublicationDate);

        getDataContext().commitTransaction(status2);

        TransactionStatus status3 = getDataContext().beginTransaction();

        StepExecution stepExecution = getStepExecution();
        mitabProcessor.open(stepExecution.getExecutionContext());

        SortedSet<PublicationFileEntry> pubWithNoPublicationDateEntries = mitabProcessor.process(pubWithNoPublicationDate);
        SortedSet<PublicationFileEntry> pubWithPublicationDateEntries = mitabProcessor.process(pubWithPublicationDate);
        SortedSet<PublicationFileEntry> pubWithNoPublicationDateEntries2 = new TreeSet();
        SortedSet<PublicationFileEntry> pubWithPublicationDateEntries2 = new TreeSet();

        mitabProcessor.close();

        DateFormat dateFormatForEntry = new SimpleDateFormat("yyyy-MM-dd");
        String secondCreatedDate = "2009-07-09";
        String secondPublicationDate = "2021";

        PublicationFileEntry oldEntryWithNoPublicationDate = pubWithNoPublicationDateEntries.iterator().next();
        PublicationFileEntry newEntryWithNoPublicationDate = new PublicationFileEntry(
                dateFormatForEntry.parse(secondCreatedDate), "12345_10", oldEntryWithNoPublicationDate.getBinaryInteractions(), false, null);
        pubWithNoPublicationDateEntries2.add(newEntryWithNoPublicationDate);
        PublicationFileEntry oldEntryWithPublicationDate = pubWithPublicationDateEntries.iterator().next();
        PublicationFileEntry newEntryWithPublicationDate = new PublicationFileEntry(
                dateFormatForEntry.parse(secondCreatedDate), "98765_10", oldEntryWithPublicationDate.getBinaryInteractions(), false, secondPublicationDate);
        pubWithPublicationDateEntries2.add(newEntryWithPublicationDate);

        DateFormat format = new SimpleDateFormat("yyyy");

        // open the writer for the first time
        writer.open(stepExecution.getExecutionContext());
        writer.getGlobalPositiveMitabItemWriter().open(stepExecution.getExecutionContext());
        writer.getGlobalNegativeMitabItemWriter().open(stepExecution.getExecutionContext());

        writer.write(Arrays.asList(pubWithNoPublicationDateEntries, pubWithPublicationDateEntries, pubWithNoPublicationDateEntries2, pubWithPublicationDateEntries2));
        writer.getGlobalPositiveMitabItemWriter().update(stepExecution.getExecutionContext());
        writer.getGlobalNegativeMitabItemWriter().update(stepExecution.getExecutionContext());
        writer.close();
        writer.getGlobalPositiveMitabItemWriter().close();
        writer.getGlobalNegativeMitabItemWriter().close();

        pmid = new File(writer.getParentFolderPaths());
        Assert.assertTrue(pmid.exists());

        File createdYear = new File(pmid, format.format(pubWithNoPublicationDate.getCreated()));
        Assert.assertTrue(createdYear.exists());

        File[] pubmedsWithNoPublicationDate = createdYear.listFiles();
        Assert.assertEquals(1, pubmedsWithNoPublicationDate.length);
        Assert.assertEquals(pubWithNoPublicationDateEntries.iterator().next().getEntryName() + ".txt", pubmedsWithNoPublicationDate[0].getName());
        Assert.assertEquals(7, mitabReader.read(pubmedsWithNoPublicationDate[0]).size());

        File publicationYear = new File(pmid, publicationYearStr);
        Assert.assertTrue(publicationYear.exists());

        File[] pubmedsWithPublicationDate = publicationYear.listFiles();
        Assert.assertEquals(1, pubmedsWithPublicationDate.length);
        Assert.assertEquals(pubWithPublicationDateEntries.iterator().next().getEntryName() + ".txt", pubmedsWithPublicationDate[0].getName());
        Assert.assertEquals(1, mitabReader.read(pubmedsWithPublicationDate[0]).size());

        File secondCreatedYear = new File(pmid, secondCreatedDate.substring(0, 4));
        Assert.assertTrue(secondCreatedYear.exists());

        File[] secondPubmedsWithNoPublicationDate = secondCreatedYear.listFiles();
        Assert.assertEquals(1, secondPubmedsWithNoPublicationDate.length);
        Assert.assertEquals("12345_10.txt", secondPubmedsWithNoPublicationDate[0].getName());
        Assert.assertEquals(7, mitabReader.read(secondPubmedsWithNoPublicationDate[0]).size());

        File secondPublicationYear = new File(pmid, secondPublicationDate);
        Assert.assertTrue(secondPublicationYear.exists());

        File[] secondPubmedsWithPublicationDate = secondPublicationYear.listFiles();
        Assert.assertEquals(1, secondPubmedsWithPublicationDate.length);
        Assert.assertEquals("98765_10.txt", secondPubmedsWithPublicationDate[0].getName());
        Assert.assertEquals(1, mitabReader.read(secondPubmedsWithPublicationDate[0]).size());

        File globalFile = new File("target/lala.txt");
        Assert.assertTrue(globalFile.exists());
        Assert.assertEquals(16, mitabReader.read(globalFile).size());

        getDataContext().commitTransaction(status3);
    }
}
