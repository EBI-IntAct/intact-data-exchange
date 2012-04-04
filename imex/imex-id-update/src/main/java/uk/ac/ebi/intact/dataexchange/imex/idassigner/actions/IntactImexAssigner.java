package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import edu.ucla.mbi.imex.central.ws.v20.Publication;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;

import java.util.List;

/**
 * Interface for assigning IMEx id to a publication and updating intact publications, experiments and interactions
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/03/12</pre>
 */

public interface IntactImexAssigner {

    /**
     * Assign a new IMEx id to a publication and update the annotations of the publication (full coverage and imex curation). It adds an IMEx primary reference
     * to the intact publication. It is only possible to assign a new IMEx id to publications having valid pubmed ids (no unassigned and no DOI number)
     * @param intactPublication : the publication in IntAct
     * @param imexPublication : the publication in IMEx
     * @throws PublicationImexUpdaterException if IMEx central could not generate a valid IMEx id
     * @throws ImexCentralException if no record found or no IMEx id
     */
    public void assignImexIdentifier(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws PublicationImexUpdaterException, ImexCentralException;

    /**
     * Add a IMEx primary reference to the experiment
     * @param intactExperiment
     * @param imexId
     * @throws PublicationImexUpdaterException if the imexId is null
     */
    public void updateImexIdentifierForExperiment(Experiment intactExperiment, String imexId) throws PublicationImexUpdaterException;

    /**
     * Add a IMEx primary reference to the interaction
     * @param intactInteraction
     * @param imexId
     * @throws PublicationImexUpdaterException if the IMEx id is null
     */
    public void updateImexIdentifierForInteraction(Interaction intactInteraction, String imexId) throws PublicationImexUpdaterException;

    /**
     * Copy the IMEx id to all experiments of a publication if they don't have the IMEx primary reference. It will not add the IMEx primary reference if
     * there is already an IMEx primary reference to another IMEx id. It will clean up duplicated IMEx ids
     * @param intactPublication
     * @param imexId
     * @throws PublicationImexUpdaterException if IMEx id is null
     */
    public void updateImexIdentifiersForAllExperiments(uk.ac.ebi.intact.model.Publication intactPublication, String imexId) throws PublicationImexUpdaterException;

    /**
     * Collect all the interaction IMEx ids associated with this publication
     * @param intactPublication
     * @return a list of interaction IMEx ids associated with the publication
     */
    public List<String> collectExistingInteractionImexIdsForPublication(uk.ac.ebi.intact.model.Publication intactPublication);

    /**
     * Assign an IMEx id for all the interactions of a publication if not already done. Does not assign IMEx ids to interactions already having a primary reference to another
     * IMEx id or interactions which are not PPI. It will clean up duplicated IMEx id and a IMEx primary reference which is invalid will become imex secondary.
     * @param intactPublication
     * @param imexId
     * @throws PublicationImexUpdaterException when IMEx id is null
     */
    public void assignImexIdentifiersForAllInteractions(uk.ac.ebi.intact.model.Publication intactPublication, String imexId) throws PublicationImexUpdaterException;

    /**
     * Add imex curation and full coverage annotations if not already present
     * @param intactPublication
     */
    public void updatePublicationAnnotations(uk.ac.ebi.intact.model.Publication intactPublication);

    /**
     * Add or update imex primary ref in Intact publication
     * @param intactPublication
     * @param imexPublication
     */
    public void updateImexPrimaryRef(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication);
}
