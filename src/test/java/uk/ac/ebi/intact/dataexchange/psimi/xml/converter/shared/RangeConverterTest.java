package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import org.junit.Test;
import org.junit.Assert;
import psidev.psi.mi.xml.model.*;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.PsiConversionException;

/**
 * RangeConverter Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.0
 */
public class RangeConverterTest {

    @Test
    public void psiToIntact_position() throws Exception {
        psidev.psi.mi.xml.model.Range psiObject = new Range( buildRangeStatus( "certain", "MI:0001" ),
                                                             new Position( 1 ),
                                                             buildRangeStatus( "certain", "MI:0001" ),
                                                             new Position( 2 ));

        final RangeConverter converter = new RangeConverter(new Institution());

        final uk.ac.ebi.intact.model.Range intactObject = converter.psiToIntact( psiObject );
        Assert.assertNotNull( intactObject );
        Assert.assertEquals( 1, intactObject.getFromIntervalStart() );
        Assert.assertEquals( 1, intactObject.getFromIntervalEnd() );
        Assert.assertEquals( 2, intactObject.getToIntervalStart() );
        Assert.assertEquals( 2, intactObject.getToIntervalEnd() );
    }

    @Test
    public void psiToIntact_interval_1() throws Exception {
        psidev.psi.mi.xml.model.Range psiObject = new Range( new Interval(2, 2),
                                                             new Interval(1, 1),
                                                             buildRangeStatus( "certain", "MI:0001" ),
                                                             buildRangeStatus( "certain", "MI:0001" ));

        final RangeConverter converter = new RangeConverter(new Institution());

        final uk.ac.ebi.intact.model.Range intactObject = converter.psiToIntact( psiObject );
        Assert.assertNotNull( intactObject );
        Assert.assertEquals( 1, intactObject.getFromIntervalStart() );
        Assert.assertEquals( 1, intactObject.getFromIntervalEnd() );
        Assert.assertEquals( 2, intactObject.getToIntervalStart() );
        Assert.assertEquals( 2, intactObject.getToIntervalEnd() );
    }

    @Test
    public void psiToIntact_interval_diff() throws Exception {
        psidev.psi.mi.xml.model.Range psiObject = new Range( new Interval(3, 4),
                                                             new Interval(1, 2),
                                                             buildRangeStatus( "range", "MI:0338" ),
                                                             buildRangeStatus( "range", "MI:0338" ));

        final RangeConverter converter = new RangeConverter(new Institution());

        final uk.ac.ebi.intact.model.Range intactObject = converter.psiToIntact( psiObject );
        Assert.assertNotNull( intactObject );
        Assert.assertEquals( 1, intactObject.getFromIntervalStart() );
        Assert.assertEquals( 2, intactObject.getFromIntervalEnd() );
        Assert.assertEquals( 3, intactObject.getToIntervalStart() );
        Assert.assertEquals( 4, intactObject.getToIntervalEnd() );
    }

    @Test
    public void psiToIntact_interval_position() throws Exception {
        psidev.psi.mi.xml.model.Range psiObject = new Range( buildRangeStatus( "range", "MI:0338" ),
                                                             new Interval(2,3),
                                                             buildRangeStatus( "certain", "MI:0335" ),
                                                             new Position(4));

        final RangeConverter converter = new RangeConverter(new Institution());

        final uk.ac.ebi.intact.model.Range intactObject = converter.psiToIntact( psiObject );
        Assert.assertNotNull( intactObject );
        Assert.assertEquals( 2, intactObject.getFromIntervalStart() );
        Assert.assertEquals( 3, intactObject.getFromIntervalEnd() );
        Assert.assertEquals( 4, intactObject.getToIntervalStart() );
        Assert.assertEquals( 4, intactObject.getToIntervalEnd() );
    }

    @Test
    public void psiToIntact_position_interval() throws Exception {
        psidev.psi.mi.xml.model.Range psiObject = new Range( buildRangeStatus( "certain", "MI:0335" ),
                                                             buildRangeStatus( "range", "MI:0338" ),
                                                             new Position(2),
                                                             new Interval(3,4));

        final RangeConverter converter = new RangeConverter(new Institution());

        final uk.ac.ebi.intact.model.Range intactObject = converter.psiToIntact( psiObject );
        Assert.assertNotNull( intactObject );
        Assert.assertEquals( 2, intactObject.getFromIntervalStart() );
        Assert.assertEquals( 2, intactObject.getFromIntervalEnd() );
        Assert.assertEquals( 3, intactObject.getToIntervalStart() );
        Assert.assertEquals( 4, intactObject.getToIntervalEnd() );
    }

    @Test
    public void intactToPsi() throws Exception {
    }

    private RangeStatus buildRangeStatus( String label, String miRef ) {
        final RangeStatus status = new RangeStatus();
        status.setNames( new Names() );
        status.getNames().setShortLabel( label );
        status.setXref( new Xref( ) );
        status.getXref().setPrimaryRef( new DbReference( "psi-mi","MI:0488",miRef,"identity","MI:0356" ) );
        return status;
    }
}
