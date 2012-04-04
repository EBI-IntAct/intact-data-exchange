package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import edu.ucla.mbi.imex.central.ws.v20.IcentralFault;
import edu.ucla.mbi.imex.central.ws.v20.Publication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.ImexCentralPublicationRegister;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.ImexCentralUpdater;

/**
 * This class can register a publication in IMEx central and collect a publication record in IMEx central using imex central webservice
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/03/12</pre>
 */

public class ImexCentralPublicationRegisterImpl extends ImexCentralUpdater implements ImexCentralPublicationRegister{

    private static final Log log = LogFactory.getLog(ImexCentralPublicationRegisterImpl.class);

    private static int NO_RECORD_FOUND = 6;
    private static int RECORD_NO_CREATED = 7;

    /**
     *
     * @param publicationId : valid pubmed id or doi number or IMEx id. Unassigned ids are not recognized
     * @return the registered publication in IMEx central matching the publication id of the publication. Null if the publication has not been registered
     * @throws ImexCentralException
     */
    public Publication getExistingPublicationInImexCentral(String publicationId) throws ImexCentralException {

        if (publicationId != null){
            try {
                return imexCentral.getPublicationById(publicationId);
            } catch (ImexCentralException e) {
                IcentralFault f = (IcentralFault) e.getCause();

                // IMEx central did throw an Exception because this publication does not exist in IMEx central
                if( f.getFaultInfo().getFaultCode() == NO_RECORD_FOUND ) {
                    log.info("Cannot find publication " + publicationId + " in IMEx central.", e);
                }
                else {
                    throw e;
                }
            }
        }

        return null;
    }

    /**
     * Register a publication in IMEx central. Only publications having a valid pubmed ID (no unassigned pubmed id) can be registered in IMEx central using the webservice.
     * @param intactPublication
     * @return the record created in IMEx central, null if it could not be created in IMEx central
     * @throws ImexCentralException
     */
    public Publication registerPublicationInImexCentral(uk.ac.ebi.intact.model.Publication intactPublication) throws ImexCentralException{
        // create a new publication record in IMEx central
        Publication newPublication = null;
        String pubId = extractPubIdFromIntactPublication(intactPublication);

        try {
            newPublication = imexCentral.createPublicationById(pubId);
            log.info("Registered publication : " + pubId + " in IMEx central.");

            return newPublication;
        } catch (ImexCentralException e) {
            IcentralFault f = (IcentralFault) e.getCause();
            // IMEx central throw an Exception when the record cannot be created
            if( f.getFaultInfo().getFaultCode() == RECORD_NO_CREATED ) {
                log.error("Cannot create a new record in IMEx central for publication " + intactPublication.getShortLabel(), e);
                return null;
            }
            else {
                throw e;
            }
        }
    }
}
