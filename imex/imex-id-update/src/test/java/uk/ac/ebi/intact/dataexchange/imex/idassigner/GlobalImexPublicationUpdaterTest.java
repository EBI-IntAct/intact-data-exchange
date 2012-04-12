package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import edu.ucla.mbi.imex.central.ws.v20.*;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

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
                Assert.assertEquals("IM-3-"+index, ref2.getPrimaryId());
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

}
