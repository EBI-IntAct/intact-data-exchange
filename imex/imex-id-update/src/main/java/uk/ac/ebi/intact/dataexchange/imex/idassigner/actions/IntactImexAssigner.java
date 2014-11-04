package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import psidev.psi.mi.jami.imex.actions.ImexAssigner;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.ImexCentralManager;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Interface for assigning IMEx id to a publication and updating intact publications, experiments and interactions
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/03/12</pre>
 */

public interface IntactImexAssigner extends ImexAssigner{

    /**
     * Copy the IMEx id to experiments of a publication if they don't have the IMEx primary reference. It will not add the IMEx primary reference if
     * there is already an IMEx primary reference to another IMEx id. It will clean up duplicated IMEx ids
     * @param expAcs : list of experiments acs
     * @param imexId
     * @param imexCentralManager ; to fire events if provided.
     * @param updatedExpAcs : the experiment acs which have been updated
     * @throws PublicationImexUpdaterException if IMEx id is null or imex conflict and no imexCentralManager was provided to fire an error event
     */
    public void assignImexIdentifierToExperiments(Collection<String> expAcs, String imexId, ImexCentralManager imexCentralManager, Set<String> updatedExpAcs) throws PublicationImexUpdaterException;

    /**
     * Collect all the interaction IMEx ids associated with this publication
     * @param intactPublication
     * @return a list of interaction IMEx ids associated with the publication
     */
    public List<String> collectExistingInteractionImexIdsForPublication(IntactPublication intactPublication);

    /**
     * Assign an IMEx id for the interactions if not already done. Does not assign IMEx ids to interactions already having a primary reference to another
     * IMEx id or interactions which are not PPI. It will clean up duplicated IMEx id and a IMEx primary reference which is invalid will become imex secondary. It will not update interactions having valid imex primary reference which are in conflict with publication imex primary reference
     * @param interactionAcs : list of interaction acs to update
     * @param imexId
     * @param imexCentralManager ; to fire events if provided.
     * @param updatedIntAcs : the updated interaction acs
     * @return a list of experiments which have been updated
     * @throws PublicationImexUpdaterException when IMEx id is null or imex conflict and no imexCentralManager was provided to fire an error event
     */
    public void assignImexIdentifierToInteractions(Collection<String> interactionAcs, String imexId, ImexCentralManager imexCentralManager, Set<String> updatedIntAcs) throws PublicationImexUpdaterException;

    /**
     * This method allows to clean cache methods to collect information about existing interaction IMEx ids attached to a publicatioon/interactions
     * @param pub
     * @param imexId
     */
    public void resetPublicationContext(IntactPublication pub, String imexId);

    /**
     * Collect experiment acs associated with this publication which need to be updated for this IMEx identifier
     * @param pub
     * @param imex
     * @return list if experiment acs needing to be updated
     */
    public List<String> collectExperimentsToUpdateFrom(IntactPublication pub, String imex);

    /**
     * Collect interaction acs associated with this publication which need to be updated for this IMEx identifier
     * @param pub
     * @param imex
     * @return list of interaction acs
     */
    public List<String> collectInteractionsToUpdateFrom(IntactPublication pub, String imex);
}
