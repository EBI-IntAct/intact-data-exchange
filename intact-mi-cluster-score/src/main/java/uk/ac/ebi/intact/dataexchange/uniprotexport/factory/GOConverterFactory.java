package uk.ac.ebi.intact.dataexchange.uniprotexport.factory;

import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.EncoreInteractionToGoLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.EncoreInteractionToGoLineConverter1;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.EncoreInteractionToGoLineConverter2;

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
                return new EncoreInteractionToGoLineConverter1();
             case 2:
                 return new EncoreInteractionToGoLineConverter2();
            default:
                return null;
        }
    }
}
