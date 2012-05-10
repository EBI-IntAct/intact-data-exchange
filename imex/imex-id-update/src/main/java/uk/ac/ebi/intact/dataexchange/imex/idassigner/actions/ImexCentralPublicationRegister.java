package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import edu.ucla.mbi.imex.central.ws.v20.Publication;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralClient;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;

/**
 * Interface for registering a publication in IMEx central and collect a publication record in IMEx central
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/03/12</pre>
 */

public interface ImexCentralPublicationRegister {

    /**
     * Retrieve an existing record in IMEx central matching the publication identifier (pubmed, doi, jint or imex).
     * Returns null if we cannot find a record in IMEx central for this publication identifier.
     * @param publicationId
     * @return
     * @throws PublicationImexUpdaterException if it is not possible to retrieve any publications with this identifier
     */
    public Publication getExistingPublicationInImexCentral(String publicationId) throws ImexCentralException;

    /**
     * Register a publication in IMEx central which is not existing in IMEx central. Can only register publications having valid pubmed id, jint identifier such as unassigned,
     * imex ids or doi numbers.
     * @param intactPublication
     * @return  the record in IMEx central which have been created, Null if it could not create a record in IMEx central
     * @throws PublicationImexUpdaterException if it is not possible to create a new record for this publication (may already exists, publication identifier not recognized, etc.)
     */
    public Publication registerPublicationInImexCentral(uk.ac.ebi.intact.model.Publication intactPublication) throws ImexCentralException;

    public ImexCentralClient getImexCentralClient();

    public void setImexCentralClient(ImexCentralClient imexClient);
}
