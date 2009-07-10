// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi2;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.PsiVersion;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.PsiDownloadTest;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.BioSource2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.BioSource2xmlI;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.CvCellType;
import uk.ac.ebi.intact.model.CvTissue;

/**
 * TODO document this ;o)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class BioSource2xmlPSI2Test extends PsiDownloadTest {

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( BioSource2xmlPSI2Test.class );
    }

    ////////////////////////
    // Tests

    private void testBuildBioSource_nullArguments( PsiVersion version ) {

        UserSessionDownload session = new UserSessionDownload( version );

        // create a container
        Element parent = session.createElement( "proteinInteractor" );

        // TODO test with wrong parent name

        // call the method we are testing
        Element element = null;

        BioSource2xmlI bsi = BioSource2xmlFactory.getInstance( session );

        try {
            element = bsi.createOrganism( session, parent, null );
            fail( "giving a null BioSource should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( element );

        // create the IntAct object
        BioSource bioSource = new BioSource( owner, "human", "9606" );

        try {
            element = bsi.createOrganism( null, parent, bioSource );
            fail( "giving a null session should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( element );

        try {
            element = bsi.createOrganism( session, null, bioSource );
            fail( "giving a null parent Element should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( element );
    }

    public void testBuildBioSource_nullArguments_PSI2() {
        testBuildBioSource_nullArguments( PsiVersion.getVersion2() );
    }

    public void testBuildOrganism_simple_ok_PSI2() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion1() );
        BioSource2xmlI bsi = BioSource2xmlFactory.getInstance( session );

        // create a container
        Element parent = session.createElement( "proteinInteractor" );

        // create the IntAct object
        BioSource bioSource = new BioSource( owner, "human", "9606" );
        assertNotNull( bioSource );

        // call the method we are testing
        Element element = bsi.createOrganism( session, parent, bioSource );

        assertEquals( 1, parent.getChildNodes().getLength() );
        assertNotNull( element );

        assertEquals( 1, parent.getElementsByTagName( "organism" ).getLength() );
        Element organism = (Element) parent.getElementsByTagName( "organism" ).item( 0 );
        assertNotNull( organism );
        assertEquals( organism, element );
        assertEquals( "organism", organism.getNodeName() );
        assertEquals( "9606", organism.getAttribute( "ncbiTaxId" ) );

        // should have names
        assertEquals( 1, organism.getChildNodes().getLength() );

        // check names
        assertEquals( 1, organism.getElementsByTagName( "names" ).getLength() );
        Element names = (Element) organism.getElementsByTagName( "names" ).item( 0 );
        assertEquals( "names", names.getNodeName() );
        assertNotNull( names );
        assertEquals( 1, names.getChildNodes().getLength() );

        // check shortlabel
        assertHasShortlabel( names, "human" );
    }

    public void testBuildHostOrganism_simple_ok_PSI2() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion2() );
        BioSource2xmlI bsi = BioSource2xmlFactory.getInstance( session );

        // create a container
        Element parent = session.createElement( "experimentDescription" );

        // create the IntAct object
        BioSource bioSource = new BioSource( owner, "human", "9606" );
        assertNotNull( bioSource );

        // call the method we are testing
        Element element = bsi.createHostOrganism( session, parent, bioSource );

        assertEquals( 1, parent.getChildNodes().getLength() );
        assertNotNull( element );

        assertEquals( 1, parent.getElementsByTagName( "hostOrganism" ).getLength() );
        Element organism = (Element) parent.getElementsByTagName( "hostOrganism" ).item( 0 );
        assertNotNull( organism );
        assertEquals( organism, element );
        assertEquals( "hostOrganism", organism.getNodeName() );
        assertEquals( "9606", organism.getAttribute( "ncbiTaxId" ) );

        // should have names
        assertEquals( 1, organism.getChildNodes().getLength() );

        // check names
        assertEquals( 1, organism.getElementsByTagName( "names" ).getLength() );
        Element names = (Element) organism.getElementsByTagName( "names" ).item( 0 );
        assertEquals( "names", names.getNodeName() );
        assertNotNull( names );
        assertEquals( 1, names.getChildNodes().getLength() );

        // check shortlabel
        assertHasShortlabel( names, "human" );
    }

    public void testBuildBioSource_complex_ok_PSI2() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion2() );
        BioSource2xmlI bsi = BioSource2xmlFactory.getInstance( session );

        // create a container
        Element parent = session.createElement( "proteinInteractor" );

        // create the IntAct object
        BioSource bioSource = new BioSource( owner, "human", "9606" );
        bioSource.setFullName( "Homo Sapiens" );
        assertNotNull( bioSource );

        CvCellType cellType = (CvCellType) createCvObject( CvCellType.class, "a_431", "Human epidermoid carcinoma",
                                                           "MI:0001" );
        assertNotNull( cellType );
        bioSource.setCvCellType( cellType );

        CvTissue tissue = (CvTissue) createCvObject( CvTissue.class, "brain", "Brain [cerebrum]", "MI:0002" );
        assertNotNull( tissue );
        bioSource.setCvTissue( tissue );

        // call the method we are testing
        Element element = bsi.createOrganism( session, parent, bioSource );

        assertEquals( 1, parent.getChildNodes().getLength() );
        assertNotNull( element );

        assertEquals( 1, parent.getElementsByTagName( "organism" ).getLength() );
        Element organism = (Element) parent.getElementsByTagName( "organism" ).item( 0 );
        assertNotNull( organism );
        assertEquals( "organism", organism.getNodeName() );
        assertEquals( "9606", organism.getAttribute( "ncbiTaxId" ) );

        // should have names
        assertEquals( 3, organism.getChildNodes().getLength() );

        // check names
        // 4 because we count all descendant Element having the name 'names', biosource(1) + CVs(3)
        assertEquals( 3, organism.getElementsByTagName( "names" ).getLength() );
        // TODO write a method that returns an Element by name coming from the direct level
        Element names = (Element) organism.getElementsByTagName( "names" ).item( 0 );
        assertNotNull( names );
        assertEquals( 2, names.getChildNodes().getLength() );
        assertHasShortlabel( names, "human" );
        assertHasFullname( names, "Homo Sapiens" );

        // check celltype
        Element cellTypeElement = (Element) organism.getElementsByTagName( "cellType" ).item( 0 );
        assertNotNull( cellTypeElement );

        // check celltype's names
        names = (Element) cellTypeElement.getElementsByTagName( "names" ).item( 0 );
        assertNotNull( names );
        assertEquals( 2, names.getChildNodes().getLength() );
        assertHasShortlabel( names, "a_431" );
        assertHasFullname( names, "Human epidermoid carcinoma" );

        // check xrefs
        Element xrefElement = (Element) cellTypeElement.getElementsByTagName( "xref" ).item( 0 );
        assertHasPrimaryRef( xrefElement, "MI:0001", "psi-mi", null, null );

        // check tissue
        Element tissueElement = (Element) organism.getElementsByTagName( "tissue" ).item( 0 );
        assertNotNull( tissueElement );

        // check tissue's names
        names = (Element) tissueElement.getElementsByTagName( "names" ).item( 0 );
        assertNotNull( names );
        assertEquals( 2, names.getChildNodes().getLength() );
        assertHasShortlabel( names, "brain" ); // intact converts shortlabel to lowercase.
        assertHasFullname( names, "Brain [cerebrum]" );

        // check xrefs
        xrefElement = (Element) tissueElement.getElementsByTagName( "xref" ).item( 0 );
        assertHasPrimaryRef( xrefElement, "MI:0002", "psi-mi", null, null );
    }
}