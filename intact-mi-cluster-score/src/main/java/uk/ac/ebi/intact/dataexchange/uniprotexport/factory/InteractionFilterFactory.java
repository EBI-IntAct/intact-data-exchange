package uk.ac.ebi.intact.dataexchange.uniprotexport.factory;

import uk.ac.ebi.intact.dataexchange.uniprotexport.variables.InteractionSource;
import uk.ac.ebi.intact.util.uniprotExport.exporters.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.filters.IntactFilter;
import uk.ac.ebi.intact.util.uniprotExport.filters.InteractionFilter;
import uk.ac.ebi.intact.util.uniprotExport.filters.mitab.NonClusteredMitabFilter;

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
                return new NonClusteredMitabFilter(exporter);
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
