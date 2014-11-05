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
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;
import uk.ac.ebi.intact.jami.model.extension.IntactSource;
import uk.ac.ebi.intact.jami.model.user.User;
import uk.ac.ebi.intact.jami.service.PublicationService;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;

import java.util.Iterator;

/**
 * Unit tester of PublicationAdminUserSynchronizer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/04/12</pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/META-INF/intact-jami-test.spring.xml",
        "classpath*:/META-INF/imex-test.spring.xml"})
public class PublicationAdminUserSynchronizerTest {

    private PublicationAdminUserSynchronizer imexAdminUserSynchronizerTest;
    private ImexPublication intactPub;
    private ImexPublication noIntactPub;
    private ImexPublication intactPub2;

    @Before
    public void createImexPublications() throws BridgeFailedException {

        this.imexAdminUserSynchronizerTest = ApplicationContextProvider.getBean("intactImexAdminUserSynchronizer");

        Publication pub = new Publication();
        Identifier pubmed = new Identifier();
        pubmed.setNs("pmid");
        pubmed.setAc("12345");
        pub.getIdentifier().add(pubmed);
        pub.setAdminUserList(new Publication.AdminUserList());
        pub.getAdminUserList().getUser().add("intact");
        intactPub = new ImexPublication(pub);
        imexAdminUserSynchronizerTest.getImexCentralClient().createPublication(intactPub);

        Publication pub2 = new Publication();
        Identifier pubmed2 = new Identifier();
        pubmed2.setNs("pmid");
        pubmed2.setAc("12346");
        pub2.getIdentifier().add(pubmed2);
        noIntactPub = new ImexPublication(pub2);
        imexAdminUserSynchronizerTest.getImexCentralClient().createPublication(noIntactPub);

        Publication pub3 = new Publication();
        Identifier pubmed3 = new Identifier();
        pubmed3.setNs("pmid");
        pubmed3.setAc("12347");
        pub3.getIdentifier().add(pubmed3);
        pub3.setAdminUserList(new Publication.AdminUserList());
        pub3.getAdminUserList().getUser().add("intact");
        intactPub2 = new ImexPublication(pub3);
        imexAdminUserSynchronizerTest.getImexCentralClient().createPublication(intactPub2);
    }

    @Test
    @DirtiesContext
    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    public void synchronize_intact_user() throws BridgeFailedException, SynchronizerException, PersisterException, FinderException {
        User user = new User("intact", "intact", "default", "default@ebi.ac.uk") ;
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        dao.getUserDao().persist(user);

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");

        IntactPublication intactPublication = new IntactPublication("12346");
        intactPublication.setSource(new IntactSource("intact"));
        intactPublication.setCurrentOwner(user);
        pubService.saveOrUpdate(intactPublication);

        imexAdminUserSynchronizerTest.synchronizePublicationAdminUser(intactPublication, noIntactPub);

        Assert.assertEquals(1, noIntactPub.getCurators().size());
        Assert.assertEquals("intact", noIntactPub.getCurators().iterator().next());
        noIntactPub.getCurators().clear();
    }

    @Test
    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    @DirtiesContext
    public void synchronize_intact_user_alreadyPresent() throws BridgeFailedException, SynchronizerException, FinderException, PersisterException {

        User user = new User("intact", "intact", "default", "default@ebi.ac.uk") ;
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        dao.getUserDao().persist(user);

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");

        IntactPublication intactPublication = new IntactPublication("12345");
        intactPublication.setSource(new IntactSource("intact"));
        intactPublication.setCurrentOwner(user);
        pubService.saveOrUpdate(intactPublication);

        imexAdminUserSynchronizerTest.synchronizePublicationAdminUser(intactPublication, intactPub);

        Assert.assertEquals(1, intactPub.getCurators().size());
        Assert.assertEquals("intact", intactPub.getCurators().iterator().next());
    }

    @Test
    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    @DirtiesContext
    public void synchronize_phantom_user() throws BridgeFailedException, SynchronizerException, FinderException, PersisterException {

        User user = new User("sylvie", "sylvie", "default", "default@ebi.ac.uk") ;
        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        dao.getUserDao().persist(user);

        PublicationService pubService = ApplicationContextProvider.getBean("publicationService");

        IntactPublication intactPublication = new IntactPublication("12347");
        intactPublication.setSource(new IntactSource("intact"));
        intactPublication.setCurrentOwner(user);
        pubService.saveOrUpdate(intactPublication);

        imexAdminUserSynchronizerTest.synchronizePublicationAdminUser(intactPublication, intactPub2);

        Assert.assertEquals(2, intactPub2.getCurators().size());
        Iterator<String> group = intactPub2.getCurators().iterator();

        Assert.assertEquals("intact", group.next());
        Assert.assertEquals("phantom", group.next());
        group.remove();
    }
}
