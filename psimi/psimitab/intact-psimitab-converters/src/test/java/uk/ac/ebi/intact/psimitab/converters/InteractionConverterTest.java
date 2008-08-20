package uk.ac.ebi.intact.psimitab.converters;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.Assert;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.ComponentParameter;
import uk.ac.ebi.intact.model.InteractionParameter;
import uk.ac.ebi.intact.psimitab.model.Parameter;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/**
 * InteractionConverter Tester.
 *
 * @author Nadin Neuhauser
 * @version 1.0
 * @since <pre>11/12/2007</pre>
 */
public class InteractionConverterTest extends IntactBasicTestCase {

    @Test
    public void convertToMitab() throws Exception {
        InteractionConverter interactionConverter = new InteractionConverter();

        final Interaction interaction = getMockBuilder().createInteractionRandomBinary();
        interaction.setAc( "EBI-zzzzzzz" );
        Iterator<Component> i = interaction.getComponents().iterator();
        i.next().getInteractor().setAc( "EBI-xxxxxxx" );
        i.next().getInteractor().setAc( "EBI-yyyyyyy" );

        BinaryInteraction bi = interactionConverter.toBinaryInteraction( interaction );

        assertNotNull( bi );
    }

    @Test
    public void convertToIntactMitab() throws Exception {
        InteractionConverter interactionConverter = new InteractionConverter();

        final Interaction interaction = getMockBuilder().createInteractionRandomBinary();
        Iterator<Component> i = interaction.getComponents().iterator();
        i.next().getInteractor().setAc( "EBI-xxxxxxx" );
        i.next().getInteractor().setAc( "EBI-yyyyyyy" );

        BinaryInteraction bi = interactionConverter.toBinaryInteraction( interaction );

        assertNotNull( bi );

    }

    @Test
    public void parameterTest() throws Exception {
        InteractionConverter interactionConverter = new InteractionConverter();

        final Interaction interaction = getMockBuilder().createInteractionRandomBinary();
        Iterator<Component> i = interaction.getComponents().iterator();
        i.next().getInteractor().setAc( "EBI-xxxxxxx" );
        i.next().getInteractor().setAc( "EBI-yyyyyyy" );

        BinaryInteraction bi = interactionConverter.toBinaryInteraction( interaction );

        assertNotNull( bi );
        final Collection<Component> components = interaction.getComponents();
        if ( components.size() != 2 ) {
            throw new IllegalArgumentException( "We only convert binary interaction (2 components or a single with stoichiometry 2)" );
        }
        Iterator<Component> iterator = components.iterator();
        uk.ac.ebi.intact.model.Interactor intactInteractorA = iterator.next().getInteractor();
        uk.ac.ebi.intact.model.Interactor intactInteractorB = iterator.next().getInteractor();

        //set parameters
        List<Parameter> parametersA = new ArrayList<Parameter>();
        List<uk.ac.ebi.intact.psimitab.model.Parameter> parametersB = new ArrayList<uk.ac.ebi.intact.psimitab.model.Parameter>();
        List<uk.ac.ebi.intact.psimitab.model.Parameter> parametersInteraction = new ArrayList<uk.ac.ebi.intact.psimitab.model.Parameter>();

        for ( Component component : interaction.getComponents() ) {
            if ( component.getInteractor().equals( intactInteractorA ) ) {

                //parameters  for InteractorA
                for ( ComponentParameter componentParameterA : component.getParameters() ) {
                    uk.ac.ebi.intact.psimitab.model.Parameter parameterA = interactionConverter.getParameter( componentParameterA );
                    if ( parameterA != null ) {
                        parametersA.add( parameterA );
                    }
                }

            }
            if ( component.getInteractor().equals( intactInteractorB ) ) {

                //paramters for InteractorB
                for ( ComponentParameter componentParameterB : component.getParameters() ) {
                    uk.ac.ebi.intact.psimitab.model.Parameter parameterB = interactionConverter.getParameter( componentParameterB );
                    if ( parameterB != null ) {
                        parametersB.add( parameterB );
                    }
                }

            }
        }//end for

        //for ParametersInterection -- but it is empty
        if ( interaction.getParameters() != null ) {
            for ( InteractionParameter interactionParameter : interaction.getParameters() ) {
                uk.ac.ebi.intact.psimitab.model.Parameter parameterInteraction = interactionConverter.getParameter( interactionParameter );
                if ( parameterInteraction != null ) {
                    parametersInteraction.add( parameterInteraction );
                }
            }

        }

        //check Parameter A
        Assert.assertEquals( "temperature", parametersA.iterator().next().getType() );
        Assert.assertEquals( "302.0E0", parametersA.iterator().next().getValue() );
        Assert.assertEquals( "kelvin", parametersA.iterator().next().getUnit() );

        //check Parameter B
        Assert.assertEquals( "temperature", parametersB.iterator().next().getType() );
        Assert.assertEquals( "302.0E0", parametersB.iterator().next().getValue() );
        Assert.assertEquals( "kelvin", parametersB.iterator().next().getUnit() );

        //check ParametersInteraction
        Assert.assertEquals(0,parametersInteraction.size());



    }//end method
}