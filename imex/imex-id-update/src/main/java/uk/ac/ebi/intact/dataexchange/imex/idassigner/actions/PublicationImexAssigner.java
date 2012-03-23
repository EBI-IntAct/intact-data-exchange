package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

//import edu.ucla.mbi.imex.central.ws.v20.IcentralFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralClient;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;

/**
 * This class will assign an IMEx id to a publication
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/03/12</pre>
 */

public class PublicationImexAssigner {

    private static final Log log = LogFactory.getLog(PublicationImexAssigner.class);

    private ImexCentralClient imexCentral;

    private CvDatabase imex;
    private CvXrefQualifier imexPrimary;

    public void PublicationImexAssigner(ImexCentralClient imexCentral){

        this.imexCentral = imexCentral;
    }

    public void initializeCvs(){
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        imex = daoFactory.getCvObjectDao( CvDatabase.class ).getByPsiMiRef( CvDatabase.IMEX_MI_REF );
        if ( imex == null ) {
            throw new IllegalArgumentException( "Could not find CV term: imex" );
        }

        imexPrimary = daoFactory.getCvObjectDao( CvXrefQualifier.class ).getByPsiMiRef( CvXrefQualifier.IMEX_PRIMARY_MI_REF );
        if ( imexPrimary == null ) {
            throw new IllegalArgumentException( "Could not find CV term: imexPrimary" );
        }
    }

    /*public edu.ucla.mbi.imex.central.ws.v20.Publication getExistingPublication(String publicationId) throws ImexCentralException {

        if (publicationId != null){
            return imexCentral.getPublicationById(publicationId);
        }

        return null;
    }

    public boolean assignNewIMExIdToPublication(Publication publication) {

        try{
            // create a new IMEx record
            edu.ucla.mbi.imex.central.ws.v20.Publication newPublication = imexCentral.createPublicationById(publication.getPublicationId());
            System.out.println( "Assigned IMEx id : " + newPublication.getImexAccession() + " to publication " + publication.getPublicationId() );

            // add an IMEx primary ref to the publication
            ImexUtils.addImexPrimaryRefTo(publication, newPublication.getImexAccession());

            // add institution and update admin group
            final String institution = publication.getOwner().getShortLabel().toUpperCase();
            imexCentral.updatePublicationAdminGroup( publication.getPublicationId(), Operation.ADD, institution );
            System.out.println( "Updated publication admin group to: " + institution );

            // Note: we assume the curators' login in intact are the same as in IMExCentral.
            //       Also they must be all lowercase in IMExCentral.
            // assign a curator
            String curator =publication.getCurrentOwner().getLogin().toLowerCase();

            try {
                imexCentral.updatePublicationAdminUser( publication.getPublicationId(), Operation.ADD, curator );
                System.out.println( "Updated publication admin user to: " + curator );
                
                return true;
            } catch ( ImexCentralException e ) {
                IcentralFault f = (IcentralFault) e.getCause();
                if( f.getFaultInfo().getFaultCode() == 10 ) {
                    // unknown user, we automaticaly re-assign this record to user 'phantom'
                    curator = "phantom";  // this will apply to all curators that have left the group
                    imexCentral.updatePublicationAdminUser( publication.getPublicationId(), Operation.ADD, curator );
                    System.out.println( "Updated publication admin user to: " + curator );

                }
            }
        } catch ( ImexCentralException e ) {
            log.error( "An error occured while processing publication: " + publication.getPublicationId(), e );

            if( e.getCause() instanceof IcentralFault ) {
                IcentralFault f = ((IcentralFault)e.getCause());
                log.error( "This exception was thrown by UCLA's IMEx Central Web Service and provided the " +
                        "following extra information. Error code: " + f.getFaultInfo().getFaultCode() +
                        ". Message: '"+ f.getFaultInfo().getMessage() +"'.");
            }
        }
        
        return false;
    }

    public boolean assignNewIMExIdToExperiment(Experiment experiment, String imexId) {

        // create a new IMEx record
        edu.ucla.mbi.imex.central.ws.v20.Publication newPublication = imexCentral.createPublicationById(publication.getPublicationId());
        System.out.println( "Assigned IMEx id : " + newPublication.getImexAccession() + " to publication " + publication.getPublicationId() );

        // add an IMEx primary ref to the publication
        ImexUtils.addImexPrimaryRefTo(publication, newPublication.getImexAccession());

        // add institution and update admin group
        final String institution = publication.getOwner().getShortLabel().toUpperCase();
        imexCentral.updatePublicationAdminGroup( publication.getPublicationId(), Operation.ADD, institution );
        System.out.println( "Updated publication admin group to: " + institution );

        // Note: we assume the curators' login in intact are the same as in IMExCentral.
        //       Also they must be all lowercase in IMExCentral.
        // assign a curator
        String curator =publication.getCurrentOwner().getLogin().toLowerCase();

        try {
            imexCentral.updatePublicationAdminUser( publication.getPublicationId(), Operation.ADD, curator );
            System.out.println( "Updated publication admin user to: " + curator );

            return true;
        } catch ( ImexCentralException e ) {
            IcentralFault f = (IcentralFault) e.getCause();
            if( f.getFaultInfo().getFaultCode() == 10 ) {
                // unknown user, we automaticaly re-assign this record to user 'phantom'
                curator = "phantom";  // this will apply to all curators that have left the group
                imexCentral.updatePublicationAdminUser( publication.getPublicationId(), Operation.ADD, curator );
                System.out.println( "Updated publication admin user to: " + curator );

            }
        }    }
        }

        return false;
    }*/
}
