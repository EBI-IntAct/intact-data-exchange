package uk.ac.ebi.intact.dataexchange.uniprotexport.factory;

import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.GoLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.GoLineConverter1;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.GoLineConverter2;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters;

/**
 * Factory for the GO line converters
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public class GOConverterFactory {

    public static GoLineConverter<? extends GOParameters> createGOConverter(int version){
         switch (version) {
            case 1:
                return new GoLineConverter1();
             case 2:
                 return new GoLineConverter2();
            default:
                return null;
        }
    }
}
