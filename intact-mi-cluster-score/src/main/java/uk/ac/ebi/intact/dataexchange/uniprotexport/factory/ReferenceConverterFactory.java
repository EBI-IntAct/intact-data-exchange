package uk.ac.ebi.intact.dataexchange.uniprotexport.factory;

import uk.ac.ebi.intact.util.uniprotExport.converters.ReferenceLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.ReferenceLineConverter1;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ReferenceConverterFactory {

    public static ReferenceLineConverter createReferenceLineConverter(int version) {
        switch (version) {
            case 1:
                return new ReferenceLineConverter1();
            default:
                return null;
        }
    }
}
