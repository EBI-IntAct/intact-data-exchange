package uk.ac.ebi.intact.util.uniprotExport.converters;

import uk.ac.ebi.enfin.mi.cluster.EncoreInteractionForScoring;
import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DRParameters;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

import java.util.Set;

/**
 * Interface for DR line converters
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public interface DRLineConverter {

    /**
     * Converts an interactor into a DR line
     * @param interactorAc
     * @param interactions : the interactions the interactor is involved in
     * @param context : the context of the cluster
     * @return the converted DRParameter
     * @throws java.io.IOException
     */
    public DRParameters convertInteractorIntoDRLine(String interactorAc, Set<EncoreInteractionForScoring> interactions, MiClusterContext context);
}
