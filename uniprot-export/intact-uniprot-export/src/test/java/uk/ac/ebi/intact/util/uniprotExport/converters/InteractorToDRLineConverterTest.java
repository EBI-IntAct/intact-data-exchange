package uk.ac.ebi.intact.util.uniprotExport.converters;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.util.uniprotExport.converters.InteractorToDRLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DRParameters;

/**
 * Tester of the DR line converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/02/11</pre>
 */

public class InteractorToDRLineConverterTest{

    @Test
    public void test_dr_convert_ok(){
        InteractorToDRLineConverter converter = new InteractorToDRLineConverter();

        DRParameters parameters = converter.convertInteractorToDRLine("EBI-xxxxx", 3);
        Assert.assertNotNull(parameters);
        Assert.assertEquals("EBI-xxxxx", parameters.getUniprotAc());
        Assert.assertEquals(3, parameters.getNumberOfInteractions());
    }

    @Test
    public void test_dr_convert_null(){
        InteractorToDRLineConverter converter = new InteractorToDRLineConverter();

        DRParameters parameters = converter.convertInteractorToDRLine(null, 3);
        Assert.assertNull(parameters);
    }
}
