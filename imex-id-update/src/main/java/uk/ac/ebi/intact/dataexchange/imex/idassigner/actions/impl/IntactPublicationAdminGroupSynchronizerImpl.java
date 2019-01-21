package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import edu.ucla.mbi.imex.central.ws.v20.IcentralFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.imex.Constants;
import psidev.psi.mi.jami.bridges.imex.ImexCentralClient;
import psidev.psi.mi.jami.bridges.imex.ImexCentralUtility;
import psidev.psi.mi.jami.bridges.imex.Operation;
import psidev.psi.mi.jami.bridges.imex.extension.ImexPublication;
import psidev.psi.mi.jami.imex.actions.impl.PublicationAdminGroupSynchronizerImpl;
import psidev.psi.mi.jami.model.Publication;
import psidev.psi.mi.jami.model.Source;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.model.impl.DefaultSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        Set<String> imexPartners= Constants.IMEX_PARTNERS;
        Source imexPubOwnerGroup=getImexPublicationOwnerGroup(sources,imexPartners);

        String pubId = publication.getPubmedId() != null ? publication.getPubmedId() : publication.getDoi();
        String source = publication.getPubmedId() != null ? Xref.PUBMED : Xref.DOI;
        if (pubId == null && !publication.getIdentifiers().isEmpty()){
            Xref id = publication.getXrefs().iterator().next();
            source = id.getDatabase().getShortName();
            pubId = id.getId();
        }

        // add other database admin group if it exists
        Source institution = publication.getSource();
        String imexInstitutionName=institution.getShortName().toUpperCase();
        if (source == null){
            return;
        }

        Source imexCurators=new DefaultSource(Constants.IMEX_NAME+Constants.IMEX_DB_CURATORS_SUFFIX);
        Source sourceDBCurators=new DefaultSource(imexInstitutionName+Constants.IMEX_DB_CURATORS_SUFFIX);

        //add DB group
        if (!containsAdminGroup(sources, institution)&&imexPubOwnerGroup==null){
            addGroupInImexCentralForPub(pubId,source,imexInstitutionName);
        }
        //add Imex Curators group
        if (!containsAdminGroup(sources, imexCurators)){
            addGroupInImexCentralForPub(pubId,source,imexCurators.getShortName());
        }
        // add DB Curators group if a imex partner is already assigned to imex publication which is different than owner of intact publication
        if (!containsAdminGroup(sources, sourceDBCurators)&&imexPubOwnerGroup!=null&&!imexPubOwnerGroup.getShortName().toUpperCase().equals(imexInstitutionName)){
            addGroupInImexCentralForPub(pubId,source,sourceDBCurators.getShortName());
        }
    }

    private void addGroupInImexCentralForPub(String pubId,String source,String groupToAdd) throws BridgeFailedException{
        try {
            getImexCentralClient().updatePublicationAdminGroup( pubId, source, Operation.ADD,
                    groupToAdd);
            log.info("Added other publication admin group : " + groupToAdd);

        } catch ( BridgeFailedException e1 ) {
            IcentralFault f2 = (IcentralFault) e1.getCause();
            if( f2.getFaultInfo().getFaultCode() == ImexCentralClient.UNKNOWN_GROUP) {
                // unknown intact admin group, we cannot add another admin group for this institution
                log.warn("The admin group "+groupToAdd+ "is not recognized in IMEx central");
            }
            // operation invalid is fired if group already assigned
            else if (f2.getFaultInfo().getFaultCode() != ImexCentralClient.OPERATION_NOT_VALID){
                log.warn("Operation is not valid");
            }
            throw e1;
        }
    }
}
