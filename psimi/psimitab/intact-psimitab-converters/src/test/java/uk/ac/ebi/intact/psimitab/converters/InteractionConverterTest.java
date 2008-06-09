package uk.ac.ebi.intact.psimitab.converters;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.psimitab.IntActBinaryInteraction;

import java.util.Iterator;

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
        interactionConverter.setBinaryInteractionClass( IntActBinaryInteraction.class );
        interactionConverter.setBinaryInteractionHandler( new IntactBinaryInteractionHandler() );

        final Interaction interaction = getMockBuilder().createInteractionRandomBinary();
        Iterator<Component> i = interaction.getComponents().iterator();
        i.next().getInteractor().setAc( "EBI-xxxxxxx" );
        i.next().getInteractor().setAc( "EBI-yyyyyyy" );

        BinaryInteraction bi = interactionConverter.toBinaryInteraction( interaction );

        assertNotNull( bi );

    }

}