package uk.ac.ebi.intact.dataexchange.uniprotexport.factory;

import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.DefaultEncoreInteractionToGoLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.EncoreInteractionToGoLineConverter;

/**
 * Factory for the GO line converters
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public class GOConverterFactory {

    public static EncoreInteractionToGoLineConverter createGOConverter(int version){
         switch (version) {
            case 1:
                return new DefaultEncoreInteractionToGoLineConverter();
            default:
                return null;
        }
    }
}
