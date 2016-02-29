package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import edu.ucla.mbi.imex.central.ws.v20.IcentralFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.imex.ImexCentralClient;
import psidev.psi.mi.jami.bridges.imex.Operation;
import psidev.psi.mi.jami.bridges.imex.extension.ImexPublication;
import psidev.psi.mi.jami.imex.actions.impl.PublicationAdminGroupSynchronizerImpl;
import psidev.psi.mi.jami.model.Publication;
import psidev.psi.mi.jami.model.Source;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.model.impl.DefaultSource;

import java.util.List;

/**
 * This class is for synchronizing the admin group of a publication in imex central
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/03/12</pre>
 */

public class IntactPublicationAdminGroupSynchronizerImpl extends PublicationAdminGroupSynchronizerImpl{

    private static final Log log = LogFactory.getLog(IntactPublicationAdminGroupSynchronizerImpl.class);

    /**
     * This group is for publication curated and owned by INTACT
     */
    private static String INTACT_ADMIN = "INTACT";
    /**
     * This group is for publications maintained and reviewed by INTACT but not owned by IntAct
     */
    private static String INTACT_ADMIN_CURATOR = "INTACT CURATORS";

    public IntactPublicationAdminGroupSynchronizerImpl(ImexCentralClient client) {
        super(client);
    }

    @Override
    public void synchronizePublicationAdminGroup(Publication publication, ImexPublication imexPublication) throws BridgeFailedException {

        List<Source> sources = imexPublication.getSources();
        String pubId = publication.getPubmedId() != null ? publication.getPubmedId() : publication.getDoi();
        String source = publication.getPubmedId() != null ? Xref.PUBMED : Xref.DOI;
        if (pubId == null && !publication.getIdentifiers().isEmpty()){
            Xref id = publication.getXrefs().iterator().next();
            source = id.getDatabase().getShortName();
            pubId = id.getId();
        }

        // add other database admin group if it exists
        Source institution = publication.getSource();
        if (source == null){
            return;
        }

        if (!containsAdminGroup(sources, institution)){
            try {
                imexPublication = (ImexPublication)getImexCentralClient().updatePublicationAdminGroup( pubId, source, Operation.ADD,
                        institution.getShortName().toUpperCase() );
                log.info("Added other publication admin group : " + institution);

                // now add intact admin group curators for publications maintained by intact but not owned by INTACT
                if (!INTACT_ADMIN.equals(institution.getShortName().toUpperCase())
                        && !containsAdminGroup(sources, new DefaultSource(INTACT_ADMIN_CURATOR))){
                    // add first INTACT admin curators
                    try {
                        imexPublication = (ImexPublication)getImexCentralClient().updatePublicationAdminGroup(pubId, source,
                                Operation.ADD, INTACT_ADMIN_CURATOR);
                        log.info("Updated publication admin group to: " + INTACT_ADMIN_CURATOR);
                    } catch ( BridgeFailedException e ) {
                        IcentralFault f = (IcentralFault) e.getCause();
                        if( f.getFaultInfo().getFaultCode() == ImexCentralClient.UNKNOWN_GROUP ) {
                            // unknown intact admin group, we cannot add another admin group for this institution
                            log.warn("The intact curator admin group is not recognized in IMEx central, we cannot tag the publication as maintained " +
                                    "and review by INTACT.");
                        }
                        // operation invalid is fired if group already assigned
                        else if (f.getFaultInfo().getFaultCode() != ImexCentralClient.OPERATION_NOT_VALID) {
                            throw e;
                        }
                    }
                }
            } catch ( BridgeFailedException e ) {
                IcentralFault f = (IcentralFault) e.getCause();
                if( f.getFaultInfo().getFaultCode() == ImexCentralClient.UNKNOWN_GROUP
                        || f.getFaultInfo().getFaultCode() == ImexCentralClient.OPERATION_NOT_VALID ) {
                    // unknown admin group, we cannot add another admin group for this institution
                    if (f.getFaultInfo().getFaultCode() == ImexCentralClient.UNKNOWN_GROUP){
                        log.warn("The institution " + institution + " is not recognized in IMEx central so we will add INTACT admin group.");
                    }

                    if (!INTACT_ADMIN.equals(institution.getShortName().toUpperCase()) && !containsAdminGroup(sources, new DefaultSource(INTACT_ADMIN))){
                        try {
                            // add first INTACT admin
                            imexPublication = (ImexPublication)getImexCentralClient().updatePublicationAdminGroup(pubId, source, Operation.ADD, INTACT_ADMIN);
                            log.info("Updated publication admin group to: " + INTACT_ADMIN);
                        } catch ( BridgeFailedException e2 ) {
                            IcentralFault f2 = (IcentralFault) e2.getCause();
                            if( f2.getFaultInfo().getFaultCode() == ImexCentralClient.UNKNOWN_GROUP) {
                                // unknown intact admin group, we cannot add another admin group for this institution
                                log.warn("The intact curator admin group is not recognized in IMEx central, we cannot tag the publication as maintained and review by INTACT.");
                            }
                            // operation invalid is fired if group already assigned
                            else if (f2.getFaultInfo().getFaultCode() != ImexCentralClient.OPERATION_NOT_VALID){
                                throw e;
                            }
                        }
                    }
                }
                else {
                    throw e;
                }
            }
        }
        // now add intact admin group curators for publications maintained by intact but not owned by INTACT
        else if (!INTACT_ADMIN.equals(institution.getShortName().toUpperCase()) && !containsAdminGroup(sources, new DefaultSource(INTACT_ADMIN_CURATOR))){
            // add first INTACT admin curators
            try {
                imexPublication = (ImexPublication)getImexCentralClient().updatePublicationAdminGroup(pubId, source, Operation.ADD, INTACT_ADMIN_CURATOR);
                log.info("Updated publication admin group to: " + INTACT_ADMIN_CURATOR);
            } catch ( BridgeFailedException e ) {
                IcentralFault f = (IcentralFault) e.getCause();
                if( f.getFaultInfo().getFaultCode() == ImexCentralClient.UNKNOWN_GROUP ) {
                    // unknown intact admin group, we cannot add another admin group for this institution
                    log.warn("The intact curator admin group is not recognized in IMEx central, we cannot tag the publication as maintained and review by INTACT.");
                }
                // operation invalid is fired if group already assigned
                else if (f.getFaultInfo().getFaultCode() != ImexCentralClient.OPERATION_NOT_VALID) {
                    throw e;
                }
                else {
                    throw e;
                }
            }
        }
    }
}
