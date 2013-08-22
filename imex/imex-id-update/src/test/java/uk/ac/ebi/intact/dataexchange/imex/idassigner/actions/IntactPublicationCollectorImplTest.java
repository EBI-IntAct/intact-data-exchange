package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import junit.framework.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.lifecycle.LifecycleManager;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.user.User;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import java.text.ParseException;
import java.util.*;

/**
 * Unit test class for IntactPublicationCollectorImpl
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/04/12</pre>
 */
@ContextConfiguration(locations = {"classpath*:/META-INF/intact.spring.xml",
        "classpath*:/META-INF/standalone/*-standalone.spring.xml",
        "classpath*:/META-INF/beansimex-test.spring.xml"})
public class IntactPublicationCollectorImplTest extends IntactBasicTestCase{
    
    @Autowired
    private IntactPublicationCollector publicationCollectorTest;
    
    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void get_publications_Without_Imex_But_With_Interaction_Imex() throws ParseException {

        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);
        
        // one publication with imex primary ref
        Publication pubWithImex = getMockBuilder().createPublicationRandom();
        Experiment exp1 = getMockBuilder().createExperimentRandom(2);
        exp1.setPublication(pubWithImex);
        pubWithImex.addExperiment(exp1);
        PublicationXref pubXref = new PublicationXref( pubWithImex.getOwner(), imex, "IM-3", imexPrimary );
        pubWithImex.addXref(pubXref);
        
        // one publication without imex-primary ref but interaction with imex primary ref
        Publication pubWithoutImex = getMockBuilder().createPublicationRandom();
        Experiment exp2 = getMockBuilder().createExperimentRandom(2);
        exp2.setPublication(pubWithoutImex);
        pubWithoutImex.addExperiment(exp2);
        Interaction interWithImex = exp2.getInteractions().iterator().next();
        interWithImex.getXrefs().clear();
        InteractorXref intXref = new InteractorXref( pubWithoutImex.getOwner(), imex, "IM-4-1", imexPrimary );
        interWithImex.addXref(intXref);
        
        getCorePersister().saveOrUpdate(pubWithImex);
        getCorePersister().saveOrUpdate(pubWithoutImex);
        
        getDataContext().commitTransaction(status);

        // reset collections
        publicationCollectorTest.initialise();

        // collect publications without imex but interaction with imex
        Collection<String> pubAcs = publicationCollectorTest.getPublicationsWithoutImexButWithInteractionImex();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithoutImex.getAc(), pubAcs.iterator().next());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void get_publications_Without_Imex_But_With_Experiment_Imex() throws ParseException {

        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        // one publication with imex primary ref
        Publication pubWithImex = getMockBuilder().createPublicationRandom();
        Experiment exp1 = getMockBuilder().createExperimentRandom(2);
        exp1.setPublication(pubWithImex);
        pubWithImex.addExperiment(exp1);
        PublicationXref pubXref = new PublicationXref( pubWithImex.getOwner(), imex, "IM-3", imexPrimary );
        pubWithImex.addXref(pubXref);

        // one publication without imex-primary ref but interaction with imex primary ref
        Publication pubWithoutImex = getMockBuilder().createPublicationRandom();
        Experiment exp2 = getMockBuilder().createExperimentRandom(2);
        exp2.setPublication(pubWithoutImex);
        pubWithoutImex.addExperiment(exp2);
        ExperimentXref intXref = new ExperimentXref( pubWithoutImex.getOwner(), imex, "IM-4", imexPrimary );
        exp2.addXref(intXref);

        getCorePersister().saveOrUpdate(pubWithImex);
        getCorePersister().saveOrUpdate(pubWithoutImex);

        getDataContext().commitTransaction(status);

        // reset collection
        publicationCollectorTest.initialise();

        // collect publications without imex but experiment with imex
        Collection<String> pubAcs = publicationCollectorTest.getPublicationsWithoutImexButWithExperimentImex();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithoutImex.getAc(), pubAcs.iterator().next());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void get_publications_Having_Imex_Curation_Level_But_Are_Not_Eligible_Imex_because_too_old() throws ParseException {
        IntactMockBuilder builder = new IntactMockBuilder();
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        CvTopic imexCuration = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, "MI:0955", "curation depth");
        getCorePersister().saveOrUpdate(imexCuration);

        // one publication with imex primary ref and imex curation and 2 PPI (eligible imex)
        Publication pubWithImex = builder.createPublicationRandom();
        Experiment exp1 = getMockBuilder().createExperimentRandom(2);
        exp1.setPublication(pubWithImex);
        pubWithImex.addExperiment(exp1);
        PublicationXref pubXref = new PublicationXref( pubWithImex.getOwner(), imex, "IM-3", imexPrimary );
        pubWithImex.addXref(pubXref);
        Annotation imexCurationAnn1 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        pubWithImex.addAnnotation(imexCurationAnn1);

        // one publication without imex-primary, not eligible IMEx too old
        Publication pubWithoutImex = builder.createPublicationRandom();
        Experiment exp2 = getMockBuilder().createExperimentRandom(2);
        exp2.setPublication(pubWithoutImex);
        pubWithoutImex.addExperiment(exp2);
        Annotation imexCurationAnn2 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        pubWithoutImex.addAnnotation(imexCurationAnn2);
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(2005, 12, 31);
        pubWithoutImex.setCreated(cal.getTime());

        getCorePersister().saveOrUpdate(pubWithImex);
        getCorePersister().saveOrUpdate(pubWithoutImex);

        getDataContext().commitTransaction(status);

        // reset collection
        publicationCollectorTest.initialise();

        Collection<String> pubAcs = publicationCollectorTest.getPublicationsHavingImexCurationLevelButAreNotEligibleImex();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithoutImex.getAc(), pubAcs.iterator().next());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void get_publications_Not_Eligible_Imex_because_no_imex_curation_depth() throws ParseException {
        IntactMockBuilder builder = new IntactMockBuilder();
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        CvTopic imexCuration = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, "MI:0955", "curation depth");
        getCorePersister().saveOrUpdate(imexCuration);

        // one publication with imex primary ref and imex curation and 2 PPI (eligible imex)
        Publication pubWithImex = builder.createPublicationRandom();
        Experiment exp1 = getMockBuilder().createExperimentRandom(2);
        exp1.setPublication(pubWithImex);
        pubWithImex.addExperiment(exp1);
        PublicationXref pubXref = new PublicationXref( pubWithImex.getOwner(), imex, "IM-3", imexPrimary );
        pubWithImex.addXref(pubXref);
        Annotation imexCurationAnn1 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        pubWithImex.addAnnotation(imexCurationAnn1);

        // one publication without imex-primary, not eligible IMEx no curation depth
        Publication pubWithoutImex = builder.createPublicationRandom();
        Experiment exp2 = getMockBuilder().createExperimentRandom(2);
        exp2.setPublication(pubWithoutImex);
        pubWithoutImex.addExperiment(exp2);

        getCorePersister().saveOrUpdate(pubWithImex);
        getCorePersister().saveOrUpdate(pubWithoutImex);

        getDataContext().commitTransaction(status);

        // reset collection
        publicationCollectorTest.initialise();

        Collection<String> pubAcs = publicationCollectorTest.getPublicationsNeedingAnImexId();
        Assert.assertEquals(0, pubAcs.size());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void get_publications_Having_Imex_Curation_Level_But_Are_Not_Eligible_Imex_other_institution() throws ParseException {
        IntactMockBuilder builder = new IntactMockBuilder();

        TransactionStatus status = getDataContext().beginTransaction();

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


        // one publication eligible IMEx and imex curation and 2 PPI (eligible imex)
        Publication pubWithImex = builder.createPublicationRandom();
        Experiment exp1 = getMockBuilder().createExperimentRandom(2);
        exp1.setPublication(pubWithImex);
        pubWithImex.addExperiment(exp1);
        Annotation imexCurationAnn1 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        pubWithImex.addAnnotation(imexCurationAnn1);
        Annotation journalAnn = getMockBuilder().createAnnotation("Cell (0092-8674)", journal);
        pubWithImex.addAnnotation(journalAnn);
        Annotation dateAnn = getMockBuilder().createAnnotation("2008", date);
        pubWithImex.addAnnotation(dateAnn);

        // one publication without imex-primary, not eligible IMEx (eligible journal, no dataset, no imex, no PPI) but does have imex-curation level
        Publication pubWithoutImex = getMockBuilder().createPublicationRandom();
        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(pubWithoutImex);
        pubWithoutImex.addExperiment(exp2);
        Interaction interaction = exp2.getInteractions().iterator().next();
        for (Component comp : interaction.getComponents()){
            comp.setInteractor(getMockBuilder().createNucleicAcidRandom());
        }
        Annotation imexCurationAnn2 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        pubWithoutImex.addAnnotation(imexCurationAnn2);
        Annotation journalAnn2 = getMockBuilder().createAnnotation("Cell (0092-8674)", journal);
        pubWithoutImex.addAnnotation(journalAnn2);
        Annotation dateAnn2 = getMockBuilder().createAnnotation("2008", date);
        pubWithoutImex.addAnnotation(dateAnn2);

        getCorePersister().saveOrUpdate(pubWithImex);
        getCorePersister().saveOrUpdate(pubWithoutImex);

        getDataContext().commitTransaction(status);

        // reset collection
        publicationCollectorTest.initialise();

        Collection<String> pubAcs = publicationCollectorTest.getPublicationsHavingImexCurationLevelButAreNotEligibleImex();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithoutImex.getAc(), pubAcs.iterator().next());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void get_publications_Having_Imex_Curation_Level_PPI_peptide() throws ParseException {
        IntactMockBuilder builder = new IntactMockBuilder();
        TransactionStatus status = getDataContext().beginTransaction();

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


        // one publication eligible IMEx and imex curation and 2 PPI (eligible imex)
        Publication pubWithImex = builder.createPublicationRandom();
        Experiment exp1 = getMockBuilder().createExperimentRandom(2);
        exp1.setPublication(pubWithImex);
        pubWithImex.addExperiment(exp1);
        Annotation imexCurationAnn1 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        pubWithImex.addAnnotation(imexCurationAnn1);
        Annotation journalAnn = getMockBuilder().createAnnotation("Cell (0092-8674)", journal);
        pubWithImex.addAnnotation(journalAnn);
        Annotation dateAnn = getMockBuilder().createAnnotation("2008", date);
        pubWithImex.addAnnotation(dateAnn);

        // one publication eligible IMEx but does have protein-peptide interaction
        Publication pubWithoutImex = builder.createPublicationRandom();
        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(pubWithoutImex);
        pubWithoutImex.addExperiment(exp2);
        Interaction interaction = exp2.getInteractions().iterator().next();
        for (Component comp : interaction.getComponents()){
            comp.setInteractor(getMockBuilder().createPeptideRandom());
        }
        Annotation imexCurationAnn2 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        pubWithoutImex.addAnnotation(imexCurationAnn2);
        Annotation journalAnn2 = getMockBuilder().createAnnotation("Cell (0092-8674)", journal);
        pubWithoutImex.addAnnotation(journalAnn2);
        Annotation dateAnn2 = getMockBuilder().createAnnotation("2008", date);
        pubWithoutImex.addAnnotation(dateAnn2);

        getCorePersister().saveOrUpdate(pubWithImex);
        getCorePersister().saveOrUpdate(pubWithoutImex);

        getDataContext().commitTransaction(status);

        // reset collection
        publicationCollectorTest.initialise();

        Collection<String> pubAcs = publicationCollectorTest.getPublicationsHavingImexCurationLevelButAreNotEligibleImex();
        Assert.assertEquals(0, pubAcs.size());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void get_publications_Having_Imex_Curation_Level_But_Are_Not_Eligible_Imex_dataset_no_PPI() throws ParseException {
        IntactMockBuilder builder = new IntactMockBuilder();
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        CvTopic imexCuration = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, "MI:0955", "curation depth");
        getCorePersister().saveOrUpdate(imexCuration);

        CvTopic dataset = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, CvTopic.DATASET_MI_REF, CvTopic.DATASET);
        getCorePersister().saveOrUpdate(dataset);

        // one publication eligible imex and imex curation and 2 PPI (eligible imex)
        Publication pubWithImex = builder.createPublicationRandom();
        Experiment exp1 = getMockBuilder().createExperimentRandom(2);
        exp1.setPublication(pubWithImex);
        pubWithImex.addExperiment(exp1);
        Annotation imexCurationAnn1 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        pubWithImex.addAnnotation(imexCurationAnn1);
        Annotation datasetAnn = getMockBuilder().createAnnotation("BioCreative - Critical Assessment of Information Extraction systems in Biology", dataset);
        pubWithImex.addAnnotation(datasetAnn);

        // one publication without imex-primary, not eligible IMEx (eligible journal, no dataset, no imex, no PPI) but does have imex-curation level
        Publication pubWithoutImex = builder.createPublicationRandom();
        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(pubWithoutImex);
        pubWithoutImex.addExperiment(exp2);
        Interaction interaction = exp2.getInteractions().iterator().next();
        for (Component comp : interaction.getComponents()){
            comp.setInteractor(getMockBuilder().createNucleicAcidRandom());
        }
        Annotation imexCurationAnn2 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        pubWithoutImex.addAnnotation(imexCurationAnn2);
        Annotation datasetAnn2 = getMockBuilder().createAnnotation("BioCreative - Critical Assessment of Information Extraction systems in Biology", dataset);
        pubWithoutImex.addAnnotation(datasetAnn2);

        getCorePersister().saveOrUpdate(pubWithImex);
        getCorePersister().saveOrUpdate(pubWithoutImex);

        getDataContext().commitTransaction(status);

        // reset collection
        publicationCollectorTest.initialise();

        Collection<String> pubAcs = publicationCollectorTest.getPublicationsHavingImexCurationLevelButAreNotEligibleImex();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithoutImex.getAc(), pubAcs.iterator().next());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void get_publications_having_IMEx_Id_And_Not_Imex_Curation_Level() throws ParseException {
        IntactMockBuilder builder = new IntactMockBuilder();
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        CvTopic imexCuration = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, "MI:0955", "curation depth");
        getCorePersister().saveOrUpdate(imexCuration);

        // one publication with imex primary ref and imex curation and 2 PPI (eligible imex)
        Publication pubWithImex = builder.createPublicationRandom();
        Experiment exp1 = getMockBuilder().createExperimentRandom(2);
        exp1.setPublication(pubWithImex);
        pubWithImex.addExperiment(exp1);
        PublicationXref pubXref = new PublicationXref( pubWithImex.getOwner(), imex, "IM-3", imexPrimary );
        pubWithImex.addXref(pubXref);
        Annotation imexCurationAnn1 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        pubWithImex.addAnnotation(imexCurationAnn1);

        // one publication with imex-primary and no imex curation level
        Publication pubWithoutImexCuration = builder.createPublicationRandom();
        Experiment exp2 = getMockBuilder().createExperimentRandom(2);
        exp2.setPublication(pubWithoutImexCuration);
        pubWithoutImexCuration.addExperiment(exp2);
        PublicationXref pubXref2 = new PublicationXref( pubWithImex.getOwner(), imex, "IM-4", imexPrimary );
        pubWithoutImexCuration.addXref(pubXref2);

        getCorePersister().saveOrUpdate(pubWithImex);
        getCorePersister().saveOrUpdate(pubWithoutImexCuration);

        getDataContext().commitTransaction(status);

        // reset collection
        publicationCollectorTest.initialise();

        Collection<String> pubAcs = publicationCollectorTest.getPublicationsHavingIMExIdAndNotImexCurationLevel();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithoutImexCuration.getAc(), pubAcs.iterator().next());
    }
    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void get_publications_Having_IMEx_Id_And_No_PPI_Interactions() throws ParseException {
        IntactMockBuilder builder = new IntactMockBuilder();
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        // one publication with imex primary ref
        Publication pubWithImex = builder.createPublicationRandom();
        Experiment exp1 = getMockBuilder().createExperimentRandom(2);
        exp1.setPublication(pubWithImex);
        pubWithImex.addExperiment(exp1);
        PublicationXref pubXref = new PublicationXref( pubWithImex.getOwner(), imex, "IM-3", imexPrimary );
        pubWithImex.addXref(pubXref);

        // one publication with imex-primary, no PPI
        Publication pubWithoutPPI = builder.createPublicationRandom();
        pubWithoutPPI.setStatus(getDaoFactory().getCvObjectDao(CvPublicationStatus.class).getByShortLabel("released"));
        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(pubWithoutPPI);
        pubWithoutPPI.addExperiment(exp2);
        Interaction interaction = exp2.getInteractions().iterator().next();
        for (Component comp : interaction.getComponents()){
            comp.setInteractor(getMockBuilder().createNucleicAcidRandom());
        }
        Component protein = getMockBuilder().createComponentPrey(getMockBuilder().createProteinRandom());
        interaction.addComponent(protein);

        PublicationXref pubXref2 = new PublicationXref( pubWithImex.getOwner(), imex, "IM-4", imexPrimary );
        pubWithoutPPI.addXref(pubXref2);

        getCorePersister().saveOrUpdate(pubWithImex);
        getCorePersister().saveOrUpdate(pubWithoutPPI);

        getDataContext().commitTransaction(status);

        // reset collection
        publicationCollectorTest.initialise();

        Collection<String> pubAcs = publicationCollectorTest.getPublicationsHavingIMExIdAndNoPPI();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithoutPPI.getAc(), pubAcs.iterator().next());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void get_publications_Having_PPI_Interactions_one_with_NucleicAcid() throws ParseException {
        IntactMockBuilder builder = new IntactMockBuilder();
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        // one publication with imex primary ref
        Publication pubWithImex = builder.createPublicationRandom();
        Experiment exp1 = getMockBuilder().createExperimentRandom(2);
        exp1.setPublication(pubWithImex);
        pubWithImex.addExperiment(exp1);
        PublicationXref pubXref = new PublicationXref( pubWithImex.getOwner(), imex, "IM-3", imexPrimary );
        pubWithImex.addXref(pubXref);

        // one publication with imex-primary, one nucleic acid and the rest protein
        Publication pubWithoutPPI = builder.createPublicationRandom();
        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(pubWithoutPPI);
        pubWithoutPPI.addExperiment(exp2);
        Interaction interaction = exp2.getInteractions().iterator().next();
        Component nucleicacid = getMockBuilder().createComponentPrey(getMockBuilder().createNucleicAcidRandom());
        interaction.addComponent(nucleicacid);
        PublicationXref pubXref2 = new PublicationXref( pubWithImex.getOwner(), imex, "IM-4", imexPrimary );
        pubWithoutPPI.addXref(pubXref2);

        getCorePersister().saveOrUpdate(pubWithImex);
        getCorePersister().saveOrUpdate(pubWithoutPPI);

        getDataContext().commitTransaction(status);

        // reset collection
        publicationCollectorTest.initialise();

        Set<String> pubAcs = new HashSet<String> (publicationCollectorTest.getPublicationsHavingIMExIdAndNoPPI());
        Assert.assertEquals(0, pubAcs.size());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void get_publications_Having_PPI_Interactions2() throws ParseException {
        IntactMockBuilder builder = new IntactMockBuilder();
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        // one publication with imex primary ref
        Publication pubWithImex = builder.createPublicationRandom();
        Experiment exp1 = getMockBuilder().createExperimentRandom(2);
        exp1.setPublication(pubWithImex);
        pubWithImex.addExperiment(exp1);
        PublicationXref pubXref = new PublicationXref( pubWithImex.getOwner(), imex, "IM-3", imexPrimary );
        pubWithImex.addXref(pubXref);

        // one publication with imex-primary, one nucleic acid and the rest protein
        Publication pubWithoutPPI = builder.createPublicationRandom();
        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(pubWithoutPPI);
        pubWithoutPPI.addExperiment(exp2);
        Interaction interaction = exp2.getInteractions().iterator().next();
        Iterator<Component> componentIterator = interaction.getComponents().iterator();
        componentIterator.next().setInteractor(getMockBuilder().createPeptideRandom());
        componentIterator.next().setInteractor(getMockBuilder().createProteinRandom());
        PublicationXref pubXref2 = new PublicationXref( pubWithImex.getOwner(), imex, "IM-4", imexPrimary );
        pubWithoutPPI.addXref(pubXref2);

        getCorePersister().saveOrUpdate(pubWithImex);
        getCorePersister().saveOrUpdate(pubWithoutPPI);

        getDataContext().commitTransaction(status);

        // reset collection
        publicationCollectorTest.initialise();

        Set<String> pubAcs = new HashSet<String> (publicationCollectorTest.getPublicationsHavingIMExIdAndNoPPI());
        Assert.assertEquals(0, pubAcs.size());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void get_publications_Having_IMEx_Id_ToUpdate() throws ParseException {
        IntactMockBuilder builder = new IntactMockBuilder();
        TransactionStatus status = getDataContext().beginTransaction();

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        CvTopic imexCuration = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, "MI:0955", "curation depth");
        getCorePersister().saveOrUpdate(imexCuration);

        // one publication with imex primary ref and imex curation and 2 PPI (eligible imex)
        Publication pubWithImex = builder.createPublicationRandom();
        Experiment exp1 = getMockBuilder().createExperimentRandom(2);
        exp1.setPublication(pubWithImex);
        pubWithImex.addExperiment(exp1);
        PublicationXref pubXref = new PublicationXref( pubWithImex.getOwner(), imex, "IM-3", imexPrimary );
        pubWithImex.addXref(pubXref);
        Annotation imexCurationAnn1 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        pubWithImex.addAnnotation(imexCurationAnn1);

        // one publication with imex-primary, no PPI
        Publication pubWithoutPPI = builder.createPublicationRandom();
        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(pubWithoutPPI);
        pubWithoutPPI.addExperiment(exp2);
        Interaction interaction = exp2.getInteractions().iterator().next();
        for (Component comp : interaction.getComponents()){
            comp.setInteractor(getMockBuilder().createNucleicAcidRandom());
        }
        PublicationXref pubXref2 = new PublicationXref( pubWithImex.getOwner(), imex, "IM-4", imexPrimary );
        pubWithoutPPI.addXref(pubXref2);

        getCorePersister().saveOrUpdate(pubWithImex);
        getCorePersister().saveOrUpdate(pubWithoutPPI);

        getDataContext().commitTransaction(status);

        // reset collection
        publicationCollectorTest.initialise();

        // collect publications having IMEx id to update
        Collection<String> pubAcs = publicationCollectorTest.getPublicationsHavingIMExIdToUpdate();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithImex.getAc(), pubAcs.iterator().next());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void get_publications_needing_An_Imex_Id() throws ParseException {
        IntactMockBuilder builder = new IntactMockBuilder();
        TransactionStatus status = getDataContext().beginTransaction();

        User reviewer = getMockBuilder().createReviewer("reviewer", "reviewer", "reviewer", "reviewer@ebi.ac.uk");
        getCorePersister().saveOrUpdate(reviewer);

        CvDatabase imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        getCorePersister().saveOrUpdate(imex);

        CvXrefQualifier imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        getCorePersister().saveOrUpdate(imexPrimary);

        CvTopic imexCuration = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, "MI:0955", "curation depth");
        getCorePersister().saveOrUpdate(imexCuration);
        CvTopic dataset = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, CvTopic.DATASET_MI_REF, CvTopic.DATASET);
        getCorePersister().saveOrUpdate(dataset);

        // one publication without imex primary ref but imex curation and 2 PPI (eligible imex with dataset)
        Publication pubWithImex = builder.createPublicationRandom();
        Experiment exp1 = getMockBuilder().createExperimentRandom(2);
        exp1.setPublication(pubWithImex);
        pubWithImex.addExperiment(exp1);
        Annotation imexCurationAnn1 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        pubWithImex.addAnnotation(imexCurationAnn1);
        Annotation datasetAnn = getMockBuilder().createAnnotation("BioCreative - Critical Assessment of Information Extraction systems in Biology", dataset);
        pubWithImex.addAnnotation(datasetAnn);

        LifecycleManager lifecycleManager = IntactContext.getCurrentInstance().getLifecycleManager();
        lifecycleManager.getNewStatus().claimOwnership(pubWithImex);
        lifecycleManager.getAssignedStatus().startCuration(pubWithImex);
        lifecycleManager.getCurationInProgressStatus().readyForChecking(pubWithImex, "test", true);
        lifecycleManager.getReadyForCheckingStatus().accept(pubWithImex, "accepted");

        // one publication without imex-primary, no PPI but with imex curation level and dataset
        Publication pubWithoutPPI = builder.createPublicationRandom();
        Experiment exp2 = getMockBuilder().createExperimentRandom(1);
        exp2.setPublication(pubWithoutPPI);
        pubWithoutPPI.addExperiment(exp2);
        Interaction interaction = exp2.getInteractions().iterator().next();
        for (Component comp : interaction.getComponents()){
            comp.setInteractor(getMockBuilder().createNucleicAcidRandom());
        }
        Annotation imexCurationAnn2 = getMockBuilder().createAnnotation("imex curation", imexCuration);
        pubWithoutPPI.addAnnotation(imexCurationAnn2);
        Annotation datasetAnn2 = getMockBuilder().createAnnotation("BioCreative - Critical Assessment of Information Extraction systems in Biology", dataset);
        pubWithoutPPI.addAnnotation(datasetAnn2);

        getCorePersister().saveOrUpdate(pubWithImex);
        getCorePersister().saveOrUpdate(pubWithoutPPI);

        lifecycleManager.getNewStatus().claimOwnership(pubWithoutPPI);
        lifecycleManager.getAssignedStatus().startCuration(pubWithoutPPI);
        lifecycleManager.getCurationInProgressStatus().readyForChecking(pubWithoutPPI, "test", true);
        lifecycleManager.getReadyForCheckingStatus().accept(pubWithoutPPI, "accepted");

        getDataContext().commitTransaction(status);

        // reset collection
        publicationCollectorTest.initialise();

        // collect publications needing IMEx id
        Collection<String> pubAcs = publicationCollectorTest.getPublicationsNeedingAnImexId();
        Assert.assertEquals(1, pubAcs.size());
        Assert.assertEquals(pubWithImex.getAc(), pubAcs.iterator().next());
    }
}
