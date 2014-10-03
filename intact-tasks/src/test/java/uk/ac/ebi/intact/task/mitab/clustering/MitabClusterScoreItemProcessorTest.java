package uk.ac.ebi.intact.task.mitab.clustering;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Confidence;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Unit tester of MitabClusterScore
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>06/02/12</pre>
 */
@ContextConfiguration(locations = {"/META-INF/mitab-creation.spring.xml", "/META-INF/job-tests.spring.xml"})
public class MitabClusterScoreItemProcessorTest extends IntactBasicTestCase{

    @Autowired
    private MitabClusterScoreItemProcessor mitabClusteredProcessor;

    @Before
    public void openProcessor(){
        mitabClusteredProcessor.open(null);
    }

    @Before
    public void closeProcessor(){
        mitabClusteredProcessor.close();
    }

    @Test
    @Ignore
    public void process_binaryInteraction_one_existing_mi_score() throws Exception, IOException {

        PsimiTabReader mitabReader = new PsimiTabReader();

        Iterator<BinaryInteraction> binaryInteractionIterator = mitabReader.iterate(new File(MitabClusterScoreItemProcessorTest.class.getResource("/mitab.txt").getFile()));

        // skip the two first lines because we want to test third and fourth line
        binaryInteractionIterator.next();
        binaryInteractionIterator.next();

        BinaryInteraction binary1 = binaryInteractionIterator.next();
        Assert.assertTrue(binary1.getConfidenceValues().isEmpty());

        BinaryInteraction processedBinary1 = mitabClusteredProcessor.process(binary1);
        Assert.assertEquals(1, processedBinary1.getConfidenceValues().size());
        Confidence conf = (Confidence) processedBinary1.getConfidenceValues().iterator().next();
        Assert.assertEquals("intact-miscore", conf.getType());
        Assert.assertEquals("0.72", conf.getValue());

        BinaryInteraction binary2 = binaryInteractionIterator.next();
        Assert.assertTrue(binary2.getConfidenceValues().isEmpty());

        BinaryInteraction processedBinary2 = mitabClusteredProcessor.process(binary2);
        Assert.assertEquals(1, processedBinary2.getConfidenceValues().size());
        conf = (Confidence) processedBinary2.getConfidenceValues().iterator().next();
        Assert.assertEquals("intact-miscore", conf.getType());
        Assert.assertEquals("0.72", conf.getValue());
    }

    @Test
    @Ignore
    public void process_binaryInteraction_several_existing_scores() throws Exception, IOException {

        PsimiTabReader mitabReader = new PsimiTabReader();

        Iterator<BinaryInteraction> binaryInteractionIterator = mitabReader.iterate(new File(MitabClusterScoreItemProcessorTest.class.getResource("/mitab.txt").getFile()));

        BinaryInteraction binary1 = binaryInteractionIterator.next();
        Assert.assertTrue(binary1.getConfidenceValues().isEmpty());

        BinaryInteraction processedBinary1 = mitabClusteredProcessor.process(binary1);
        Assert.assertEquals(1, processedBinary1.getConfidenceValues().size());
        Confidence conf = (Confidence) processedBinary1.getConfidenceValues().iterator().next();
        Assert.assertEquals("intact-miscore", conf.getType());
        Assert.assertEquals("0.50", conf.getValue());

        BinaryInteraction binary2 = binaryInteractionIterator.next();
        Assert.assertTrue(binary2.getConfidenceValues().isEmpty());

        BinaryInteraction processedBinary2 = mitabClusteredProcessor.process(binary2);
        Assert.assertEquals(1, processedBinary2.getConfidenceValues().size());
        conf = (Confidence) processedBinary2.getConfidenceValues().iterator().next();
        Assert.assertEquals("intact-miscore", conf.getType());
        Assert.assertEquals("0.50", conf.getValue());
    }

    @Test
    @Ignore
    public void process_binaryInteraction_no_mi_score() throws Exception, IOException {

        PsimiTabReader mitabReader = new PsimiTabReader();

        Iterator<BinaryInteraction> binaryInteractionIterator = mitabReader.iterate(new File(MitabClusterScoreItemProcessorTest.class.getResource("/mitab.txt").getFile()));

        // skip the eight first lines because we want to test 9th and 10th line
        binaryInteractionIterator.next();
        binaryInteractionIterator.next();
        binaryInteractionIterator.next();
        binaryInteractionIterator.next();
        binaryInteractionIterator.next();
        binaryInteractionIterator.next();
        binaryInteractionIterator.next();
        binaryInteractionIterator.next();

        BinaryInteraction binary1 = binaryInteractionIterator.next();
        Assert.assertTrue(binary1.getConfidenceValues().isEmpty());

        BinaryInteraction processedBinary1 = mitabClusteredProcessor.process(binary1);
        Assert.assertTrue(processedBinary1.getConfidenceValues().isEmpty());

        BinaryInteraction binary2 = binaryInteractionIterator.next();
        Assert.assertTrue(binary2.getConfidenceValues().isEmpty());

        BinaryInteraction processedBinary2 = mitabClusteredProcessor.process(binary2);
        Assert.assertTrue(processedBinary2.getConfidenceValues().isEmpty());

    }
}
