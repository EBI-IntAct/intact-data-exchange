package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters;

import java.util.List;

/**
 * Interface for GO line converters
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public interface EncoreInteractionToGoLineConverter {

    /**
     *
     * @param interaction : the encore interaction to convert into a single GO line
     * @param firstInteractor : the first interactor of the interaction
     * @return a GOParameters with the information provided by the interaction
     */
    public GOParameters convertInteractionIntoGOParameters(EncoreInteraction interaction, String firstInteractor);

    /**
     *
     * @param interactions : the encore interactions to convert into a single GO line
     * @param parentAc : the master uniprot ac of the first interactor shared by all the interactions
     * @return a clustered list of GOParameters with the information provided by all the interactions. Must merge feature chains with the master protein
     * and demerge the isoforms (one go parameter per master protein/feature chains and one go parameter for each isoforms)
     */
    public List<GOParameters> convertInteractionsIntoGOParameters(List<EncoreInteraction> interactions, String parentAc);

}
