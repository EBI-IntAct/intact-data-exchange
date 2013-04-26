package uk.ac.ebi.intact.dataexchange.imex.idassigner;

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
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

/**
 * Unit tester of ImexCentralManager
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11/04/12</pre>
 */
@ContextConfiguration(locations = {"classpath*:/META-INF/intact.spring.xml",
        "classpath*:/META-INF/standalone/*-standalone.spring.xml",
        "classpath*:/META-INF/beansimex-test.spring.xml"})
public class ImexCentralManagerTest extends IntactBasicTestCase{

    @Autowired
    private ImexCentralManager imexManagerTest;
    
    @Before
    public void createImexRecord() throws ImexCentralException {
        Publication pubmedPub = new Publication();
        pubmedPub.setImexAccession("IM-3");
        imexManagerTest.getImexCentralRegister().getImexCentralClient().createPublication(pubmedPub);

        Publication existingRecordWithImex = new Publication();
        Identifier pubmed = new Identifier();
        pubmed.setNs("pmid");
        pubmed.setAc("12347");
        existingRecordWithImex.getIdentifier().add(pubmed);
        existingRecordWithImex.setImexAccession("IM-7");
        imexManagerTest.getImexCentralRegister().getImexCentralClient().createPublication(existingRecordWithImex);

        Publication existingRecordWithoutImex = new Publication();
        Identifier pubmed2 = new Identifier();
        pubmed2.setNs("pmid");
        pubmed2.setAc("12348");
        existingRecordWithoutImex.getIdentifier().add(pubmed2);
        imexManagerTest.getImexCentralRegister().getImexCentralClient().createPublication(existingRecordWithoutImex);

        Publication existingRecordWithoutImexNoPubmed = new Publication();
        Identifier pubmed3 = new Identifier();
        pubmed3.setNs("jint");
        pubmed3.setAc("unassigned504");
        existingRecordWithoutImexNoPubmed.getIdentifier().add(pubmed3);
        imexManagerTest.getImexCentralRegister().getImexCentralClient().createPublication(existingRecordWithoutImexNoPubmed);

        Publication existingRecordWithoutImexNoPubmed2 = new Publication();
        Identifier pubmed4 = new Identifier();
        pubmed4.setNs("jint");
        pubmed4.setAc("unassigned604");
        existingRecordWithoutImexNoPubmed2.getIdentifier().add(pubmed4);
        existingRecordWithoutImexNoPubmed2.setImexAccession("IM-4");
        imexManagerTest.getImexCentralRegister().getImexCentralClient().createPublication(existingRecordWithoutImexNoPubmed2);
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.NEVER)
    public void collect_update_imex_primaryRef_publications(){
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);
        
        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");
        PublicationXref pubXref = new PublicationXref( intactPub.getOwner(), imex, "IM-3", imexPrimary );
        intactPub.addXref(pubXref);
        getCorePersister().saveOrUpdate(intactPub);

        getDataContext().commitTransaction(status);

        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());
        String imexId = imexManagerTest.collectAndCleanUpImexPrimaryReferenceFrom(intactPubReloaded);

        Assert.assertNotNull(imexId);
        Assert.assertEquals("IM-3", imexId);
        Assert.assertEquals(1, intactPubReloaded.getXrefs().size());
        
        getDataContext().commitTransaction(status2);
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.NEVER)
    public void collect_imex_primaryRef_publications_delete_duplicated(){
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");
        PublicationXref pubXref = new PublicationXref( intactPub.getOwner(), imex, "IM-3", imexPrimary );
        intactPub.addXref(pubXref);
        PublicationXref pubXref2 = new PublicationXref( intactPub.getOwner(), imex, "IM-3", imexPrimary );
        intactPub.addXref(pubXref2);
        getCorePersister().saveOrUpdate(intactPub);

        Assert.assertEquals(2, intactPub.getXrefs().size());

        getDataContext().commitTransaction(status);

        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());
        String imexId = imexManagerTest.collectAndCleanUpImexPrimaryReferenceFrom(intactPubReloaded);

        Assert.assertNotNull(imexId);
        Assert.assertEquals("IM-3", imexId);
        Assert.assertEquals(1, intactPubReloaded.getXrefs().size());

        getDataContext().commitTransaction(status2);
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.NEVER)
    public void collect_imex_primaryRef_publications_imex_conflict(){
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");
        PublicationXref pubXref = new PublicationXref( intactPub.getOwner(), imex, "IM-3", imexPrimary );
        intactPub.addXref(pubXref);
        PublicationXref pubXref2 = new PublicationXref( intactPub.getOwner(), imex, "IM-4", imexPrimary );
        intactPub.addXref(pubXref2);
        getCorePersister().saveOrUpdate(intactPub);

        Assert.assertEquals(2, intactPub.getXrefs().size());

        getDataContext().commitTransaction(status);

        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());
        String imexId = imexManagerTest.collectAndCleanUpImexPrimaryReferenceFrom(intactPubReloaded);

        Assert.assertNull(imexId);
        Assert.assertEquals(2, intactPubReloaded.getXrefs().size());

        getDataContext().commitTransaction(status2);
    }

    @Test
    @DirtiesContext
    public void update_publication_having_imex() throws PublicationImexUpdaterException, ImexCentralException {
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");
        
        PublicationXref pubXref = new PublicationXref( intactPub.getOwner(), imex, "IM-3", imexPrimary );
        intactPub.addXref(pubXref);

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

        getDataContext().commitTransaction(status);

        Publication imexPublication = imexManagerTest.updateIntactPublicationHavingIMEx(intactPub.getAc());

        TransactionStatus status2 = getDataContext().beginTransaction();

        // updated imex identifier because not present
        Assert.assertNotNull(imexPublication);
        Assert.assertEquals(1, imexPublication.getIdentifier().size());
        Identifier id = imexPublication.getIdentifier().iterator().next();
        Assert.assertEquals("pmid", id.getNs());
        Assert.assertEquals("12345", id.getAc());
        
        // valid pubmed so whould have synchronized admin group, admin user and status
        // admin group INTACT
        Assert.assertEquals(1, imexPublication.getAdminGroupList().getGroup().size());
        Assert.assertEquals("INTACT", imexPublication.getAdminGroupList().getGroup().iterator().next());
        // admin user phantom because curator is not known in imex central
        Assert.assertEquals(1, imexPublication.getAdminUserList().getUser().size());
        Assert.assertEquals("phantom", imexPublication.getAdminUserList().getUser().iterator().next());        
        // status released
        Assert.assertEquals("NEW", imexPublication.getStatus());
        
        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());

        // updated annotations publication 
        Assert.assertEquals(3, intactPubReloaded.getAnnotations().size());
        boolean hasFullCuration = false;
        boolean hasImexCuration = false;
        boolean hasImexDepth = false;

        for (uk.ac.ebi.intact.model.Annotation ann : intactPubReloaded.getAnnotations()){
            if ("imex curation".equals(ann.getCvTopic().getShortLabel())){
                hasImexCuration = true;
            }
            else if ("full coverage".equals(ann.getCvTopic().getShortLabel()) && "Only protein-protein interactions".equalsIgnoreCase(ann.getAnnotationText())){
                hasFullCuration = true;
            }
            else if ("curation depth".equals(ann.getCvTopic().getShortLabel()) && "imex curation".equalsIgnoreCase(ann.getAnnotationText())){
                hasImexDepth = true;
            }
        }

        Assert.assertTrue(hasFullCuration);
        Assert.assertTrue(hasImexCuration);
        Assert.assertTrue(hasImexDepth);
        
        int index = 0;

        // updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(1, exp.getXrefs().size());
            
            ExperimentXref ref = exp.getXrefs().iterator().next();
            Assert.assertEquals("IM-3", ref.getPrimaryId());
            Assert.assertEquals(imex.getIdentifier(), ref.getCvDatabase().getIdentifier());
            Assert.assertEquals(imexPrimary.getIdentifier(), ref.getCvXrefQualifier().getIdentifier());

            // updated interaction imex primary ref
            for (Interaction inter : exp.getInteractions()){
                index++;
                Assert.assertEquals(1, inter.getXrefs().size());

                InteractorXref ref2 = inter.getXrefs().iterator().next();
                Assert.assertTrue(ref2.getPrimaryId().startsWith("IM-3-"));
                Assert.assertEquals(imex.getIdentifier(), ref2.getCvDatabase().getIdentifier());
                Assert.assertEquals(imexPrimary.getIdentifier(), ref2.getCvXrefQualifier().getIdentifier());
            }
        }        
        
        getDataContext().commitTransaction(status2);
    }

    @Test
    @DirtiesContext
    public void update_publication_having_imex_conflict() throws PublicationImexUpdaterException, ImexCentralException {
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");

        PublicationXref pubXref = new PublicationXref( intactPub.getOwner(), imex, "IM-3", imexPrimary );
        intactPub.addXref(pubXref);
        PublicationXref pubXref2 = new PublicationXref( intactPub.getOwner(), imex, "IM-4", imexPrimary );
        intactPub.addXref(pubXref2);

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

        getDataContext().commitTransaction(status);

        Publication imexPublication = imexManagerTest.updateIntactPublicationHavingIMEx(intactPub.getAc());

        TransactionStatus status2 = getDataContext().beginTransaction();

        // did not collect anything from IMExcentral because of imex conflicts
        Assert.assertNull(imexPublication);

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());

        // did not update annotations because of conflict
        Assert.assertEquals(0, intactPubReloaded.getAnnotations().size());

        // did not updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(0, exp.getXrefs().size());

            // did not updated interaction imex primary ref
            for (Interaction inter : exp.getInteractions()){
                Assert.assertEquals(0, inter.getXrefs().size());
            }
        }

        getDataContext().commitTransaction(status2);
    }

    @Test
    @DirtiesContext
    public void update_publication_having_imex_imex_not_recognized_in_imexcentral() throws PublicationImexUpdaterException, ImexCentralException {
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12345");

        PublicationXref pubXref2 = new PublicationXref( intactPub.getOwner(), imex, "IM-5", imexPrimary );
        intactPub.addXref(pubXref2);

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

        getDataContext().commitTransaction(status);

        Publication imexPublication = imexManagerTest.updateIntactPublicationHavingIMEx(intactPub.getAc());

        TransactionStatus status2 = getDataContext().beginTransaction();

        // did not collect anything from IMExcentral because of imex not recognized
        Assert.assertNull(imexPublication);

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());

        // did not update annotations because of imex unknown
        Assert.assertEquals(0, intactPubReloaded.getAnnotations().size());

        // did not updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(0, exp.getXrefs().size());

            // did not updated interaction imex primary ref
            for (Interaction inter : exp.getInteractions()){
                Assert.assertEquals(0, inter.getXrefs().size());
            }
        }

        getDataContext().commitTransaction(status2);
    }

    @Test
    @DirtiesContext
    public void update_publication_having_imex_noPubmedId() throws PublicationImexUpdaterException, ImexCentralException {
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("unassigned604");

        PublicationXref pubXref = new PublicationXref( intactPub.getOwner(), imex, "IM-4", imexPrimary );
        intactPub.addXref(pubXref);

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

        getDataContext().commitTransaction(status);

        TransactionStatus status2 = getDataContext().beginTransaction();

        Publication imexPublication = imexManagerTest.updateIntactPublicationHavingIMEx(intactPub.getAc());

        // updated imex identifier because not present
        Assert.assertNotNull(imexPublication);
        Assert.assertEquals(1, imexPublication.getIdentifier().size());
        Identifier id = imexPublication.getIdentifier().iterator().next();
        Assert.assertEquals("jint", id.getNs());
        Assert.assertEquals("unassigned604", id.getAc());

        Assert.assertNotNull(imexPublication.getAdminGroupList());
        Assert.assertNotNull(imexPublication.getAdminUserList());
        Assert.assertNotNull(imexPublication.getStatus());

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());

        // updated annotations publication
        Assert.assertEquals(3, intactPubReloaded.getAnnotations().size());
        boolean hasFullCuration = false;
        boolean hasImexCuration = false;
        boolean hasImexDepth = false;

        for (uk.ac.ebi.intact.model.Annotation ann : intactPubReloaded.getAnnotations()){
            if ("imex curation".equals(ann.getCvTopic().getShortLabel())){
                hasImexCuration = true;
            }
            else if ("full coverage".equals(ann.getCvTopic().getShortLabel()) && "Only protein-protein interactions".equalsIgnoreCase(ann.getAnnotationText())){
                hasFullCuration = true;
            }
            else if ("curation depth".equals(ann.getCvTopic().getShortLabel()) && "imex curation".equalsIgnoreCase(ann.getAnnotationText())){
                hasImexDepth = true;
            }
        }

        Assert.assertTrue(hasFullCuration);
        Assert.assertTrue(hasImexCuration);
        Assert.assertTrue(hasImexDepth);

        // updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(1, exp.getXrefs().size());

            ExperimentXref ref = exp.getXrefs().iterator().next();
            Assert.assertEquals("IM-4", ref.getPrimaryId());
            Assert.assertEquals(imex.getIdentifier(), ref.getCvDatabase().getIdentifier());
            Assert.assertEquals(imexPrimary.getIdentifier(), ref.getCvXrefQualifier().getIdentifier());

            // updated interaction imex primary ref
            for (Interaction inter : exp.getInteractions()){
                Assert.assertEquals(1, inter.getXrefs().size());

                InteractorXref ref2 = inter.getXrefs().iterator().next();
                Assert.assertTrue(ref2.getPrimaryId().startsWith("IM-4-"));
                Assert.assertEquals(imex.getIdentifier(), ref2.getCvDatabase().getIdentifier());
                Assert.assertEquals(imexPrimary.getIdentifier(), ref2.getCvXrefQualifier().getIdentifier());
            }
        }

        getDataContext().commitTransaction(status2);
    }

    @Test
    @DirtiesContext
    public void assign_imex_register_update_publication() throws PublicationImexUpdaterException, ImexCentralException {
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12346");
        intactPub.getXrefs().clear();
        
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

        getDataContext().commitTransaction(status);

        Publication imexPublication = imexManagerTest.assignImexAndUpdatePublication(intactPub.getAc());

        TransactionStatus status2 = getDataContext().beginTransaction();

        // updated imex identifier because not present
        Assert.assertNotNull(imexPublication);
        Assert.assertEquals(1, imexPublication.getIdentifier().size());
        Identifier id = imexPublication.getIdentifier().iterator().next();
        Assert.assertEquals("pmid", id.getNs());
        Assert.assertEquals("12346", id.getAc());

        // valid pubmed so whould have synchronized admin group, admin user and status
        // admin group INTACT
        Assert.assertEquals(1, imexPublication.getAdminGroupList().getGroup().size());
        Assert.assertEquals("INTACT", imexPublication.getAdminGroupList().getGroup().iterator().next());
        // admin user phantom because curator is not known in imex central
        Assert.assertEquals(1, imexPublication.getAdminUserList().getUser().size());
        Assert.assertEquals("phantom", imexPublication.getAdminUserList().getUser().iterator().next());
        // status released
        Assert.assertEquals("NEW", imexPublication.getStatus());
        
        // assigned IMEx
        Assert.assertNotNull(imexPublication.getImexAccession());

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());

        // added imex primary ref to publication
        Assert.assertEquals(1, intactPubReloaded.getXrefs().size());
        PublicationXref pubRef = intactPubReloaded.getXrefs().iterator().next();
        Assert.assertEquals(CvDatabase.IMEX_MI_REF, pubRef.getCvDatabase().getIdentifier());
        Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, pubRef.getCvXrefQualifier().getIdentifier());
        Assert.assertEquals(imexPublication.getImexAccession(), pubRef.getPrimaryId());

        // updated annotations publication 
        Assert.assertEquals(3, intactPubReloaded.getAnnotations().size());
        boolean hasFullCuration = false;
        boolean hasImexCuration = false;
        boolean hasImexDepth = false;

        for (uk.ac.ebi.intact.model.Annotation ann : intactPubReloaded.getAnnotations()){
            if ("imex curation".equals(ann.getCvTopic().getShortLabel())){
                hasImexCuration = true;
            }
            else if ("full coverage".equals(ann.getCvTopic().getShortLabel()) && "Only protein-protein interactions".equalsIgnoreCase(ann.getAnnotationText())){
                hasFullCuration = true;
            }
            else if ("curation depth".equals(ann.getCvTopic().getShortLabel()) && "imex curation".equalsIgnoreCase(ann.getAnnotationText())){
                hasImexDepth = true;
            }
        }

        Assert.assertTrue(hasFullCuration);
        Assert.assertTrue(hasImexCuration);
        Assert.assertTrue(hasImexDepth);

        // updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(1, exp.getXrefs().size());

            ExperimentXref ref = exp.getXrefs().iterator().next();
            Assert.assertEquals(imexPublication.getImexAccession(), ref.getPrimaryId());
            Assert.assertEquals(imex.getIdentifier(), ref.getCvDatabase().getIdentifier());
            Assert.assertEquals(imexPrimary.getIdentifier(), ref.getCvXrefQualifier().getIdentifier());

            // updated interaction imex primary ref
            for (Interaction inter : exp.getInteractions()){
                Assert.assertEquals(1, inter.getXrefs().size());

                InteractorXref ref2 = inter.getXrefs().iterator().next();
                Assert.assertTrue(ref2.getPrimaryId().startsWith(imexPublication.getImexAccession()+"-"));
                Assert.assertEquals(imex.getIdentifier(), ref2.getCvDatabase().getIdentifier());
                Assert.assertEquals(imexPrimary.getIdentifier(), ref2.getCvXrefQualifier().getIdentifier());
            }
        }

        getDataContext().commitTransaction(status2);
    }

    @Test
    @DirtiesContext
    public void cannot_assign_imex_update_publication_existingRecordWithImex() throws PublicationImexUpdaterException, ImexCentralException {
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12347");
        intactPub.getXrefs().clear();

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

        getDataContext().commitTransaction(status);

        Publication imexPublication = imexManagerTest.assignImexAndUpdatePublication(intactPub.getAc());

        TransactionStatus status2 = getDataContext().beginTransaction();

        // did not create imex record because conflict with imex
        Assert.assertNull(imexPublication);

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());

        // no imex primary ref to publication
        Assert.assertEquals(0, intactPubReloaded.getXrefs().size());

        // no updated annotations publication
        Assert.assertEquals(0, intactPubReloaded.getAnnotations().size());

        // updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(0, exp.getXrefs().size());

            // updated interaction imex primary ref
            for (Interaction inter : exp.getInteractions()){
                Assert.assertEquals(0, inter.getXrefs().size());
            }
        }

        getDataContext().commitTransaction(status2);
    }

    @Test
    @DirtiesContext
    public void assign_imex_update_publication_existingRecordNoExistingImex() throws PublicationImexUpdaterException, ImexCentralException {
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("12348");
        intactPub.getXrefs().clear();

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

        getDataContext().commitTransaction(status);

        Publication imexPublication = imexManagerTest.assignImexAndUpdatePublication(intactPub.getAc());

        TransactionStatus status2 = getDataContext().beginTransaction();

        // updated imex identifier because not present
        Assert.assertNull(imexPublication);

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());

        // not added imex primary ref to publication
        Assert.assertEquals(0, intactPubReloaded.getXrefs().size());

        // not updated annotations publication
        Assert.assertEquals(0, intactPubReloaded.getAnnotations().size());

        // not updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(0, exp.getXrefs().size());

            // not updated interaction imex primary ref
            for (Interaction inter : exp.getInteractions()){
                Assert.assertEquals(0, inter.getXrefs().size());
            }
        }

        getDataContext().commitTransaction(status2);
    }

    @Test
    @DirtiesContext
    public void cannot_assign_imex_update_publication_existingRecordWithoutImexNoPubmed() throws PublicationImexUpdaterException, ImexCentralException {
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        uk.ac.ebi.intact.model.Publication intactPub = getMockBuilder().createPublication("unassigned504");
        intactPub.getXrefs().clear();

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

        getDataContext().commitTransaction(status);

        Publication imexPublication = imexManagerTest.assignImexAndUpdatePublication(intactPub.getAc());

        TransactionStatus status2 = getDataContext().beginTransaction();

        // did not create imex record because conflict with imex
        Assert.assertNull(imexPublication);

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(intactPub.getAc());

        // no imex primary ref to publication
        Assert.assertEquals(0, intactPubReloaded.getXrefs().size());

        // no updated annotations publication
        Assert.assertEquals(0, intactPubReloaded.getAnnotations().size());

        // updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(0, exp.getXrefs().size());

            // updated interaction imex primary ref
            for (Interaction inter : exp.getInteractions()){
                Assert.assertEquals(0, inter.getXrefs().size());
            }
        }

        getDataContext().commitTransaction(status2);
    }
}
