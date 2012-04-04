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
     * Assign a new IMEx id to a publication and update the annotations of the publication (full coverage and imex curation).
     * It is only possible to assign a new IMEx id to publications having valid pubmed ids (no unassigned and no DOI number)
     * @param intactPublication : the publication in IntAct
     * @param imexPublication : the publication in IMEx
     * @throws PublicationImexUpdaterException if IMEx central could not generate a valid IMEx id
     * @throws ImexCentralException
     */
    public void assignImexIdentifier(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws PublicationImexUpdaterException, ImexCentralException;

    /**
     * Add a IMEx primary reference to the experiment
     * @param intactExperiment
     * @param imexId
     * @throws PublicationImexUpdaterException
     */
    public void updateImexIdentifierForExperiment(Experiment intactExperiment, String imexId) throws PublicationImexUpdaterException;

    /**
     * Add a IMEx primary reference to the interaction
     * @param intactInteraction
     * @param imexId
     * @throws PublicationImexUpdaterException
     */
    public void updateImexIdentifierForInteraction(Interaction intactInteraction, String imexId) throws PublicationImexUpdaterException;

    /**
     * Copy the IMEx id to all experiments of a publication and checks if any conflicts in IntAct
     * @param intactPublication
     * @param imexId
     * @throws PublicationImexUpdaterException
     */
    public void updateImexIdentifiersForAllExperiments(uk.ac.ebi.intact.model.Publication intactPublication, String imexId) throws PublicationImexUpdaterException;

    /**
     *
     * @param intactPublication
     * @return a list of interaction IMEx ids associated with the publication
     */
    public List<String> collectExistingInteractionImexIdsForPublication(uk.ac.ebi.intact.model.Publication intactPublication);

    /**
     * Assign an IMEx id for all the interactions of a publication if not already done. Checks if any conflicts in Intact
     * @param intactPublication
     * @param imexId
     * @throws PublicationImexUpdaterException
     */
    public void assignImexIdentifiersForAllInteractions(uk.ac.ebi.intact.model.Publication intactPublication, String imexId) throws PublicationImexUpdaterException;

    /**
     * Add imex curation and full coverage annotations if not alrteady there
     * @param intactPublication
     */
    public void updatePublicationAnnotations(uk.ac.ebi.intact.model.Publication intactPublication);

    /**
     * Add or update imex primary ref in Intact
     * @param intactPublication
     * @param imexPublication
     */
    public void updateImexPrimaryRef(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication);
}
