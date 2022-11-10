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
import java.util.Arrays;
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
        Publication pub = getMockBuilder().createPublicationRandom();
        pub.setShortLabel("12345");
        Experiment smallScale = getMockBuilder().createExperimentRandom(7);
        smallScale.setPublication(pub);
        pub.addExperiment(smallScale);
        getCorePersister().saveOrUpdate(pub);

        getDataContext().commitTransaction(status);

        TransactionStatus status2 = getDataContext().beginTransaction();

        StepExecution stepExecution = getStepExecution();
        mitabProcessor.open(stepExecution.getExecutionContext());

        SortedSet<PublicationFileEntry> pubEntries = mitabProcessor.process(pub);
        SortedSet<PublicationFileEntry> pubEntries2 = new TreeSet();

        mitabProcessor.close();

        DateFormat dateFormatForEntry = new SimpleDateFormat("yyyy-MM-dd");
        String secondDate = "2009-07-09";

        PublicationFileEntry oldEntry = pubEntries.iterator().next();
        PublicationFileEntry newEntry = new PublicationFileEntry(dateFormatForEntry.parse(secondDate), "12345_10", oldEntry.getBinaryInteractions(), false);
        pubEntries2.add(newEntry);

        DateFormat format = new SimpleDateFormat("yyyy");

        // open the writer for the first time
        writer.open(stepExecution.getExecutionContext());
        writer.getGlobalPositiveMitabItemWriter().open(stepExecution.getExecutionContext());
        writer.getGlobalNegativeMitabItemWriter().open(stepExecution.getExecutionContext());

        writer.write(Arrays.asList(pubEntries, pubEntries2));
        writer.getGlobalPositiveMitabItemWriter().update(stepExecution.getExecutionContext());
        writer.getGlobalNegativeMitabItemWriter().update(stepExecution.getExecutionContext());
        writer.close();
        writer.getGlobalPositiveMitabItemWriter().close();
        writer.getGlobalNegativeMitabItemWriter().close();

        pmid = new File(writer.getParentFolderPaths());

        Assert.assertTrue(pmid.exists());
        File year = new File(pmid, format.format(pub.getCreated()));
        Assert.assertTrue(year.exists());

        File[] pubmeds = year.listFiles();

        Assert.assertEquals(1, pubmeds.length);
        Assert.assertEquals(pubEntries.iterator().next().getEntryName() + ".txt", pubmeds[0].getName());
        Assert.assertEquals(7, mitabReader.read(pubmeds[0]).size());

        File secondYear = new File(pmid, "2009");
        Assert.assertTrue(secondYear.exists());

        File[] secondPubmeds = secondYear.listFiles();

        Assert.assertEquals(1, secondPubmeds.length);
        Assert.assertEquals("12345_10.txt", secondPubmeds[0].getName());
        Assert.assertEquals(7, mitabReader.read(secondPubmeds[0]).size());

        File globalFile = new File("target/lala.txt");
        Assert.assertTrue(globalFile.exists());
        Assert.assertEquals(14, mitabReader.read(globalFile).size());

        getDataContext().commitTransaction(status2);
    }
}
