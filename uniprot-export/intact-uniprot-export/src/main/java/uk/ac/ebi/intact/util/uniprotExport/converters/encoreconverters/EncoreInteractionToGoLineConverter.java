package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import uk.ac.ebi.enfin.mi.cluster.EncoreInteractionForScoring;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

import java.util.List;
import java.util.Set;

/**
 * Interface for GO line converters
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public interface EncoreInteractionToGoLineConverter<T extends GOParameters> {

    /**
     *
     * @param interaction : the encore interaction to convert into a single GO line
     * @param firstInteractor : the first interactor of the interaction
     * @param context : the context of the cluster
     * @return a GOParameters with the information provided by the interaction
     */
    public T convertInteractionIntoGOParameters(EncoreInteractionForScoring interaction, String firstInteractor, MiClusterContext context);

    /**
     *
     * @param interactions : the encore interactions to convert into a single GO line
     * @param parentAc : the master uniprot ac of the first interactor shared by all the interactions
     * @param context : the context of the cluster
     * @return a clustered list of GOParameters with the information provided by all the interactions. Must merge feature chains with the master protein
     * and demerge the isoforms (one go parameter per master protein/feature chains and one go parameter for each isoforms)
     */
    public List<T> convertInteractionsIntoGOParameters(Set<EncoreInteractionForScoring> interactions, String parentAc, MiClusterContext context);

}
