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
 * SpokeWithoutBaitExpansion Tester.
 *
 * @author Nadin Neuhauser
 * @since <pre>11/16/2007</pre>
 * @version 1.0
 */
public class SpokeWithoutBaitExpansionTest extends IntactBasicTestCase {


    @Test
    public void expandTest_1() {

        Component baitComponent = getMockBuilder().createComponentBait( getMockBuilder().createProteinRandom() );
        Component preyComponent1 = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );
        Component preyComponent2 = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );

        Interaction interaction = getMockBuilder().createInteraction( baitComponent, preyComponent1, preyComponent2 );
        SpokeWithoutBaitExpansion spokeWithoutBaitExpansion = new SpokeWithoutBaitExpansion();
        Collection<Interaction> interactions = spokeWithoutBaitExpansion.expand( interaction );
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
    public void expandTest_2() {

        Component baitComponent = getMockBuilder().createComponentBait( getMockBuilder().createProteinRandom() );
        Component preyComponent = getMockBuilder().createComponentPrey( getMockBuilder().createProteinRandom() );

        Interaction interaction = getMockBuilder().createInteraction( baitComponent, preyComponent );
        SpokeWithoutBaitExpansion spokeWithoutBaitExpansion = new SpokeWithoutBaitExpansion();
        Collection<Interaction> interactions = spokeWithoutBaitExpansion.expand( interaction );
        assertNotNull( interactions );
        assertEquals( 1, interactions.size() );
    }

    @Test
    public void expandTest_3() {

        // relies on the fact that the created component have role: neutral
        Interaction interaction = getMockBuilder().createInteraction( "neutral1", "neutral2", "neutral3" );
        SpokeWithoutBaitExpansion spokeWithoutBaitExpansion = new SpokeWithoutBaitExpansion();
        Collection<Interaction> interactions = spokeWithoutBaitExpansion.expand( interaction );
        assertNotNull( interactions );
        assertEquals( 2, interactions.size() );
    }

    @Test
    public void expandTest_4() {

        // generate a interaction with only one Component
        Component selfComponent = getMockBuilder().createComponentPrey(getMockBuilder().createProteinRandom());
        selfComponent.getCvExperimentalRole().setShortLabel(CvExperimentalRole.SELF);
        selfComponent.getCvExperimentalRole().setIdentifier(CvExperimentalRole.SELF_PSI_REF);
        selfComponent.setStoichiometry( 2 );

        Interaction interaction = getMockBuilder().createInteraction( selfComponent );

        SpokeWithoutBaitExpansion spokeWithoutBaitExpansion = new SpokeWithoutBaitExpansion();
        Collection<Interaction> interactions = spokeWithoutBaitExpansion.expand( interaction );
        assertNotNull( interactions );
        assertEquals( 1, interactions.size() );
    }

    @Test
    public void getNameTest() throws Exception {
        SpokeWithoutBaitExpansion spokeWithoutBaitExpansion = new SpokeWithoutBaitExpansion();
        assertEquals( "Spoke", spokeWithoutBaitExpansion.getName() );
    }
}
