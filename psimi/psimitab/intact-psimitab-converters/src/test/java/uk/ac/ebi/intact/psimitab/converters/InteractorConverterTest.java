package uk.ac.ebi.intact.psimitab.converters;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.Component;

/**
 * InteractorConverter Tester.
 *
 * @author Nadin Neuhauser
 * @version 1.0
 * @since <pre>11/12/2007</pre>
 */
public class InteractorConverterTest extends IntactBasicTestCase {


    @Test
    public void toMitabTest() {
        InteractorConverter converter = new InteractorConverter();

        final Component c = getMockBuilder().createComponentRandom();
        c.getInteractor().setAc( "EBI-xxxxxx" );

        Interactor interactor = converter.toMitab( c.getInteractor() );
        assertNotNull( interactor );

    }

}
