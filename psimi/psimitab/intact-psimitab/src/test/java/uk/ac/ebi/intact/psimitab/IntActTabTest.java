package uk.ac.ebi.intact.psimitab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;

import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.converter.xml2tab.Xml2Tab;
import psidev.psi.mi.tab.expansion.SpokeWithoutBaitExpansion;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceFactory;
import psidev.psi.mi.tab.processor.ClusterInteractorPairProcessor;

public class IntActTabTest extends AbstractPsimitabTestCase {

    public static final Log log = LogFactory.getLog( IntActTabTest.class );

    @Test
    public void testBinaryInteractionHandler() throws Exception {

        File xmlFile = new File( IntActTabTest.class.getResource( "/psi25-testset/bantscheff.xml" ).getFile() );
        assertTrue( xmlFile.canRead() );

        // convert into Tab object model
        Xml2Tab xml2tab = new Xml2Tab();

        xml2tab.setBinaryInteractionClass( IntActBinaryInteraction.class );
        xml2tab.setColumnHandler( new IntActColumnHandler() );
        xml2tab.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        xml2tab.addOverrideSourceDatabase( CrossReferenceFactory.getInstance().build( "MI", "0469", "intact" ) );
        xml2tab.setPostProcessor( new ClusterInteractorPairProcessor() );

        Collection<BinaryInteraction> interactions = xml2tab.convert( xmlFile, false );

        PsimiTabWriter writer = new PsimiTabWriter();
        writer.setColumnHandler( new IntActColumnHandler() );
        writer.setBinaryInteractionClass( IntActBinaryInteraction.class );

        File tabFile = new File( getTargetDirectory(), "bantscheff_expanded.txt" );
        assertTrue( tabFile.getParentFile().canWrite() );
        writer.write( interactions, tabFile );
        assertEquals( 760, interactions.size() );

        for ( BinaryInteraction interaction : interactions ) {
            assertTrue( interaction instanceof IntActBinaryInteraction );
        }
    }

    @Test
    public void testPsimiTabReader() throws Exception {

        File tabFile = new File( IntActTabTest.class.getResource( "/mitab-testset/bantscheff_expanded.txt" ).getFile() );
        assertTrue( tabFile.canRead() );

        boolean hasHeaderLine = true;

        PsimiTabReader reader = new PsimiTabReader( hasHeaderLine );
        reader.setBinaryInteractionClass( IntActBinaryInteraction.class );
        reader.setColumnHandler( new IntActColumnHandler() );

        Collection<BinaryInteraction> bis = reader.read( tabFile );

        File xmlFile = new File( IntActTabTest.class.getResource( "/psi25-testset/bantscheff.xml" ).getFile() );
        assertTrue( xmlFile.canRead() );

        // convert into Tab object model
        Xml2Tab xml2tab = new Xml2Tab();

        xml2tab.setBinaryInteractionClass( IntActBinaryInteraction.class );
        xml2tab.setColumnHandler( new IntActColumnHandler() );
        xml2tab.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        xml2tab.addOverrideSourceDatabase( CrossReferenceFactory.getInstance()
                .build( "MI", "0469", "intact" ) );
        xml2tab.setPostProcessor( new ClusterInteractorPairProcessor() );

        Collection<BinaryInteraction> interactions = xml2tab.convert( xmlFile, false );
        assertEquals( interactions.size(), bis.size() );

        for ( BinaryInteraction bi : bis ) {
            IntActBinaryInteraction dbi = ( IntActBinaryInteraction ) bi;
            assertTrue( dbi.getAuthors().get( 0 ).getName().contains( "Bantscheff" ) );
            assertTrue( dbi.hasExperimentalRolesInteractorA() );
            assertTrue( dbi.hasExperimentalRolesInteractorB() );
            assertTrue( dbi.hasPropertiesA() );
            assertTrue( dbi.hasPropertiesB() );
            assertTrue( BinaryInteractionImpl.class.isAssignableFrom( dbi.getClass() ) );
        }
    }

    @Test
    public void testExpansion() throws Exception {

        File xmlFile = new File( IntActTabTest.class.getResource( "/psi25-testset/simple.xml" ).getFile() );
        assertTrue( xmlFile.canRead() );

        // convert into Tab object model
        Xml2Tab xml2tab = new Xml2Tab();

        xml2tab.setBinaryInteractionClass( IntActBinaryInteraction.class );
        xml2tab.setColumnHandler( new IntActColumnHandler() );
        xml2tab.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        xml2tab.addOverrideSourceDatabase( CrossReferenceFactory.getInstance().build( "MI", "0469", "intact" ) );
        xml2tab.setPostProcessor( new ClusterInteractorPairProcessor() );

        Collection<BinaryInteraction> interactions = xml2tab.convert( xmlFile, false );

        PsimiTabWriter writer = new PsimiTabWriter();
        writer.setColumnHandler( new IntActColumnHandler() );
        writer.setBinaryInteractionClass( IntActBinaryInteraction.class );

        File tabFile = new File( getTargetDirectory(), "simple.txt" );
        assertTrue( tabFile.getParentFile().canWrite() );

        writer.write( interactions, tabFile );
        assertEquals( 2, interactions.size() );

        BinaryInteraction interaction = ( BinaryInteraction ) interactions.toArray()[1];
        assertTrue( interaction instanceof IntActBinaryInteraction );

        IntActBinaryInteraction ibi = ( IntActBinaryInteraction ) interaction;
        assertTrue( ibi.getAuthors().get( 0 ).getName().contains( "Liu et al." ) );

        assertTrue( ibi.getHostOrganism().size() == 2 );
        for ( CrossReference o : ibi.getHostOrganism() ) {
            assertTrue( o.getDatabase().contains( "yeast" ) );
        }

        assertTrue( ibi.getExperimentalRolesInteractorA().size() == 2 );
        assertTrue( ibi.getExperimentalRolesInteractorB().size() == 2 );

        assertTrue( ibi.getInteractorTypeA().size() == 1 );
        assertTrue( ibi.getInteractorTypeA().get( 0 ).getText().contains( "protein" ) );
        assertTrue( ibi.getInteractorTypeB().size() == 1 );
        assertTrue( ibi.getInteractorTypeB().get( 0 ).getText().contains( "protein" ) );

        assertTrue( ibi.hasPropertiesA() );
        assertTrue( ibi.hasPropertiesB() );
    }
}
