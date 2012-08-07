package uk.ac.ebi.intact.psimitab.converters.expansion;

import junit.framework.Assert;
import org.junit.Test;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvExperimentalRole;
import uk.ac.ebi.intact.model.Interaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * SpokeExpansion Tester.
 *
 * @author Nadin Neuhauser
 * @since <pre>11/16/2007</pre>
 * @version 1.0
 */
public class SpokeExpansionTest extends IntactBasicTestCase {
    @Test
    public void expandTest_1() throws Exception{

        Component baitComponent = getMockBuilder().createComponentBait( getMockBuilder().createProteinRandom() );
        Component preyComponent1 = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );
        Component preyComponent2 = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );

        Interaction interaction = getMockBuilder().createInteraction( baitComponent, preyComponent1, preyComponent2 );
        SpokeExpansion spokeExpansion = new SpokeExpansion();
        Collection<BinaryInteraction> interactions = spokeExpansion.expand( interaction );
        assertNotNull( interactions );
        assertEquals( 2, interactions.size() );

        for ( BinaryInteraction newInteraction : interactions ){
            Assert.assertNotNull(newInteraction.getInteractorA());
            Assert.assertNotNull(newInteraction.getInteractorB());
            boolean baitFound = false, preyFound = false;

            String roleA = newInteraction.getInteractorA().getExperimentalRoles().iterator().next().getText();
            if (roleA.equals( CvExperimentalRole.BAIT)) baitFound = true;
            if (roleA.equals( CvExperimentalRole.PREY)) preyFound = true;
            String roleB = newInteraction.getInteractorB().getExperimentalRoles().iterator().next().getText();
            if (roleB.equals( CvExperimentalRole.BAIT)) baitFound = true;
            if (roleB.equals( CvExperimentalRole.PREY)) preyFound = true;

            assertTrue( baitFound && preyFound);
        }
    }

    @Test
    public void expandTest_2() throws Exception{

        Component baitComponent = getMockBuilder().createComponentBait( getMockBuilder().createProteinRandom() );
        Component preyComponent = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );

        Interaction interaction = getMockBuilder().createInteraction( baitComponent, preyComponent );
        SpokeExpansion spokeExpansion = new SpokeExpansion();
        Collection<BinaryInteraction> interactions = spokeExpansion.expand( interaction );
        assertNotNull( interactions );
        assertEquals( 1, interactions.size() );
    }

    @Test (expected = NotExpandableInteractionException.class)
    public void expandTest_3() throws Exception{

        // relies on the fact that the created component have role: neutral
        Interaction interaction = getMockBuilder().createInteraction( "neutral1", "neutral2", "neutral3" );
        
        SpokeExpansion spokeExpansion = new SpokeExpansion();
        Collection<BinaryInteraction> interactions = spokeExpansion.expand( interaction );
    }

    @Test
    public void expandTest_4() throws Exception{

        Component selfComponent = getMockBuilder().createComponentPrey(getMockBuilder().createProteinRandom());
        selfComponent.getCvExperimentalRole().setShortLabel(CvExperimentalRole.SELF);
        selfComponent.getCvExperimentalRole().setIdentifier(CvExperimentalRole.SELF_PSI_REF);
        selfComponent.setStoichiometry( 2 );

        Interaction interaction = getMockBuilder().createInteractionRandomBinary( );

        for (Component component : interaction.getComponents() ){
            interaction.removeComponent(component);
        }
        List<Component> selfComponents = new ArrayList<Component>();
        selfComponents.add( selfComponent );
        interaction.setComponents( selfComponents );

        SpokeExpansion spokeExpansion = new SpokeExpansion();

        assertTrue(spokeExpansion.isExpandable(interaction));

        Collection<BinaryInteraction> interactions = spokeExpansion.expand( interaction );
        assertNotNull( interactions );
        assertEquals( 1, interactions.size() );
    }


    @Test
    public void getNameTest() throws Exception {
        SpokeExpansion spokeExpansion = new SpokeExpansion();
        assertEquals( "spoke expansion", spokeExpansion.getName() );
    }

}
