package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import edu.ucla.mbi.imex.central.ws.v20.Publication;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.IntactPublicationCollector;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;

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

    public ImexCentralManager getImexCentralManager() {
        return imexCentralManager;
    }

    public void setImexCentralManager(ImexCentralManager imexCentralManager) {
        this.imexCentralManager = imexCentralManager;
    }

    public void assignNewImexIdsToPublications(){
        Collection<String> publicationsNeedingNewImexId = intactPublicationCollector.getPublicationsNeedingAnImexId();

        for (String publication : publicationsNeedingNewImexId){
            try {
                Publication imexRecord = imexCentralManager.assignImexAndUpdatePublication(publication);

            } catch (PublicationImexUpdaterException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ImexCentralException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public void updateExistingImexPublications(){
        Collection<String> publicationsToUpdate = intactPublicationCollector.getPublicationsHavingIMExIdToUpdate();

        for (String publication : publicationsToUpdate){
            try {
                imexCentralManager.updateIntactPublicationHavingIMEx(publication);
            } catch (PublicationImexUpdaterException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ImexCentralException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public IntactPublicationCollector getIntactPublicationCollector() {
        return intactPublicationCollector;
    }

    public void setIntactPublicationCollector(IntactPublicationCollector intactPublicationCollector) {
        this.intactPublicationCollector = intactPublicationCollector;
    }
}
