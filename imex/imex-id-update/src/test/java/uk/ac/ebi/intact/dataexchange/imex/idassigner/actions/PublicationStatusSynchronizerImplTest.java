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
import uk.ac.ebi.intact.model.CvPublicationStatusType;

/**
 * Unit tester of publicationStatus synchronizer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11/04/12</pre>
 */
@ContextConfiguration(locations = {"classpath*:/META-INF/intact.spring.xml",
        "classpath*:/META-INF/standalone/*-standalone.spring.xml",
        "classpath*:/META-INF/imex-test.spring.xml"})
public class PublicationStatusSynchronizerImplTest extends IntactBasicTestCase{

    @Autowired
    private PublicationStatusSynchronizer imexStatusSynchronizerTest;
    private Publication intactPub1;
    private Publication intactPub2;

    @Before
    public void createImexPublications() throws ImexCentralException {
        intactPub1 = new Publication();
        Identifier pubmed = new Identifier();
        pubmed.setNs("pmid");
        pubmed.setAc("12345");
        intactPub1.getIdentifier().add(pubmed);
        intactPub1.setStatus("NEW");
        imexStatusSynchronizerTest.getImexCentralClient().createPublication(intactPub1);

        intactPub2 = new Publication();
        Identifier pubmed2 = new Identifier();
        pubmed2.setNs("pmid");
        pubmed2.setAc("12346");
        intactPub2.getIdentifier().add(pubmed2);
        intactPub2.setStatus("INPROGRESS");
        imexStatusSynchronizerTest.getImexCentralClient().createPublication(intactPub2);

    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void synchronize_no_change_status_new() throws ImexCentralException {

        uk.ac.ebi.intact.model.Publication intactPublication = getMockBuilder().createPublication("12345");

        imexStatusSynchronizerTest.synchronizePublicationStatusWithImexCentral(intactPublication, intactPub1);

        Assert.assertEquals("NEW", intactPub1.getStatus());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void synchronize_release_status_update() throws ImexCentralException {

        uk.ac.ebi.intact.model.Publication intactPublication = getMockBuilder().createPublication("12346");
        intactPublication.getStatus().setIdentifier(CvPublicationStatusType.RELEASED.identifier());
        intactPublication.getStatus().setShortLabel(CvPublicationStatusType.RELEASED.shortLabel());

        imexStatusSynchronizerTest.synchronizePublicationStatusWithImexCentral(intactPublication, intactPub2);

        Assert.assertEquals("RELEASED", intactPub2.getStatus());
    }
}
