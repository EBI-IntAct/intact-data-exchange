// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.PsiVersion;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.PsiDownloadTest;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Annotation;

/**
 * TODO document this ;o)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Annotation2xmlTest extends PsiDownloadTest {

    protected void setUp() throws Exception
    {
        super.setUp();
        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        IntactContext.getCurrentInstance().getDataContext().commitAllActiveTransactions();
    }

    ////////////////////////
    // Tests

    public void testBuildXref_primaryRef_nullArguments() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion1() );

        // create a container
        Element attributeListElement = session.createElement( "attributeList" );

        // call the method we are testing
        Element annotationElement = null;

        try {
            annotationElement = Annotation2xml.createAttribute( session, attributeListElement, null );
            fail( "giving a null annotation should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( annotationElement );

        // create the IntAct object
        Annotation annotation = new Annotation( owner, comment );
        annotation.setAnnotationText( "Blablabla blabla." );

        try {
            annotationElement = Annotation2xml.createAttribute( null, attributeListElement, annotation );
            fail( "giving a null session should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( annotationElement );

        try {
            annotationElement = Annotation2xml.createAttribute( session, null, annotation );
            fail( "giving a null parent Element should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( annotationElement );
    }

    public void testBuildAnnotation_ok() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion1() );

        // create a container
        Element attributeListElement = session.createElement( "attributeList" );

        // create the IntAct object
        Annotation annotation = new Annotation( owner, comment );
        annotation.setAnnotationText( "Blablabla blabla." );

        // call the method we are testing
        Element annotationElement = Annotation2xml.createAttribute( session, attributeListElement, annotation );
        assertNotNull( annotationElement );

        // check that we have a primaryRef attached to the given parent tag
        assertEquals( 1, attributeListElement.getChildNodes().getLength() );
        Element _annotationElement = (Element) attributeListElement.getChildNodes().item( 0 );
        assertEquals( annotationElement, _annotationElement );

        // check content of the tag
        assertEquals( "attribute", annotationElement.getNodeName() );
        assertEquals( "comment", annotationElement.getAttribute( "name" ) );

        // extract the text.
        String text = getTextFromElement( annotationElement );
        assertNotNull( text );
        assertEquals( "Blablabla blabla.", text );
    }
}