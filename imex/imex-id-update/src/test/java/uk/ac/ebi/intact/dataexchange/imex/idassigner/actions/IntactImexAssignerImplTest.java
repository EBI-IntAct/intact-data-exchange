package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import edu.ucla.mbi.imex.central.ws.v20.Identifier;
import edu.ucla.mbi.imex.central.ws.v20.Publication;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.imex.extension.ImexPublication;
import psidev.psi.mi.jami.model.Experiment;
import psidev.psi.mi.jami.model.InteractionEvidence;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.utils.XrefUtils;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.jami.service.PublicationService;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tester for IntactImexAssignerImpl
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>05/04/12</pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/META-INF/intact-jami-test.spring.xml",
        "classpath*:/META-INF/imex-test.spring.xml"})
public class IntactImexAssignerImplTest{

    private IntactImexAssigner assignerTest;

    private ImexPublication imexPublication;

    @Before
    public void createImexPublications() throws BridgeFailedException {

        this.assignerTest = ApplicationContextProvider.getBean("intactImexAssigner");

        Publication pub = new Publication();
        Identifier pubmed = new Identifier();
        pubmed.setNs("pmid");
        pubmed.setAc("12345");
        pub.getIdentifier().add(pubmed);
        imexPublication = new ImexPublication(pub);
        assignerTest.getImexCentralClient().createPublication(imexPublication);
    }

    @Test
    @DirtiesContext
    public void assignImexPublication_validPubId_succesfull() throws BridgeFailedException {

        Assert.assertNull(imexPublication.getImexId());

        psidev.psi.mi.jami.model.Publication intactPub = new IntactPublication("12345");

        psidev.psi.mi.jami.model.Publication assigned = assignerTest.assignImexIdentifier(intactPub, imexPublication);

        Assert.assertNotNull(assigned);
        Assert.assertNotNull(assigned.getImexId());

        Assert.assertEquals(1, intactPub.getXrefs().size());
        Assert.assertFalse(XrefUtils.collectAllXrefsHavingDatabaseQualifierAndId(intactPub.getXrefs(),
                Xref.IMEX_MI, Xref.IMEX, assigned.getImexId(), Xref.IMEX_PRIMARY_MI, Xref.IMEX_PRIMARY).isEmpty());
    }

    @Test
    @DirtiesContext
    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    public void updateImexIdentifiersForAllExperiments() throws BridgeFailedException, PublicationImexUpdaterException, SynchronizerException, PersisterException, FinderException {

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        IntactPublication intactPub = new IntactPublication("12345");
        intactPub.setSource(new IntactSource("intact"));

        Experiment exp1 = new IntactExperiment(intactPub);
        exp1.setHostOrganism(new IntactOrganism(9606));
        intactPub.addExperiment(exp1);

        Experiment exp2 = new IntactExperiment(intactPub);
        exp2.setHostOrganism(new IntactOrganism(9607));
        intactPub.addExperiment(exp2);

        Experiment exp3 = new IntactExperiment(intactPub);
        exp3.setHostOrganism(new IntactOrganism(9608));
        intactPub.addExperiment(exp3);

        pubService.saveOrUpdate(intactPub);

        Assert.assertEquals(3, intactPub.getExperiments().size());

        List<String> expAcs = assignerTest.collectExperimentsToUpdateFrom(intactPub, "IM-1");
        Assert.assertEquals(3, expAcs.size());

        Set<String> expAcsUpdated = new HashSet<String>(expAcs.size());
        assignerTest.assignImexIdentifierToExperiments(expAcs, "IM-1", null, expAcsUpdated);

        psidev.psi.mi.jami.model.Publication intactPubReloaded = dao.getPublicationDao().getByAc(intactPub.getAc());
        List<Experiment> experiments = new ArrayList<Experiment>(intactPubReloaded.getExperiments());
        
        for (Experiment exp : experiments){
            Assert.assertEquals(1, exp.getXrefs().size());
            Assert.assertFalse(XrefUtils.collectAllXrefsHavingDatabaseQualifierAndId(exp.getXrefs(),
                    Xref.IMEX_MI, Xref.IMEX, "IM-1", Xref.IMEX_PRIMARY_MI, Xref.IMEX_PRIMARY).isEmpty());
        }
    }

    @Test(expected = PublicationImexUpdaterException.class)
    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    @DirtiesContext
    public void updateImexIdentifiersForAllExperiments_conflict() throws BridgeFailedException, SynchronizerException,
            PersisterException, FinderException, PublicationImexUpdaterException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        IntactPublication intactPub = new IntactPublication("12345");
        intactPub.setSource(new IntactSource("intact"));

        Experiment exp1 = new IntactExperiment(intactPub);
        exp1.setHostOrganism(new IntactOrganism(9606));
        intactPub.addExperiment(exp1);

        Experiment exp2 = new IntactExperiment(intactPub);
        exp2.setHostOrganism(new IntactOrganism(9607));
        intactPub.addExperiment(exp2);

        Experiment exp3 = new IntactExperiment(intactPub);
        intactPub.addExperiment(exp3);
        exp3.setHostOrganism(new IntactOrganism(9608));
        exp3.getXrefs().add(XrefUtils.createXrefWithQualifier(Xref.IMEX, Xref.IMEX_MI, "IM-3", Xref.IMEX_PRIMARY, Xref.IMEX_PRIMARY_MI));

        pubService.saveOrUpdate(intactPub);

        Assert.assertEquals(3, intactPub.getExperiments().size());

        List<String> expAcs = assignerTest.collectExperimentsToUpdateFrom(intactPub, "IM-1");
        Assert.assertEquals(3, expAcs.size());

        Set<String> expAcsUpdated = new HashSet<String>(expAcs.size());
        assignerTest.assignImexIdentifierToExperiments(expAcs, "IM-1", null, expAcsUpdated);
    }

    @Test
    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    @DirtiesContext
    public void updateImexIdentifiersForAllExperiments_existingImex() throws BridgeFailedException, PublicationImexUpdaterException,
            SynchronizerException, PersisterException, FinderException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        IntactPublication intactPub = new IntactPublication("12345");
        intactPub.setSource(new IntactSource("intact"));

        IntactExperiment exp1 = new IntactExperiment(intactPub);
        exp1.setHostOrganism(new IntactOrganism(9606));
        intactPub.addExperiment(exp1);
        exp1.getXrefs().add(XrefUtils.createXrefWithQualifier(Xref.IMEX, Xref.IMEX_MI, "IM-1", Xref.IMEX_PRIMARY, Xref.IMEX_PRIMARY_MI));

        IntactExperiment exp2 = new IntactExperiment(intactPub);
        exp2.setHostOrganism(new IntactOrganism(9607));
        intactPub.addExperiment(exp2);

        IntactExperiment exp3 = new IntactExperiment(intactPub);
        exp3.setHostOrganism(new IntactOrganism(9608));
        intactPub.addExperiment(exp3);

        pubService.saveOrUpdate(intactPub);

        Assert.assertEquals(3, intactPub.getExperiments().size());

        List<String> expAcs = assignerTest.collectExperimentsToUpdateFrom(intactPub, "IM-1");
        // only two experiments updated
        Assert.assertEquals(2, expAcs.size());

        Set<String> expAcsUpdated = new HashSet<String>(expAcs.size());
        assignerTest.assignImexIdentifierToExperiments(expAcs, "IM-1", null, expAcsUpdated);

        // check that exp1 has not been updated
        Experiment exp1Reloaded = dao.getExperimentDao().getByAc(exp1.getAc());
        Assert.assertEquals(1, exp1Reloaded.getXrefs().size());
        Assert.assertFalse(XrefUtils.collectAllXrefsHavingDatabaseQualifierAndId(exp1Reloaded.getXrefs(),
                Xref.IMEX_MI, Xref.IMEX, "IM-1", Xref.IMEX_PRIMARY_MI, Xref.IMEX_PRIMARY).isEmpty());

        Experiment exp2Reloaded = dao.getExperimentDao().getByAc(exp2.getAc());
        Assert.assertEquals(1, exp2Reloaded.getXrefs().size());
        Assert.assertFalse(XrefUtils.collectAllXrefsHavingDatabaseQualifierAndId(exp2Reloaded.getXrefs(),
                Xref.IMEX_MI, Xref.IMEX, "IM-1", Xref.IMEX_PRIMARY_MI, Xref.IMEX_PRIMARY).isEmpty());

        Experiment exp3Reloaded = dao.getExperimentDao().getByAc(exp3.getAc());
        Assert.assertEquals(1, exp3Reloaded.getXrefs().size());
        Assert.assertFalse(XrefUtils.collectAllXrefsHavingDatabaseQualifierAndId(exp3Reloaded.getXrefs(),
                Xref.IMEX_MI, Xref.IMEX, "IM-1", Xref.IMEX_PRIMARY_MI, Xref.IMEX_PRIMARY).isEmpty());
    }

    @Test
    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    @DirtiesContext
    public void updateImexIdentifiersForAllExperiments_duplicatedImex() throws BridgeFailedException, PublicationImexUpdaterException,
            SynchronizerException, PersisterException, FinderException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        IntactPublication intactPub = new IntactPublication("12345");
        intactPub.setSource(new IntactSource("intact"));

        IntactExperiment exp1 = new IntactExperiment(intactPub);
        intactPub.addExperiment(exp1);
        exp1.setHostOrganism(new IntactOrganism(9606));
        exp1.getXrefs().add(XrefUtils.createXrefWithQualifier(Xref.IMEX, Xref.IMEX_MI, "IM-1", Xref.IMEX_PRIMARY, Xref.IMEX_PRIMARY_MI));
        exp1.getXrefs().add(XrefUtils.createXrefWithQualifier(Xref.IMEX, Xref.IMEX_MI, "IM-1", Xref.IMEX_PRIMARY, Xref.IMEX_PRIMARY_MI));

        IntactExperiment exp2 = new IntactExperiment(intactPub);
        exp2.setHostOrganism(new IntactOrganism(9607));
        intactPub.addExperiment(exp2);

        IntactExperiment exp3 = new IntactExperiment(intactPub);
        exp3.setHostOrganism(new IntactOrganism(9608));
        exp3.getXrefs().add(XrefUtils.createXrefWithQualifier(Xref.INTERPRO, Xref.INTERPRO_MI, "test"));
        intactPub.addExperiment(exp3);

        pubService.saveOrUpdate(intactPub);

        Assert.assertEquals(3, intactPub.getExperiments().size());

        List<String> expAcs = assignerTest.collectExperimentsToUpdateFrom(intactPub, "IM-1");
        // only two experiments updated
        Assert.assertEquals(2, expAcs.size());

        Set<String> expAcsUpdated = new HashSet<String>(expAcs.size());
        assignerTest.assignImexIdentifierToExperiments(expAcs, "IM-1", null, expAcsUpdated);

        // check that exp1 has not been updated
        Experiment exp1Reloaded = dao.getExperimentDao().getByAc(exp1.getAc());
        Assert.assertEquals(1, exp1Reloaded.getXrefs().size());
        Assert.assertFalse(XrefUtils.collectAllXrefsHavingDatabaseQualifierAndId(exp1Reloaded.getXrefs(),
                Xref.IMEX_MI, Xref.IMEX, "IM-1", Xref.IMEX_PRIMARY_MI, Xref.IMEX_PRIMARY).isEmpty());

        Experiment exp2Reloaded = dao.getExperimentDao().getByAc(exp2.getAc());
        Assert.assertEquals(1, exp2Reloaded.getXrefs().size());
        Assert.assertFalse(XrefUtils.collectAllXrefsHavingDatabaseQualifierAndId(exp2Reloaded.getXrefs(),
                Xref.IMEX_MI, Xref.IMEX, "IM-1", Xref.IMEX_PRIMARY_MI, Xref.IMEX_PRIMARY).isEmpty());

        Experiment exp3Reloaded = dao.getExperimentDao().getByAc(exp3.getAc());
        Assert.assertEquals(2, exp3Reloaded.getXrefs().size());
        Assert.assertFalse(XrefUtils.collectAllXrefsHavingDatabaseQualifierAndId(exp3Reloaded.getXrefs(),
                Xref.IMEX_MI, Xref.IMEX, "IM-1", Xref.IMEX_PRIMARY_MI, Xref.IMEX_PRIMARY).isEmpty());
    }

    @Test
    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    @DirtiesContext
    public void collectExistingInteractionImexIds() throws BridgeFailedException, SynchronizerException,
            PersisterException, FinderException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        IntactPublication intactPub = new IntactPublication("12345");
        intactPub.setSource(new IntactSource("intact"));

        IntactExperiment exp1 = new IntactExperiment(intactPub);
        intactPub.addExperiment(exp1);
        IntactInteractionEvidence ev1 = new IntactInteractionEvidence();
        ev1.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12345")));
        ev1.assignImexId("IM-1-1");
        exp1.addInteractionEvidence(ev1);

        IntactExperiment exp2 = new IntactExperiment(intactPub);
        intactPub.addExperiment(exp2);
        IntactInteractionEvidence ev2 = new IntactInteractionEvidence();
        ev2.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12346")));
        ev2.assignImexId("IM-1-2");
        exp2.addInteractionEvidence(ev2);

        IntactExperiment exp3 = new IntactExperiment(intactPub);
        intactPub.addExperiment(exp3);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12347")));
        ev3.assignImexId("IM-1");
        exp3.addInteractionEvidence(ev3);

        pubService.saveOrUpdate(intactPub);

        Assert.assertEquals(3, dao.getInteractionDao().countAll());

        List<String> imexIds = assignerTest.collectExistingInteractionImexIdsForPublication(intactPub);
        Assert.assertEquals(3, imexIds.size());

        Assert.assertTrue(imexIds.contains("IM-1-1"));
        Assert.assertTrue(imexIds.contains("IM-1-2"));
        Assert.assertTrue(imexIds.contains("IM-1"));
    }

    @Test
    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    @DirtiesContext
    public void updateImexIdentifiersForAllInteractions() throws BridgeFailedException, PublicationImexUpdaterException,
            SynchronizerException, PersisterException, FinderException {

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        IntactPublication intactPub = new IntactPublication("12345");
        intactPub.setSource(new IntactSource("intact"));

        IntactExperiment exp1 = new IntactExperiment(intactPub);
        intactPub.addExperiment(exp1);
        IntactInteractionEvidence ev1 = new IntactInteractionEvidence();
        ev1.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp1.addInteractionEvidence(ev1);

        IntactExperiment exp2 = new IntactExperiment(intactPub);
        intactPub.addExperiment(exp2);
        IntactInteractionEvidence ev2 = new IntactInteractionEvidence();
        ev2.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12346")));
        exp2.addInteractionEvidence(ev2);

        IntactExperiment exp3 = new IntactExperiment(intactPub);
        intactPub.addExperiment(exp3);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12347")));
        exp3.addInteractionEvidence(ev3);

        pubService.saveOrUpdate(intactPub);

        Assert.assertEquals(3, dao.getInteractionDao().countAll());

        assignerTest.resetPublicationContext(intactPub, "IM-1");

        List<String> interactionAcs = assignerTest.collectInteractionsToUpdateFrom(intactPub, "IM-1");
        Assert.assertEquals(3, interactionAcs.size());

        Set<String> intUpdated = new HashSet<String>(interactionAcs.size());
        assignerTest.assignImexIdentifierToInteractions(interactionAcs, "IM-1", null, intUpdated);
        InteractionEvidence int1Reloaded = dao.getInteractionDao().getByAc(ev1.getAc());
        Assert.assertEquals(1, int1Reloaded.getXrefs().size());
        Xref ref = int1Reloaded.getXrefs().iterator().next();
        Assert.assertEquals(Xref.IMEX_MI, ref.getDatabase().getMIIdentifier());
        Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref.getQualifier().getMIIdentifier());
        Assert.assertTrue(ref.getId().startsWith("IM-1-"));

        InteractionEvidence int2Reloaded = dao.getInteractionDao().getByAc(ev2.getAc());
        Assert.assertEquals(1, int2Reloaded.getXrefs().size());
        Xref ref2 = int2Reloaded.getXrefs().iterator().next();
        Assert.assertEquals(Xref.IMEX_MI, ref2.getDatabase().getMIIdentifier());
        Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref2.getQualifier().getMIIdentifier());
        Assert.assertTrue(ref2.getId().startsWith("IM-1-"));

        InteractionEvidence int3Reloaded = dao.getInteractionDao().getByAc(ev3.getAc());
        Assert.assertEquals(1, int3Reloaded.getXrefs().size());
        Xref ref3 = int3Reloaded.getXrefs().iterator().next();
        Assert.assertEquals(Xref.IMEX_MI, ref3.getDatabase().getMIIdentifier());
        Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref3.getQualifier().getMIIdentifier());
        Assert.assertTrue(ref3.getId().startsWith("IM-1-"));
        
        Assert.assertNotSame(ref3.getId(), ref2.getId());
        Assert.assertNotSame(ref.getId(), ref2.getId());
        Assert.assertNotSame(ref.getId(), ref3.getId());
    }

    @Test(expected = PublicationImexUpdaterException.class)
    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    @DirtiesContext
    public void updateImexIdentifiersForAllInteractions_conflict() throws BridgeFailedException, SynchronizerException,
            PersisterException, FinderException, PublicationImexUpdaterException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        IntactPublication intactPub = new IntactPublication("12345");
        intactPub.setSource(new IntactSource("intact"));

        IntactExperiment exp1 = new IntactExperiment(intactPub);
        intactPub.addExperiment(exp1);
        IntactInteractionEvidence ev1 = new IntactInteractionEvidence();
        ev1.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12345")));
        ev1.assignImexId("IM-1-1");
        exp1.addInteractionEvidence(ev1);

        IntactExperiment exp2 = new IntactExperiment(intactPub);
        intactPub.addExperiment(exp2);
        IntactInteractionEvidence ev2 = new IntactInteractionEvidence();
        ev2.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12346")));
        exp2.addInteractionEvidence(ev2);

        IntactExperiment exp3 = new IntactExperiment(intactPub);
        intactPub.addExperiment(exp3);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12347")));
        ev3.assignImexId("IM-3-1");
        exp3.addInteractionEvidence(ev3);

        pubService.saveOrUpdate(intactPub);

        Assert.assertEquals(3, dao.getInteractionDao().countAll());

        assignerTest.resetPublicationContext(intactPub, "IM-1");
        List<String> interactionAcs = assignerTest.collectInteractionsToUpdateFrom(intactPub, "IM-1");
        Assert.assertEquals(2, interactionAcs.size());

        Set<String> intUpdated = new HashSet<String>(interactionAcs.size());
        assignerTest.assignImexIdentifierToInteractions(interactionAcs, "IM-1", null, intUpdated);
    }

    @Test
    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    @DirtiesContext
    public void updateImexIdentifiersForAllInteractions_existingImex() throws BridgeFailedException, PublicationImexUpdaterException,
            SynchronizerException, PersisterException, FinderException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        IntactPublication intactPub = new IntactPublication("12345");
        intactPub.setSource(new IntactSource("intact"));

        IntactExperiment exp1 = new IntactExperiment(intactPub);
        intactPub.addExperiment(exp1);
        IntactInteractionEvidence ev1 = new IntactInteractionEvidence();
        ev1.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12345")));
        ev1.assignImexId("IM-1-1");
        exp1.addInteractionEvidence(ev1);

        IntactExperiment exp2 = new IntactExperiment(intactPub);
        intactPub.addExperiment(exp2);
        IntactInteractionEvidence ev2 = new IntactInteractionEvidence();
        ev2.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12346")));
        exp2.addInteractionEvidence(ev2);

        IntactExperiment exp3 = new IntactExperiment(intactPub);
        intactPub.addExperiment(exp3);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12347")));
        exp3.addInteractionEvidence(ev3);

        pubService.saveOrUpdate(intactPub);

        Assert.assertEquals(3, dao.getInteractionDao().countAll());

        assignerTest.resetPublicationContext(intactPub, "IM-1");

        List<String> interactionAcs = assignerTest.collectInteractionsToUpdateFrom(intactPub, "IM-1");
        Assert.assertEquals(2, interactionAcs.size());

        Set<String> intUpdated = new HashSet<String>(interactionAcs.size());
        assignerTest.assignImexIdentifierToInteractions(interactionAcs, "IM-1", null, intUpdated);

        InteractionEvidence int1Reloaded = dao.getInteractionDao().getByAc(ev1.getAc());
        Assert.assertEquals(1, int1Reloaded.getXrefs().size());
        Xref ref = int1Reloaded.getXrefs().iterator().next();
        Assert.assertEquals(Xref.IMEX_MI, ref.getDatabase().getMIIdentifier());
        Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref.getQualifier().getMIIdentifier());
        Assert.assertTrue(ref.getId().startsWith("IM-1-"));

        InteractionEvidence int2Reloaded = dao.getInteractionDao().getByAc(ev2.getAc());
        Assert.assertEquals(1, int2Reloaded.getXrefs().size());
        Xref ref2 = int2Reloaded.getXrefs().iterator().next();
        Assert.assertEquals(Xref.IMEX_MI, ref2.getDatabase().getMIIdentifier());
        Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref2.getQualifier().getMIIdentifier());
        Assert.assertTrue(ref2.getId().startsWith("IM-1-"));

        InteractionEvidence int3Reloaded = dao.getInteractionDao().getByAc(ev3.getAc());
        Assert.assertEquals(1, int3Reloaded.getXrefs().size());
        Xref ref3 = int3Reloaded.getXrefs().iterator().next();
        Assert.assertEquals(Xref.IMEX_MI, ref3.getDatabase().getMIIdentifier());
        Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref3.getQualifier().getMIIdentifier());
        Assert.assertTrue(ref3.getId().startsWith("IM-1-"));

        Assert.assertNotSame(ref3.getId(), ref2.getId());
        Assert.assertNotSame(ref.getId(), ref2.getId());
        Assert.assertNotSame(ref.getId(), ref3.getId());
    }

    @Test
    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    @DirtiesContext
    public void updateImexIdentifiersForAllInteractions_duplicatedImex() throws BridgeFailedException, PublicationImexUpdaterException,
            SynchronizerException, PersisterException, FinderException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        IntactPublication intactPub = new IntactPublication("12345");
        intactPub.setSource(new IntactSource("intact"));
        IntactExperiment exp1 = new IntactExperiment(intactPub);
        intactPub.addExperiment(exp1);
        IntactInteractionEvidence ev1 = new IntactInteractionEvidence();
        ev1.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12345")));
        ev1.getXrefs().add(XrefUtils.createXrefWithQualifier(Xref.IMEX, Xref.IMEX_MI, "IM-1-1", Xref.IMEX_PRIMARY, Xref.IMEX_PRIMARY_MI));
        ev1.assignImexId("IM-1-1");
        exp1.addInteractionEvidence(ev1);
        IntactExperiment exp2 = new IntactExperiment(intactPub);
        intactPub.addExperiment(exp2);
        IntactInteractionEvidence ev2 = new IntactInteractionEvidence();
        ev2.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12346")));
        exp2.addInteractionEvidence(ev2);

        IntactExperiment exp3 = new IntactExperiment(intactPub);
        intactPub.addExperiment(exp3);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12347")));
        exp3.addInteractionEvidence(ev3);

        pubService.saveOrUpdate(intactPub);

        Assert.assertEquals(3, dao.getInteractionDao().countAll());

        assignerTest.resetPublicationContext(intactPub, "IM-1");

        List<String> interactionAcs = assignerTest.collectInteractionsToUpdateFrom(intactPub, "IM-1");
        Assert.assertEquals(2, interactionAcs.size());

        Set<String> intUpdated = new HashSet<String>(interactionAcs.size());
        assignerTest.assignImexIdentifierToInteractions(interactionAcs, "IM-1", null, intUpdated);

        InteractionEvidence int1Reloaded = dao.getInteractionDao().getByAc(ev1.getAc());
        Assert.assertEquals(1, int1Reloaded.getXrefs().size());
        Xref ref = int1Reloaded.getXrefs().iterator().next();
        Assert.assertEquals(Xref.IMEX_MI, ref.getDatabase().getMIIdentifier());
        Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref.getQualifier().getMIIdentifier());
        Assert.assertTrue(ref.getId().startsWith("IM-1-"));

        InteractionEvidence int2Reloaded = dao.getInteractionDao().getByAc(ev2.getAc());
        Assert.assertEquals(1, int2Reloaded.getXrefs().size());
        Xref ref2 = int2Reloaded.getXrefs().iterator().next();
        Assert.assertEquals(Xref.IMEX_MI, ref2.getDatabase().getMIIdentifier());
        Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref2.getQualifier().getMIIdentifier());
        Assert.assertTrue(ref2.getId().startsWith("IM-1-"));

        InteractionEvidence int3Reloaded = dao.getInteractionDao().getByAc(ev3.getAc());
        Assert.assertEquals(1, int3Reloaded.getXrefs().size());
        Xref ref3 = int3Reloaded.getXrefs().iterator().next();
        Assert.assertEquals(Xref.IMEX_MI, ref3.getDatabase().getMIIdentifier());
        Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref3.getQualifier().getMIIdentifier());
        Assert.assertTrue(ref3.getId().startsWith("IM-1-"));

        Assert.assertNotSame(ref3.getId(), ref2.getId());
        Assert.assertNotSame(ref.getId(), ref2.getId());
        Assert.assertNotSame(ref.getId(), ref3.getId());
    }
}
