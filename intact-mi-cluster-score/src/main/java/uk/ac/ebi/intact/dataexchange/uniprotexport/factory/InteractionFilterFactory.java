package uk.ac.ebi.intact.dataexchange.uniprotexport.factory;

import uk.ac.ebi.intact.dataexchange.uniprotexport.variables.InteractionSource;
import uk.ac.ebi.intact.util.uniprotExport.miscore.exporter.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.filter.IntactFilter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.filter.InteractionFilter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.filter.MitabFilter;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/02/11</pre>
 */

public class InteractionFilterFactory {

    public static InteractionFilter createInteractionFilter(InteractionSource source, InteractionExporter exporter){

        switch (source) {
            case intact:
                return new IntactFilter(exporter);
            case mitab:
                return new MitabFilter(exporter);
            default:
                return null;
        }
    }

    public static InteractionSource convertIntoInteractionSourceName(String source){

        if (source == null){
            return InteractionSource.none;
        }
        else if (InteractionSource.intact.toString().equalsIgnoreCase(source)){
            return InteractionSource.intact;
        }
        else if (InteractionSource.mitab.toString().equalsIgnoreCase(source)){
            return InteractionSource.mitab;
        }
        else {
            return InteractionSource.none;
        }
    }
}
