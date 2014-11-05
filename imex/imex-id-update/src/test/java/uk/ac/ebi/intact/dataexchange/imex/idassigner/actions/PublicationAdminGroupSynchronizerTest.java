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
import psidev.psi.mi.jami.imex.actions.PublicationAdminGroupSynchronizer;
import psidev.psi.mi.jami.model.Source;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;
import uk.ac.ebi.intact.jami.model.extension.IntactSource;
import uk.ac.ebi.intact.jami.service.PublicationService;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;

import java.util.Iterator;

/**
 * Unit tester of PublicationAdminGroupSynchronizer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/04/12</pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/META-INF/intact-jami-test.spring.xml",
        "classpath*:/META-INF/imex-test.spring.xml"})
public class PublicationAdminGroupSynchronizerTest{

    private PublicationAdminGroupSynchronizer imexAdminGroupSynchronizerTest;
    private ImexPublication intactPub;
    private ImexPublication noIntactPub;
    private ImexPublication intactPub2;
    private ImexPublication noIntactPub2;

    @Before
    public void createImexPublications() throws BridgeFailedException {

        this.imexAdminGroupSynchronizerTest = ApplicationContextProvider.getBean("intactImexAdminGroupSynchronizer");

        Publication pub = new Publication();
        Identifier pubmed = new Identifier();
        pubmed.setNs("pmid");
        pubmed.setAc("12345");
        pub.getIdentifier().add(pubmed);
        pub.setAdminGroupList(new Publication.AdminGroupList());
        pub.getAdminGroupList().getGroup().add("INTACT");
        pub.getAdminGroupList().getGroup().add("INTACT Curators");
        intactPub = new ImexPublication(pub);
        imexAdminGroupSynchronizerTest.getImexCentralClient().createPublication(intactPub);

        Publication pub2 = new Publication();
        Identifier pubmed2 = new Identifier();
        pubmed2.setNs("pmid");
        pubmed2.setAc("12346");
        pub2.getIdentifier().add(pubmed2);
        noIntactPub = new ImexPublication(pub2);
        imexAdminGroupSynchronizerTest.getImexCentralClient().createPublication(noIntactPub);

        Publication pub3 = new Publication();
        Identifier pubmed3 = new Identifier();
        pubmed3.setNs("pmid");
        pubmed3.setAc("12347");
        pub3.getIdentifier().add(pubmed3);
        pub3.setAdminGroupList(new Publication.AdminGroupList());
        pub3.getAdminGroupList().getGroup().add("INTACT");
        intactPub2 = new ImexPublication(pub3);
        imexAdminGroupSynchronizerTest.getImexCentralClient().createPublication(intactPub2);

        Publication pub4 = new Publication();
        Identifier pubmed4 = new Identifier();
        pubmed4.setNs("pmid");
        pubmed4.setAc("12348");
        pub4.getIdentifier().add(pubmed4);
        noIntactPub2 = new ImexPublication(pub4);
        imexAdminGroupSynchronizerTest.getImexCentralClient().createPublication(noIntactPub2);

    }

    @Test
    @DirtiesContext
    public void synchronize_intact_admin() throws BridgeFailedException, SynchronizerException, PersisterException, FinderException {
        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");

        IntactPublication intactPublication = new IntactPublication("12346");
        intactPublication.setSource(new IntactSource("intact"));
        pubService.saveOrUpdate(intactPublication);
        
        imexAdminGroupSynchronizerTest.synchronizePublicationAdminGroup(intactPublication, noIntactPub);
        
        Assert.assertEquals(1, noIntactPub.getSources().size());
        Assert.assertEquals("INTACT", noIntactPub.getSources().iterator().next().getShortName().toUpperCase());
        noIntactPub.getSources().clear();
    }

    @Test
    @DirtiesContext
    public void synchronize_intact_admin_alreadyPresent() throws BridgeFailedException, SynchronizerException, PersisterException, FinderException {

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");

        IntactPublication intactPublication = new IntactPublication("12345");
        intactPublication.setSource(new IntactSource("intact"));
        pubService.saveOrUpdate(intactPublication);

        imexAdminGroupSynchronizerTest.synchronizePublicationAdminGroup(intactPublication, intactPub);

        Assert.assertEquals(2, intactPub.getSources().size());
        Assert.assertEquals("INTACT", intactPub.getSources().iterator().next().getShortName().toUpperCase());
    }

    @Test
    @DirtiesContext
    public void synchronize_matrixDb_admin_alreadyPresent() throws BridgeFailedException, SynchronizerException, PersisterException, FinderException {

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");

        IntactPublication intactPublication = new IntactPublication("12347");
        intactPublication.setSource(new IntactSource("matrixdb"));
        pubService.saveOrUpdate(intactPublication);

        imexAdminGroupSynchronizerTest.synchronizePublicationAdminGroup(intactPublication, intactPub2);

        Assert.assertEquals(2, intactPub2.getSources().size());
        Iterator<Source> group = intactPub2.getSources().iterator();
        
        Assert.assertEquals("INTACT", group.next().getShortName().toUpperCase());
        Assert.assertEquals("MATRIXDB", group.next().getShortName().toUpperCase());
        group.remove();
    }

    @DirtiesContext
    public void synchronize_unknown_admin_add_intact() throws BridgeFailedException, SynchronizerException, PersisterException, FinderException {

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");

        IntactPublication intactPublication = new IntactPublication("12348");
        intactPublication.setSource(new IntactSource("i2d"));
        pubService.saveOrUpdate(intactPublication);

        imexAdminGroupSynchronizerTest.synchronizePublicationAdminGroup(intactPublication, noIntactPub2);
        Assert.assertEquals(1, noIntactPub2.getSources().size());
        Iterator<Source> group = noIntactPub2.getSources().iterator();

        Assert.assertEquals("INTACT", group.next().getShortName().toUpperCase());
        noIntactPub2.getSources().clear();
    }
}
