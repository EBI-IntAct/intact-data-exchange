package uk.ac.ebi.intact.psimitab.converters.expansion;

import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvExperimentalRole;
import uk.ac.ebi.intact.model.Interaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
    @DirtiesContext
    public void expandTest_1() throws Exception{

        Component baitComponent = getMockBuilder().createComponentBait( getMockBuilder().createProteinRandom() );
        Component preyComponent1 = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );
        Component preyComponent2 = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );

        Interaction interaction = getMockBuilder().createInteraction( baitComponent, preyComponent1, preyComponent2 );
        SpokeExpansion spokeExpansion = new SpokeExpansion();
        Collection<Interaction> interactions = spokeExpansion.expand( interaction );
        assertNotNull( interactions );
        assertEquals( 2, interactions.size() );

        for ( Interaction newInteraction : interactions ){
            Collection<Component> components = newInteraction.getComponents();
            assertEquals(2, components.size());
            Iterator<Component> iterator = components.iterator();
            boolean baitFound = false, preyFound = false;
            while (iterator.hasNext()) {
                String role = iterator.next().getCvExperimentalRole().getShortLabel();
                if (role.equals( CvExperimentalRole.BAIT)) baitFound = true;
                if (role.equals( CvExperimentalRole.PREY)) preyFound = true;
            }
            assertTrue( baitFound && preyFound);
        }
    }

    @Test
    @DirtiesContext
    public void expandTest_2() throws Exception{

        Component baitComponent = getMockBuilder().createComponentBait( getMockBuilder().createProteinRandom() );
        Component preyComponent = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );

        Interaction interaction = getMockBuilder().createInteraction( baitComponent, preyComponent );
        SpokeExpansion spokeExpansion = new SpokeExpansion();
        Collection<Interaction> interactions = spokeExpansion.expand( interaction );
        assertNotNull( interactions );
        assertEquals( 1, interactions.size() );
    }

    @Test (expected = NotExpandableInteractionException.class)
    @DirtiesContext
    public void expandTest_3() throws Exception{

        // relies on the fact that the created component have role: neutral
        Interaction interaction = getMockBuilder().createInteraction( "neutral1", "neutral2", "neutral3" );
        
        SpokeExpansion spokeExpansion = new SpokeExpansion();
        Collection<Interaction> interactions = spokeExpansion.expand( interaction );
    }

    @Test
    @DirtiesContext
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

        Collection<Interaction> interactions = spokeExpansion.expand( interaction );
        assertNotNull( interactions );
        assertEquals( 1, interactions.size() );
    }


    @Test
    @DirtiesContext
    public void getNameTest() throws Exception {
        SpokeExpansion spokeExpansion = new SpokeExpansion();
        assertEquals( "Spoke", spokeExpansion.getName() );
    }

}
