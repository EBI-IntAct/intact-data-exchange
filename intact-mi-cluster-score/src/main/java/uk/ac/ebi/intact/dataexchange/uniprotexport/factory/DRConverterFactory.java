package uk.ac.ebi.intact.dataexchange.uniprotexport.factory;

import uk.ac.ebi.intact.util.uniprotExport.converters.DefaultInteractorToDRLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.InteractorToDRLineConverter;

/**
 * Factory for the DR line converters
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public class DRConverterFactory {

    public static InteractorToDRLineConverter createDRLineConverter(int version){
        switch (version) {
            case 1:
                return new DefaultInteractorToDRLineConverter();
            default:
                return null;
        }
    }
}
