package uk.ac.ebi.intact.dataexchange.uniprotexport.factory;

import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.EncoreInteractionToCCLine1Converter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.EncoreInteractionToCCLine2Converter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.EncoreInteractionToCCLineConverter;

/**
 * Factory for the CC line converters
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public class CCConverterFactory {

    public static EncoreInteractionToCCLineConverter createCCConverter(int version){
         switch (version) {
            case 1:
                return new EncoreInteractionToCCLine1Converter();
            case 2:
                return new EncoreInteractionToCCLine2Converter();
            default:
                return null;
        }
    }
}
