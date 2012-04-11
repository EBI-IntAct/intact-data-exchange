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

/**
 * Unit tester of PublicationIdentifierSynchronizer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/04/12</pre>
 */
@ContextConfiguration(locations = {"classpath*:/META-INF/intact.spring.xml",
        "classpath*:/META-INF/standalone/*-standalone.spring.xml",
        "classpath*:/META-INF/beansimex-test.spring.xml"})
public class PublicationIdentifierSynchronizerImplTest extends IntactBasicTestCase {

    @Autowired
    private PublicationIdentifierSynchronizer identifierSynchronizerTest;
    private Publication intactPub12345;
    private Publication intactPub12346;
    private Publication intactPub12347;
    private Publication intactPubUnassigned;

    @Before
    public void createImexPublications() throws ImexCentralException {
        intactPub12345 = new Publication();
        Identifier pubmed = new Identifier();
        pubmed.setNs("pmid");
        pubmed.setAc("12345");
        intactPub12345.getIdentifier().add(pubmed);
        intactPub12345.setImexAccession("IM-1");
        identifierSynchronizerTest.getImexCentralClient().createPublication(intactPub12345);

        intactPub12346 = new Publication();
        Identifier pubmed2 = new Identifier();
        pubmed2.setNs("pmid");
        pubmed2.setAc("12346");
        intactPub12346.getIdentifier().add(pubmed2);
        intactPub12346.setImexAccession("IM-2");
        identifierSynchronizerTest.getImexCentralClient().createPublication(intactPub12346);

        intactPub12347 = new Publication();
        Identifier pubmed3 = new Identifier();
        pubmed3.setNs("pmid");
        pubmed3.setAc("12347");
        intactPub12347.getIdentifier().add(pubmed3);
        intactPub12347.setImexAccession("IM-3");
        identifierSynchronizerTest.getImexCentralClient().createPublication(intactPub12347);

        intactPubUnassigned = new Publication();
        intactPubUnassigned.setImexAccession("IM-4");
        identifierSynchronizerTest.getImexCentralClient().createPublication(intactPubUnassigned);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void intact_publication_identifier_synchronized_with_imex_central() throws ImexCentralException {

        Assert.assertTrue(identifierSynchronizerTest.isIntactPublicationIdentifierInSyncWithImexCentral("12345", intactPub12345));
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void intact_publication_unassigned_synchronized_with_imex_central() throws ImexCentralException {

        Assert.assertTrue(identifierSynchronizerTest.isIntactPublicationIdentifierInSyncWithImexCentral("unassigned604", intactPubUnassigned));
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void intact_publication_identifier_no_identifier_imex_central() throws ImexCentralException {

        Assert.assertFalse(identifierSynchronizerTest.isIntactPublicationIdentifierInSyncWithImexCentral("12345", intactPubUnassigned));
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void intact_publication_identifier_mismatch_imex_central() throws ImexCentralException {

        Assert.assertFalse(identifierSynchronizerTest.isIntactPublicationIdentifierInSyncWithImexCentral("12346", intactPub12345));
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void synchronize_intact_publication_identifier_already_synchronized_with_imex_central() throws ImexCentralException, PublicationImexUpdaterException {

        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "12345");

        identifierSynchronizerTest.synchronizePublicationIdentifier(intactPublication, intactPub12345);
        
        Assert.assertEquals("12345", intactPub12345.getIdentifier().iterator().next().getAc());
    }

    /*@Test
    @Transactional(propagation = Propagation.NEVER)
    public void synchronize_intact_publication_identifier_no_identifier_in_imex_central() throws ImexCentralException, PublicationImexUpdaterException {
        intactPub12345.getIdentifier().clear();

        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "12345");

        identifierSynchronizerTest.synchronizePublicationIdentifier(intactPublication, intactPub12345);

        Assert.assertEquals("12345", intactPub12345.getIdentifier().iterator().next().getAc());
    }*/
}
