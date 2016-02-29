package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.model.impl.DefaultNucleicAcid;
import psidev.psi.mi.jami.model.impl.DefaultProtein;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import psidev.psi.mi.jami.utils.XrefUtils;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleStatus;
import uk.ac.ebi.intact.jami.service.PublicationService;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.*;

/**
 * Unit test class for IntactPublicationCollectorImpl
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/04/12</pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/META-INF/intact-jami-test.spring.xml",
        "classpath*:/META-INF/imex-test.spring.xml"})
public class IntactPublicationCollectorImplTest {

    @Resource(name = "intactPublicationCollector")
    private IntactPublicationCollector publicationCollectorTest;
    
    @Test
    @DirtiesContext
    public void get_publications_Without_Imex_But_With_Interaction_Imex() throws ParseException, SynchronizerException,
            PersisterException, FinderException {

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        // one publication with imex primary ref
        IntactPublication pubWithImex = new IntactPublication("12345");
        Experiment exp1 = new IntactExperiment(pubWithImex);
        pubWithImex.addExperiment(exp1);
        pubWithImex.assignImexId("IM-3");
        pubWithImex.setSource(new IntactSource("intact"));
        pubService.saveOrUpdate(pubWithImex);
        
        // one publication without imex-primary ref but interaction with imex primary ref
        IntactPublication pubWithoutImex = new IntactPublication("12346");
        Experiment exp2 = new IntactExperiment(pubWithoutImex);
        pubWithoutImex.addExperiment(exp2);
        IntactInteractionEvidence interWithImex = new IntactInteractionEvidence();
        interWithImex.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12345")));
        interWithImex.assignImexId("IM-4-1");
        exp2.addInteractionEvidence(interWithImex);
        pubWithoutImex.setSource(new IntactSource("intact"));
        pubService.saveOrUpdate(pubWithoutImex);

        // reset collections
        publicationCollectorTest.initialise();

        // collect publications without imex but interaction with imex
        Collection<String> pubAcs = publicationCollectorTest.getPublicationsWithoutImexButWithInteractionImex();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithoutImex.getAc(), pubAcs.iterator().next());
    }

    @Test
    @DirtiesContext
    public void get_publications_Without_Imex_But_With_Experiment_Imex() throws ParseException, SynchronizerException,
            PersisterException, FinderException {

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        // one publication with imex primary ref
        IntactPublication pubWithImex = new IntactPublication("12345");
        Experiment exp1 = new IntactExperiment(pubWithImex);
        pubWithImex.addExperiment(exp1);
        pubWithImex.assignImexId("IM-3");
        pubWithImex.setSource(new IntactSource("intact"));
        pubService.saveOrUpdate(pubWithImex);

        // one publication without imex-primary ref but experiment with imex primary ref
        IntactPublication pubWithoutImex = new IntactPublication("12346");
        Experiment exp2 = new IntactExperiment(pubWithoutImex);
        pubWithoutImex.addExperiment(exp2);
        exp2.getXrefs().add(XrefUtils.createXrefWithQualifier(Xref.IMEX, Xref.IMEX_MI, "IM-4", Xref.IMEX_PRIMARY, Xref.IMEX_PRIMARY_MI));
        pubWithoutImex.setSource(new IntactSource("intact"));
        pubService.saveOrUpdate(pubWithoutImex);

        // reset collection
        publicationCollectorTest.initialise();

        // collect publications without imex but experiment with imex
        Collection<String> pubAcs = publicationCollectorTest.getPublicationsWithoutImexButWithExperimentImex();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithoutImex.getAc(), pubAcs.iterator().next());
    }

    @Test
    @DirtiesContext
    public void get_publications_Having_Imex_Curation_Level_But_Are_Not_Eligible_Imex_because_too_old() throws ParseException, SynchronizerException,
            PersisterException, FinderException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        // one publication with imex primary ref and imex curation and 2 PPI (eligible imex)
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

        // one publication without imex-primary, not eligible IMEx too old
        IntactPublication pubWithoutImex = new IntactPublication("12346");
        pubWithoutImex.setCurationDepth(CurationDepth.IMEx);
        Experiment exp2 = new IntactExperiment(pubWithoutImex);
        pubWithoutImex.addExperiment(exp2);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12345")));
        IntactInteractionEvidence ev4 = new IntactInteractionEvidence();
        ev4.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12346")));
        exp2.addInteractionEvidence(ev3);
        exp2.addInteractionEvidence(ev4);
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(2005, 12, 31);
        pubWithoutImex.setCreated(cal.getTime());
        pubWithoutImex.setSource(new IntactSource("intact"));
        pubService.saveOrUpdate(pubWithoutImex);

        // reset collection
        publicationCollectorTest.initialise();

        Collection<String> pubAcs = publicationCollectorTest.getPublicationsHavingImexCurationLevelButAreNotEligibleImex();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithoutImex.getAc(), pubAcs.iterator().next());
    }

    @Test
    @DirtiesContext
    public void get_publications_Not_Eligible_Imex_because_no_imex_curation_depth() throws ParseException, SynchronizerException,
            PersisterException, FinderException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        // one publication with imex primary ref and imex curation and 2 PPI (eligible imex)
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
        // one publication without imex-primary, not eligible IMEx no curation depth
        IntactPublication pubWithoutImex = new IntactPublication("12346");
        pubWithoutImex.setCurationDepth(CurationDepth.MIMIx);
        Experiment exp2 = new IntactExperiment(pubWithoutImex);
        pubWithoutImex.addExperiment(exp2);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12345")));
        IntactInteractionEvidence ev4 = new IntactInteractionEvidence();
        ev4.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12346")));
        exp2.addInteractionEvidence(ev3);
        exp2.addInteractionEvidence(ev4);
        pubWithoutImex.setSource(new IntactSource("intact"));
        pubService.saveOrUpdate(pubWithoutImex);

        // reset collection
        publicationCollectorTest.initialise();

        Collection<String> pubAcs = publicationCollectorTest.getPublicationsNeedingAnImexId();
        Assert.assertEquals(0, pubAcs.size());
    }

    @Test
    @DirtiesContext
    public void get_publications_Having_Imex_Curation_Level_But_Are_Not_Eligible_Imex_other_institution() throws ParseException, SynchronizerException,
            PersisterException, FinderException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        // one publication with imex primary ref and imex curation and 2 PPI (eligible imex)
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

        // one publication without imex-primary, not eligible IMEx (eligible journal, no dataset, no imex, no PPI) but does have imex-curation level
        IntactPublication pubWithoutImex = new IntactPublication("12346");
        Experiment exp2 = new IntactExperiment(pubWithoutImex);
        pubWithoutImex.addExperiment(exp2);
        InteractionEvidence interaction = new IntactInteractionEvidence();
        interaction.setExperimentAndAddInteractionEvidence(exp2);
        interaction.getParticipants().add(new IntactParticipantEvidence(new DefaultNucleicAcid("test")));
        pubWithoutImex.setCurationDepth(CurationDepth.IMEx);
        pubWithoutImex.setSource(new IntactSource("intact"));
        pubService.saveOrUpdate(pubWithoutImex);

        // reset collection
        publicationCollectorTest.initialise();

        Collection<String> pubAcs = publicationCollectorTest.getPublicationsHavingImexCurationLevelButAreNotEligibleImex();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithoutImex.getAc(), pubAcs.iterator().next());
    }

    @Test
    @DirtiesContext
    public void get_publications_Having_Imex_Curation_Level_PPI_peptide() throws ParseException, SynchronizerException,
            PersisterException, FinderException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        // one publication with imex primary ref and imex curation and 2 PPI (eligible imex)
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

        // one publication eligible IMEx but does have protein-peptide interaction
        IntactPublication pubWithoutImex = new IntactPublication("12346");
        Experiment exp2 = new IntactExperiment(pubWithoutImex);
        pubWithoutImex.addExperiment(exp2);
        InteractionEvidence interaction = new IntactInteractionEvidence();
        interaction.setExperimentAndAddInteractionEvidence(exp2);
        interaction.getParticipants().add(new IntactParticipantEvidence(new DefaultProtein("test")));
        interaction.getParticipants().add(new IntactParticipantEvidence(new DefaultProtein("test2",
                IntactUtils.createMIInteractionType(Protein.PEPTIDE, Protein.PEPTIDE_MI))));
        pubWithoutImex.setCurationDepth(CurationDepth.IMEx);
        pubWithoutImex.setSource(new IntactSource("intact"));
        pubService.saveOrUpdate(pubWithoutImex);

        // reset collection
        publicationCollectorTest.initialise();

        Collection<String> pubAcs = publicationCollectorTest.getPublicationsHavingImexCurationLevelButAreNotEligibleImex();
        Assert.assertEquals(0, pubAcs.size());
    }

    @Test
    @DirtiesContext
    public void get_publications_Having_Imex_Curation_Level_But_Are_Not_Eligible_Imex_dataset_no_PPI() throws ParseException, SynchronizerException,
            PersisterException, FinderException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        // one publication with imex primary ref and imex curation and 2 PPI (eligible imex)
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

        // one publication without imex-primary, not eligible IMEx (eligible journal, no dataset, no imex, no PPI) but does have imex-curation level
        IntactPublication pubWithoutImex = new IntactPublication("12346");
        Experiment exp2 = new IntactExperiment(pubWithoutImex);
        pubWithoutImex.addExperiment(exp2);
        InteractionEvidence interaction = new IntactInteractionEvidence();
        interaction.setExperimentAndAddInteractionEvidence(exp2);
        interaction.getParticipants().add(new IntactParticipantEvidence(new DefaultNucleicAcid("test")));
        pubWithoutImex.getAnnotations().add(AnnotationUtils.createAnnotation("imex curation", null));
        pubWithoutImex.setSource(new IntactSource("intact"));
        pubWithoutImex.setCurationDepth(CurationDepth.IMEx);
        pubService.saveOrUpdate(pubWithoutImex);

        // reset collection
        publicationCollectorTest.initialise();

        Collection<String> pubAcs = publicationCollectorTest.getPublicationsHavingImexCurationLevelButAreNotEligibleImex();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithoutImex.getAc(), pubAcs.iterator().next());
    }

    @Test
    @DirtiesContext
    public void get_publications_having_IMEx_Id_And_Not_Imex_Curation_Level() throws ParseException, SynchronizerException,
            PersisterException, FinderException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        // one publication with imex primary ref and imex curation and 2 PPI (eligible imex)
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

        // one publication with imex-primary and no imex curation level
        IntactPublication pubWithoutImexCuration = new IntactPublication("12346");
        Experiment exp2 = new IntactExperiment(pubWithoutImexCuration);
        pubWithoutImexCuration.addExperiment(exp2);
        pubWithoutImexCuration.setSource(new IntactSource("intact"));
        pubWithoutImexCuration.assignImexId("IM-4");
        pubWithoutImexCuration.setCurationDepth(CurationDepth.MIMIx);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12345")));
        IntactInteractionEvidence ev4 = new IntactInteractionEvidence();
        ev4.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12346")));
        exp2.addInteractionEvidence(ev3);
        exp2.addInteractionEvidence(ev4);
        pubService.saveOrUpdate(pubWithoutImexCuration);

        // reset collection
        publicationCollectorTest.initialise();

        Collection<String> pubAcs = publicationCollectorTest.getPublicationsHavingIMExIdAndNotImexCurationLevel();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithoutImexCuration.getAc(), pubAcs.iterator().next());
    }
    @Test
    @DirtiesContext
    public void get_publications_Having_IMEx_Id_And_No_PPI_Interactions() throws ParseException, SynchronizerException,
            PersisterException, FinderException {
        // one publication with imex primary ref
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        // one publication with imex primary ref
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
        pubWithImex.assignImexId("Im-4");
        pubWithImex.setStatus(LifeCycleStatus.ACCEPTED);
        pubService.saveOrUpdate(pubWithImex);

        // one publication with imex-primary, no PPI
        IntactPublication pubWithoutPPI = new IntactPublication("12346");
        Experiment exp2 = new IntactExperiment(pubWithoutPPI);
        pubWithoutPPI.addExperiment(exp2);
        pubWithoutPPI.setSource(new IntactSource("intact"));
        pubWithoutPPI.setCurationDepth(CurationDepth.IMEx);
        pubWithoutPPI.assignImexId("IM-5");
        pubWithoutPPI.setStatus(LifeCycleStatus.ACCEPTED);
        pubService.saveOrUpdate(pubWithoutPPI);

        // reset collection
        publicationCollectorTest.initialise();

        Collection<String> pubAcs = publicationCollectorTest.getPublicationsHavingIMExIdAndNoPPI();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithoutPPI.getAc(), pubAcs.iterator().next());
    }

    @Test
    @DirtiesContext
    public void get_publications_Having_PPI_Interactions_one_with_NucleicAcid() throws ParseException, SynchronizerException,
            PersisterException, FinderException {
        // one publication with imex primary ref
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        // one publication with imex primary ref
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
        pubWithImex.assignImexId("Im-4");
        pubService.saveOrUpdate(pubWithImex);

        // one publication with imex-primary, one nucleic acid and the rest protein
        IntactPublication pubWithoutPPI = new IntactPublication("12346");
        Experiment exp2 = new IntactExperiment(pubWithoutPPI);
        pubWithoutPPI.addExperiment(exp2);
        pubWithoutPPI.setSource(new IntactSource("intact"));
        pubWithoutPPI.setCurationDepth(CurationDepth.MIMIx);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12345")));
        IntactInteractionEvidence ev4 = new IntactInteractionEvidence();
        ev4.addParticipant(new IntactParticipantEvidence(new IntactNucleicAcid("test")));
        exp2.addInteractionEvidence(ev3);
        exp2.addInteractionEvidence(ev4);
        pubService.saveOrUpdate(pubWithoutPPI);

        // reset collection
        publicationCollectorTest.initialise();

        Set<String> pubAcs = new HashSet<String> (publicationCollectorTest.getPublicationsHavingIMExIdAndNoPPI());
        Assert.assertEquals(0, pubAcs.size());
    }

    @Test
    @DirtiesContext
    public void get_publications_Having_PPI_Interactions2() throws ParseException, SynchronizerException,
            PersisterException, FinderException {
        // one publication with imex primary ref
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        // one publication with imex primary ref
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
        pubWithImex.assignImexId("Im-3");
        pubService.saveOrUpdate(pubWithImex);

        // one publication with imex-primary, one nucleic acid and the rest protein
        IntactPublication pubWithoutPPI = new IntactPublication("12346");
        Experiment exp2 = new IntactExperiment(pubWithoutPPI);
        pubWithoutPPI.addExperiment(exp2);
        pubWithoutPPI.setSource(new IntactSource("intact"));
        pubWithoutPPI.setCurationDepth(CurationDepth.IMEx);
        IntactInteractionEvidence ev3 = new IntactInteractionEvidence();
        ev3.addParticipant(new IntactParticipantEvidence(new IntactProtein("P12345")));
        IntactInteractionEvidence ev4 = new IntactInteractionEvidence();
        ev4.addParticipant(new IntactParticipantEvidence(new IntactNucleicAcid("test")));
        exp2.addInteractionEvidence(ev3);
        exp2.addInteractionEvidence(ev4);
        pubWithoutPPI.assignImexId("IM-4");
        pubService.saveOrUpdate(pubWithoutPPI);

        // reset collection
        publicationCollectorTest.initialise();

        Set<String> pubAcs = new HashSet<String> (publicationCollectorTest.getPublicationsHavingIMExIdAndNoPPI());
        Assert.assertEquals(0, pubAcs.size());
    }

    @Test
    @DirtiesContext
    public void get_publications_Having_IMEx_Id_ToUpdate() throws ParseException, SynchronizerException,
            PersisterException, FinderException {
        // one publication with imex primary ref
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        // one publication with imex primary ref
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
        pubWithImex.assignImexId("Im-3");
        pubService.saveOrUpdate(pubWithImex);

        // one publication with imex-primary, no PPI
        IntactPublication pubWithoutPPI = new IntactPublication("12346");
        Experiment exp2 = new IntactExperiment(pubWithoutPPI);
        pubWithoutPPI.addExperiment(exp2);
        pubWithoutPPI.setSource(new IntactSource("intact"));
        pubWithoutPPI.setCurationDepth(CurationDepth.IMEx);
        IntactInteractionEvidence ev4 = new IntactInteractionEvidence();
        ev4.addParticipant(new IntactParticipantEvidence(new IntactNucleicAcid("test")));
        exp2.addInteractionEvidence(ev4);
        pubWithoutPPI.assignImexId("IM-4");
        pubService.saveOrUpdate(pubWithoutPPI);

        // reset collection
        publicationCollectorTest.initialise();

        // collect publications having IMEx id to update
        Collection<String> pubAcs = publicationCollectorTest.getPublicationsHavingIMExIdToUpdate();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithImex.getAc(), pubAcs.iterator().next());
    }

    @Test
    @DirtiesContext
    public void get_publications_needing_An_Imex_Id() throws ParseException, SynchronizerException,
            PersisterException, FinderException {
        // one publication with imex primary ref
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");

        // one publication with imex primary ref
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
        pubWithImex.setStatus(LifeCycleStatus.RELEASED);
        pubService.saveOrUpdate(pubWithImex);


        // one publication without imex-primary, no PPI but with imex curation level and dataset
        IntactPublication pubWithoutPPI = new IntactPublication("12346");
        Experiment exp2 = new IntactExperiment(pubWithoutPPI);
        pubWithoutPPI.addExperiment(exp2);
        pubWithoutPPI.setSource(new IntactSource("intact"));
        pubWithoutPPI.setCurationDepth(CurationDepth.IMEx);
        IntactInteractionEvidence ev4 = new IntactInteractionEvidence();
        ev4.addParticipant(new IntactParticipantEvidence(new IntactNucleicAcid("test")));
        exp2.addInteractionEvidence(ev4);
        pubWithoutPPI.setStatus(LifeCycleStatus.RELEASED);
        pubService.saveOrUpdate(pubWithoutPPI);

        // reset collection
        publicationCollectorTest.initialise();

        // collect publications needing IMEx id
        Collection<String> pubAcs = publicationCollectorTest.getPublicationsNeedingAnImexId();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithImex.getAc(), pubAcs.iterator().next());
    }
}
