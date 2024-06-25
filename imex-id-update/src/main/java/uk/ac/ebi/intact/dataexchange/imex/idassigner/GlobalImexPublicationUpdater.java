package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import edu.ucla.mbi.imex.central.ws.v20.IcentralFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import psidev.psi.mi.jami.bridges.imex.ImexCentralClient;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.IntactPublicationCollector;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorType;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;

import java.util.Collection;

/**
 * This class is for updating/assigning IMEx publications for the all IntAct database
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30/03/12</pre>
 */
@Service("globalImexPublicationUpdater")
public class GlobalImexPublicationUpdater {

    private static final Log log = LogFactory.getLog(GlobalImexPublicationUpdater.class);

    public ImexCentralManager getImexCentralManager() {
        return ApplicationContextProvider.getBean("imexCentralManager");
    }

    /**
     * Assign new IMEx ids to publications that are eligible IMEx
     */
    public void assignNewImexIdsToPublications(){

        ImexCentralManager imexCentralManager = getImexCentralManager();
        IntactPublicationCollector intactPublicationCollector = ApplicationContextProvider.getBean("intactPublicationCollector");

        getImexCentralManager().registerListenersIfNotDoneYet();

        Collection<String> publicationsNeedingNewImexId = intactPublicationCollector.getPublicationsNeedingAnImexId();

            for (String publication : publicationsNeedingNewImexId){
                log.info("Processing new publication " + publication);
                try {
                    imexCentralManager.assignImexAndUpdatePublication(publication);

                }
                catch (PublicationImexUpdaterException e) {
                    ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, publication, null, null, null, e.getMessage());
                    imexCentralManager.fireOnImexError(errorEvt);
                }
                catch (EnricherException e) {
                    processEnricherException(imexCentralManager, publication, e);
                }
                catch (Exception e) {
                    ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, publication, null, null, null, e.getMessage());
                    imexCentralManager.fireOnImexError(errorEvt);
                }
            }

            // fire error event for publications without imex but experiment has imex
            Collection<String> publicationsWithouImexAndExperimentImex = intactPublicationCollector.getPublicationsWithoutImexButWithExperimentImex();
            for (String pubAc : publicationsWithouImexAndExperimentImex){
                ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.publication_not_imex_experiment_imex, pubAc, null, null, null, "Publication does not have IMEx primary reference but the experiments do have a IMEx primary reference.");
                imexCentralManager.fireOnImexError(errorEvt);
            }

            // fire error event for publications without imex id but interactions have imex
            Collection<String> publicationsWithouImexAndInteractionImex = intactPublicationCollector.getPublicationsWithoutImexButWithInteractionImex();
            for (String pubAc : publicationsWithouImexAndInteractionImex){
                ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.publication_not_imex_interaction_imex, pubAc, null, null, null, "Publication does not have IMEx primary reference but the interactions do have a IMEx primary reference.");
                imexCentralManager.fireOnImexError(errorEvt);
            }

        // fire error event for publications having IMEx curation level but dr-export = no
        /*Collection<String> publicationsWithImexCurationLevelAndUniprotDrExportNo = intactPublicationCollector.getPublicationsHavingIMExCurationLevelAndUniprotDrExportNo();
        for (String pubAc : publicationsWithImexCurationLevelAndUniprotDrExportNo){
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.imex_curation_uniprot_dr_export_no, pubAc, null, null, null, "Publication does contain experiments having uniprot-dr-export = no and has IMEx curation level.");
            imexCentralManager.fireOnImexError(errorEvt);
        }*/
    }

    private void processEnricherException(ImexCentralManager imexCentralManager, String publication, EnricherException e) {
        if (e.getCause() instanceof IcentralFault){
            IcentralFault f = (IcentralFault) e.getCause();

            processImexCentralException(publication, e, f, imexCentralManager);
        }
        else if (e.getCause() != null){
            Throwable child = e.getCause();
            if (child.getCause() instanceof IcentralFault){
                IcentralFault f = (IcentralFault) child.getCause();

                processImexCentralException(publication, e, f, imexCentralManager);
            }
            // possible brige exception wrapping icentral fault
            else if (child.getCause() != null){
                Throwable child2 = child.getCause();
                if (child2.getCause() instanceof IcentralFault){
                    IcentralFault f = (IcentralFault) child2.getCause();

                    processImexCentralException(publication, e, f, imexCentralManager);
                }
                else {
                    ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, publication, null, null, null, e.getMessage());
                    imexCentralManager.fireOnImexError(errorEvt);
                }
            }
            else{
                ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, publication, null, null, null, e.getMessage());
                imexCentralManager.fireOnImexError(errorEvt);
            }
        }
        else{
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, publication, null, null, null, e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
    }

    private void processImexCentralException(String publication, Exception e, IcentralFault f, ImexCentralManager imexCentralManager) {
        if( f.getFaultInfo().getFaultCode() == ImexCentralClient.USER_NOT_AUTHORIZED ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.user_not_authorized, publication, null, null, null, "missing/unknown user and/or missing/invalid password provided : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == ImexCentralClient.OPERATION_NOT_VALID ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.operation_not_valid, publication, null, null, null, "invalid operation : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == ImexCentralClient.IDENTIFIER_MISSING ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.identifier_missing, publication, null, null, null, "missing identifier : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == ImexCentralClient.IDENTIFIER_UNKNOWN ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.identifier_unknown, publication, null, null, null, "unrecognized identifier : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == ImexCentralClient.NO_RECORD ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.no_record, publication, null, null, null, "query returned no records : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == ImexCentralClient.NO_RECORD_CREATED ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.no_record_created, publication, null, null, null, "requested records were not created : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == ImexCentralClient.STATUS_UNKNOWN ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.status_unknown, publication, null, null, null, "unknown status : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == ImexCentralClient.NO_IMEX_ID ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.no_IMEX_id, publication, null, null, null, "IMEx identifier missing : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == ImexCentralClient.UNKNOWN_USER ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.unknown_user, publication, null, null, null, "passed user parameter does not specify a known user : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == ImexCentralClient.UNKNOWN_GROUP ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.unknown_group, publication, null, null, null, "passed group parameter does not specify a known group : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == ImexCentralClient.OPERATION_NOT_SUPPORTED ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.operation_not_supported, publication, null, null, null, "operation not (yet) supported : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else if( f.getFaultInfo().getFaultCode() == ImexCentralClient.INTERNAL_SERVER_ERROR ) {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.internal_server_error, publication, null, null, null, "internal server error : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
        else {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, publication, null, null, null, "Impossible to update publication : " + e.getMessage());
            imexCentralManager.fireOnImexError(errorEvt);
        }
    }

    /**
     * Update existing IMEx publications in IntAct and assign interaction imex ids if not already done
     */
    public void updateExistingImexPublications(){
        ImexCentralManager imexCentralManager = getImexCentralManager();

        imexCentralManager.registerListenersIfNotDoneYet();
        IntactPublicationCollector intactPublicationCollector = ApplicationContextProvider.getBean("intactPublicationCollector");

        Collection<String> publicationsToUpdate = intactPublicationCollector.getPublicationsHavingIMExIdToUpdate();

            for (String publication : publicationsToUpdate){
                log.info("Processing update of publication " + publication);

                try {
                    imexCentralManager.updateIntactPublicationHavingIMEx(publication);
                } catch (PublicationImexUpdaterException e) {
                    ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, publication, null, null, null, e.getMessage());
                    imexCentralManager.fireOnImexError(errorEvt);
                } catch (EnricherException e) {
                    processEnricherException(imexCentralManager, publication, e);
                }
                catch (Exception e) {
                    ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, publication, null, null, null, e.getMessage());
                    imexCentralManager.fireOnImexError(errorEvt);
                }
            }

            // fire error event for publications with imex id and without imex curation level
            Collection<String> publicationsWithImexIdWithouImexCuration = intactPublicationCollector.getPublicationsHavingIMExIdAndNotImexCurationLevel();
            for (String pubAc : publicationsWithImexIdWithouImexCuration){
                ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.publication_imex_id_not_imex_curation, pubAc, null, null, null, "Publication does have a IMEx primary reference but does not have IMEx curation level.");
                imexCentralManager.fireOnImexError(errorEvt);
            }

            // fire error event for publications with imex id and no PPI
            /*Collection<String> publicationsWithImexIdWithoutPPIInteractions = intactPublicationCollector.getPublicationsHavingIMExIdAndNoPPI();
            for (String pubAc : publicationsWithImexIdWithoutPPIInteractions){
                ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.publication_imex_id_not_PPI, pubAc, null, null, null, "Publication does have a IMEx primary reference but does not have a single PPI.");
                imexCentralManager.fireOnImexError(errorEvt);
            }*/
    }
}
