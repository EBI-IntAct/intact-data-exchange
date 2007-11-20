package uk.ac.ebi.intact.psimitab.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.psimitab.IntActBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntActColumnHandler;
import uk.ac.ebi.intact.psimitab.converters.expansion.SpokeWithoutBaitExpansion;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Intact2Tab Tester.
 *
 * @author Nadin Neuhauser
 * @version 1.0
 * @since <pre>11/13/2007</pre>
 */
public class Intact2TabTest extends IntactBasicTestCase {

    @Test
    public void convertIntact2TabTest_BinaryInteractionImpl() throws Exception {

        Intact2Tab i2t = new Intact2Tab();
        i2t.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        final Experiment e = getMockBuilder().createExperimentRandom( 3 );

        Collection<Interaction> interactions = new ArrayList<Interaction>();
        for ( Interaction interaction : e.getInteractions() ) {
            interaction.setAc( "EBI-zzzzzzz" );
            Iterator<Component> iter = interaction.getComponents().iterator();
            iter.next().getInteractor().setAc( "EBI-xxxxxxx" );
            iter.next().getInteractor().setAc( "EBI-yyyyyyy" );

            interactions.add( interaction );
        }
        assertEquals( 3, interactions.size() );

        Collection<BinaryInteraction> bis = i2t.convert( interactions );
        assertNotNull(bis);

        File file = new File( TestHelper.getTargetDirectory(), "test_1.txt");
        PsimiTabWriter writer = new PsimiTabWriter();
        writer.write( bis, file );
    }

    @Test
    public void convertIntact2TabTest_IntactBinaryInteraction() throws Exception {

        Intact2Tab i2t = new Intact2Tab();
        i2t.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        i2t.setBinaryInteractionClass( IntActBinaryInteraction.class );
        i2t.setBinaryInteractionHandler( new IntactBinaryInteractionHandler() );
        final Experiment e = getMockBuilder().createExperimentRandom( 3 );

        Collection<Interaction> interactions = new ArrayList<Interaction>();
        for ( Interaction interaction : e.getInteractions() ) {
            interaction.setAc( "EBI-zzzzzzz" );
            Iterator<Component> iter = interaction.getComponents().iterator();
            iter.next().getInteractor().setAc( "EBI-xxxxxxx" );
            iter.next().getInteractor().setAc( "EBI-yyyyyyy" );

            interactions.add( interaction );
        }
        assertEquals( 3, interactions.size() );

        Collection<BinaryInteraction> bis = i2t.convert( interactions );
        assertNotNull(bis);

        File file = new File( TestHelper.getTargetDirectory(), "test_2.txt");
        PsimiTabWriter writer = new PsimiTabWriter();
        writer.setBinaryInteractionClass( IntActBinaryInteraction.class );
        writer.setColumnHandler( new IntActColumnHandler() );
        writer.write( bis, file );
    }

 /*
    @Test
    public void convertIntact2TabTest_1() throws Exception {

        Intact2Tab i2t = new Intact2Tab();
        i2t.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        i2t.setBinaryInteractionClass( IntActBinaryInteraction.class );
        i2t.setBinaryInteractionHandler( new IntactBinaryInteractionHandler() );

        Collection<Interaction> interactions = new ArrayList<Interaction>();

        Component baitComponent = getMockBuilder().createComponentBait( getMockBuilder().createProteinRandom() );
        baitComponent.getInteractor().setAc( "bait1" );
        Component preyComponent1 = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );
        preyComponent1.getInteractor().setAc( "prey1" );
        Component preyComponent2 = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );
        preyComponent2.getInteractor().setAc( "prey2" );

        Interaction interaction1 = getMockBuilder().createInteraction( baitComponent, preyComponent1, preyComponent2 );
        interaction1.setAc( "EBI-1" );
        interactions.add( interaction1 );

        Collection<BinaryInteraction> bis = i2t.convert( interactions );
        assertNotNull(bis);
        assertEquals( 2, bis.size() );

        File file = new File( TestHelper.getTargetDirectory(), "test1.txt");
        PsimiTabWriter writer = new PsimiTabWriter();
        writer.setBinaryInteractionClass( IntActBinaryInteraction.class );
        writer.setColumnHandler( new IntActColumnHandler() );
        writer.write( bis, file );
    }

    @Test
    public void convertIntact2TabTest_2() throws Exception {

        Intact2Tab i2t = new Intact2Tab();
        i2t.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        i2t.setBinaryInteractionClass( IntActBinaryInteraction.class );
        i2t.setBinaryInteractionHandler( new IntactBinaryInteractionHandler() );

        Collection<Interaction> interactions = new ArrayList<Interaction>();

        Component baitComponent = getMockBuilder().createComponentBait( getMockBuilder().createProteinRandom() );
        baitComponent.getInteractor().setAc( "baitA" );
        Component preyComponent1 = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );
        preyComponent1.getInteractor().setAc( "preyA" );

        Interaction interaction2 = getMockBuilder().createInteraction( baitComponent, preyComponent1 );
        interaction2.setAc( "EBI-2" );
        interactions.add( interaction2 );

        Collection<BinaryInteraction> bis = i2t.convert( interactions );
        assertNotNull(bis);
        assertEquals( 1, bis.size() );

        File file = new File( TestHelper.getTargetDirectory(), "test2.txt");
        PsimiTabWriter writer = new PsimiTabWriter();
        writer.setBinaryInteractionClass( IntActBinaryInteraction.class );
        writer.setColumnHandler( new IntActColumnHandler() );
        writer.write( bis, file );
    }

    @Test
    public void convertIntact2TabTest_3() throws Exception {

        Intact2Tab i2t = new Intact2Tab();
        i2t.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        i2t.setBinaryInteractionClass( IntActBinaryInteraction.class );
        i2t.setBinaryInteractionHandler( new IntactBinaryInteractionHandler() );

        Collection<Interaction> interactions = new ArrayList<Interaction>();

        Interaction interaction3 = getMockBuilder().createInteraction( "neutral1", "neutral2", "neutral3" );
        int i = 1;
        for ( Component component : interaction3.getComponents() ) {
            component.getInteractor().setAc( "neutral" + i );
            i++;
        }
        interaction3.setAc( "EBI-3" );
        interactions.add( interaction3 );

        Collection<BinaryInteraction> bis = i2t.convert( interactions );
        assertNotNull(bis);
        assertEquals( 2, bis.size() );

        File file = new File( TestHelper.getTargetDirectory(), "test3.txt");
        PsimiTabWriter writer = new PsimiTabWriter();
        writer.setBinaryInteractionClass( IntActBinaryInteraction.class );
        writer.setColumnHandler( new IntActColumnHandler() );
        writer.write( bis, file );
    }
*/

}
