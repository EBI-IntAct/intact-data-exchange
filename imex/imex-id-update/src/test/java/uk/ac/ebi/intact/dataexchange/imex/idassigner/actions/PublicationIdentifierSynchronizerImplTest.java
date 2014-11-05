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
import psidev.psi.mi.jami.bridges.imex.extension.ImexPublication;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.imex.actions.PublicationIdentifierSynchronizer;
import psidev.psi.mi.jami.utils.XrefUtils;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;
import uk.ac.ebi.intact.jami.service.PublicationService;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;

/**
 * Unit tester of PublicationIdentifierSynchronizer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/04/12</pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/META-INF/intact-jami-test.spring.xml",
        "classpath*:/META-INF/imex-test.spring.xml"})
public class PublicationIdentifierSynchronizerImplTest {

    private PublicationIdentifierSynchronizer identifierSynchronizerTest;
    private ImexPublication intactPub12345;
    private ImexPublication intactDoi;
    private ImexPublication intactPub12347;
    private ImexPublication intactPubUnassigned;

    @Before
    public void createImexPublications() throws BridgeFailedException {

        this.identifierSynchronizerTest = ApplicationContextProvider.getBean("publicationIdentifierSynchronizer");

        Publication pub = new Publication();
        Identifier pubmed = new Identifier();
        pubmed.setNs("pmid");
        pubmed.setAc("12345");
        pub.getIdentifier().add(pubmed);
        pub.setImexAccession("IM-1");
        intactPub12345 = new ImexPublication(pub);
        identifierSynchronizerTest.getImexCentralClient().createPublication(intactPub12345);

        Publication pub2 = new Publication();
        Identifier pubmed2 = new Identifier();
        pubmed2.setNs("doi");
        pubmed2.setAc("123/1(a2)");
        pub2.getIdentifier().add(pubmed2);
        pub2.setImexAccession("IM-2");
        intactDoi = new ImexPublication(pub2);
        identifierSynchronizerTest.getImexCentralClient().createPublication(intactDoi);

        Publication pub3 = new Publication();
        Identifier pubmed3 = new Identifier();
        pubmed3.setNs("pmid");
        pubmed3.setAc("12347");
        pub3.getIdentifier().add(pubmed3);
        pub3.setImexAccession("IM-3");
        intactPub12347 = new ImexPublication(pub3);
        identifierSynchronizerTest.getImexCentralClient().createPublication(intactPub12347);

        Publication pub4 = new Publication();
        pub4.setImexAccession("IM-4");
        intactPubUnassigned = new ImexPublication(pub4);
        identifierSynchronizerTest.getImexCentralClient().createPublication(intactPubUnassigned);
    }

    @Test
    @DirtiesContext
    public void intact_publication_identifier_synchronized_with_imex_central() throws BridgeFailedException {

        Assert.assertTrue(identifierSynchronizerTest.isPublicationIdentifierInSyncWithImexCentral("12345", "pubmed", intactPub12345));
    }

    @Test
    @DirtiesContext
    public void intact_publication_unassigned_not_synchronized_with_imex_central() throws BridgeFailedException {

        Assert.assertFalse(identifierSynchronizerTest.isPublicationIdentifierInSyncWithImexCentral("unassigned604", "pubmed", intactPubUnassigned));
    }

    @Test
    @DirtiesContext
    public void intact_publication_identifier_no_identifier_imex_central() throws BridgeFailedException {

        Assert.assertFalse(identifierSynchronizerTest.isPublicationIdentifierInSyncWithImexCentral("12345", "pubmed", intactPubUnassigned));
    }

    @Test
    @DirtiesContext
    public void intact_publication_identifier_mismatch_imex_central() throws BridgeFailedException {

        Assert.assertFalse(identifierSynchronizerTest.isPublicationIdentifierInSyncWithImexCentral("12346", "pubmed", intactPub12345));
    }

    @Test
    @DirtiesContext
    public void synchronize_intact_publication_identifier_already_synchronized_with_imex_central() throws BridgeFailedException,
            PublicationImexUpdaterException, SynchronizerException, PersisterException, FinderException, EnricherException {

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");

        IntactPublication intactPublication = new IntactPublication("12345");
        pubService.saveOrUpdate(intactPublication);

        identifierSynchronizerTest.synchronizePublicationIdentifier(intactPublication, intactPub12345);
        
        Assert.assertEquals("12345", intactPub12345.getIdentifiers().iterator().next().getId());
    }

    @Test
    @DirtiesContext
    public void synchronize_intact_publication_identifier_no_identifier_in_imex_central() throws BridgeFailedException,
            PublicationImexUpdaterException, SynchronizerException, PersisterException, FinderException, EnricherException {
        intactPub12345.getIdentifiers().clear();

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");

        IntactPublication intactPublication = new IntactPublication("12345");
        pubService.saveOrUpdate(intactPublication);

        identifierSynchronizerTest.synchronizePublicationIdentifier(intactPublication, intactPub12345);

        Assert.assertEquals("12345", intactPub12345.getIdentifiers().iterator().next().getId());
    }

    @Test
    @DirtiesContext
    public void update_unassigned_identifier() throws BridgeFailedException, PublicationImexUpdaterException,
            SynchronizerException, PersisterException, FinderException, EnricherException {
        Assert.assertTrue(intactPubUnassigned.getIdentifiers().isEmpty());

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");

        IntactPublication intactPublication = new IntactPublication("unassigned604");
        pubService.saveOrUpdate(intactPublication);

        identifierSynchronizerTest.synchronizePublicationIdentifier(intactPublication, intactPubUnassigned);

        Assert.assertEquals("unassigned604", intactPubUnassigned.getIdentifiers().iterator().next().getId());
        intactPubUnassigned.getIdentifiers().clear();
    }

    @Test(expected = PublicationImexUpdaterException.class)
    @DirtiesContext
    public void synchronized_mismatch_pubmedId_aborted() throws BridgeFailedException, SynchronizerException, PersisterException, FinderException,
            EnricherException {

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");

        IntactPublication intactPublication = new IntactPublication("12346");
        pubService.saveOrUpdate(intactPublication);

        identifierSynchronizerTest.synchronizePublicationIdentifier(intactPublication, intactPub12345);
    }

    @Test(expected = PublicationImexUpdaterException.class)
    @DirtiesContext
    public void synchronized_mismatch_unassigned_intact_aborted() throws BridgeFailedException, SynchronizerException,
            PersisterException, FinderException, EnricherException {

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");

        IntactPublication intactPublication = new IntactPublication("unassigned604");
        pubService.saveOrUpdate(intactPublication);

        identifierSynchronizerTest.synchronizePublicationIdentifier(intactPublication, intactPub12345);
    }

    @Test(expected = PublicationImexUpdaterException.class)
    @DirtiesContext
    public void synchronized_mismatch_doi_aborted() throws BridgeFailedException, SynchronizerException, PersisterException, FinderException,
            EnricherException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");

        IntactPublication intactPublication = new IntactPublication();
        intactPublication.getIdentifiers().add(XrefUtils.createDoiIdentity("1234-5(7a)"));
        pubService.saveOrUpdate(intactPublication);

        identifierSynchronizerTest.synchronizePublicationIdentifier(intactPublication, intactDoi);
    }

    @Test(expected = EnricherException.class)
    @DirtiesContext
    public void synchronized_new_identifier_already_existing_aborted() throws PublicationImexUpdaterException, SynchronizerException,
            PersisterException, FinderException, BridgeFailedException, EnricherException {

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");

        IntactPublication intactPublication = new IntactPublication("12345");
        pubService.saveOrUpdate(intactPublication);
        identifierSynchronizerTest.synchronizePublicationIdentifier(intactPublication, intactPubUnassigned);

    }
}
