package uk.ac.ebi.intact.util.uniprotExport.converters;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DRParameters;

/**
 * Tester of the DR line converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/02/11</pre>
 */

public class DRLineConverter1Test {

    @Test
    public void test_dr_convert_ok(){
        DRLineConverter1 converter = new DRLineConverter1();

        DRParameters parameters = converter.convertInteractorIntoDRLine("EBI-xxxxx", 3);
        Assert.assertNotNull(parameters);
        Assert.assertEquals("EBI-xxxxx", parameters.getUniprotAc());
        Assert.assertEquals(3, parameters.getNumberOfInteractions());
    }

    @Test
    public void test_dr_convert_null(){
        DRLineConverter1 converter = new DRLineConverter1();

        DRParameters parameters = converter.convertInteractorIntoDRLine(null, 3);
        Assert.assertNull(parameters);
    }
}
