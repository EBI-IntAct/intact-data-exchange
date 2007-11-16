package uk.ac.ebi.intact.psimitab.converters.expansion;

import static org.junit.Assert.*;
import org.junit.Test;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvExperimentalRole;
import uk.ac.ebi.intact.model.Interaction;

import java.util.Collection;
import java.util.Iterator;

/**
 * MatrixExpansion Tester.
 *
 * @author Nadin Neuhauser
 * @since <pre>11/16/2007</pre>
 * @version 1.0
 */
public class MatrixExpansionTest extends IntactBasicTestCase {

    @Test
    public void expandTest_1() {

        Component baitComponent = getMockBuilder().createComponentBait( getMockBuilder().createProteinRandom() );
        Component preyComponent1 = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );
        Component preyComponent2 = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );

        Interaction interaction = getMockBuilder().createInteraction( baitComponent, preyComponent1, preyComponent2 );
        MatrixExpansion matrixExpansion = new MatrixExpansion();
        Collection<Interaction> interactions = matrixExpansion.expand( interaction );
        assertNotNull( interactions );
        assertEquals( 3, interactions.size() );

        for ( Interaction newInteraction : interactions ){
            Collection<Component> components = newInteraction.getComponents();
            assertEquals(2, components.size());
        }
    }

    @Test
    public void expandTest_2() {

        Component baitComponent = getMockBuilder().createComponentBait( getMockBuilder().createProteinRandom() );
        Component preyComponent = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );

        Interaction interaction = getMockBuilder().createInteraction( baitComponent, preyComponent );
        MatrixExpansion matrixExpansion = new MatrixExpansion();
        Collection<Interaction> interactions = matrixExpansion.expand( interaction );
        assertNotNull( interactions );
        assertEquals( 1, interactions.size() );
    }

    @Test
    public void expandTest_3() {
        
        // relies on the fact that the created component have role: neutral
        Interaction interaction = getMockBuilder().createInteraction( "neutral1", "neutral2", "neutral3" );
        MatrixExpansion matrixExpansion = new MatrixExpansion();
        Collection<Interaction> interactions = matrixExpansion.expand( interaction );
        assertNotNull( interactions );
        assertEquals( 3, interactions.size() );
    }


    @Test
    public void getNameTest() throws Exception {
        MatrixExpansion matrixExpansion = new MatrixExpansion();
        assertEquals( "Matrix", matrixExpansion.getName() );
    }
}
