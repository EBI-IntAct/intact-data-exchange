package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import edu.ucla.mbi.imex.central.ws.v20.IcentralFault;
import edu.ucla.mbi.imex.central.ws.v20.Publication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.ImexCentralPublicationRegister;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.ImexCentralUpdater;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;

/**
 * This class can register a publication in IMEx central and collect a publication record in IMEx central using imex central webservice
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/03/12</pre>
 */

public class ImexCentralPublicationRegisterImpl extends ImexCentralUpdater implements ImexCentralPublicationRegister{

    private static final Log log = LogFactory.getLog(ImexCentralPublicationRegisterImpl.class);

    private static int UNKNOWN_IDENTIFIER = 5;
    private static int RECORD_NOT_CREATED = 7;
    private static int NO_RECORD_FOUND = 6;

    /**
     *
     * @param publicationId : valid pubmed id or doi number or IMEx id
     * @return the registered publication in IMEx central matching the publication id of the publication. Null if the publication has not been registered
     * @throws ImexCentralException
     */
    public Publication getExistingPublicationInImexCentral(String publicationId) throws PublicationImexUpdaterException {

        if (publicationId != null){
            try {
                return imexCentral.getPublicationById(publicationId);
            } catch (ImexCentralException e) {
                IcentralFault f = (IcentralFault) e.getCause();
                if( f.getFaultInfo().getFaultCode() == UNKNOWN_IDENTIFIER ) {
                    throw new PublicationImexUpdaterException("The identifier "+publicationId+"is not recognized in IMEx central", e);
                }
                else if( f.getFaultInfo().getFaultCode() == NO_RECORD_FOUND ) {
                    throw new PublicationImexUpdaterException("Cannot find publication " + publicationId + " in IMEx central.", e);
                }
                else {
                    throw new PublicationImexUpdaterException("Cannot retrieve Publication " + publicationId + "in IMEx central.", e);
                }
            }
        }

        return null;
    }

    public Publication registerPublicationInImexCentral(uk.ac.ebi.intact.model.Publication intactPublication) throws PublicationImexUpdaterException{
        // create a new publication record in IMEx central
        Publication newPublication = null;
        String pubId = extractPubIdFromIntactPublication(intactPublication);

        try {

            newPublication = imexCentral.createPublicationById(pubId);
            System.out.println("Registered publication : " + pubId + " in IMEx central.");

            return newPublication;
        } catch (ImexCentralException e) {
            IcentralFault f = (IcentralFault) e.getCause();
            if( f.getFaultInfo().getFaultCode() == UNKNOWN_IDENTIFIER ) {
                throw new PublicationImexUpdaterException("Cannot create a new record because the identifier " + pubId + "is not recognized in IMEx central", e);
            }
            else if( f.getFaultInfo().getFaultCode() == RECORD_NOT_CREATED ) {
                throw new PublicationImexUpdaterException("Cannot create a new record in IMEx central for publication " + intactPublication.getShortLabel(), e);
            }
            else {
                throw new PublicationImexUpdaterException("Cannot register publication " + intactPublication.getShortLabel()+" in IMEx central.", e);
            }
        }
    }
}
