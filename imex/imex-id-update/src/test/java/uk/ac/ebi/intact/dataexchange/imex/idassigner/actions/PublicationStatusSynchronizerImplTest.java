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
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.imex.PublicationStatus;
import psidev.psi.mi.jami.bridges.imex.extension.ImexPublication;
import psidev.psi.mi.jami.imex.actions.PublicationStatusSynchronizer;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;
import uk.ac.ebi.intact.jami.model.extension.IntactSource;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleStatus;
import uk.ac.ebi.intact.jami.service.PublicationService;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;

/**
 * Unit tester of publicationStatus synchronizer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11/04/12</pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/META-INF/intact-jami-test.spring.xml",
        "classpath*:/META-INF/imex-test.spring.xml"})
public class PublicationStatusSynchronizerImplTest {

    private PublicationStatusSynchronizer imexStatusSynchronizerTest;
    private ImexPublication intactPub1;
    private ImexPublication intactPub2;

    @Before
    public void createImexPublications() throws BridgeFailedException {
        this.imexStatusSynchronizerTest = ApplicationContextProvider.getBean("intactImexStatusSynchronizer");

        Publication pub = new Publication();
        Identifier pubmed = new Identifier();
        pubmed.setNs("pmid");
        pubmed.setAc("12345");
        pub.getIdentifier().add(pubmed);
        pub.setStatus("NEW");
        intactPub1 = new ImexPublication(pub);
        imexStatusSynchronizerTest.getImexCentralClient().createPublication(intactPub1);

        Publication pub2 = new Publication();
        Identifier pubmed2 = new Identifier();
        pubmed2.setNs("pmid");
        pubmed2.setAc("12346");
        pub2.getIdentifier().add(pubmed2);
        pub2.setStatus("INPROGRESS");
        intactPub2 = new ImexPublication(pub2);
        imexStatusSynchronizerTest.getImexCentralClient().createPublication(intactPub2);

    }

    @Test
    @DirtiesContext
    public void synchronize_no_change_status_new() throws BridgeFailedException, SynchronizerException, PersisterException, FinderException {

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");

        IntactPublication intactPublication = new IntactPublication("12345");
        intactPublication.setSource(new IntactSource("intact"));
        pubService.saveOrUpdate(intactPublication);
        imexStatusSynchronizerTest.synchronizePublicationStatusWithImexCentral(intactPublication, intactPub1);
        Assert.assertEquals(PublicationStatus.NEW, intactPub1.getStatus());
    }

    @Test
    @DirtiesContext
    public void synchronize_release_status_update() throws BridgeFailedException, SynchronizerException, PersisterException, FinderException {

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");

        IntactPublication intactPublication = new IntactPublication("12345");
        intactPublication.setSource(new IntactSource("intact"));
        intactPublication.setStatus(LifeCycleStatus.RELEASED);
        pubService.saveOrUpdate(intactPublication);

        imexStatusSynchronizerTest.synchronizePublicationStatusWithImexCentral(intactPublication, intactPub2);

        Assert.assertEquals(PublicationStatus.RELEASED, intactPub2.getStatus());
    }
}
