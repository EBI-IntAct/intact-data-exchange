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
import psidev.psi.mi.jami.bridges.imex.extension.ImexPublication;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.XrefUtils;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleStatus;
import uk.ac.ebi.intact.jami.model.user.User;
import uk.ac.ebi.intact.jami.service.PublicationService;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tester of GlobalImexPublicationUpdater
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>12/04/12</pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/META-INF/intact-jami-test.spring.xml",
        "classpath*:/META-INF/imex-test.spring.xml"})
public class GlobalImexPublicationUpdaterTest{

    @Autowired
    @Qualifier("globalImexPublicationUpdater")
    private GlobalImexPublicationUpdater globalImexUpdaterTest;

    @Before
    public void createImexRecords() throws BridgeFailedException {
        Publication pub = new edu.ucla.mbi.imex.central.ws.v20.Publication();
        Identifier pubmed = new Identifier();
        pubmed.setNs("pmid");
        pubmed.setAc("12345");
        pub.getIdentifier().add(pubmed);
        pub.setImexAccession("IM-3");
        globalImexUpdaterTest.getImexCentralManager().getImexCentralRegister().getImexCentralClient().createPublication(new ImexPublication(pub));

        Publication pub2 = new edu.ucla.mbi.imex.central.ws.v20.Publication();
        Identifier pubmed2 = new Identifier();
        pubmed2.setNs("pmid");
        pubmed2.setAc("12346");
        pub2.getIdentifier().add(pubmed2);
        pub2.setImexAccession("IM-4");
        globalImexUpdaterTest.getImexCentralManager().getImexCentralRegister().getImexCentralClient().createPublication(new ImexPublication(pub2));

        Publication pub3 = new edu.ucla.mbi.imex.central.ws.v20.Publication();
        Identifier pubmed3 = new Identifier();
        pubmed3.setNs("pmid");
        pubmed3.setAc("12347");
        pub3.getIdentifier().add(pubmed3);
        pub3.setImexAccession("IM-5");
        globalImexUpdaterTest.getImexCentralManager().getImexCentralRegister().getImexCentralClient().createPublication(new ImexPublication(pub3));

    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void update_Existing_Imex_Publications() throws SynchronizerException, PersisterException, FinderException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        // one publication with imex primary ref, 1 experiment, 2 interactions
        IntactPublication pubWithImex = new IntactPublication("12345");
        Experiment exp1 = new IntactExperiment(pubWithImex);
        pubWithImex.addExperiment(exp1);
        pubWithImex.assignImexId("IM-3");
        IntactInteractionEvidence ev1 = new IntactInteractionEvidence();
        ev1.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12345")));
        IntactInteractionEvidence ev2 = new IntactInteractionEvidence();
        ev2.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12346")));
        exp1.addInteractionEvidence(ev1);
        exp1.addInteractionEvidence(ev2);
        pubWithImex.setSource(new IntactSource("intact"));
        pubWithImex.setCurationDepth(CurationDepth.IMEx);
        pubService.saveOrUpdate(pubWithImex);

        // publication with imex id and no imex curation level -> not updated but reported
        IntactPublication pubWithoutCurationDepth = new IntactPublication("12346");
        Experiment exp2 = new IntactExperiment(pubWithoutCurationDepth);
        pubWithoutCurationDepth.addExperiment(exp2);
        pubWithImex.setSource(new IntactSource("intact"));
        pubWithImex.setCurationDepth(CurationDepth.MIMIx);
        IntactInteractionEvidence ev4 = new IntactInteractionEvidence();
        ev4.addParticipant(new IntactParticipantEvidence(new IntactProtein("test")));
        exp2.addInteractionEvidence(ev4);
        pubWithoutCurationDepth.assignImexId("IM-4");
        pubService.saveOrUpdate(pubWithoutCurationDepth);

        // publication with imex id and no PPI -> not updated but reported
        IntactPublication pubWithoutPPI = new IntactPublication("12347");
        Experiment exp3 = new IntactExperiment(pubWithoutPPI);
        pubWithoutPPI.addExperiment(exp3);
        pubWithImex.setSource(new IntactSource("intact"));
        pubWithImex.setCurationDepth(CurationDepth.IMEx);
        IntactInteractionEvidence ev5 = new IntactInteractionEvidence();
        ev5.addParticipant(new IntactParticipantEvidence(new IntactNucleicAcid("test")));
        exp3.addInteractionEvidence(ev5);
        pubWithoutPPI.assignImexId("IM-5");
        pubService.saveOrUpdate(pubWithoutPPI);

        // update existing publications
        globalImexUpdaterTest.updateExistingImexPublications();

        IntactPublication intactPubReloaded = dao.getPublicationDao().getByAc(pubWithImex.getAc());

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
            Assert.assertEquals("IM-3", ref.getId());
            Assert.assertEquals(Xref.IMEX_MI, ref.getDatabase().getMIIdentifier());
            Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref.getQualifier().getMIIdentifier());

            Set<String> imexIds  = new HashSet<String>(3);
            // updated interaction imex primary ref
            for (InteractionEvidence inter : exp.getInteractionEvidences()){
                Assert.assertEquals(1, inter.getXrefs().size());

                ref = inter.getXrefs().iterator().next();
                Assert.assertTrue(ref.getId().startsWith("IM-3-"));
                Assert.assertEquals(Xref.IMEX_MI, ref.getDatabase().getMIIdentifier());
                Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref.getQualifier().getMIIdentifier());

                imexIds.add(ref.getId());
            }

            Assert.assertEquals(2, imexIds.size());
        }

        // pub 2 is not updated because error
        IntactPublication intactPubReloaded2 = dao.getPublicationDao().getByAc(pubWithoutCurationDepth.getAc());

        Assert.assertEquals(0, intactPubReloaded2.getAnnotations().size());
        for (Experiment exp : intactPubReloaded2.getExperiments()){
            Assert.assertEquals(0, exp.getXrefs().size());

            // updated interaction imex primary ref
            for (Interaction inter : exp.getInteractionEvidences()){
                Assert.assertEquals(0, inter.getXrefs().size());
            }
        }

        // pub 3 is not updated because error
        IntactPublication intactPubReloaded3 = dao.getPublicationDao().getByAc(pubWithoutPPI.getAc());

        Assert.assertEquals(0, intactPubReloaded3.getAnnotations().size());
        for (Experiment exp : intactPubReloaded3.getExperiments()){
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
    public void assign_new_Imex_to_Publications() throws SynchronizerException, FinderException, PersisterException {

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        User reviewer = new User("reviewer", "reviewer", "reviewer", "reviewer@ebi.ac.uk");
        dao.getUserDao().persist(reviewer);

        // one publication without imex primary ref, 1 experiment, 2 interactions,journal cell 2006, accepted -> to assign IMEX
        IntactPublication pubWithImex = new IntactPublication("12348");
        Experiment exp1 = new IntactExperiment(pubWithImex);
        pubWithImex.addExperiment(exp1);
        IntactInteractionEvidence ev1 = new IntactInteractionEvidence();
        ev1.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12345")));
        IntactInteractionEvidence ev2 = new IntactInteractionEvidence();
        ev2.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12346")));
        exp1.addInteractionEvidence(ev1);
        exp1.addInteractionEvidence(ev2);
        pubWithImex.setSource(new IntactSource("intact"));
        pubWithImex.setCurationDepth(CurationDepth.IMEx);
        pubWithImex.setStatus(LifeCycleStatus.ACCEPTED);
        pubService.saveOrUpdate(pubWithImex);

        // publication not elligible imex (no journal, no datasets, no IMEx id) but imex curation level -> not updated but reported
        IntactPublication pub2 = new IntactPublication("12349");
        Experiment exp2 = new IntactExperiment(pub2);
        pubWithImex.addExperiment(exp2);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12345")));
        IntactInteractionEvidence ev4 = new IntactInteractionEvidence();
        ev4.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12346")));
        exp2.addInteractionEvidence(ev1);
        exp2.addInteractionEvidence(ev2);
        pubWithImex.setSource(new IntactSource("intact"));
        pubWithImex.setCurationDepth(CurationDepth.IMEx);
        pubWithImex.setStatus(LifeCycleStatus.ACCEPTED);
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(2005, 12, 31);
        pub2.setCreated(cal.getTime());
        pubService.saveOrUpdate(pubWithImex);

        // publication without imex id but interaction does have IMEx -> not updated but reported
        // one publication without imex-primary ref but interaction with imex primary ref
        IntactPublication pubWithoutImex = new IntactPublication("12350");
        Experiment exp3 = new IntactExperiment(pubWithoutImex);
        pubWithoutImex.addExperiment(exp3);
        IntactInteractionEvidence interWithImex = new IntactInteractionEvidence();
        interWithImex.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12345")));
        interWithImex.assignImexId("IM-4-1");
        exp3.addInteractionEvidence(interWithImex);
        pubWithoutImex.setSource(new IntactSource("intact"));
        pubWithImex.setStatus(LifeCycleStatus.ACCEPTED);
        pubService.saveOrUpdate(pubWithoutImex);

        // publication without imex id but experiment does have IMEx -> not updated but reported
        IntactPublication pubWithoutImex2 = new IntactPublication("12351");
        Experiment exp4 = new IntactExperiment(pubWithoutImex2);
        pubWithoutImex2.addExperiment(exp4);
        exp2.getXrefs().add(XrefUtils.createXrefWithQualifier(Xref.IMEX, Xref.IMEX_MI, "IM-4", Xref.IMEX_PRIMARY, Xref.IMEX_PRIMARY_MI));
        pubWithoutImex2.setSource(new IntactSource("intact"));
        pubWithoutImex2.setStatus(LifeCycleStatus.ACCEPTED);
        pubService.saveOrUpdate(pubWithoutImex2);

        // publication without imex id, elligible IMEx but experiment conflict -> experiment not updated but reported
        IntactPublication pubWithoutImex3 = new IntactPublication("12352");
        pubWithoutImex3.setCurationDepth(CurationDepth.IMEx);
        Experiment exp5 = new IntactExperiment(pubWithoutImex3);
        pubWithoutImex3.addExperiment(exp5);
        exp3.getXrefs().add(XrefUtils.createXrefWithQualifier(Xref.IMEX, Xref.IMEX_MI, "IM-5", Xref.IMEX_PRIMARY, Xref.IMEX_PRIMARY_MI));
        pubWithoutImex3.setSource(new IntactSource("intact"));
        pubWithoutImex3.setStatus(LifeCycleStatus.ACCEPTED);

        pubService.saveOrUpdate(pubWithoutImex2);

        // update existing publications
        globalImexUpdaterTest.assignNewImexIdsToPublications();

        IntactPublication intactPubReloaded = dao.getPublicationDao().getByAc(pubWithImex.getAc());

        // added imex primary ref to publication (pub registered in IMEx central)
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

            Set<String> imexIds  = new HashSet<String>(3);
            // updated interaction imex primary ref
            for (InteractionEvidence inter : exp.getInteractionEvidences()){
                Assert.assertEquals(1, inter.getXrefs().size());

                ref = inter.getXrefs().iterator().next();
                Assert.assertTrue(ref.getId().startsWith("IM-3-"));
                Assert.assertEquals(Xref.IMEX_MI, ref.getDatabase().getMIIdentifier());
                Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref.getQualifier().getMIIdentifier());

                imexIds.add(ref.getId());
            }

            Assert.assertEquals(2, imexIds.size());
        }

        // pub 2 is not updated because error
        IntactPublication intactPubReloaded2 = dao.getPublicationDao().getByAc(pub2.getAc());

        Assert.assertEquals(0, intactPubReloaded2.getXrefs().size());
        Assert.assertEquals(0, intactPubReloaded2.getAnnotations().size());
        for (Experiment exp : intactPubReloaded2.getExperiments()){
            Assert.assertEquals(0, exp.getXrefs().size());

            // updated interaction imex primary ref
            for (Interaction inter : exp.getInteractionEvidences()){
                Assert.assertEquals(0, inter.getXrefs().size());
            }
        }

        // pub 3 is not updated because error
        IntactPublication intactPubReloaded3 = dao.getPublicationDao().getByAc(pubWithImex.getAc());

        Assert.assertEquals(0, intactPubReloaded3.getXrefs().size());
        Assert.assertEquals(0, intactPubReloaded3.getAnnotations().size());
        for (Experiment exp : intactPubReloaded3.getExperiments()){
            Assert.assertEquals(0, exp.getXrefs().size());

            // interaction with one conflict ref not deleted
            for (Interaction inter : exp.getInteractionEvidences()){
                Assert.assertEquals(1, inter.getXrefs().size());
            }
        }

        // pub 4 is not updated because error
        IntactPublication intactPubReloaded4 = dao.getPublicationDao().getByAc(pubWithoutImex2.getAc());

        Assert.assertEquals(0, intactPubReloaded4.getXrefs().size());
        Assert.assertEquals(0, intactPubReloaded4.getAnnotations().size());
        for (Experiment exp : intactPubReloaded4.getExperiments()){

            // experiment having conflict, does not touch xref
            Assert.assertEquals(1, exp.getXrefs().size());

            // updated interaction imex primary ref
            for (Interaction inter : exp.getInteractionEvidences()){
                Assert.assertEquals(0, inter.getXrefs().size());
            }
        }

        // pub 5 is updated because publication is IMEx eligible, it will report the experiment as imex conflict and not do anything in experiment
        IntactPublication intactPubReloaded5 = dao.getPublicationDao().getByAc(pubWithoutImex3.getAc());

        // added imex primary ref to publication (pub not registered in IMEx central)
        Assert.assertEquals(1, intactPubReloaded5.getXrefs().size());
        Assert.assertNotNull(intactPubReloaded5.getImexId());
        // updated annotations publication
        Assert.assertEquals(2, intactPubReloaded5.getAnnotations().size());
        boolean hasFullCuration2 = false;
        boolean hasImexCuration2 = false;

        for (Annotation ann : intactPubReloaded5.getAnnotations()){
            if ("imex curation".equals(ann.getTopic().getShortName())){
                hasImexCuration2 = true;
            }
            else if ("full coverage".equals(ann.getTopic().getShortName())
                    && "Only protein-protein interactions".equalsIgnoreCase(ann.getValue())){
                hasFullCuration2 = true;
            }
        }

        Assert.assertTrue(hasFullCuration2);
        Assert.assertTrue(hasImexCuration2);

        // updated experiments imex primary ref
        for (Experiment exp : intactPubReloaded5.getExperiments()){
            // does not change experiment
            Assert.assertEquals(1, exp.getXrefs().size());

            Xref ref = exp.getXrefs().iterator().next();
            Assert.assertEquals("IM-3", ref.getId());
            Assert.assertEquals(Xref.IMEX_MI, ref.getDatabase().getMIIdentifier());
            Assert.assertEquals(Xref.IMEX_PRIMARY_MI, ref.getQualifier().getMIIdentifier());

            // updated interaction imex primary ref
            for (InteractionEvidence inter : exp.getInteractionEvidences()){
                Assert.assertEquals(1, inter.getXrefs().size());

                Assert.assertNotNull(inter.getImexId());
            }
        }
    }
}
