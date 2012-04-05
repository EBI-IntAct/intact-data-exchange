package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import edu.ucla.mbi.imex.central.ws.v20.Identifier;
import edu.ucla.mbi.imex.central.ws.v20.Publication;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl.IntactImexAssignerImpl;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import java.util.Iterator;
import java.util.List;

/**
 * Unit tester for IntactImexAssignerImpl
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>05/04/12</pre>
 */
@ContextConfiguration(locations = {"classpath*:/META-INF/intact.spring.xml",
        "classpath*:/META-INF/standalone/*-standalone.spring.xml",
        "classpath*:/META-INF/beansimex-test.spring.xml"})
public class IntactImexAssignerImplTest extends IntactBasicTestCase{

    @Autowired
    private IntactImexAssigner assignerTest;

    private Publication imexPublication;

    @Before
    public void createImexPublications() throws ImexCentralException {
        imexPublication = new Publication();
        Identifier pubmed = new Identifier();
        pubmed.setNs("pubmed");
        pubmed.setAc("12345");
        imexPublication.getIdentifier().add(pubmed);

        assignerTest.getImexCentralClient().createPublication(imexPublication);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void assignImexPublication_validPubId_succesfull() throws ImexCentralException {

        Assert.assertNull(imexPublication.getImexAccession());

        TransactionStatus status = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");
        getCorePersister().saveOrUpdate(intactPub);

        getDataContext().commitTransaction(status);

        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());
        boolean hasAssigned = assignerTest.assignImexIdentifier(intactPubReloaded, imexPublication);

        Assert.assertTrue(hasAssigned);
        Assert.assertNotNull(imexPublication.getImexAccession());
        Assert.assertNotSame("N/A", imexPublication.getImexAccession());

        Assert.assertEquals(1, intactPubReloaded.getXrefs().size());
        PublicationXref ref = intactPubReloaded.getXrefs().iterator().next();
        Assert.assertEquals(CvDatabase.IMEX_MI_REF, ref.getCvDatabase().getIdentifier());
        Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, ref.getCvXrefQualifier().getIdentifier());
        Assert.assertEquals(imexPublication.getImexAccession(), ref.getPrimaryId());

        Assert.assertEquals(2, intactPubReloaded.getAnnotations().size());
        boolean hasFullCuration = false;
        boolean hasImexCuration = false;

        for (uk.ac.ebi.intact.model.Annotation ann : intactPubReloaded.getAnnotations()){
            if ("imex curation".equals(ann.getCvTopic().getShortLabel())){
                hasImexCuration = true;
            }
            else if ("full coverage".equals(ann.getCvTopic().getShortLabel()) && "Only protein-protein interactions".equalsIgnoreCase(ann.getAnnotationText())){
                hasFullCuration = true;
            }
        }

        Assert.assertTrue(hasFullCuration);
        Assert.assertTrue(hasImexCuration);

        getDataContext().commitTransaction(status2);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void assignImexPublication_validPubId_existingAnnotations() throws ImexCentralException {
        Assert.assertNull(imexPublication.getImexAccession());

        TransactionStatus status = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");

        CvTopic fullCoverage = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, IntactImexAssignerImpl.FULL_COVERAGE_MI, IntactImexAssignerImpl.FULL_COVERAGE);
        getCorePersister().saveOrUpdate(fullCoverage);
        Annotation fullCoverageAnnot = new Annotation( fullCoverage, IntactImexAssignerImpl.FULL_COVERAGE_TEXT );
        intactPub.addAnnotation(fullCoverageAnnot);
        CvTopic imexCuration = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, IntactImexAssignerImpl.IMEX_CURATION_MI, IntactImexAssignerImpl.IMEX_CURATION);
        getCorePersister().saveOrUpdate(imexCuration);
        Annotation imexCurationAnnot = new Annotation( imexCuration, null );
        intactPub.addAnnotation(imexCurationAnnot);

        getCorePersister().saveOrUpdate(intactPub);

        Assert.assertEquals(2, intactPub.getAnnotations().size());

        getDataContext().commitTransaction(status);

        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());
        boolean hasAssigned = assignerTest.assignImexIdentifier(intactPubReloaded, imexPublication);

        Assert.assertTrue(hasAssigned);
        Assert.assertNotNull(imexPublication.getImexAccession());
        Assert.assertNotSame("N/A", imexPublication.getImexAccession());

        Assert.assertEquals(1, intactPubReloaded.getXrefs().size());
        PublicationXref ref = intactPubReloaded.getXrefs().iterator().next();
        Assert.assertEquals(CvDatabase.IMEX_MI_REF, ref.getCvDatabase().getIdentifier());
        Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, ref.getCvXrefQualifier().getIdentifier());
        Assert.assertEquals(imexPublication.getImexAccession(), ref.getPrimaryId());

        Assert.assertEquals(2, intactPubReloaded.getAnnotations().size());
        boolean hasFullCuration = false;
        boolean hasImexCuration = false;

        for (uk.ac.ebi.intact.model.Annotation ann : intactPubReloaded.getAnnotations()){
            if ("imex curation".equals(ann.getCvTopic().getShortLabel())){
                hasImexCuration = true;
            }
            else if ("full coverage".equals(ann.getCvTopic().getShortLabel()) && "Only protein-protein interactions".equalsIgnoreCase(ann.getAnnotationText())){
                hasFullCuration = true;
            }
        }

        Assert.assertTrue(hasFullCuration);
        Assert.assertTrue(hasImexCuration);

        getDataContext().commitTransaction(status2);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void assignImexPublication_validPubId_duplicatedAnnotations() throws ImexCentralException {
        Assert.assertNull(imexPublication.getImexAccession());

        TransactionStatus status = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");

        CvTopic fullCoverage = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, IntactImexAssignerImpl.FULL_COVERAGE_MI, IntactImexAssignerImpl.FULL_COVERAGE);
        getCorePersister().saveOrUpdate(fullCoverage);
        Annotation fullCoverageAnnot = new Annotation( fullCoverage, IntactImexAssignerImpl.FULL_COVERAGE_TEXT );
        Annotation fullCoverageAnnot2 = new Annotation( fullCoverage, null );
        intactPub.addAnnotation(fullCoverageAnnot);
        intactPub.getAnnotations().add(fullCoverageAnnot2);
        CvTopic imexCuration = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, IntactImexAssignerImpl.IMEX_CURATION_MI, IntactImexAssignerImpl.IMEX_CURATION);
        getCorePersister().saveOrUpdate(imexCuration);
        Annotation imexCurationAnnot = new Annotation( imexCuration, null );
        Annotation imexCurationAnnot2 = new Annotation( imexCuration, null );
        intactPub.addAnnotation(imexCurationAnnot);
        intactPub.getAnnotations().add(imexCurationAnnot2);

        getCorePersister().saveOrUpdate(intactPub);

        Assert.assertEquals(4, intactPub.getAnnotations().size());

        getDataContext().commitTransaction(status);

        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());
        boolean hasAssigned = assignerTest.assignImexIdentifier(intactPubReloaded, imexPublication);

        Assert.assertTrue(hasAssigned);
        Assert.assertNotNull(imexPublication.getImexAccession());
        Assert.assertNotSame("N/A", imexPublication.getImexAccession());

        Assert.assertEquals(1, intactPubReloaded.getXrefs().size());
        PublicationXref ref = intactPubReloaded.getXrefs().iterator().next();
        Assert.assertEquals(CvDatabase.IMEX_MI_REF, ref.getCvDatabase().getIdentifier());
        Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, ref.getCvXrefQualifier().getIdentifier());
        Assert.assertEquals(imexPublication.getImexAccession(), ref.getPrimaryId());

        Assert.assertEquals(2, intactPubReloaded.getAnnotations().size());
        boolean hasFullCuration = false;
        boolean hasImexCuration = false;

        for (uk.ac.ebi.intact.model.Annotation ann : intactPubReloaded.getAnnotations()){
            if ("imex curation".equals(ann.getCvTopic().getShortLabel())){
                hasImexCuration = true;
            }
            else if ("full coverage".equals(ann.getCvTopic().getShortLabel()) && "Only protein-protein interactions".equalsIgnoreCase(ann.getAnnotationText())){
                hasFullCuration = true;
            }
        }

        Assert.assertTrue(hasFullCuration);
        Assert.assertTrue(hasImexCuration);

        getDataContext().commitTransaction(status2);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void assignImexPublication_validPubId_differentFullCuration() throws ImexCentralException {
        Assert.assertNull(imexPublication.getImexAccession());

        TransactionStatus status = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");

        CvTopic fullCoverage = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, IntactImexAssignerImpl.FULL_COVERAGE_MI, IntactImexAssignerImpl.FULL_COVERAGE);
        getCorePersister().saveOrUpdate(fullCoverage);
        Annotation fullCoverageAnnot = new Annotation( fullCoverage, null );
        intactPub.addAnnotation(fullCoverageAnnot);

        getCorePersister().saveOrUpdate(intactPub);

        Assert.assertEquals(1, intactPub.getAnnotations().size());

        getDataContext().commitTransaction(status);

        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());
        boolean hasAssigned = assignerTest.assignImexIdentifier(intactPubReloaded, imexPublication);

        Assert.assertTrue(hasAssigned);
        Assert.assertNotNull(imexPublication.getImexAccession());
        Assert.assertNotSame("N/A", imexPublication.getImexAccession());

        Assert.assertEquals(1, intactPubReloaded.getXrefs().size());
        PublicationXref ref = intactPubReloaded.getXrefs().iterator().next();
        Assert.assertEquals(CvDatabase.IMEX_MI_REF, ref.getCvDatabase().getIdentifier());
        Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, ref.getCvXrefQualifier().getIdentifier());
        Assert.assertEquals(imexPublication.getImexAccession(), ref.getPrimaryId());

        Assert.assertEquals(2, intactPubReloaded.getAnnotations().size());
        boolean hasFullCuration = false;
        boolean hasImexCuration = false;

        for (uk.ac.ebi.intact.model.Annotation ann : intactPubReloaded.getAnnotations()){
            if ("imex curation".equals(ann.getCvTopic().getShortLabel())){
                hasImexCuration = true;
            }
            else if ("full coverage".equals(ann.getCvTopic().getShortLabel()) && "Only protein-protein interactions".equalsIgnoreCase(ann.getAnnotationText())){
                hasFullCuration = true;
            }
        }

        Assert.assertTrue(hasFullCuration);
        Assert.assertTrue(hasImexCuration);

        getDataContext().commitTransaction(status2);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void updateImexIdentifiersForAllExperiments() throws ImexCentralException, PublicationImexUpdaterException {

        TransactionStatus status = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");

        Experiment exp1 = getMockBuilder().createExperimentRandom(1);
        exp1.getXrefs().clear();
        exp1.setPublication(intactPub);
        intactPub.addExperiment(exp1);

        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(intactPub);
        exp2.getXrefs().clear();
        intactPub.addExperiment(exp2);

        Experiment exp3 = getMockBuilder().createExperimentRandom(1);
        exp3.setPublication(intactPub);
        exp3.getXrefs().clear();
        intactPub.addExperiment(exp3);

        getCorePersister().saveOrUpdate(intactPub);

        Assert.assertEquals(3, intactPub.getExperiments().size());

        getDataContext().commitTransaction(status);

        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());
        List<Experiment> experiments = assignerTest.updateImexIdentifiersForAllExperiments(intactPubReloaded, "IM-1", null);

        Assert.assertEquals(3, experiments.size());

        for (Experiment exp : experiments){
            Assert.assertEquals(1, exp.getXrefs().size());
            ExperimentXref ref = exp.getXrefs().iterator().next();
            Assert.assertEquals(CvDatabase.IMEX_MI_REF, ref.getCvDatabase().getIdentifier());
            Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, ref.getCvXrefQualifier().getIdentifier());
            Assert.assertEquals("IM-1", ref.getPrimaryId());
        }

        getDataContext().commitTransaction(status2);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void updateImexIdentifiersForAllExperiments_conflict() throws ImexCentralException {
        TransactionStatus status = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        Experiment exp1 = getMockBuilder().createExperimentRandom(1);
        exp1.setPublication(intactPub);
        exp1.getXrefs().clear();
        intactPub.addExperiment(exp1);
        ExperimentXref expXref = new ExperimentXref( exp1.getOwner(), imex, "IM-3", imexPrimary );
        exp1.addXref(expXref);

        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(intactPub);
        exp2.getXrefs().clear();
        intactPub.addExperiment(exp2);

        Experiment exp3 = getMockBuilder().createExperimentRandom(1);
        exp3.setPublication(intactPub);
        exp3.getXrefs().clear();
        intactPub.addExperiment(exp3);

        getCorePersister().saveOrUpdate(intactPub);

        Assert.assertEquals(3, intactPub.getExperiments().size());

        getDataContext().commitTransaction(status);

        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());
        List<Experiment> experiments = null;
        try {
            experiments = assignerTest.updateImexIdentifiersForAllExperiments(intactPubReloaded, "IM-1", null);
            Assert.assertTrue(false);
        } catch (PublicationImexUpdaterException e) {
            Assert.assertTrue(true);
        }

        getDataContext().commitTransaction(status2);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void updateImexIdentifiersForAllExperiments_existingImex() throws ImexCentralException, PublicationImexUpdaterException {
        TransactionStatus status = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        Experiment exp1 = getMockBuilder().createExperimentRandom(1);
        exp1.setPublication(intactPub);
        exp1.getXrefs().clear();
        intactPub.addExperiment(exp1);
        ExperimentXref expXref = new ExperimentXref( exp1.getOwner(), imex, "IM-1", imexPrimary );
        exp1.addXref(expXref);

        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(intactPub);
        exp2.getXrefs().clear();
        intactPub.addExperiment(exp2);

        Experiment exp3 = getMockBuilder().createExperimentRandom(1);
        exp3.setPublication(intactPub);
        exp3.getXrefs().clear();
        intactPub.addExperiment(exp3);

        getCorePersister().saveOrUpdate(intactPub);

        Assert.assertEquals(3, intactPub.getExperiments().size());

        getDataContext().commitTransaction(status);

        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());
        List<Experiment> experiments = assignerTest.updateImexIdentifiersForAllExperiments(intactPubReloaded, "IM-1", null);

        // only two experiments updated
        Assert.assertEquals(2, experiments.size());

        for (Experiment exp : experiments){
            Assert.assertNotSame(exp1.getAc(), exp.getAc());
            Assert.assertEquals(1, exp.getXrefs().size());
            ExperimentXref ref = exp.getXrefs().iterator().next();
            Assert.assertEquals(CvDatabase.IMEX_MI_REF, ref.getCvDatabase().getIdentifier());
            Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, ref.getCvXrefQualifier().getIdentifier());
            Assert.assertEquals("IM-1", ref.getPrimaryId());
        }

        // check that exp1 has not been updated
        Experiment exp1Reloaded = getDaoFactory().getExperimentDao().getByAc(exp1.getAc());
        Assert.assertEquals(1, exp1Reloaded.getXrefs().size());
        ExperimentXref ref = exp1Reloaded.getXrefs().iterator().next();
        Assert.assertEquals(CvDatabase.IMEX_MI_REF, ref.getCvDatabase().getIdentifier());
        Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, ref.getCvXrefQualifier().getIdentifier());
        Assert.assertEquals("IM-1", ref.getPrimaryId());

        getDataContext().commitTransaction(status2);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void updateImexIdentifiersForAllExperiments_duplicatedImex() throws ImexCentralException, PublicationImexUpdaterException {
        TransactionStatus status = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        Experiment exp1 = getMockBuilder().createExperimentRandom(1);
        exp1.setPublication(intactPub);
        exp1.getXrefs().clear();
        intactPub.addExperiment(exp1);
        ExperimentXref expXref = new ExperimentXref( exp1.getOwner(), imex, "IM-1", imexPrimary );
        exp1.addXref(expXref);
        ExperimentXref expXref2 = new ExperimentXref( exp1.getOwner(), imex, "IM-1", imexPrimary );
        exp1.getXrefs().add(expXref2);

        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(intactPub);
        exp2.getXrefs().clear();
        intactPub.addExperiment(exp2);

        Experiment exp3 = getMockBuilder().createExperimentRandom(1);
        exp3.setPublication(intactPub);
        exp3.getXrefs().clear();
        intactPub.addExperiment(exp3);

        getCorePersister().saveOrUpdate(intactPub);

        Assert.assertEquals(3, intactPub.getExperiments().size());
        Assert.assertEquals(2, exp1.getXrefs().size());

        getDataContext().commitTransaction(status);

        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());
        List<Experiment> experiments = assignerTest.updateImexIdentifiersForAllExperiments(intactPubReloaded, "IM-1", null);

        // 3 experiments updated  (one duplicated xref deleted)
        Assert.assertEquals(3, experiments.size());

        for (Experiment exp : experiments){
            Assert.assertEquals(1, exp.getXrefs().size());
            ExperimentXref ref = exp.getXrefs().iterator().next();
            Assert.assertEquals(CvDatabase.IMEX_MI_REF, ref.getCvDatabase().getIdentifier());
            Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, ref.getCvXrefQualifier().getIdentifier());
            Assert.assertEquals("IM-1", ref.getPrimaryId());
        }

        getDataContext().commitTransaction(status2);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void collectExistingInteractionImexIds() throws ImexCentralException {
        TransactionStatus status = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);
        
        Experiment exp1 = getMockBuilder().createExperimentRandom(1);
        exp1.setPublication(intactPub);
        intactPub.addExperiment(exp1);
        InteractorXref expXref = new InteractorXref( exp1.getOwner(), imex, "IM-1-1", imexPrimary );
        exp1.getInteractions().iterator().next().addXref(expXref);

        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(intactPub);
        exp2.getXrefs().clear();
        intactPub.addExperiment(exp2);
        InteractorXref expXref2 = new InteractorXref( exp2.getOwner(), imex, "IM-1-2", imexPrimary );
        exp2.getInteractions().iterator().next().addXref(expXref2);

        Experiment exp3 = getMockBuilder().createExperimentRandom(1);
        exp3.setPublication(intactPub);
        exp3.getXrefs().clear();
        intactPub.addExperiment(exp3);
        InteractorXref expXref3 = new InteractorXref( exp3.getOwner(), imex, "IM-1", imexPrimary );
        exp3.getInteractions().iterator().next().addXref(expXref3);

        getCorePersister().saveOrUpdate(intactPub);

        Assert.assertEquals(3, getDaoFactory().getInteractionDao().countAll());

        getDataContext().commitTransaction(status);
        
        List<String> imexIds = assignerTest.collectExistingInteractionImexIdsForPublication(intactPub);
        Assert.assertEquals(3, imexIds.size());

        Iterator<String> imexIdsIterator = imexIds.iterator();
        Assert.assertEquals("IM-1", imexIdsIterator.next());
        Assert.assertEquals("IM-1-1", imexIdsIterator.next());
        Assert.assertEquals("IM-1-2", imexIdsIterator.next());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void updateImexIdentifiersForAllInteractions() throws ImexCentralException, PublicationImexUpdaterException {

        TransactionStatus status = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");

        Experiment exp1 = getMockBuilder().createExperimentRandom(1);
        exp1.setPublication(intactPub);
        intactPub.addExperiment(exp1);

        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(intactPub);
        intactPub.addExperiment(exp2);

        Experiment exp3 = getMockBuilder().createExperimentRandom(1);
        exp3.setPublication(intactPub);
        intactPub.addExperiment(exp3);

        getCorePersister().saveOrUpdate(intactPub);

        Assert.assertEquals(3, getDaoFactory().getInteractionDao().countAll());

        getDataContext().commitTransaction(status);

        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());
        List<Interaction> interactions = assignerTest.assignImexIdentifiersForAllInteractions(intactPubReloaded, "IM-1", null);

        Assert.assertEquals(3, interactions.size());

        int index = 0;
        for (Interaction interation : interactions){
            index++;

            Assert.assertEquals(1, interation.getXrefs().size());
            InteractorXref ref = interation.getXrefs().iterator().next();
            Assert.assertEquals(CvDatabase.IMEX_MI_REF, ref.getCvDatabase().getIdentifier());
            Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, ref.getCvXrefQualifier().getIdentifier());
            Assert.assertEquals("IM-1-" + index, ref.getPrimaryId());
        }

        getDataContext().commitTransaction(status2);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void updateImexIdentifiersForAllInteractions_conflict() throws ImexCentralException {
        TransactionStatus status = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        Experiment exp1 = getMockBuilder().createExperimentRandom(1);
        exp1.setPublication(intactPub);
        intactPub.addExperiment(exp1);
        InteractorXref expXref = new InteractorXref( exp1.getOwner(), imex, "IM-1-1", imexPrimary );
        exp1.getInteractions().iterator().next().addXref(expXref);

        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(intactPub);
        exp2.getXrefs().clear();
        intactPub.addExperiment(exp2);

        Experiment exp3 = getMockBuilder().createExperimentRandom(1);
        exp3.setPublication(intactPub);
        exp3.getXrefs().clear();
        intactPub.addExperiment(exp3);
        InteractorXref expXref3 = new InteractorXref( exp3.getOwner(), imex, "IM-2-1", imexPrimary );
        exp3.getInteractions().iterator().next().addXref(expXref3);

        getCorePersister().saveOrUpdate(intactPub);

        Assert.assertEquals(3, getDaoFactory().getInteractionDao().countAll());

        getDataContext().commitTransaction(status);

        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());
        List<Interaction> interactions = null;
        try {
            interactions = assignerTest.assignImexIdentifiersForAllInteractions(intactPubReloaded, "IM-1", null);
            Assert.assertFalse(true);
        } catch (PublicationImexUpdaterException e) {
            Assert.assertTrue(true);
        }

        getDataContext().commitTransaction(status2);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void updateImexIdentifiersForAllInteractions_existingImex() throws ImexCentralException, PublicationImexUpdaterException {
        TransactionStatus status = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        Experiment exp1 = getMockBuilder().createExperimentRandom(1);
        exp1.setPublication(intactPub);
        intactPub.addExperiment(exp1);
        InteractorXref expXref = new InteractorXref( exp1.getOwner(), imex, "IM-1-1", imexPrimary );
        Interaction int1 = exp1.getInteractions().iterator().next();
        int1.addXref(expXref);

        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(intactPub);
        exp2.getXrefs().clear();
        intactPub.addExperiment(exp2);

        Experiment exp3 = getMockBuilder().createExperimentRandom(1);
        exp3.setPublication(intactPub);
        exp3.getXrefs().clear();
        intactPub.addExperiment(exp3);

        getCorePersister().saveOrUpdate(intactPub);

        Assert.assertEquals(3, getDaoFactory().getInteractionDao().countAll());

        getDataContext().commitTransaction(status);


        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());
        List<Interaction> interactions = interactions = assignerTest.assignImexIdentifiersForAllInteractions(intactPubReloaded, "IM-1", null);

        Assert.assertEquals(2, interactions.size());

        int index = 1;
        for (Interaction interation : interactions){
            index++;

            Assert.assertNotSame(int1.getAc(), interation.getAc());
            Assert.assertEquals(1, interation.getXrefs().size());
            InteractorXref ref = interation.getXrefs().iterator().next();
            Assert.assertEquals(CvDatabase.IMEX_MI_REF, ref.getCvDatabase().getIdentifier());
            Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, ref.getCvXrefQualifier().getIdentifier());
            Assert.assertEquals("IM-1-" + index, ref.getPrimaryId());
        }
        
        Interaction int1Reloaded = getDaoFactory().getInteractionDao().getByAc(int1.getAc());
        Assert.assertEquals(1, int1Reloaded.getXrefs().size());
        InteractorXref ref = int1Reloaded.getXrefs().iterator().next();
        Assert.assertEquals(CvDatabase.IMEX_MI_REF, ref.getCvDatabase().getIdentifier());
        Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, ref.getCvXrefQualifier().getIdentifier());
        Assert.assertEquals("IM-1-1", ref.getPrimaryId());

        getDataContext().commitTransaction(status2);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void updateImexIdentifiersForAllInteractions_duplicatedImex() throws ImexCentralException, PublicationImexUpdaterException {
        TransactionStatus status = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        Experiment exp1 = getMockBuilder().createExperimentRandom(1);
        exp1.setPublication(intactPub);
        intactPub.addExperiment(exp1);
        InteractorXref expXref = new InteractorXref( exp1.getOwner(), imex, "IM-1-1", imexPrimary );
        Interaction int1 = exp1.getInteractions().iterator().next();
        InteractorXref expXref2 = new InteractorXref( exp1.getOwner(), imex, "IM-1-1", imexPrimary );
        int1.addXref(expXref);
        int1.getXrefs().add(expXref2);

        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(intactPub);
        exp2.getXrefs().clear();
        intactPub.addExperiment(exp2);

        Experiment exp3 = getMockBuilder().createExperimentRandom(1);
        exp3.setPublication(intactPub);
        exp3.getXrefs().clear();
        intactPub.addExperiment(exp3);

        getCorePersister().saveOrUpdate(intactPub);

        Assert.assertEquals(3, getDaoFactory().getInteractionDao().countAll());

        getDataContext().commitTransaction(status);


        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());
        List<Interaction> interactions = interactions = assignerTest.assignImexIdentifiersForAllInteractions(intactPubReloaded, "IM-1", null);

        Assert.assertEquals(3, interactions.size());

        int index = 1;
        for (Interaction interation : interactions){
            if (interation.getAc().equals(int1.getAc())){
                Assert.assertEquals(1, interation.getXrefs().size());
                InteractorXref ref = interation.getXrefs().iterator().next();
                Assert.assertEquals(CvDatabase.IMEX_MI_REF, ref.getCvDatabase().getIdentifier());
                Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, ref.getCvXrefQualifier().getIdentifier());
                Assert.assertEquals("IM-1-1", ref.getPrimaryId());
            }
            else {
                index++;

                Assert.assertEquals(1, interation.getXrefs().size());
                InteractorXref ref = interation.getXrefs().iterator().next();
                Assert.assertEquals(CvDatabase.IMEX_MI_REF, ref.getCvDatabase().getIdentifier());
                Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, ref.getCvXrefQualifier().getIdentifier());
                Assert.assertEquals("IM-1-" + index, ref.getPrimaryId());
            }
        }

        getDataContext().commitTransaction(status2);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void updateImexIdentifiersForAllInteractions_secondaryImex() throws ImexCentralException, PublicationImexUpdaterException {
        TransactionStatus status = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        Experiment exp1 = getMockBuilder().createExperimentRandom(1);
        exp1.setPublication(intactPub);
        intactPub.addExperiment(exp1);
        InteractorXref expXref = new InteractorXref( exp1.getOwner(), imex, "IM-1", imexPrimary );
        Interaction int1 = exp1.getInteractions().iterator().next();
        int1.addXref(expXref);

        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(intactPub);
        exp2.getXrefs().clear();
        intactPub.addExperiment(exp2);

        Experiment exp3 = getMockBuilder().createExperimentRandom(1);
        exp3.setPublication(intactPub);
        exp3.getXrefs().clear();
        intactPub.addExperiment(exp3);

        getCorePersister().saveOrUpdate(intactPub);

        Assert.assertEquals(3, getDaoFactory().getInteractionDao().countAll());

        getDataContext().commitTransaction(status);


        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());
        List<Interaction> interactions = interactions = assignerTest.assignImexIdentifiersForAllInteractions(intactPubReloaded, "IM-1", null);

        Assert.assertEquals(3, interactions.size());

        int index = 0;
        for (Interaction interation : interactions){
            if (interation.getAc().equals(int1.getAc())){
                index ++;
                Assert.assertEquals(2, interation.getXrefs().size());
                Iterator<InteractorXref> refIterator = interation.getXrefs().iterator();
                InteractorXref ref = refIterator.next();
                Assert.assertEquals(CvDatabase.IMEX_MI_REF, ref.getCvDatabase().getIdentifier());
                Assert.assertEquals("imex secondary", ref.getCvXrefQualifier().getShortLabel());
                Assert.assertEquals("IM-1", ref.getPrimaryId());
                InteractorXref ref2 = refIterator.next();
                Assert.assertEquals(CvDatabase.IMEX_MI_REF, ref2.getCvDatabase().getIdentifier());
                Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, ref2.getCvXrefQualifier().getIdentifier());
                Assert.assertEquals("IM-1-2", ref2.getPrimaryId());
            }
            else {
                index++;

                Assert.assertEquals(1, interation.getXrefs().size());
                InteractorXref ref = interation.getXrefs().iterator().next();
                Assert.assertEquals(CvDatabase.IMEX_MI_REF, ref.getCvDatabase().getIdentifier());
                Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, ref.getCvXrefQualifier().getIdentifier());
                Assert.assertEquals("IM-1-" + index, ref.getPrimaryId());
            }
        }

        getDataContext().commitTransaction(status2);
    }
}
