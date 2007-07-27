package uk.ac.ebi.intact.application.dataConversion.psiDownload;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.intact.application.dataConversion.PsiVersion;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Annotation2xml;
import uk.ac.ebi.intact.model.Institution;

/**
 * PsiDocumentFactory Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version 1.0
 * @since <pre>03/20/2005</pre>
 */
public class PsiDocumentFactoryTest extends PsiDownloadTest {

    public PsiDocumentFactoryTest( String name ) {
        super( name );
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public static Test suite() {
        return new TestSuite( PsiDocumentFactoryTest.class );
    }

    ////////////////////////
    // Tests

    private void testBuildPsiDocument( PsiVersion version ) throws Exception {

        Institution institution = new Institution( "EBI" );
        institution.setFullName( "European Bioinformatics Institute" );
        institution.setUrl( "http://www.ebi.ac.uk" );
        institution.setPostalAddress( "CB10 1SD" );

        UserSessionDownload session = new UserSessionDownload( version, institution );

        Document document = session.getPsiDocument();
        assertNotNull( document );

        // check that the PSI1 document is correctly formatted

        // check that entry is there
        assertEquals( 1, document.getChildNodes().getLength() );

        Element entry = (Element) document.getElementsByTagName( "entry" ).item( 0 );

        NodeList list = entry.getElementsByTagName( "source" );
        assertEquals( 1, list.getLength() );
        Element source = (Element) list.item( 0 );
        assertNotNull( source );

        list = source.getElementsByTagName( "names" );
        assertEquals( 1, list.getLength() );
        Element names = (Element) list.item( 0 );
        assertNotNull( names );

        assertEquals( 1, names.getChildNodes().getLength() );

        assertHasShortlabel( names, "European Bioinformatics Institute" );

        // check attributeList
        Element attributeListElement = (Element) source.getElementsByTagName( Annotation2xml.ATTRIBUTE_LIST_NODE_NAME ).item( 0 );
        assertHasAttribute( attributeListElement, "url", institution.getUrl() );
        assertHasAttribute( attributeListElement, "postalAddress", institution.getPostalAddress() );
    }

    public void testBuildPsiDocument() throws Exception {
        testBuildPsiDocument( PsiVersion.getVersion1() );
        testBuildPsiDocument( PsiVersion.getVersion2() );
    }


    private void testBuildPsiDocument1( PsiVersion version ) throws Exception {

        UserSessionDownload session = new UserSessionDownload( version, null );

        Document document = session.getPsiDocument();
        assertNotNull( document );

        // check that the PSI1 document is correctly formatted

        // check that entry is there
        assertEquals( 1, document.getChildNodes().getLength() );

        Element entry = (Element) document.getElementsByTagName( "entry" ).item( 0 );

        NodeList list = entry.getElementsByTagName( "source" );
        assertEquals( 1, list.getLength() );
        Element source = (Element) list.item( 0 );
        assertNotNull( source );

        list = source.getElementsByTagName( "names" );
        assertEquals( 1, list.getLength() );
        Element names = (Element) list.item( 0 );
        assertNotNull( names );

        assertEquals( 1, names.getChildNodes().getLength() );

        assertHasShortlabel( names, "European Bioinformatics Institute" );
    }

    public void testBuildPsiDocument1() throws Exception {
        testBuildPsiDocument1( PsiVersion.getVersion1() );
        testBuildPsiDocument1( PsiVersion.getVersion2() );
    }
}
