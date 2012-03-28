package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

public class PublicationImexAssigner extends ImexCentralUpdater{

    private static final Log log = LogFactory.getLog(PublicationImexAssigner.class);

    private CvDatabase imex;
    private CvXrefQualifier imexPrimary;

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
}
