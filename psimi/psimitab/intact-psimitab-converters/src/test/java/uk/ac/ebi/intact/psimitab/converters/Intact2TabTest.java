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
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabWriter;

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

        Intact2BinaryInteractionConverter i2t = new Intact2BinaryInteractionConverter();

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
        assertNotNull( bis );

        File file = new File( TestHelper.getTargetDirectory(), "test_1.txt" );
        PsimiTabWriter writer = new PsimiTabWriter();
        writer.write( bis, file );
    }

    @Test
    public void convertIntact2TabTest_IntactBinaryInteraction() throws Exception {

        Intact2BinaryInteractionConverter i2t = new Intact2BinaryInteractionConverter();
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
        assertNotNull( bis );

        File file = new File( TestHelper.getTargetDirectory(), "test_2.txt" );
        PsimiTabWriter writer = new IntactPsimiTabWriter();
        writer.write( bis, file );
    }

    @Test
    public void convertIntact2TabTest_PostProcessorStrategy() throws Exception {

        Intact2BinaryInteractionConverter i2t = new Intact2BinaryInteractionConverter();

        Collection<Interaction> interactions = new ArrayList<Interaction>();

        Component baitComponent = getMockBuilder().createComponentBait( getMockBuilder().createProteinRandom() );
        baitComponent.getInteractor().setAc( "EBI-A");
        Component preyComponent = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );
        preyComponent.getInteractor().setAc( "EBI-A");

        Interaction interaction_1 = getMockBuilder().createInteraction( baitComponent, preyComponent );
        interaction_1.setAc( "EBI-1" );
        interactions.add(interaction_1);

        Interaction interaction_2 = getMockBuilder().createInteraction( baitComponent, preyComponent );
        interaction_2.setAc( "EBI-2" );
        interactions.add(interaction_2);

        assertEquals( 2, interactions.size() );

        Collection<BinaryInteraction> bis = i2t.convert( interactions );
        assertNotNull( bis );

        File file = new File( TestHelper.getTargetDirectory(), "test_3.txt" );
        PsimiTabWriter writer = new IntactPsimiTabWriter();
        writer.write( bis, file );
    }
}