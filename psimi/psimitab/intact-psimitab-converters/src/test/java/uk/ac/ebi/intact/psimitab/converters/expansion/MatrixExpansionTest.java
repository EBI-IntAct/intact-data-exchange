package uk.ac.ebi.intact.psimitab.converters.expansion;

import junit.framework.Assert;
import org.junit.Test;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.Interaction;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * MatrixExpansion Tester.
 *
 * @author Nadin Neuhauser
 * @since <pre>11/16/2007</pre>
 * @version 1.0
 */
public class MatrixExpansionTest extends IntactBasicTestCase {

    @Test
    public void expandTest_1() throws NotExpandableInteractionException {

        Component baitComponent = getMockBuilder().createComponentBait( getMockBuilder().createProteinRandom() );
        Component preyComponent1 = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );
        Component preyComponent2 = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );

        Interaction interaction = getMockBuilder().createInteraction( baitComponent, preyComponent1, preyComponent2 );
        MatrixExpansion matrixExpansion = new MatrixExpansion();

        Collection<BinaryInteraction> interactions = matrixExpansion.expand( interaction );
        assertNotNull( interactions );
        assertEquals( 3, interactions.size() );

        for ( BinaryInteraction newInteraction : interactions ){
            Assert.assertNotNull(newInteraction.getInteractorA());
            Assert.assertNotNull(newInteraction.getInteractorB());
        }
    }

    @Test
    public void expandTest_2() throws NotExpandableInteractionException {

        Component baitComponent = getMockBuilder().createComponentBait( getMockBuilder().createProteinRandom() );
        Component preyComponent = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );

        Interaction interaction = getMockBuilder().createInteraction( baitComponent, preyComponent );
        MatrixExpansion matrixExpansion = new MatrixExpansion();
        Collection<BinaryInteraction> interactions = matrixExpansion.expand( interaction );
        assertNotNull( interactions );
        assertEquals( 1, interactions.size() );
    }

    @Test
    public void expandTest_3() throws NotExpandableInteractionException {
        
        // relies on the fact that the created component have role: neutral
        Interaction interaction = getMockBuilder().createInteraction( "neutral1", "neutral2", "neutral3" );
        MatrixExpansion matrixExpansion = new MatrixExpansion();
        Collection<BinaryInteraction> interactions = matrixExpansion.expand( interaction );
        assertNotNull( interactions );
        assertEquals( 3, interactions.size() );
    }


    @Test
    public void getNameTest() throws Exception {
        MatrixExpansion matrixExpansion = new MatrixExpansion();
        assertEquals( "matrix expansion", matrixExpansion.getName() );
    }
}
