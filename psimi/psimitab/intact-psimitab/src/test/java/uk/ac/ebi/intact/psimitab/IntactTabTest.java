package uk.ac.ebi.intact.psimitab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.converter.xml2tab.Xml2Tab;
import psidev.psi.mi.tab.expansion.SpokeWithoutBaitExpansion;
import psidev.psi.mi.tab.model.*;
import psidev.psi.mi.tab.processor.ClusterInteractorPairProcessor;
import uk.ac.ebi.intact.psimitab.processor.IntactClusterInteractorPairProcessor;

import java.io.File;
import java.io.StringWriter;
import java.util.Collection;

public class IntactTabTest extends AbstractPsimitabTestCase {

    @Test
    public void binaryInteractionHandler() throws Exception {

        File xmlFile = getFileByResources( "/psi25-testset/9971739.xml", IntactTabTest.class );
        assertTrue( xmlFile.canRead() );

        // convert into Tab object model
        Xml2Tab xml2tab = new IntactXml2Tab();
        
        xml2tab.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        xml2tab.addOverrideSourceDatabase( CrossReferenceFactory.getInstance().build( "MI", "0469", "intact" ) );
        xml2tab.setPostProcessor( new ClusterInteractorPairProcessor() );

        Collection<BinaryInteraction> interactions = xml2tab.convert( xmlFile, false );

        PsimiTabWriter writer = new IntactPsimiTabWriter( false, false );

        File tabFile = new File( getTargetDirectory(), "9971739_expanded.txt" );
        assertTrue( tabFile.getParentFile().canWrite() );
        writer.write( interactions, tabFile );
        //assertEquals( 3, interactions.size() );

        for ( BinaryInteraction interaction : interactions ) {
            assertTrue( interaction instanceof IntactBinaryInteraction );
        }
    }

    @Test
    public void psimiTabReader() throws Exception {

        File tabFile = getFileByResources( "/mitab-testset/9971739_expanded.txt", IntactTabTest.class );
        assertTrue( tabFile.canRead() );

        boolean hasHeaderLine = true;

        PsimiTabReader reader = new IntactPsimiTabReader( hasHeaderLine );

        Collection<BinaryInteraction> bis = reader.read( tabFile );

        File xmlFile = getFileByResources( "/psi25-testset/9971739.xml", IntactTabTest.class );
        assertTrue( xmlFile.canRead() );

        // convert into Tab object model
        Xml2Tab xml2tab = new IntactXml2Tab( false, false );

        xml2tab.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        xml2tab.addOverrideSourceDatabase( CrossReferenceFactory.getInstance().build( "MI", "0469", "intact" ) );
        xml2tab.setPostProcessor( new IntactClusterInteractorPairProcessor() );

        Collection<BinaryInteraction> interactions = xml2tab.convert( xmlFile, false );
        assertEquals( interactions.size(), bis.size() );

        // TODO (Bruno 11/08/08) THIS IS NOT WORKING, follow the calls and check if the IntactInteractionConverter
        // is invoked correctly. Thanks for your patience :)
        
        for ( BinaryInteraction bi : bis ) {
            IntactBinaryInteraction dbi = ( IntactBinaryInteraction ) bi;
            assertTrue( dbi.getAuthors().get( 0 ).getName().contains( "Leung" ) );
            assertTrue( dbi.hasExperimentalRolesInteractorA() );
            assertTrue( dbi.hasExperimentalRolesInteractorB() );
            assertTrue( dbi.hasPropertiesA() );
            assertTrue( dbi.hasPropertiesB() );
            assertTrue( BinaryInteractionImpl.class.isAssignableFrom( dbi.getClass() ) );
        }
    }

    @Test
    public void expansion() throws Exception {

        File xmlFile = getFileByResources( "/psi25-testset/simple.xml", IntactTabTest.class );
        assertTrue( xmlFile.canRead() );

        // convert into Tab object model
        Xml2Tab xml2tab = new IntactXml2Tab();

        xml2tab.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        xml2tab.addOverrideSourceDatabase( CrossReferenceFactory.getInstance().build( "MI", "0469", "intact" ) );
        xml2tab.setPostProcessor( new IntactClusterInteractorPairProcessor() );

        Collection<BinaryInteraction> interactions = xml2tab.convert( xmlFile, false );

        PsimiTabWriter writer = new IntactPsimiTabWriter( false, false );

        StringWriter sw = new StringWriter();

        writer.write( interactions, sw );
        assertEquals( 2, interactions.size() );

        BinaryInteraction interaction = ( BinaryInteraction ) interactions.toArray()[1];
        assertTrue( interaction instanceof IntactBinaryInteraction );

        IntactBinaryInteraction ibi = ( IntactBinaryInteraction ) interaction;
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

    @Test
    public void ifAuthorIsCurator() throws Exception {
        // reading a file were all interactions inferred by currators
        File xmlFile = getFileByResources( "/psi25-testset/14681455.xml", IntactTabTest.class );
        assertTrue( xmlFile.canRead() );

        // convert into Tab object model
        Xml2Tab x2t = new IntactXml2Tab( false, false );

        x2t.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        x2t.addOverrideSourceDatabase( CrossReferenceFactory.getInstance().build( "MI", "0469", "intact" ) );
        x2t.setPostProcessor( new IntactClusterInteractorPairProcessor() );

        Collection<BinaryInteraction> interactions = x2t.convert( xmlFile, false );

        for ( BinaryInteraction interaction : interactions ) {
            for ( Author author : interaction.getAuthors() ) {
                // if we know that all interactions inferred by currators,
                // alls authors should start with 'Curated complexes'
                assertTrue( author.getName().startsWith( "Curated complexes" ) );
            }
        }
    }
}
