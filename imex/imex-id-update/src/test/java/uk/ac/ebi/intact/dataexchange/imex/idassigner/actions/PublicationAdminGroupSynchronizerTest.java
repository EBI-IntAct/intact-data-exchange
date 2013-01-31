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
 * Unit tester of PublicationAdminGroupSynchronizer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/04/12</pre>
 */
@ContextConfiguration(locations = {"classpath*:/META-INF/intact.spring.xml",
        "classpath*:/META-INF/standalone/*-standalone.spring.xml",
        "classpath*:/META-INF/beansimex-test.spring.xml"})
public class PublicationAdminGroupSynchronizerTest extends IntactBasicTestCase{

    @Autowired
    private PublicationAdminGroupSynchronizer imexAdminGroupSynchronizerTest;
    private Publication intactPub;
    private Publication noIntactPub;
    private Publication intactPub2;
    private Publication noIntactPub2;

    @Before
    public void createImexPublications() throws ImexCentralException {
        intactPub = new Publication();
        Identifier pubmed = new Identifier();
        pubmed.setNs("pmid");
        pubmed.setAc("12345");
        intactPub.getIdentifier().add(pubmed);
        intactPub.setAdminGroupList(new Publication.AdminGroupList());
        intactPub.getAdminGroupList().getGroup().add("INTACT");
        intactPub.getAdminGroupList().getGroup().add("INTACT Curators");
        imexAdminGroupSynchronizerTest.getImexCentralClient().createPublication(intactPub);

        noIntactPub = new Publication();
        Identifier pubmed2 = new Identifier();
        pubmed2.setNs("pmid");
        pubmed2.setAc("12346");
        noIntactPub.getIdentifier().add(pubmed2);
        imexAdminGroupSynchronizerTest.getImexCentralClient().createPublication(noIntactPub);

        intactPub2 = new Publication();
        Identifier pubmed3 = new Identifier();
        pubmed3.setNs("pmid");
        pubmed3.setAc("12347");
        intactPub2.getIdentifier().add(pubmed3);
        intactPub2.setAdminGroupList(new Publication.AdminGroupList());
        intactPub2.getAdminGroupList().getGroup().add("INTACT");
        imexAdminGroupSynchronizerTest.getImexCentralClient().createPublication(intactPub2);

        noIntactPub2 = new Publication();
        Identifier pubmed4 = new Identifier();
        pubmed4.setNs("pmid");
        pubmed4.setAc("12348");
        noIntactPub2.getIdentifier().add(pubmed4);
        imexAdminGroupSynchronizerTest.getImexCentralClient().createPublication(noIntactPub2);

    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void synchronize_intact_admin() throws ImexCentralException {

        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "12346");
        intactPublication.getOwner().setShortLabel("intact");
        
        imexAdminGroupSynchronizerTest.synchronizePublicationAdminGroup(intactPublication, noIntactPub);
        
        Assert.assertEquals(1, noIntactPub.getAdminGroupList().getGroup().size());
        Assert.assertEquals("INTACT", noIntactPub.getAdminGroupList().getGroup().iterator().next());
        noIntactPub.getAdminGroupList().getGroup().clear();
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void synchronize_intact_admin_alreadyPresent() throws ImexCentralException {

        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "12345");
        intactPublication.getOwner().setShortLabel("intact");

        imexAdminGroupSynchronizerTest.synchronizePublicationAdminGroup(intactPublication, intactPub);

        Assert.assertEquals(2, intactPub.getAdminGroupList().getGroup().size());
        Assert.assertEquals("INTACT", intactPub.getAdminGroupList().getGroup().iterator().next());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void synchronize_matrixDb_admin_alreadyPresent() throws ImexCentralException {

        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "12347");
        intactPublication.getOwner().setShortLabel("matrixdb");

        imexAdminGroupSynchronizerTest.synchronizePublicationAdminGroup(intactPublication, intactPub2);

        Assert.assertEquals(2, intactPub2.getAdminGroupList().getGroup().size());
        Iterator<String> group = intactPub2.getAdminGroupList().getGroup().iterator();
        
        Assert.assertEquals("INTACT", group.next());
        Assert.assertEquals("MATRIXDB", group.next());
        group.remove();
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void synchronize_unknown_admin_add_intact() {

        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "12348");
        intactPublication.getOwner().setShortLabel("i2d");

        try {
            imexAdminGroupSynchronizerTest.synchronizePublicationAdminGroup(intactPublication, noIntactPub2);
            Assert.assertEquals(1, noIntactPub2.getAdminGroupList().getGroup().size());
            Iterator<String> group = noIntactPub2.getAdminGroupList().getGroup().iterator();

            Assert.assertEquals("INTACT", group.next());
            noIntactPub2.getAdminGroupList().getGroup().clear();
        } catch (ImexCentralException e) {
            Assert.assertFalse(true);
        }
    }
}
