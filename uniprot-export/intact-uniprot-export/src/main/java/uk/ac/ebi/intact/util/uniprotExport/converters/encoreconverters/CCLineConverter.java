package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import uk.ac.ebi.enfin.mi.cluster.EncoreInteractionForScoring;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

import java.util.Set;

/**
 * Interface for Encore interaction to CC line converters
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public interface CCLineConverter {

    /**
     * Convert the list of positive and negative encore interactions into a single CCParameter
     * @param positiveInteractions : contains a list of positive encore interactions attached to a same first interactor
     * @param negativeInteractions : contains a list of negative encore interactions attached to a same first interactor
     * @param context : context of the clustering
     * @param firstInteractor : the first interactor which should be in all the encore interactions
     * @return a CCParameter containing the information of all interactions attached to a same interactor
     */
    public CCParameters convertPositiveAndNegativeInteractionsIntoCCLines(Set<EncoreInteractionForScoring> positiveInteractions, Set<EncoreInteractionForScoring> negativeInteractions, MiClusterContext context, String firstInteractor);

    /**
     * Convert the list of positive encore interactions into a single CCParameter
     * @param positiveInteractions : contains a list of positive encore interactions attached to a same first interactor
     * @param context : context of the clustering
     * @param masterUniprot : the master uniprot ac which should be in all the encore interactions
     * @return a CCParameter containing the information of all positove interactions attached to a same interactor
     */
    public CCParameters convertInteractionsIntoCCLines(Set<EncoreInteractionForScoring> positiveInteractions, MiClusterContext context, String masterUniprot);
}
