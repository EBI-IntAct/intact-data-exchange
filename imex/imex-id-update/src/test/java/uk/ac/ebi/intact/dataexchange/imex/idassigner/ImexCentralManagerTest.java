package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import edu.ucla.mbi.imex.central.ws.v20.Identifier;
import edu.ucla.mbi.imex.central.ws.v20.Publication;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.imex.PublicationStatus;
import psidev.psi.mi.jami.bridges.imex.extension.ImexPublication;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.XrefUtils;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleStatus;
import uk.ac.ebi.intact.jami.model.user.User;
import uk.ac.ebi.intact.jami.service.PublicationService;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tester of ImexCentralManager
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11/04/12</pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/META-INF/intact-jami-test.spring.xml",
        "classpath*:/META-INF/imex-test.spring.xml"})
public class ImexCentralManagerTest {

    @Autowired
    @Qualifier("imexCentralManager")
    private ImexCentralManager imexManagerTest;

    @Before
    public void createImexRecord() throws BridgeFailedException {
        Publication pub = new Publication();
        pub.setImexAccession("IM-3");
        imexManagerTest.getImexCentralRegister().getImexCentralClient().createPublication(new ImexPublication(pub));

        Publication pub2 = new Publication();
        Identifier pubmed = new Identifier();
        pubmed.setNs("pmid");
        pubmed.setAc("12347");
        pub2.getIdentifier().add(pubmed);
        pub2.setImexAccession("IM-7");
        imexManagerTest.getImexCentralRegister().getImexCentralClient().createPublication(new ImexPublication(pub2));

        Publication pub3 = new Publication();
        Identifier pubmed2 = new Identifier();
        pubmed2.setNs("pmid");
        pubmed2.setAc("12348");
        pub3.getIdentifier().add(pubmed2);
        imexManagerTest.getImexCentralRegister().getImexCentralClient().createPublication(new ImexPublication(pub3));

        Publication pub4 = new Publication();
        Identifier pubmed3 = new Identifier();
        pubmed3.setNs("jint");
        pubmed3.setAc("unassigned504");
        pub4.getIdentifier().add(pubmed3);
        imexManagerTest.getImexCentralRegister().getImexCentralClient().createPublication(new ImexPublication(pub4));

        Publication pub5 = new Publication();
        Identifier pubmed4 = new Identifier();
        pubmed4.setNs("jint");
        pubmed4.setAc("unassigned604");
        pub5.getIdentifier().add(pubmed4);
        pub5.setImexAccession("IM-4");
        imexManagerTest.getImexCentralRegister().getImexCentralClient().createPublication(new ImexPublication(pub5));
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void collect_update_imex_primaryRef_publications() throws SynchronizerException, PersisterException, FinderException,
            PublicationImexUpdaterException, EnricherException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        IntactPublication intactPublication = new IntactPublication("12345");
        intactPublication.assignImexId("IM-3");
        intactPublication.setSource(new IntactSource("intact"));
        pubService.saveOrUpdate(intactPublication);

        imexManagerTest.updateIntactPublicationHavingIMEx(intactPublication.getAc());
        Assert.assertEquals(1, dao.getPublicationDao().getByAc(intactPublication.getAc()).getXrefs().size());
        Assert.assertEquals("IM-3", dao.getPublicationDao().getByAc(intactPublication.getAc()).getImexId());
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void collect_imex_primaryRef_publications_delete_duplicated() throws SynchronizerException, PersisterException, FinderException, PublicationImexUpdaterException, EnricherException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        IntactPublication intactPub = new IntactPublication("12345");
        intactPub.assignImexId("IM-3");
        intactPub.setSource(new IntactSource("intact"));
        intactPub.getXrefs().add(XrefUtils.createXrefWithQualifier(Xref.IMEX, Xref.IMEX_MI, "IM-3", Xref.IMEX_PRIMARY, Xref.IMEX_PRIMARY_MI));
        pubService.saveOrUpdate(intactPub);

        Assert.assertEquals(2, intactPub.getXrefs().size());

        imexManagerTest.updateIntactPublicationHavingIMEx(intactPub.getAc());
        IntactPublication intactPubReloaded = dao.getPublicationDao().getByAc(intactPub.getAc());

        Assert.assertEquals(1, intactPubReloaded.getXrefs().size());
        Assert.assertEquals("IM-3", intactPubReloaded.getImexId());
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void collect_imex_primaryRef_publications_imex_conflict() throws SynchronizerException, PersisterException, FinderException,
            PublicationImexUpdaterException, EnricherException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        IntactPublication intactPub = new IntactPublication("12345");
        intactPub.assignImexId("IM-3");
        intactPub.setSource(new IntactSource("intact"));

        intactPub.getXrefs().add(XrefUtils.createXrefWithQualifier(Xref.IMEX, Xref.IMEX_MI, "IM-4", Xref.IMEX_PRIMARY, Xref.IMEX_PRIMARY_MI));
        pubService.saveOrUpdate(intactPub);

        Assert.assertEquals(2, intactPub.getXrefs().size());

        imexManagerTest.updateIntactPublicationHavingIMEx(intactPub.getAc());

        IntactPublication intactPubReloaded = dao.getPublicationDao().getByAc(intactPub.getAc());

        Assert.assertEquals(2, intactPubReloaded.getXrefs().size());
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void update_publication_having_imex() throws PublicationImexUpdaterException, BridgeFailedException, SynchronizerException, PersisterException, FinderException, EnricherException {
        User user = new User("default", "default", "default", "default@ebi.ac.uk");

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        dao.getUserDao().persist(user);

        IntactPublication intactPublication = new IntactPublication("12345");
        intactPublication.setStatus(LifeCycleStatus.NEW);
        intactPublication.assignImexId("IM-3");
        intactPublication.setSource(new IntactSource("intact"));
        intactPublication.setCurrentOwner(user);

        Experiment exp1 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp1);
        IntactInteractionEvidence ev1 = new IntactInteractionEvidence();
        ev1.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp1.addInteractionEvidence(ev1);

        Experiment exp2 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp2);
        IntactInteractionEvidence ev2 = new IntactInteractionEvidence();
        ev2.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp2.addInteractionEvidence(ev2);

        Experiment exp3 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp3);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp3.addInteractionEvidence(ev3);

        pubService.saveOrUpdate(intactPublication);

        imexManagerTest.updateIntactPublicationHavingIMEx(intactPublication.getAc());
        ImexPublication imexPub = (ImexPublication)imexManagerTest.getImexCentralRegister().getExistingPublicationInImexCentral("12345", "pubmed");

        // updated imex identifier because not present
        Assert.assertNotNull(imexPub);
        Assert.assertEquals(1, imexPub.getIdentifiers().size());
        Xref id = imexPub.getIdentifiers().iterator().next();
        Assert.assertEquals("pubmed", id.getDatabase().getShortName());
        Assert.assertEquals("12345", id.getId());

        // valid pubmed so whould have synchronized admin group, admin user and status
        // admin group INTACT
        Assert.assertEquals(1, imexPub.getSources().size());
        Assert.assertEquals("INTACT", imexPub.getSources().iterator().next().getShortName().toUpperCase());
        // admin user phantom because curator is not known in imex central
        Assert.assertEquals(1, imexPub.getCurators().size());
        Assert.assertEquals("phantom", imexPub.getCurators().iterator().next());
        // status released
        Assert.assertEquals(PublicationStatus.NEW, imexPub.getStatus());

        IntactPublication intactPubReloaded = dao.getPublicationDao().getByAc(intactPublication.getAc());

        // updated annotations publication
        Assert.assertEquals(CurationDepth.IMEx, intactPubReloaded.getCurationDepth());
        Assert.assertEquals(2, intactPubReloaded.getAnnotations().size());
        boolean hasFullCuration = false;
        boolean hasImexCuration = false;

        for (Annotation ann : intactPubReloaded.getAnnotations()){
            if ("imex curation".equals(ann.getTopic().getShortName())){
                hasImexCuration = true;
            }
            else if ("full coverage".equals(ann.getTopic().getShortName())
                    && "Only protein-protein interactions".equalsIgnoreCase(ann.getValue())){
                hasFullCuration = true;
            }
        }

        Assert.assertTrue(hasFullCuration);
        Assert.assertTrue(hasImexCuration);

        int index = 0;

        // updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(1, exp.getXrefs().size());

            Xref ref = exp.getXrefs().iterator().next();
            Assert.assertEquals("IM-3", ref.getId());
            Assert.assertEquals(Xref.IMEX_MI, ref.getDatabase().getMIIdentifier());
            Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref.getQualifier().getMIIdentifier());

            Set<String> imexIds  = new HashSet<String>(3);
            // updated interaction imex primary ref
            for (InteractionEvidence inter : exp.getInteractionEvidences()){
                index++;
                Assert.assertEquals(1, inter.getXrefs().size());

                ref = inter.getXrefs().iterator().next();
                Assert.assertTrue(ref.getId().startsWith("IM-3-"));
                Assert.assertEquals(Xref.IMEX_MI, ref.getDatabase().getMIIdentifier());
                Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref.getQualifier().getMIIdentifier());

                imexIds.add(ref.getId());
            }

            Assert.assertEquals(3, imexIds.size());
        }
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void update_publication_having_imex_conflict() throws PublicationImexUpdaterException, BridgeFailedException,
            SynchronizerException, PersisterException, FinderException, EnricherException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        IntactPublication intactPublication = new IntactPublication("12345");
        intactPublication.assignImexId("IM-3");
        intactPublication.setSource(new IntactSource("intact"));
        intactPublication.getXrefs().add(XrefUtils.createXrefWithQualifier(Xref.IMEX, Xref.IMEX_MI, "IM-4", Xref.IMEX_PRIMARY, Xref.IMEX_PRIMARY_MI));

        Experiment exp1 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp1);
        IntactInteractionEvidence ev1 = new IntactInteractionEvidence();
        ev1.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp1.addInteractionEvidence(ev1);

        Experiment exp2 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp2);
        IntactInteractionEvidence ev2 = new IntactInteractionEvidence();
        ev2.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp2.addInteractionEvidence(ev2);

        Experiment exp3 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp3);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp3.addInteractionEvidence(ev3);

        pubService.saveOrUpdate(intactPublication);

        imexManagerTest.updateIntactPublicationHavingIMEx(intactPublication.getAc());

        IntactPublication intactPubReloaded = dao.getPublicationDao().getByAc(intactPublication.getAc());

        // did not update annotations because of conflict
        Assert.assertEquals(0, intactPubReloaded.getAnnotations().size());

        // did not updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(0, exp.getXrefs().size());

            // did not updated interaction imex primary ref
            for (Interaction inter : exp.getInteractionEvidences()){
                Assert.assertEquals(0, inter.getXrefs().size());
            }
        }
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void update_publication_having_imex_imex_not_recognized_in_imexcentral() throws PublicationImexUpdaterException, BridgeFailedException,
            SynchronizerException, PersisterException, FinderException, EnricherException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        IntactPublication intactPublication = new IntactPublication("12345");
        intactPublication.assignImexId("IM-5");
        intactPublication.setSource(new IntactSource("intact"));

        Experiment exp1 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp1);
        IntactInteractionEvidence ev1 = new IntactInteractionEvidence();
        ev1.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp1.addInteractionEvidence(ev1);

        Experiment exp2 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp2);
        IntactInteractionEvidence ev2 = new IntactInteractionEvidence();
        ev2.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp2.addInteractionEvidence(ev2);

        Experiment exp3 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp3);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp3.addInteractionEvidence(ev3);

        pubService.saveOrUpdate(intactPublication);

        imexManagerTest.updateIntactPublicationHavingIMEx(intactPublication.getAc());

        IntactPublication intactPubReloaded = dao.getPublicationDao().getByAc(intactPublication.getAc());

        // did not update annotations because of imex unknown
        Assert.assertEquals(0, intactPubReloaded.getAnnotations().size());

        // did not updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(0, exp.getXrefs().size());

            // did not updated interaction imex primary ref
            for (Interaction inter : exp.getInteractionEvidences()){
                Assert.assertEquals(0, inter.getXrefs().size());
            }
        }
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void update_publication_having_imex_noPubmedId() throws PublicationImexUpdaterException, BridgeFailedException,
            SynchronizerException, PersisterException, FinderException, EnricherException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        IntactPublication intactPublication = new IntactPublication("unassigned604");
        intactPublication.setStatus(LifeCycleStatus.NEW);
        intactPublication.assignImexId("IM-4");
        intactPublication.setSource(new IntactSource("intact"));

        Experiment exp1 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp1);
        IntactInteractionEvidence ev1 = new IntactInteractionEvidence();
        ev1.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp1.addInteractionEvidence(ev1);

        Experiment exp2 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp2);
        IntactInteractionEvidence ev2 = new IntactInteractionEvidence();
        ev2.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp2.addInteractionEvidence(ev2);

        Experiment exp3 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp3);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp3.addInteractionEvidence(ev3);

        pubService.saveOrUpdate(intactPublication);

        imexManagerTest.updateIntactPublicationHavingIMEx(intactPublication.getAc());
        ImexPublication imexPublication = (ImexPublication)imexManagerTest.getImexCentralRegister().getExistingPublicationInImexCentral("IM-4", "imex");
        // updated imex identifier because not present
        Assert.assertNotNull(imexPublication);
        Assert.assertEquals(1, imexPublication.getIdentifiers().size());
        Xref id = imexPublication.getIdentifiers().iterator().next();
        Assert.assertEquals("jint", id.getDatabase().getShortName());
        Assert.assertEquals("unassigned604", id.getId());

        Assert.assertNotNull(imexPublication.getSources());
        Assert.assertNotNull(imexPublication.getCurators());
        Assert.assertNotNull(imexPublication.getStatus());

        IntactPublication intactPubReloaded = dao.getPublicationDao().getByAc(intactPublication.getAc());

        // updated annotations publication
        Assert.assertEquals(CurationDepth.IMEx, intactPubReloaded.getCurationDepth());
        Assert.assertEquals(2, intactPubReloaded.getAnnotations().size());
        boolean hasFullCuration = false;
        boolean hasImexCuration = false;

        for (Annotation ann : intactPubReloaded.getAnnotations()){
            if ("imex curation".equals(ann.getTopic().getShortName())){
                hasImexCuration = true;
            }
            else if ("full coverage".equals(ann.getTopic().getShortName())
                    && "Only protein-protein interactions".equalsIgnoreCase(ann.getValue())){
                hasFullCuration = true;
            }
        }

        Assert.assertTrue(hasFullCuration);
        Assert.assertTrue(hasImexCuration);

        // updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(1, exp.getXrefs().size());

            Xref ref = exp.getXrefs().iterator().next();
            Assert.assertEquals("IM-4", ref.getId());
            Assert.assertEquals(Xref.IMEX_MI, ref.getDatabase().getMIIdentifier());
            Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref.getQualifier().getMIIdentifier());

            // updated interaction imex primary ref
            Set<String> imexIds  = new HashSet<String>(3);
            // updated interaction imex primary ref
            for (InteractionEvidence inter : exp.getInteractionEvidences()){
                Assert.assertEquals(1, inter.getXrefs().size());

                ref = inter.getXrefs().iterator().next();
                Assert.assertTrue(ref.getId().startsWith("IM-4-"));
                Assert.assertEquals(Xref.IMEX_MI, ref.getDatabase().getMIIdentifier());
                Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref.getQualifier().getMIIdentifier());

                imexIds.add(ref.getId());
            }

            Assert.assertEquals(3, imexIds.size());
        }
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void assign_imex_register_update_publication() throws PublicationImexUpdaterException, BridgeFailedException, SynchronizerException, PersisterException, FinderException, EnricherException {
        User user = new User("default", "default", "default", "default@ebi.ac.uk");

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        dao.getUserDao().persist(user);
        IntactPublication intactPublication = new IntactPublication("12346");
        intactPublication.setCurationDepth(CurationDepth.IMEx);
        intactPublication.setSource(new IntactSource("intact"));
        intactPublication.setCurrentOwner(user);

        Experiment exp1 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp1);
        IntactInteractionEvidence ev1 = new IntactInteractionEvidence();
        ev1.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp1.addInteractionEvidence(ev1);

        Experiment exp2 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp2);
        IntactInteractionEvidence ev2 = new IntactInteractionEvidence();
        ev2.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp2.addInteractionEvidence(ev2);

        Experiment exp3 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp3);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp3.addInteractionEvidence(ev3);

        pubService.saveOrUpdate(intactPublication);

        imexManagerTest.assignImexAndUpdatePublication(intactPublication.getAc());
        ImexPublication imexPublication = (ImexPublication)imexManagerTest.getImexCentralRegister().getExistingPublicationInImexCentral("12346", "pubmed");

        // updated imex identifier because not present
        Assert.assertNotNull(imexPublication);
        Assert.assertEquals(1, imexPublication.getIdentifiers().size());
        Xref id = imexPublication.getIdentifiers().iterator().next();
        Assert.assertEquals("pubmed", id.getDatabase().getShortName());
        Assert.assertEquals("12346", id.getId());

        // valid pubmed so whould have synchronized admin group, admin user and status
        // admin group INTACT
        Assert.assertEquals(1, imexPublication.getSources().size());
        Assert.assertEquals("INTACT", imexPublication.getSources().iterator().next().getShortName().toUpperCase());
        // admin user phantom because curator is not known in imex central
        Assert.assertEquals(1, imexPublication.getCurators().size());
        Assert.assertEquals("phantom", imexPublication.getCurators().iterator().next());
        // status released
        Assert.assertEquals(PublicationStatus.NEW, imexPublication.getStatus());

        // assigned IMEx
        Assert.assertNotNull(imexPublication.getImexId());

        IntactPublication intactPubReloaded = dao.getPublicationDao().getByAc(intactPublication.getAc());

        // added imex primary ref to publication
        Assert.assertEquals(1, intactPubReloaded.getXrefs().size());
        Assert.assertNotNull(intactPubReloaded.getImexId());

        // updated annotations publication 
        Assert.assertEquals(2, intactPubReloaded.getAnnotations().size());
        boolean hasFullCuration = false;
        boolean hasImexCuration = false;

        for (Annotation ann : intactPubReloaded.getAnnotations()){
            if ("imex curation".equals(ann.getTopic().getShortName())){
                hasImexCuration = true;
            }
            else if ("full coverage".equals(ann.getTopic().getShortName())
                    && "Only protein-protein interactions".equalsIgnoreCase(ann.getValue())){
                hasFullCuration = true;
            }
        }

        Assert.assertTrue(hasFullCuration);
        Assert.assertTrue(hasImexCuration);

        // updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(1, exp.getXrefs().size());

            Xref ref = exp.getXrefs().iterator().next();
            Assert.assertNotNull(ref.getId());
            Assert.assertEquals(Xref.IMEX_MI, ref.getDatabase().getMIIdentifier());
            Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref.getQualifier().getMIIdentifier());

            // updated interaction imex primary ref
            Set<String> imexIds  = new HashSet<String>(3);
            // updated interaction imex primary ref
            for (InteractionEvidence inter : exp.getInteractionEvidences()){
                Assert.assertEquals(1, inter.getXrefs().size());

                ref = inter.getXrefs().iterator().next();
                Assert.assertNotNull(ref.getId());
                Assert.assertEquals(Xref.IMEX_MI, ref.getDatabase().getMIIdentifier());
                Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref.getQualifier().getMIIdentifier());

                imexIds.add(ref.getId());
            }

            Assert.assertEquals(3, imexIds.size());
        }
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void cannot_assign_imex_update_publication_existingRecordWithImex() throws PublicationImexUpdaterException, BridgeFailedException, SynchronizerException, PersisterException, FinderException, EnricherException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        IntactPublication intactPublication = new IntactPublication("12347");
        intactPublication.setCurationDepth(CurationDepth.IMEx);
        intactPublication.setSource(new IntactSource("intact"));

        Experiment exp1 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp1);
        IntactInteractionEvidence ev1 = new IntactInteractionEvidence();
        ev1.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp1.addInteractionEvidence(ev1);

        Experiment exp2 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp2);
        IntactInteractionEvidence ev2 = new IntactInteractionEvidence();
        ev2.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp2.addInteractionEvidence(ev2);

        Experiment exp3 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp3);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp3.addInteractionEvidence(ev3);

        pubService.saveOrUpdate(intactPublication);

        imexManagerTest.assignImexAndUpdatePublication(intactPublication.getAc());

        IntactPublication intactPubReloaded = dao.getPublicationDao().getByAc(intactPublication.getAc());

        // no imex primary ref to publication
        Assert.assertEquals(0, intactPubReloaded.getXrefs().size());

        // no updated annotations publication
        Assert.assertEquals(0, intactPubReloaded.getAnnotations().size());

        // updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(0, exp.getXrefs().size());

            // updated interaction imex primary ref
            for (Interaction inter : exp.getInteractionEvidences()){
                Assert.assertEquals(0, inter.getXrefs().size());
            }
        }
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void assign_imex_update_publication_existingRecordNoExistingImex() throws PublicationImexUpdaterException, SynchronizerException,
            PersisterException, FinderException, EnricherException, BridgeFailedException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        IntactPublication intactPublication = new IntactPublication("12348");
        intactPublication.setCurationDepth(CurationDepth.IMEx);
        intactPublication.setSource(new IntactSource("intact"));

        Experiment exp1 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp1);
        IntactInteractionEvidence ev1 = new IntactInteractionEvidence();
        ev1.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp1.addInteractionEvidence(ev1);

        Experiment exp2 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp2);
        IntactInteractionEvidence ev2 = new IntactInteractionEvidence();
        ev2.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp2.addInteractionEvidence(ev2);

        Experiment exp3 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp3);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp3.addInteractionEvidence(ev3);

        pubService.saveOrUpdate(intactPublication);

        imexManagerTest.assignImexAndUpdatePublication(intactPublication.getAc());
        ImexPublication imexPublication = (ImexPublication)imexManagerTest.getImexCentralRegister().getExistingPublicationInImexCentral("12348", "pubmed");

        // did not create imex record because conflict with imex
        Assert.assertNull(imexPublication.getImexId());

        IntactPublication intactPubReloaded = dao.getPublicationDao().getByAc(intactPublication.getAc());

        // no imex primary ref to publication
        Assert.assertEquals(0, intactPubReloaded.getXrefs().size());

        // no updated annotations publication
        Assert.assertEquals(0, intactPubReloaded.getAnnotations().size());

        // updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(0, exp.getXrefs().size());

            // updated interaction imex primary ref
            for (Interaction inter : exp.getInteractionEvidences()){
                Assert.assertEquals(0, inter.getXrefs().size());
            }
        }
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void cannot_assign_imex_update_publication_existingRecordWithoutImexNoPubmed() throws PublicationImexUpdaterException,
            SynchronizerException, PersisterException, FinderException, EnricherException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        IntactPublication intactPublication = new IntactPublication("unassigned504");
        intactPublication.setCurationDepth(CurationDepth.IMEx);
        intactPublication.setSource(new IntactSource("intact"));

        Experiment exp1 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp1);
        IntactInteractionEvidence ev1 = new IntactInteractionEvidence();
        ev1.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp1.addInteractionEvidence(ev1);

        Experiment exp2 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp2);
        IntactInteractionEvidence ev2 = new IntactInteractionEvidence();
        ev2.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp2.addInteractionEvidence(ev2);

        Experiment exp3 = new IntactExperiment(intactPublication);
        intactPublication.addExperiment(exp3);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.getParticipants().add(new IntactParticipantEvidence(new IntactProtein("P12345")));
        exp3.addInteractionEvidence(ev3);

        pubService.saveOrUpdate(intactPublication);

        imexManagerTest.assignImexAndUpdatePublication(intactPublication.getAc());

        IntactPublication intactPubReloaded = dao.getPublicationDao().getByAc(intactPublication.getAc());

        // no imex primary ref to publication
        Assert.assertEquals(0, intactPubReloaded.getXrefs().size());

        // no updated annotations publication
        Assert.assertEquals(0, intactPubReloaded.getAnnotations().size());

        // updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded.getExperiments()){
            Assert.assertEquals(0, exp.getXrefs().size());

            // updated interaction imex primary ref
            for (Interaction inter : exp.getInteractionEvidences()){
                Assert.assertEquals(0, inter.getXrefs().size());
            }
        }
    }
}
