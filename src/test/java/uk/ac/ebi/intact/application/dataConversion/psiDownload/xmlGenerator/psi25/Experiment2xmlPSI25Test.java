/**
 * Copyright 2006 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi25;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.PsiVersion;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.PsiDownloadTest;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.model.TestableExperiment;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Experiment2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Experiment2xmlI;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;

/**
 * TODO document this ;o)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id:Experiment2xmlPSI25Test.java 5298 2006-07-07 09:35:05 +0000 (Fri, 07 Jul 2006) baranda $
 */
public class Experiment2xmlPSI25Test extends PsiDownloadTest
{

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
    // Utility methods

    /**
     * Build a valid IntAct experiment. <br> That method will be available to other Test class of that package.
     *
     * @return a valid IntAct experiment.
     */
    static Experiment buildExperiment() {

        Experiment experiment = new TestableExperiment( "EBI-1234", owner, "experiment-2005-1", yeast );
        experiment.setFullName( "experiment's full name." );

        CvInteraction cvInteraction =
                (CvInteraction) createCvObject( CvInteraction.class, "affinity chromatogra",
                                                "affinity chromatography technologies", "MI:0004" );
        experiment.setCvInteraction( cvInteraction );

        CvIdentification cvIdentification =
                (CvIdentification) createCvObject( CvIdentification.class, "partial dna sequence",
                                                   "partial dna sequence identification by hybridization", "MI:0080" );
        experiment.setCvIdentification( cvIdentification );

        experiment.addXref( new ExperimentXref( owner, pubmed, "12345678", null, null, primaryReference ) );
        experiment.addXref( new ExperimentXref( owner, pubmed, "98765432", null, null, seeAlso ) );
        experiment.addXref( new ExperimentXref( owner, intact, "EBI-xxxxxx", null, "jan05", null ) );

        experiment.addAnnotation( new Annotation( owner, comment, "an interresting comment." ) );
        experiment.addAnnotation( new Annotation( owner, remark, "an interresting remark." ) );
        experiment.addAnnotation( new Annotation( owner, confidence_mapping, "VERYHIGH" ) );
        experiment.addAnnotation( new Annotation( owner, confidence_mapping, "HIGH" ) );
        experiment.addAnnotation( new Annotation( owner, authorConfidence, "the interaction having a ..." ) );

        return experiment;
    }

    ////////////////////////
    // Tests

    public void testBuildExperiment_nullArguments() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion25() );

        // create a container
        Element parent = session.createElement( "experimentList" );

        // call the method we are testing
        Element element = null;

        Experiment2xmlI e = Experiment2xmlFactory.getInstance( session );

        try {
            element = e.create( session, parent, null );
            fail( "giving a null Experiment should throw an exception" );
        } catch ( IllegalArgumentException iae ) {
            // ok
        }

        assertNull( element );

        // create the IntAct object
        Experiment experiment = new Experiment( owner, "experiment-2005-1", yeast );

        try {
            element = e.create( null, parent, experiment );
            fail( "giving a null session should throw an exception" );
        } catch ( IllegalArgumentException iae ) {
            // ok
        }

        assertNull( element );

        try {
            element = e.create( session, null, experiment );
            fail( "giving a null parent Element should throw an exception" );
        } catch ( IllegalArgumentException iae ) {
            // ok
        }

        assertNull( element );
    }

    public void testBuildExperiment_full_ok() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion25() );
        Experiment2xmlI e = Experiment2xmlFactory.getInstance( session );

        // create a container
        Element parent = session.createElement( "experimentList" );

        // call the method we are testing
        Element element = null;

        // create the IntAct object
        Experiment experiment = buildExperiment();

        session.addAnnotationFilter( remark );

        // generating the PSI element...
        element = e.create( session, parent, experiment );

        // starting the checks...
        assertNotNull( element );
        assertEquals( "" + session.getExperimentIdentifier( experiment ), element.getAttribute( "id" ) );
        // names, bibRef, xref, hostOrganism, interactionDetection, participantDetectionMethod, confidenceList, attributeList

        assertEquals( 8, element.getChildNodes().getLength() );

        // Checking names...
        // TODO write a method that returns an Element by name coming from the direct level
        Element names = (Element) element.getElementsByTagName( "names" ).item( 0 );
        assertNotNull( names );
        assertEquals( 2, names.getChildNodes().getLength() );
        assertHasShortlabel( names, "experiment-2005-1" );
        assertHasFullname( names, "experiment's full name." );

        // Checking bibref...
        Element bibref = (Element) element.getElementsByTagName( "bibref" ).item( 0 );
        assertNotNull( bibref );
        Element bibrefXref = (Element) bibref.getElementsByTagName( "xref" ).item( 0 );
        assertEquals( 2, bibrefXref.getChildNodes().getLength() );
        assertHasPrimaryRef( bibrefXref, "12345678", "pubmed", null, null );
        assertHasSecondaryRef( bibrefXref, "98765432", "pubmed", null, null );

        // Checking xref...
        Element xref = (Element) element.getElementsByTagName( "xref" ).item( 1 ); // index 0 is the one from bibref
        assertNotNull( xref );
        assertEquals( 2, xref.getChildNodes().getLength() );
        assertHasPrimaryRef( xref, "EBI-1234", "intact", "experiment-2005-1", null );
        assertHasSecondaryRef( xref, "EBI-xxxxxx", "intact", null, "jan05" );

        // Checking hostOrganism...
        Element hostOrganism = (Element) element.getElementsByTagName( "hostOrganism" ).item( 0 );
        assertNotNull( hostOrganism );
        assertEquals( "4932", hostOrganism.getAttribute( "ncbiTaxId" ) );
        // check names
        assertEquals( 1, hostOrganism.getElementsByTagName( "names" ).getLength() );
        names = (Element) hostOrganism.getElementsByTagName( "names" ).item( 0 );
        assertNotNull( names );
        assertEquals( 3, names.getChildNodes().getLength() );
        assertHasShortlabel( names, "yeast" );

        // Checking interactionDetection...
        Element interactionDetection = (Element) element.getElementsByTagName( "interactionDetectionMethod" ).item( 0 );
        assertNotNull( interactionDetection );
        assertEquals( 2, interactionDetection.getChildNodes().getLength() );
        // Checking interactionDetection's names...
        names = (Element) interactionDetection.getElementsByTagName( "names" ).item( 0 );
        assertNotNull( names );
        assertEquals( 2, names.getChildNodes().getLength() );
        assertHasShortlabel( names, "affinity chromatogra" );
        assertHasFullname( names, "affinity chromatography technologies" );
        // Checking interactionDetection's PSI ID...
        xref = (Element) interactionDetection.getElementsByTagName( "xref" ).item( 0 );
        assertHasPrimaryRef( xref, "MI:0004", "psi-mi", null, null );

        // Checking interactionDetection...
        Element participantDetection = (Element) element.getElementsByTagName( "participantIdentificationMethod" ).item( 0 );
        assertNotNull( participantDetection );
        assertEquals( 2, participantDetection.getChildNodes().getLength() );
        // Checking participantDetection's names...
        names = (Element) participantDetection.getElementsByTagName( "names" ).item( 0 );
        assertNotNull( names );
        assertEquals( 2, names.getChildNodes().getLength() );
        assertHasShortlabel( names, "partial dna sequence" );
        assertHasFullname( names, "partial dna sequence identification by hybridization" );
        // Checking participantDetection's PSI ID...
        xref = (Element) participantDetection.getElementsByTagName( "xref" ).item( 0 );
        assertHasPrimaryRef( xref, "MI:0080", "psi-mi", null, null );

        // Checking on confidenceList
        Element confidenceList = (Element) element.getElementsByTagName( "confidenceList" ).item( 0 );
        assertNotNull( confidenceList );
        // names, value
        assertEquals( 2, confidenceList.getChildNodes().getLength() );

        for ( int i = 0; i < 2; i++ ) {
            Element confidenceElement = (Element) element.getElementsByTagName( "confidence" ).item( i );
            assertEquals( 2, confidenceElement.getChildNodes().getLength() );
            names = (Element) confidenceElement.getElementsByTagName( "names" ).item( 0 );
            assertHasShortlabel( names, confidence_mapping.getShortLabel() );


            Element value = (Element) confidenceElement.getElementsByTagName( "value" ).item( 0 );
            String myValue = getTextFromElement( value );
            assertTrue( "HIGH".equals( myValue ) || "VERYHIGH".equals( myValue ) );
        }

        // Checking attributeList...
        Element attributeList = (Element) element.getElementsByTagName( "attributeList" ).item( 0 );
        assertNotNull( attributeList );
        // the remark should have been filtered out.
        assertEquals( 2, attributeList.getChildNodes().getLength() );
        assertHasAttribute( attributeList, "comment", "an interresting comment." );
    }
}
