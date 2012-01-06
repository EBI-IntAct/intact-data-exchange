package uk.ac.ebi.intact.dataexchange.uniprotexport.factory;

import uk.ac.ebi.intact.util.uniprotExport.converters.DRLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.DRLineConverter1;

/**
 * Factory for the DR line converters
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public class DRConverterFactory {

    public static DRLineConverter createDRLineConverter(int version){
        switch (version) {
            case 1:
                return new DRLineConverter1();
            default:
                return null;
        }
    }
}
