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
 * Unit tester of ImexcentralPublicationregister
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>05/04/12</pre>
 */
@ContextConfiguration(locations = {"classpath*:/META-INF/intact.spring.xml",
        "classpath*:/META-INF/standalone/*-standalone.spring.xml",
        "classpath*:/META-INF/beansimex-test.spring.xml"})
public class ImexCentralPublicationRegisterImplTest extends IntactBasicTestCase{

    @Autowired
    private ImexCentralPublicationRegister imexCentralRegisterTest;

    @Before
    public void createImexPublications() throws ImexCentralException {
        Publication pubmedPub = new Publication();
        Identifier pubmed = new Identifier();
        pubmed.setNs("pmid");
        pubmed.setAc("12345");
        pubmedPub.getIdentifier().add(pubmed);
        pubmedPub.setImexAccession("N/A");
        imexCentralRegisterTest.getImexCentralClient().createPublication(pubmedPub);

        Publication doiPub = new Publication();
        Identifier doi = new Identifier();
        doi.setNs("doi");
        doi.setAc("1/a-2345");
        doiPub.getIdentifier().add(doi);
        doiPub.setImexAccession("N/A");
        imexCentralRegisterTest.getImexCentralClient().createPublication(doiPub);

        Publication imexPub = new Publication();
        Identifier imex = new Identifier();
        imex.setNs("imex");
        imex.setAc("IM-1");
        imexPub.getIdentifier().add(imex);
        imexPub.setImexAccession("IM-1");
        imexCentralRegisterTest.getImexCentralClient().createPublication(imexPub);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void getExistingPublicationInImexCentral_pubmed() throws ImexCentralException {

        Publication pubmedPub = imexCentralRegisterTest.getExistingPublicationInImexCentral("12345");
        Assert.assertNotNull(pubmedPub);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void getExistingPublicationInImexCentral_doi() throws ImexCentralException {
        Publication doiPub = imexCentralRegisterTest.getExistingPublicationInImexCentral("1/a-2345");
        Assert.assertNotNull(doiPub);

    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void getExistingPublicationInImexCentral_imex() throws ImexCentralException {
        Publication imexPub = imexCentralRegisterTest.getExistingPublicationInImexCentral("IM-1");
        Assert.assertNotNull(imexPub);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void getExistingPublicationInImexCentral_notExisting() throws ImexCentralException {
        Publication pub = imexCentralRegisterTest.getExistingPublicationInImexCentral("166667");
        Assert.assertNull(pub);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void registerPublicationInImexCentral_pubmed() throws ImexCentralException {
        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "1369");

        Publication pub = imexCentralRegisterTest.registerPublicationInImexCentral(intactPublication);
        Assert.assertNotNull(pub);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void registerPublicationInImexCentral_unassigned() throws ImexCentralException {
        uk.ac.ebi.intact.model.Publication intactPublication = new uk.ac.ebi.intact.model.Publication(getIntactContext().getInstitution(), "unassigned604");

        Publication pub = imexCentralRegisterTest.registerPublicationInImexCentral(intactPublication);
        Assert.assertNull(pub);
    }
}
