// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi1;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.PsiVersion;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.PsiDownloadTest;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.model.TestableProtein;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Interaction2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Interaction2xmlI;
import uk.ac.ebi.intact.application.dataConversion.util.DOMUtil;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * TODO document this ;o)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id:Interaction2xmlPSI1Test.java 5298 2006-07-07 09:35:05 +0000 (Fri, 07 Jul 2006) baranda $
 */
public class Interaction2xmlPSI1Test extends PsiDownloadTest {

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

    ////////////////////////////////////
    // Utility methods

    Interaction buildInteraction() {

        Collection experiments = new ArrayList( 1 );
        experiments.add( Experiment2xmlPSI1Test.buildExperiment() );

        CvInteractionType anInteractionType = new CvInteractionType( owner, "anInteractionType" );
        anInteractionType.addXref( new CvObjectXref( owner, psi, "MI:0055", null, null, identity ) );

        Interaction interaction = new InteractionImpl( experiments, anInteractionType, interactionType, "gene1-gene2-3", owner );
        interaction.setFullName( "interaction's fullname." );

        // add 2 components
        Protein protein1 = new TestableProtein( "EBI-11111", owner, yeast, "p1_yeast", proteinType, "AAAAAAAAAAAAAAAAAA" );
        Component component1 = new Component( owner, interaction, protein1, bait, unspecified );
        interaction.addComponent( component1 );

        Protein protein2 = new TestableProtein( "EBI-22222", owner, yeast, "p2_yeast", proteinType, "ZZZZZZZZZZZ" );
        Component component2 = new Component( owner, interaction, protein2, neutral, unspecified );
        interaction.addComponent( component2 );
        // put features on that Interaction !!

        // add xrefs
        interaction.addXref( new InteractorXref( owner, go, "GO:0000000", "a fake GO term", null, null ) );
        interaction.addXref( new InteractorXref( owner, go, "GO:0000001", "an other fake GO term", null, null ) );
        interaction.addXref( new InteractorXref( owner, pubmed, "12345678", null, null, null ) );

        // add annotations (internal + public)
        interaction.addAnnotation( new Annotation( owner, comment, "a comment on that interaction." ) );
        interaction.addAnnotation( new Annotation( owner, remark, "a remark about that interaction." ) );
        interaction.addAnnotation( new Annotation( owner, authorConfidence, "HIGH" ) );
        interaction.addAnnotation( new Annotation( owner, authorConfidence, "0.75" ) );
        interaction.addAnnotation( new Annotation( owner, confidence_mapping, "blablabla" ) );

        return interaction;
    }

    ////////////////////////
    // Tests

    private void testBuildInteraction_nullArguments( PsiVersion version ) {

        UserSessionDownload session = new UserSessionDownload( version );
        Interaction2xmlI i = Interaction2xmlFactory.getInstance( session );

        // create a container
        Element parent = session.createElement( "interactionList" );

        // call the method we are testing
        Element element = null;

        try {
            element = i.create( session, parent, null );
            fail( "giving a null Interaction should throw an exception" );
        } catch ( IllegalArgumentException iae ) {
            // ok
        }

        assertNull( element );

        // create the IntAct object

        Interaction interaction = buildInteraction();

        try {
            element = i.create( null, parent, interaction );
            fail( "giving a null session should throw an exception" );
        } catch ( IllegalArgumentException iae ) {
            // ok
        }

        assertNull( element );

        try {
            element = i.create( session, null, interaction );
            fail( "giving a null parent Element should throw an exception" );
        } catch ( IllegalArgumentException iae ) {
            // ok
        }

        assertNull( element );
    }

    public void testBuildInteraction_nullArguments_PSI1() {
        testBuildInteraction_nullArguments( PsiVersion.getVersion1() );
    }

    public void testBuildInteraction_full_ok_PSI1() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion1() );
        session.addAnnotationFilter( remark );

        Interaction2xmlI i = Interaction2xmlFactory.getInstance( session );

        // create a container
        Element parent = session.createElement( "interactionList" );

        // call the method we are testing
        Element element = null;

        // create the IntAct object
        Interaction interaction = buildInteraction();

        // generating the PSI element...
        element = i.create( session, parent, interaction );

        // starting the checks...
        assertNotNull( element );

        // names availabilityRef availabilityDescription experimentList participantList interactionType confidence xref attributeList
        assertEquals( 7, element.getChildNodes().getLength() );

        // Checking names...
        // TODO write a method that returns an Element by name coming from the direct level
        Element names = (Element) element.getElementsByTagName( "names" ).item( 0 );
        assertNotNull( names );
        assertEquals( 2, names.getChildNodes().getLength() );
        assertHasShortlabel( names, "gene1-gene2-3" );
        assertHasFullname( names, "interaction's fullname." );

        // checking availabilityRef...

        // checking experimentList...
        Element experimentListElement = (Element) element.getElementsByTagName( "experimentList" ).item( 0 );
        assertNotNull( experimentListElement );
        assertEquals( 1, experimentListElement.getChildNodes().getLength() );
        Element experimentRefElement = (Element) experimentListElement.getElementsByTagName( "experimentRef" ).item( 0 );
        assertNotNull( experimentRefElement );
        assertEquals( ( (Experiment) interaction.getExperiments().iterator().next() ).getAc(),
                      experimentRefElement.getAttribute( "ref" ) );

        // Checking participantList...

        // Checking interactionType...
        Element interactionType = (Element) element.getElementsByTagName( "interactionType" ).item( 0 );
        assertNotNull( interactionType );
        assertEquals( 2, interactionType.getChildNodes().getLength() );
        // Checking interactionDetection's names...
        names = (Element) interactionType.getElementsByTagName( "names" ).item( 0 );
        assertNotNull( names );
        assertEquals( 1, names.getChildNodes().getLength() );
        assertHasShortlabel( names, "anInteractionType" );
        // Checking interactionType's PSI ID...
        Element xref = (Element) interactionType.getElementsByTagName( "xref" ).item( 0 );
        assertHasPrimaryRef( xref, "MI:0055", "psi-mi", null, null );

        // Checking confidence...

        // Checking xref...
        xref = (Element) DOMUtil.getDirectElementsByTagName( element, "xref" ).iterator().next();
        assertNotNull( xref );
        assertEquals( 3, xref.getChildNodes().getLength() );
        assertHasPrimaryRef( xref, "GO:0000000", "go", "a fake GO term", null );
        assertHasSecondaryRef( xref, "GO:0000001", "go", "an other fake GO term", null );
        assertHasSecondaryRef( xref, "12345678", "pubmed", null, null );

        // Checking attributeList...
        Element attributeList = (Element) element.getElementsByTagName( "attributeList" ).item( 0 );
        assertNotNull( attributeList );
        // the remark should have been filtered out.
        assertEquals( 2, attributeList.getChildNodes().getLength() );
        assertHasAttribute( attributeList, "comment", "a comment on that interaction." );
    }

    public void testBuildInteraction_single_component_ok_PSI1() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion1() );
        session.addAnnotationFilter( remark );

        Interaction2xmlI i = Interaction2xmlFactory.getInstance( session );

        // create a container
        Element parent = session.createElement( "interactionList" );

        // call the method we are testing
        Element element = null;

        // create the IntAct object
        Interaction interaction = buildInteraction();

        // modify the interaction, we want only one component with stoichiometry 2.
        while ( interaction.getComponents().size() > 1 ) {
            // remove one component.
            Iterator iterator = interaction.getComponents().iterator();
            if ( iterator.hasNext() ) {
                iterator.next();
                iterator.remove();
            }
        }
        ( (Component) interaction.getComponents().iterator().next() ).setStoichiometry( 2 );

        // generating the PSI element...
        element = i.create( session, parent, interaction );

        for ( Iterator iterator = session.getMessages().iterator(); iterator.hasNext(); ) {
            String s = (String) iterator.next();
            System.out.println( s );
        }

        // starting the checks...
        assertNotNull( element );

        // names availabilityRef availabilityDescription experimentList participantList interactionType confidence xref attributeList
        assertEquals( 7, element.getChildNodes().getLength() );

        // Everything has been tested above, just concentrate on the participant list.

        // Checking participantList...


    }
}