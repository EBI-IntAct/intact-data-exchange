package uk.ac.ebi.intact.task.pmid;

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
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.task.mitab.BinaryInteractionItemProcessor;
import uk.ac.ebi.intact.task.mitab.InteractionExpansionCompositeProcessor;
import uk.ac.ebi.intact.task.mitab.index.OntologyEnricherItemProcessor;
import uk.ac.ebi.intact.task.mitab.pmid.PublicationFileEntry;
import uk.ac.ebi.intact.task.mitab.pmid.PublicationMitabItemProcessor;
import uk.ac.ebi.intact.task.util.FileNameGenerator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.SortedSet;

/**
 * Unit tester of PublicationMitabItemProcessor
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>20/08/12</pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/META-INF/mitab-creation.spring.xml", "/META-INF/job-tests.spring.xml"})
@Transactional(propagation = Propagation.NEVER)
public class PublicationMitabItemProcessorTest extends IntactBasicTestCase{
    
    private PublicationMitabItemProcessor publicationMitabProcessorTest;

    private PsimiTabReader mitabReader = new PsimiTabReader();

    @Before
    public void initializeProcessor (){
        publicationMitabProcessorTest = new PublicationMitabItemProcessor();

        FileNameGenerator fileNameGenerator = new FileNameGenerator();
        fileNameGenerator.setNegativeTag("negative");
        fileNameGenerator.setSeparator("_");

        publicationMitabProcessorTest.setPublicationNameGenerator(fileNameGenerator);

        InteractionExpansionCompositeProcessor processor = new InteractionExpansionCompositeProcessor();
        OntologyEnricherItemProcessor ontologyEnricher = new OntologyEnricherItemProcessor();
        ontologyEnricher.setOntologiesSolrUrl("http://localhost:18080/solr/core_ontology_pub");

        processor.setBinaryItemProcessors(Arrays.asList((BinaryInteractionItemProcessor) ontologyEnricher));

        publicationMitabProcessorTest.setCompositeProcessor(processor);

    }

    public StepExecution getStepExecution() {
        StepExecution execution = MetaDataInstanceFactory.createStepExecution();
        execution.getExecutionContext().putString("input.data", "foo,bar,spam");
        return execution;
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.NEVER)
    public void testReader_readPublications() throws Exception {
        TransactionStatus status = getDataContext().beginTransaction();

        if (getDaoFactory().getPublicationDao().countAll() > 0){
            for (Publication p : getDaoFactory().getPublicationDao().getAll()){
                getCoreDeleter().delete(p);
            }
        }

        Interaction interaction = getMockBuilder().createInteractionRandomBinary();
        Interaction interaction2 = getMockBuilder().createInteractionRandomBinary();
        Interaction interaction3 = getMockBuilder().createInteractionRandomBinary();
        interaction3.getExperiments().clear();
        interaction3.addExperiment(interaction2.getExperiments().iterator().next());
        getCorePersister().saveOrUpdate(interaction);
        getCorePersister().saveOrUpdate(interaction2, interaction3);

        // contains one small plus one negative
        Publication pub1 = interaction.getExperiments().iterator().next().getPublication();
        pub1.setCreated(new Date((System.currentTimeMillis())));
        Experiment neg = getMockBuilder().createExperimentRandom(1);
        neg.setPublication(pub1);
        pub1.addExperiment(neg);
        Interaction intNeg = neg.getInteractions().iterator().next();
        intNeg.addAnnotation(getMockBuilder().createAnnotation(null, null, "negative"));
        getCorePersister().saveOrUpdate(neg, pub1);

        // contains two small interactions
        Publication pub2 = interaction2.getExperiments().iterator().next().getPublication();

        // contains large scale experiment
        Publication pub3 = getMockBuilder().createPublicationRandom();
        Experiment largeScale = getMockBuilder().createExperimentRandom(25);
        largeScale.setPublication(pub3);
        pub3.addExperiment(largeScale);
        getCorePersister().saveOrUpdate(pub3);

        // contains small scale experiment
        Publication pub4 = getMockBuilder().createPublicationRandom();
        Experiment smallScale = getMockBuilder().createExperimentRandom(7);
        smallScale.setPublication(pub4);
        pub4.addExperiment(smallScale);
        getCorePersister().saveOrUpdate(pub4);

        // contains lot of small scale experiment
        Publication pub5 = getMockBuilder().createPublicationRandom();
        Experiment smallScale1 = getMockBuilder().createExperimentRandom(4);
        smallScale1.setPublication(pub5);
        pub5.addExperiment(smallScale1);
        Experiment smallScale2 = getMockBuilder().createExperimentRandom(4);
        smallScale2.setPublication(pub5);
        pub5.addExperiment(smallScale2);
        Experiment smallScale3 = getMockBuilder().createExperimentRandom(4);
        smallScale3.setPublication(pub5);
        pub5.addExperiment(smallScale3);
        getCorePersister().saveOrUpdate(pub5);

        // 5 publications
        Assert.assertEquals(5, getDaoFactory().getPublicationDao().countAll());

        getDataContext().commitTransaction(status);

        TransactionStatus status2 = getDataContext().beginTransaction();

        // open the processor for the first time
        StepExecution stepExecution = getStepExecution();
        publicationMitabProcessorTest.open(stepExecution.getExecutionContext());

        SortedSet<PublicationFileEntry> pubEntries = publicationMitabProcessorTest.process(pub1);
        Assert.assertEquals(2, pubEntries.size());

        PublicationFileEntry entry1 = pubEntries.first();
        Assert.assertEquals(entry1.getCreatedDate(), pub1.getCreated());
        Assert.assertEquals(entry1.getEntryName(), pub1.getShortLabel());
        Assert.assertNotNull(entry1.getBinaryInteractions());
        Assert.assertTrue(entry1.getBinaryInteractions().length() > 0);
        StringBuffer entrySet1 = entry1.getBinaryInteractions();

        Collection<BinaryInteraction> interactions = mitabReader.read(entrySet1.toString());
        Assert.assertEquals(1, interactions.size());

        PublicationFileEntry entry2 = pubEntries.last();
        Assert.assertEquals(entry2.getCreatedDate(), pub1.getCreated());
        Assert.assertEquals(entry2.getEntryName(), pub1.getShortLabel() + "_negative");
        Assert.assertNotNull(entry2.getBinaryInteractions());
        Assert.assertTrue(entry2.getBinaryInteractions().length() > 0);
        StringBuffer entrySet2 = entry2.getBinaryInteractions();

        Collection<BinaryInteraction> interactions2 = mitabReader.read(entrySet2.toString());
        Assert.assertEquals(1, interactions2.size());

        SortedSet<PublicationFileEntry> pubEntries2 = publicationMitabProcessorTest.process(pub2);
        Assert.assertEquals(1, pubEntries2.size());

        PublicationFileEntry entry3 = pubEntries2.first();
        Assert.assertEquals(entry3.getCreatedDate(), pub2.getCreated());
        Assert.assertEquals(entry3.getEntryName(), pub2.getShortLabel());
        Assert.assertNotNull(entry3.getBinaryInteractions());
        Assert.assertTrue(entry3.getBinaryInteractions().length() > 0);
        StringBuffer entrySet3 = entry3.getBinaryInteractions();

        Collection<BinaryInteraction> interactions3 = mitabReader.read(entrySet3.toString());
        Assert.assertEquals(2, interactions3.size());

        SortedSet<PublicationFileEntry> pubEntries3 = publicationMitabProcessorTest.process(pub3);
        Assert.assertEquals(1, pubEntries3.size());

        PublicationFileEntry entry4 = pubEntries3.iterator().next();
        Assert.assertEquals(entry4.getCreatedDate(), pub3.getCreated());
        Assert.assertEquals(entry4.getEntryName(), pub3.getShortLabel());
        Assert.assertNotNull(entry4.getBinaryInteractions());
        Assert.assertTrue(entry4.getBinaryInteractions().length() > 0);
        StringBuffer entrySet4 = entry4.getBinaryInteractions();

        Collection<BinaryInteraction> interactions4 = mitabReader.read(entrySet4.toString());
        Assert.assertEquals(25, interactions4.size());

        SortedSet<PublicationFileEntry> pubEntries4 = publicationMitabProcessorTest.process(pub4);
        Assert.assertEquals(1, pubEntries4.size());

        PublicationFileEntry entry7 = pubEntries4.first();
        Assert.assertEquals(entry7.getCreatedDate(), pub4.getCreated());
        Assert.assertEquals(entry7.getEntryName(), pub4.getShortLabel());
        Assert.assertNotNull(entry7.getBinaryInteractions());
        Assert.assertTrue(entry7.getBinaryInteractions().length() > 0);
        StringBuffer entrySet7 = entry7.getBinaryInteractions();

        Collection<BinaryInteraction> interactions5 = mitabReader.read(entrySet7.toString());
        Assert.assertEquals(7, interactions5.size());

        SortedSet<PublicationFileEntry> pubEntries5 = publicationMitabProcessorTest.process(pub5);
        Assert.assertEquals(1, pubEntries5.size());

        PublicationFileEntry entry8 = pubEntries5.iterator().next();
        Assert.assertEquals(entry8.getCreatedDate(), pub5.getCreated());
        Assert.assertEquals(entry8.getEntryName(), pub5.getShortLabel());
        Assert.assertNotNull(entry8.getBinaryInteractions());
        Assert.assertTrue(entry8.getBinaryInteractions().length() > 0);
        StringBuffer entrySet8 = entry8.getBinaryInteractions();

        Collection<BinaryInteraction> interactions6 = mitabReader.read(entrySet8.toString());

        Assert.assertEquals(12, interactions6.size());
        publicationMitabProcessorTest.close();

        getDataContext().commitTransaction(status2);
    }
}
