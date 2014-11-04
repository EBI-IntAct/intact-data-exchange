package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import edu.ucla.mbi.imex.central.ws.v20.Identifier;
import edu.ucla.mbi.imex.central.ws.v20.Publication;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
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
        "classpath*:/META-INF/imex-test.spring.xml"})
public class PublicationIdentifierSynchronizerImplTest extends IntactBasicTestCase {

    @Autowired
    private PublicationIdentifierSynchronizer identifierSynchronizerTest;
    private Publication intactPub12345;
    private Publication intactDoi;
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

        intactDoi = new Publication();
        Identifier pubmed2 = new Identifier();
        pubmed2.setNs("doi");
        pubmed2.setAc("123/1(a2)");
        intactDoi.getIdentifier().add(pubmed2);
        intactDoi.setImexAccession("IM-2");
        identifierSynchronizerTest.getImexCentralClient().createPublication(intactDoi);

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
    public void intact_publication_unassigned_not_synchronized_with_imex_central() throws ImexCentralException {

        Assert.assertFalse(identifierSynchronizerTest.isIntactPublicationIdentifierInSyncWithImexCentral("unassigned604", intactPubUnassigned));
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
    @DirtiesContext
    public void synchronize_intact_publication_identifier_already_synchronized_with_imex_central() throws ImexCentralException, PublicationImexUpdaterException {

        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "12345");

        identifierSynchronizerTest.synchronizePublicationIdentifier(intactPublication, intactPub12345);
        
        Assert.assertEquals("12345", intactPub12345.getIdentifier().iterator().next().getAc());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void synchronize_intact_publication_identifier_no_identifier_in_imex_central() throws ImexCentralException, PublicationImexUpdaterException {
        intactPub12345.getIdentifier().clear();

        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "12345");

        identifierSynchronizerTest.synchronizePublicationIdentifier(intactPublication, intactPub12345);

        Assert.assertEquals("12345", intactPub12345.getIdentifier().iterator().next().getAc());
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void update_unassigned_identifier() throws ImexCentralException, PublicationImexUpdaterException {
        Assert.assertTrue(intactPubUnassigned.getIdentifier().isEmpty());

        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "unassigned604");

        identifierSynchronizerTest.synchronizePublicationIdentifier(intactPublication, intactPubUnassigned);

        Assert.assertEquals("unassigned604", intactPubUnassigned.getIdentifier().iterator().next().getAc());
        intactPubUnassigned.getIdentifier().clear();
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void synchronized_mismatch_pubmedId_aborted() throws ImexCentralException {

        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "12346");

        try {
            identifierSynchronizerTest.synchronizePublicationIdentifier(intactPublication, intactPub12345);
            Assert.assertFalse(true);
        } catch (PublicationImexUpdaterException e) {
            Assert.assertEquals("12345", intactPub12345.getIdentifier().iterator().next().getAc());
        }
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void synchronized_mismatch_unassigned_intact_aborted() throws ImexCentralException {

        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "unassigned604");

        try {
            identifierSynchronizerTest.synchronizePublicationIdentifier(intactPublication, intactPub12345);
            Assert.assertFalse(true);
        } catch (PublicationImexUpdaterException e) {
            Assert.assertEquals("12345", intactPub12345.getIdentifier().iterator().next().getAc());
        }
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void synchronized_mismatch_doi_aborted() throws ImexCentralException {

        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "1234-5(7a)");

        try {
            identifierSynchronizerTest.synchronizePublicationIdentifier(intactPublication, intactDoi);
            Assert.assertFalse(true);
        } catch (PublicationImexUpdaterException e) {
            Assert.assertEquals("123/1(a2)", intactDoi.getIdentifier().iterator().next().getAc());
        }
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void synchronized_new_identifier_already_existing_aborted() throws PublicationImexUpdaterException {

        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "12345");

        try {
            identifierSynchronizerTest.synchronizePublicationIdentifier(intactPublication, intactPubUnassigned);
            Assert.assertFalse(true);
        } catch (ImexCentralException e) {
            Assert.assertTrue(intactPubUnassigned.getIdentifier().isEmpty());
        }
    }
}
