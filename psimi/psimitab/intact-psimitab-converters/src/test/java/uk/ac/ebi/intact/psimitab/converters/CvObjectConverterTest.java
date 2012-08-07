package uk.ac.ebi.intact.psimitab.converters;

import org.junit.Test;
import psidev.psi.mi.tab.model.*;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvInteraction;
import uk.ac.ebi.intact.model.CvInteractionType;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * CvObjectConverter Tester.
 *
 * @author Nadin Neuhauser
 * @version 1.0
 * @since <pre>11/13/2007</pre>
 */
public class CvObjectConverterTest extends IntactBasicTestCase {

    @Test
    public void convertToMitab_InteractionType() throws Exception {
        CvObjectConverter converter = new CvObjectConverter();

        CvInteractionType t = getMockBuilder().createCvObject( CvInteractionType.class, "MI:0407", "direct interaction" );
        CrossReference interactionType = converter.toCrossReference( t );
        assertNotNull( interactionType );
        assertEquals( "psi-mi", interactionType.getDatabase() );
        assertEquals( "MI:0407", interactionType.getIdentifier() );
        assertEquals( "direct interaction", interactionType.getText() );
    }

    @Test
    public void convertToMitab_InteractionDetectionMethod() throws Exception {
        CvObjectConverter converter = new CvObjectConverter();

        CvInteraction t = getMockBuilder().createCvObject( CvInteraction.class, "MI:0027", "cosedimentation" );

        CrossReference detectionMethod = converter.toCrossReference( t );
        assertNotNull( detectionMethod );
        assertEquals( "psi-mi", detectionMethod.getDatabase() );
        assertEquals( "MI:0027", detectionMethod.getIdentifier() );
        assertEquals( "cosedimentation", detectionMethod.getText() );
    }

    @Test
    public void convertToMitab_non_mi_detectionMethod() throws Exception {
        CvObjectConverter converter = new CvObjectConverter();

        CvInteraction t = CvObjectUtils.createCvObject(getMockBuilder().getInstitution(), CvInteraction.class, "MOD:00001", "test-mod");
        t.getXrefs().iterator().next().setCvDatabase(getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.PSI_MOD_MI_REF, CvDatabase.PSI_MOD));

        CrossReference detectionMethod = converter.toCrossReference( t );
        assertNotNull( detectionMethod );
        assertEquals( "psi-mod", detectionMethod.getDatabase() );
        assertEquals( "MOD:00001", detectionMethod.getIdentifier() );
        assertEquals( "test-mod", detectionMethod.getText() );
    }

}
