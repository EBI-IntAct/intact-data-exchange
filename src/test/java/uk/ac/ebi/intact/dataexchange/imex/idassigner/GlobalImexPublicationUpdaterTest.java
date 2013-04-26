package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import edu.ucla.mbi.imex.central.ws.v20.Identifier;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.lifecycle.LifecycleManager;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.user.User;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Unit tester of GlobalImexPublicationUpdater
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>12/04/12</pre>
 */
@ContextConfiguration(locations = {"classpath*:/META-INF/intact.spring.xml",
        "classpath*:/META-INF/standalone/*-standalone.spring.xml",
        "classpath*:/META-INF/beansimex-test.spring.xml"})
public class GlobalImexPublicationUpdaterTest extends IntactBasicTestCase{

    @Autowired
    private GlobalImexPublicationUpdater globalImexUpdaterTest;

    @Before
    public void createImexRecords() throws ImexCentralException {
        edu.ucla.mbi.imex.central.ws.v20.Publication existingRecordWithImex = new edu.ucla.mbi.imex.central.ws.v20.Publication();
        Identifier pubmed = new Identifier();
        pubmed.setNs("pmid");
        pubmed.setAc("12345");
        existingRecordWithImex.getIdentifier().add(pubmed);
        existingRecordWithImex.setImexAccession("IM-3");
        globalImexUpdaterTest.getImexCentralManager().getImexCentralRegister().getImexCentralClient().createPublication(existingRecordWithImex);

        edu.ucla.mbi.imex.central.ws.v20.Publication existingRecordWithImex2 = new edu.ucla.mbi.imex.central.ws.v20.Publication();
        Identifier pubmed2 = new Identifier();
        pubmed2.setNs("pmid");
        pubmed2.setAc("12346");
        existingRecordWithImex2.getIdentifier().add(pubmed2);
        existingRecordWithImex2.setImexAccession("IM-4");
        globalImexUpdaterTest.getImexCentralManager().getImexCentralRegister().getImexCentralClient().createPublication(existingRecordWithImex2);

        edu.ucla.mbi.imex.central.ws.v20.Publication existingRecordWithImex3 = new edu.ucla.mbi.imex.central.ws.v20.Publication();
        Identifier pubmed3 = new Identifier();
        pubmed3.setNs("pmid");
        pubmed3.setAc("12347");
        existingRecordWithImex3.getIdentifier().add(pubmed3);
        existingRecordWithImex3.setImexAccession("IM-5");
        globalImexUpdaterTest.getImexCentralManager().getImexCentralRegister().getImexCentralClient().createPublication(existingRecordWithImex3);

    }

    @Test
    @DirtiesContext
    public void update_Existing_Imex_Publications(){

        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        CvTopic imexCuration = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, "MI:0955", "curation depth");
        getCorePersister().saveOrUpdate(imexCuration);

        // one publication with imex primary ref, 1 experiment, 2 interactions
        Publication pubWithImex = getMockBuilder().createPublication("12345");
        Experiment exp1 = getMockBuilder().createExperimentRandom(2);
        exp1.getXrefs().clear();
        exp1.setPublication(pubWithImex);
        pubWithImex.addExperiment(exp1);
        PublicationXref pubXref = new PublicationXref( pubWithImex.getOwner(), imex, "IM-3", imexPrimary );
        pubWithImex.addXref(pubXref);
        Annotation imexCurationAnn1 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        pubWithImex.addAnnotation(imexCurationAnn1);

        // publication with imex id and no imex curation level -> not updated but reported
        Publication pubWithImex2 = getMockBuilder().createPublication("12346");
        Experiment exp2 = getMockBuilder().createExperimentRandom(2);
        exp2.getXrefs().clear();
        exp2.setPublication(pubWithImex2);
        pubWithImex2.addExperiment(exp2);
        PublicationXref pubXref2 = new PublicationXref( pubWithImex2.getOwner(), imex, "IM-4", imexPrimary );
        pubWithImex2.addXref(pubXref2);

        // publication with imex id and no PPI -> not updated but reported
        Publication pubWithImex3 = getMockBuilder().createPublication("12347");
        Experiment exp3 = getMockBuilder().createExperimentRandom(1);
        exp3.getXrefs().clear();
        Interaction interaction = exp3.getInteractions().iterator().next();
        for (Component comp : interaction.getComponents()){
            comp.setInteractor(getMockBuilder().createNucleicAcidRandom());
        }

        exp3.setPublication(pubWithImex3);
        pubWithImex3.addExperiment(exp3);
        PublicationXref pubXref3 = new PublicationXref( pubWithImex3.getOwner(), imex, "IM-5", imexPrimary );
        pubWithImex3.addXref(pubXref3);
        Annotation imexCurationAnn3 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        pubWithImex3.addAnnotation(imexCurationAnn3);

        getCorePersister().saveOrUpdate(pubWithImex, pubWithImex2, pubWithImex3);

        getDataContext().commitTransaction(status);

        // update existing publications
        globalImexUpdaterTest.updateExistingImexPublications();

        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(pubWithImex.getAc());

        // updated annotations publication
        Assert.assertEquals(3, intactPubReloaded.getAnnotations().size());
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

        // updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(1, exp.getXrefs().size());

            ExperimentXref ref = exp.getXrefs().iterator().next();
            Assert.assertEquals("IM-3", ref.getPrimaryId());
            Assert.assertEquals(imex.getIdentifier(), ref.getCvDatabase().getIdentifier());
            Assert.assertEquals(imexPrimary.getIdentifier(), ref.getCvXrefQualifier().getIdentifier());

            // updated interaction imex primary ref
            for (Interaction inter : exp.getInteractions()){
                Assert.assertEquals(1, inter.getXrefs().size());

                InteractorXref ref2 = inter.getXrefs().iterator().next();
                Assert.assertTrue(ref2.getPrimaryId().startsWith("IM-3-"));
                Assert.assertEquals(imex.getIdentifier(), ref2.getCvDatabase().getIdentifier());
                Assert.assertEquals(imexPrimary.getIdentifier(), ref2.getCvXrefQualifier().getIdentifier());
            }
        }

        // pub 2 is not updated because error
        uk.ac.ebi.intact.model.Publication intactPubReloaded2 = getDaoFactory().getPublicationDao().getByAc(pubWithImex2.getAc());

        Assert.assertEquals(0, intactPubReloaded2.getAnnotations().size());
        for (Experiment exp : intactPubReloaded2.getExperiments()){
            Assert.assertEquals(0, exp.getXrefs().size());

            // updated interaction imex primary ref
            for (Interaction inter : exp.getInteractions()){
                Assert.assertEquals(0, inter.getXrefs().size());
            }
        }

        // pub 3 is not updated because error
        uk.ac.ebi.intact.model.Publication intactPubReloaded3 = getDaoFactory().getPublicationDao().getByAc(pubWithImex3.getAc());

        Assert.assertEquals(1, intactPubReloaded3.getAnnotations().size());
        for (Experiment exp : intactPubReloaded3.getExperiments()){
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
    public void assign_new_Imex_to_Publications(){

        TransactionStatus status = getDataContext().beginTransaction();

        User reviewer = getMockBuilder().createReviewer("reviewer", "reviewer", "reviewer", "reviewer@ebi.ac.uk");
        getCorePersister().saveOrUpdate(reviewer);

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        CvTopic imexCuration = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, "MI:0955", "curation depth");
        getCorePersister().saveOrUpdate(imexCuration);

        CvTopic journal = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, CvTopic.JOURNAL_MI_REF, CvTopic.JOURNAL);
        getCorePersister().saveOrUpdate(journal);
        CvTopic date = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, CvTopic.PUBLICATION_YEAR_MI_REF, CvTopic.PUBLICATION_YEAR);
        getCorePersister().saveOrUpdate(date);

        IntactMockBuilder builder = new IntactMockBuilder();

        // one publication without imex primary ref, 1 experiment, 2 interactions,journal cell 2006, accepted -> to assign IMEX
        Publication validPub = builder.createPublicationRandom();
        Experiment exp1 = getMockBuilder().createExperimentRandom(2);
        exp1.getXrefs().clear();
        exp1.setPublication(validPub);
        validPub.addExperiment(exp1);
        Annotation imexCurationAnn1 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        validPub.addAnnotation(imexCurationAnn1);
        Annotation journalAnn = getMockBuilder().createAnnotation("Cell (0092-8674)", journal);
        validPub.addAnnotation(journalAnn);
        Annotation dateAnn = getMockBuilder().createAnnotation("2006", date);
        validPub.addAnnotation(dateAnn);

        LifecycleManager lifecycleManager = getIntactContext().getLifecycleManager();
        lifecycleManager.getNewStatus().claimOwnership(validPub);
        lifecycleManager.getAssignedStatus().startCuration(validPub);
        lifecycleManager.getCurationInProgressStatus().readyForChecking(validPub, "test", true);
        lifecycleManager.getReadyForCheckingStatus().accept(validPub, "accepted");

        // publication not elligible imex (no journal, no datasets, no IMEx id) but imex curation level -> not updated but reported
        Publication imexCurationLevel = builder.createPublicationRandom();
        Experiment exp2 = getMockBuilder().createExperimentRandom(2);
        exp2.getXrefs().clear();
        exp2.setPublication(imexCurationLevel);
        imexCurationLevel.addExperiment(exp2);
        Annotation imexCurationAnn2 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        imexCurationLevel.addAnnotation(imexCurationAnn2);
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(2005, 12, 31);
        imexCurationLevel.setCreated(cal.getTime());

        lifecycleManager.getNewStatus().claimOwnership(imexCurationLevel);
        lifecycleManager.getAssignedStatus().startCuration(imexCurationLevel);
        lifecycleManager.getCurationInProgressStatus().readyForChecking(imexCurationLevel, "test", true);
        lifecycleManager.getReadyForCheckingStatus().accept(imexCurationLevel, "accepted");

        // publication without imex id but interaction does have IMEx -> not updated but reported
        // one publication without imex-primary ref but interaction with imex primary ref
        Publication pubInteractionImex = builder.createPublicationRandom();
        Experiment exp3 = getMockBuilder().createExperimentRandom(1);
        exp3.getXrefs().clear();
        exp3.setPublication(pubInteractionImex);
        pubInteractionImex.addExperiment(exp3);
        Interaction interWithImex = exp3.getInteractions().iterator().next();
        interWithImex.getXrefs().clear();
        InteractorXref intXref = new InteractorXref( pubInteractionImex.getOwner(), imex, "IM-1-1", imexPrimary );
        interWithImex.addXref(intXref);

        lifecycleManager.getNewStatus().claimOwnership(pubInteractionImex);
        lifecycleManager.getAssignedStatus().startCuration(pubInteractionImex);
        lifecycleManager.getCurationInProgressStatus().readyForChecking(pubInteractionImex, "test", true);
        lifecycleManager.getReadyForCheckingStatus().accept(pubInteractionImex, "accepted");

        // publication without imex id but experiment does have IMEx -> not updated but reported
        Publication pubExperimentImex = builder.createPublicationRandom();
        Experiment exp4 = getMockBuilder().createExperimentRandom(2);
        exp4.getXrefs().clear();
        exp4.setPublication(pubExperimentImex);
        pubExperimentImex.addExperiment(exp4);
        ExperimentXref expXref = new ExperimentXref( pubExperimentImex.getOwner(), imex, "IM-2", imexPrimary );
        exp4.addXref(expXref);

        lifecycleManager.getNewStatus().claimOwnership(pubExperimentImex);
        lifecycleManager.getAssignedStatus().startCuration(pubExperimentImex);
        lifecycleManager.getCurationInProgressStatus().readyForChecking(pubExperimentImex, "test", true);
        lifecycleManager.getReadyForCheckingStatus().accept(pubExperimentImex, "accepted");

        // publication without imex id, elligible IMEx but experiment conflict -> experiment not updated but reported
        Publication pubExperimentImex2 = builder.createPublicationRandom();
        Experiment exp5 = getMockBuilder().createExperimentRandom(2);
        exp5.getXrefs().clear();
        exp5.setPublication(pubExperimentImex2);
        pubExperimentImex2.addExperiment(exp5);
        ExperimentXref expXref2 = new ExperimentXref( pubExperimentImex2.getOwner(), imex, "IM-3", imexPrimary );
        exp5.addXref(expXref2);

        Annotation imexCurationAnn3 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        pubExperimentImex2.addAnnotation(imexCurationAnn3);
        Annotation journalAnn3 = getMockBuilder().createAnnotation("Cell (0092-8674)", journal);
        pubExperimentImex2.addAnnotation(journalAnn3);
        Annotation dateAnn3 = getMockBuilder().createAnnotation("2010", date);
        pubExperimentImex2.addAnnotation(dateAnn3);

        lifecycleManager.getNewStatus().claimOwnership(pubExperimentImex2);
        lifecycleManager.getAssignedStatus().startCuration(pubExperimentImex2);
        lifecycleManager.getCurationInProgressStatus().readyForChecking(pubExperimentImex2, "test", true);
        lifecycleManager.getReadyForCheckingStatus().accept(pubExperimentImex2, "accepted");

        getCorePersister().saveOrUpdate(validPub, imexCurationLevel, pubInteractionImex, pubExperimentImex, pubExperimentImex2);

        getDataContext().commitTransaction(status);

        // update existing publications
        globalImexUpdaterTest.assignNewImexIdsToPublications();

        TransactionStatus status2 = getDataContext().beginTransaction();

        uk.ac.ebi.intact.model.Publication intactPubReloaded = getDaoFactory().getPublicationDao().getByAc(validPub.getAc());

        // added imex primary ref to publication (pub registered in IMEx central)
        Assert.assertEquals(1, intactPubReloaded.getXrefs().size());
        PublicationXref pubRef = intactPubReloaded.getXrefs().iterator().next();
        Assert.assertEquals(CvDatabase.IMEX_MI_REF, pubRef.getCvDatabase().getIdentifier());
        Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, pubRef.getCvXrefQualifier().getIdentifier());

        // updated annotations publication
        Assert.assertEquals(5, intactPubReloaded.getAnnotations().size());
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

        int index = 0;

        // updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(1, exp.getXrefs().size());

            ExperimentXref ref = exp.getXrefs().iterator().next();
            Assert.assertEquals(pubRef.getPrimaryId(), ref.getPrimaryId());
            Assert.assertEquals(imex.getIdentifier(), ref.getCvDatabase().getIdentifier());
            Assert.assertEquals(imexPrimary.getIdentifier(), ref.getCvXrefQualifier().getIdentifier());

            // updated interaction imex primary ref
            for (Interaction inter : exp.getInteractions()){
                index++;
                Assert.assertEquals(1, inter.getXrefs().size());

                InteractorXref ref2 = inter.getXrefs().iterator().next();
                Assert.assertTrue(ref2.getPrimaryId().startsWith(pubRef.getPrimaryId() +"-"));
                Assert.assertEquals(imex.getIdentifier(), ref2.getCvDatabase().getIdentifier());
                Assert.assertEquals(imexPrimary.getIdentifier(), ref2.getCvXrefQualifier().getIdentifier());
            }
        }

        // pub 2 is not updated because error
        uk.ac.ebi.intact.model.Publication intactPubReloaded2 = getDaoFactory().getPublicationDao().getByAc(imexCurationLevel.getAc());

        Assert.assertEquals(0, intactPubReloaded2.getXrefs().size());
        Assert.assertEquals(1, intactPubReloaded2.getAnnotations().size());
        for (Experiment exp : intactPubReloaded2.getExperiments()){
            Assert.assertEquals(0, exp.getXrefs().size());

            // updated interaction imex primary ref
            for (Interaction inter : exp.getInteractions()){
                Assert.assertEquals(0, inter.getXrefs().size());
            }
        }

        // pub 3 is not updated because error
        uk.ac.ebi.intact.model.Publication intactPubReloaded3 = getDaoFactory().getPublicationDao().getByAc(pubInteractionImex.getAc());

        Assert.assertEquals(0, intactPubReloaded3.getXrefs().size());
        Assert.assertEquals(0, intactPubReloaded3.getAnnotations().size());
        for (Experiment exp : intactPubReloaded3.getExperiments()){
            Assert.assertEquals(0, exp.getXrefs().size());

            // interaction with one conflict ref not deleted
            for (Interaction inter : exp.getInteractions()){
                Assert.assertEquals(1, inter.getXrefs().size());
            }
        }

        // pub 4 is not updated because error
        uk.ac.ebi.intact.model.Publication intactPubReloaded4 = getDaoFactory().getPublicationDao().getByAc(pubExperimentImex.getAc());

        Assert.assertEquals(0, intactPubReloaded4.getXrefs().size());
        Assert.assertEquals(0, intactPubReloaded4.getAnnotations().size());
        for (Experiment exp : intactPubReloaded4.getExperiments()){

            // experiment having conflict, does not touch xref
            Assert.assertEquals(1, exp.getXrefs().size());

            // updated interaction imex primary ref
            for (Interaction inter : exp.getInteractions()){
                Assert.assertEquals(0, inter.getXrefs().size());
            }
        }

        // pub 5 is updated because publication is IMEx eligible, it will report the experiment as imex conflict and not do anything in experiment
        uk.ac.ebi.intact.model.Publication intactPubReloaded5 = getDaoFactory().getPublicationDao().getByAc(pubExperimentImex2.getAc());

        // added imex primary ref to publication (pub not registered in IMEx central)
        Assert.assertEquals(1, intactPubReloaded5.getXrefs().size());
        PublicationXref pubRef2 = intactPubReloaded5.getXrefs().iterator().next();
        Assert.assertEquals(CvDatabase.IMEX_MI_REF, pubRef2.getCvDatabase().getIdentifier());
        Assert.assertEquals(CvXrefQualifier.IMEX_PRIMARY_MI_REF, pubRef2.getCvXrefQualifier().getIdentifier());

        // updated annotations publication
        Assert.assertEquals(5, intactPubReloaded5.getAnnotations().size());
        boolean hasFullCuration2 = false;
        boolean hasImexCuration2 = false;

        for (uk.ac.ebi.intact.model.Annotation ann : intactPubReloaded5.getAnnotations()){
            if ("imex curation".equals(ann.getCvTopic().getShortLabel())){
                hasImexCuration2 = true;
            }
            else if ("full coverage".equals(ann.getCvTopic().getShortLabel()) && "Only protein-protein interactions".equalsIgnoreCase(ann.getAnnotationText())){
                hasFullCuration2 = true;
            }
        }

        Assert.assertTrue(hasFullCuration2);
        Assert.assertTrue(hasImexCuration2);

        // updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded5.getExperiments()){
            // does not change experiment
            Assert.assertEquals(1, exp.getXrefs().size());

            ExperimentXref ref = exp.getXrefs().iterator().next();
            Assert.assertEquals("IM-3", ref.getPrimaryId());
            Assert.assertEquals(imex.getIdentifier(), ref.getCvDatabase().getIdentifier());
            Assert.assertEquals(imexPrimary.getIdentifier(), ref.getCvXrefQualifier().getIdentifier());

            // updated interaction imex primary ref
            for (Interaction inter : exp.getInteractions()){
                Assert.assertEquals(1, inter.getXrefs().size());

                InteractorXref ref2 = inter.getXrefs().iterator().next();
                Assert.assertTrue(ref2.getPrimaryId().startsWith(pubRef2.getPrimaryId() +"-"));
                Assert.assertEquals(imex.getIdentifier(), ref2.getCvDatabase().getIdentifier());
                Assert.assertEquals(imexPrimary.getIdentifier(), ref2.getCvXrefQualifier().getIdentifier());
            }
        }

        getDataContext().commitTransaction(status2);
    }
}
