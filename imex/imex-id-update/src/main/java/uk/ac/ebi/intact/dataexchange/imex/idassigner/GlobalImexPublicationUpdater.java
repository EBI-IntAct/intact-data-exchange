package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import edu.ucla.mbi.imex.central.ws.v20.IcentralFault;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.IntactPublicationCollector;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorType;

import java.util.Collection;

/**
 * This class is for updating/assigning IMEx publications for the IntAct database
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30/03/12</pre>
 */

public class GlobalImexPublicationUpdater {

    private ImexCentralManager imexCentralManager;

    private IntactPublicationCollector intactPublicationCollector;
    public static int USER_NOT_AUTHORIZED = 2;
    public static int OPERATION_NOT_VALID = 3;
    public static int IDENTIFIER_MISSING = 4;
    public static int IDENTIFIER_UNKNOWN = 5;
    public static int NO_RECORD = 6;
    public static int NO_RECORD_CREATED = 7;
    public static int STATUS_UNKNOWN = 8;
    public static int NO_IMEX_ID = 9;
    public static int UNKNOWN_USER = 10;
    public static int UNKNOWN_GROUP = 11;
    public static int OPERATION_NOT_SUPPORTED = 98;
    public static int INTERNAL_SERVER_ERROR = 99;

    public ImexCentralManager getImexCentralManager() {
        return imexCentralManager;
    }

    public void setImexCentralManager(ImexCentralManager imexCentralManager) {
        this.imexCentralManager = imexCentralManager;
    }

    public void assignNewImexIdsToPublications(){
        imexCentralManager.registerListenersIfNotDoneYet();
        
        Collection<String> publicationsNeedingNewImexId = intactPublicationCollector.getPublicationsNeedingAnImexId();

        for (String publication : publicationsNeedingNewImexId){
            try {
                imexCentralManager.assignImexAndUpdatePublication(publication);

            } catch (PublicationImexUpdaterException e) {
                ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, publication, null, null, null, e.getMessage());
                imexCentralManager.fireOnImexError(errorEvt);
            } catch (ImexCentralException e) {
                IcentralFault f = (IcentralFault) e.getCause();

                processImexCentralException(publication, e, f);
            }
        }

        // fire error event for publications without imex id and without imex curation level
        // fire error event for publications without imex id, not elligible but with imex curation level
        // fire error event for publications without imex but experiment has imex
        // fire error event for publications without imex id but interactions have imex
    }

    private void processImexCentralException(String publication, ImexCentralException e, IcentralFault f) {
        if( f.getFaultInfo().getFaultCode() == USER_NOT_AUTHORIZED ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.user_not_authorized, publication, null, null, null, "missing/unknown user and/or missing/invalid password provided : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == OPERATION_NOT_VALID ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.operation_not_valid, publication, null, null, null, "invalid operation : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == IDENTIFIER_MISSING ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.identifier_missing, publication, null, null, null, "missing identifier : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == IDENTIFIER_UNKNOWN ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.identifier_unknown, publication, null, null, null, "unrecognized identifier : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == NO_RECORD ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.no_record, publication, null, null, null, "query returned no records : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == NO_RECORD_CREATED ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.no_record_created, publication, null, null, null, "requested records were not created : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == STATUS_UNKNOWN ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.status_unknown, publication, null, null, null, "unknown status : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == NO_IMEX_ID ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.no_IMEX_id, publication, null, null, null, "IMEx identifier missing : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == UNKNOWN_USER ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.unknown_user, publication, null, null, null, "passed user parameter does not specify a known user : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == UNKNOWN_GROUP ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.unknown_group, publication, null, null, null, "passed group parameter does not specify a known group : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == OPERATION_NOT_SUPPORTED ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.operation_not_supported, publication, null, null, null, "operation not (yet) supported : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == INTERNAL_SERVER_ERROR ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.internal_server_error, publication, null, null, null, "internal server error : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, publication, null, null, null, "Impossible to update publication : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
    }

    public void updateExistingImexPublications(){
        imexCentralManager.registerListenersIfNotDoneYet();

        Collection<String> publicationsToUpdate = intactPublicationCollector.getPublicationsHavingIMExIdToUpdate();

        for (String publication : publicationsToUpdate){
            try {
                imexCentralManager.updateIntactPublicationHavingIMEx(publication);
            } catch (PublicationImexUpdaterException e) {
                ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, publication, null, null, null, e.getMessage());
                imexCentralManager.fireOnImexError(errorEvt);
            } catch (ImexCentralException e) {
                IcentralFault f = (IcentralFault) e.getCause();

                processImexCentralException(publication, e, f);
            }
        }

        // fire error event for publications with imex id and without imex curation level
        // fire error event for publications with imex id and no PPI
    }

    public IntactPublicationCollector getIntactPublicationCollector() {
        return intactPublicationCollector;
    }

    public void setIntactPublicationCollector(IntactPublicationCollector intactPublicationCollector) {
        this.intactPublicationCollector = intactPublicationCollector;
    }
}
