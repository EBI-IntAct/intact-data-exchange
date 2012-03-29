package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import edu.ucla.mbi.imex.central.ws.v20.Publication;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;

/**
 * Interface for assigning IMEx id to a publication and updating intact publications, experiments and interactions
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/03/12</pre>
 */

public interface IntactImexAssigner {

    /**
     * Assign a new IMEx id to a publication and update the intact publication
     * @param intactPublication
     * @param imexPublication
     * @throws PublicationImexUpdaterException
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

}
