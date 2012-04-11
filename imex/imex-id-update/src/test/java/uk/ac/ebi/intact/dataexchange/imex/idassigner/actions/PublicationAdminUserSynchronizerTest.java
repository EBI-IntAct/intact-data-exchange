package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import edu.ucla.mbi.imex.central.ws.v20.Identifier;
import edu.ucla.mbi.imex.central.ws.v20.Publication;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;

import java.util.Iterator;

/**
 * Unit tester of PublicationAdminUserSynchronizer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/04/12</pre>
 */
@ContextConfiguration(locations = {"classpath*:/META-INF/intact.spring.xml",
        "classpath*:/META-INF/standalone/*-standalone.spring.xml",
        "classpath*:/META-INF/beansimex-test.spring.xml"})
public class PublicationAdminUserSynchronizerTest extends IntactBasicTestCase{

    @Autowired
    private PublicationAdminUserSynchronizer imexAdminUserSynchronizerTest;
    private Publication intactPub;
    private Publication noIntactPub;
    private Publication intactPub2;

    @Before
    public void createImexPublications() throws ImexCentralException {
        intactPub = new Publication();
        Identifier pubmed = new Identifier();
        pubmed.setNs("pmid");
        pubmed.setAc("12345");
        intactPub.getIdentifier().add(pubmed);
        intactPub.setAdminUserList(new Publication.AdminUserList());
        intactPub.getAdminUserList().getUser().add("intact");
        imexAdminUserSynchronizerTest.getImexCentralClient().createPublication(intactPub);

        noIntactPub = new Publication();
        Identifier pubmed2 = new Identifier();
        pubmed2.setNs("pmid");
        pubmed2.setAc("12346");
        noIntactPub.getIdentifier().add(pubmed2);
        imexAdminUserSynchronizerTest.getImexCentralClient().createPublication(noIntactPub);

        intactPub2 = new Publication();
        Identifier pubmed3 = new Identifier();
        pubmed3.setNs("pmid");
        pubmed3.setAc("12347");
        intactPub2.getIdentifier().add(pubmed3);
        intactPub2.setAdminUserList(new Publication.AdminUserList());
        intactPub2.getAdminUserList().getUser().add("intact");
        imexAdminUserSynchronizerTest.getImexCentralClient().createPublication(intactPub2);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void synchronize_intact_user() throws ImexCentralException {

        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "12346");
        intactPublication.setCurrentOwner(getIntactContext().getUserContext().getUser());
        intactPublication.getCurrentOwner().setLogin("intact");

        imexAdminUserSynchronizerTest.synchronizePublicationAdminUser(intactPublication, noIntactPub);

        Assert.assertEquals(1, noIntactPub.getAdminUserList().getUser().size());
        Assert.assertEquals("intact", noIntactPub.getAdminUserList().getUser().iterator().next());
        noIntactPub.getAdminUserList().getUser().clear();
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void synchronize_intact_user_alreadyPresent() throws ImexCentralException {

        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "12345");
        intactPublication.setCurrentOwner(getIntactContext().getUserContext().getUser());
        intactPublication.getCurrentOwner().setLogin("intact");

        imexAdminUserSynchronizerTest.synchronizePublicationAdminUser(intactPublication, intactPub);

        Assert.assertEquals(1, intactPub.getAdminUserList().getUser().size());
        Assert.assertEquals("intact", intactPub.getAdminUserList().getUser().iterator().next());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void synchronize_phantom_user() throws ImexCentralException {

        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "12347");
        intactPublication.setCurrentOwner(getIntactContext().getUserContext().getUser());
        intactPublication.getCurrentOwner().setLogin("sylvie");

        imexAdminUserSynchronizerTest.synchronizePublicationAdminUser(intactPublication, intactPub2);

        Assert.assertEquals(2, intactPub2.getAdminUserList().getUser().size());
        Iterator<String> group = intactPub2.getAdminUserList().getUser().iterator();

        Assert.assertEquals("intact", group.next());
        Assert.assertEquals("phantom", group.next());
        group.remove();
    }
}
