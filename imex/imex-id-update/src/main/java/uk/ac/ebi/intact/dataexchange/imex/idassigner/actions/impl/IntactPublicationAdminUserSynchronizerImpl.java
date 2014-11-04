package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import edu.ucla.mbi.imex.central.ws.v20.IcentralFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.imex.ImexCentralClient;
import psidev.psi.mi.jami.bridges.imex.Operation;
import psidev.psi.mi.jami.bridges.imex.extension.ImexPublication;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationAdminUserSynchronizer;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;

import java.util.Collection;

/**
 * This class is for synchronizing admin users to publications registered in IMEx central
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/03/12</pre>
 */

public class IntactPublicationAdminUserSynchronizerImpl implements PublicationAdminUserSynchronizer {

    private static final Log log = LogFactory.getLog(IntactPublicationAdminUserSynchronizerImpl.class);

    private static String PHANTOM_CURATOR = "phantom";

    private ImexCentralClient imexCentralClient;

    public IntactPublicationAdminUserSynchronizerImpl(ImexCentralClient imexCentralClient){
        if (imexCentralClient == null){
            throw new IllegalArgumentException("the admin user synchronizer cannot be null");
        }
        this.imexCentralClient = imexCentralClient;
    }

    public void synchronizePublicationAdminUser(IntactPublication intactPublication, ImexPublication imexPublication) throws BridgeFailedException {
        String curator = intactPublication.getCurrentOwner() != null ? intactPublication.getCurrentOwner().getLogin().toLowerCase() : null;

        Collection<String> adminUserList = imexPublication.getCurators();
        String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
        String source = intactPublication.getPubmedId() != null ? Xref.PUBMED : Xref.DOI;
        if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
            Xref id = intactPublication.getXrefs().iterator().next();
            source = id.getDatabase().getShortName();
            pubId = id.getId();
        }

        if (curator != null && !containsAdminUser(adminUserList, curator)){

            try {
                imexPublication = (ImexPublication)getImexCentralClient().updatePublicationAdminUser(pubId, source, Operation.ADD, curator);
                log.info("Updated publication admin user to: " + curator);

            } catch ( BridgeFailedException e ) {
                IcentralFault f = (IcentralFault) e.getCause();
                if( f.getFaultInfo().getFaultCode() == ImexCentralClient.UNKNOWN_USER && !containsAdminUser(adminUserList, PHANTOM_CURATOR)) {
                    // unknown user, we automaticaly re-assign this record to user 'phantom'
                    imexPublication = (ImexPublication)getImexCentralClient().updatePublicationAdminUser( pubId, source, Operation.ADD, PHANTOM_CURATOR );
                    log.info("Updated publication admin user to phantom user ");
                }
                else {
                    throw e;
                }
            }
        }
        else if (curator == null && !containsAdminUser(adminUserList, PHANTOM_CURATOR)){
            // unknown user, we automaticaly re-assign this record to user 'phantom'
            imexPublication = (ImexPublication)getImexCentralClient().updatePublicationAdminUser( pubId, source, Operation.ADD, PHANTOM_CURATOR );
            log.info("Updated publication admin user to phantom user ");
        }
    }

    public ImexCentralClient getImexCentralClient() {
        return imexCentralClient;
    }

    private boolean containsAdminUser(Collection<String> adminUserList, String user){

        if (!adminUserList.isEmpty()){
            return adminUserList.contains(user);
        }

        return false;
    }
}
