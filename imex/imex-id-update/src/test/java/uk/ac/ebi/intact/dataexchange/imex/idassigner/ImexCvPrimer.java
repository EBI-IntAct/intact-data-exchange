package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import uk.ac.ebi.intact.core.config.impl.SmallCvPrimer;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.CvXrefQualifier;

/**
 * Set of CVs required for the IMEx update to run.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.1
 */
public class ImexCvPrimer extends SmallCvPrimer {
    public ImexCvPrimer( DaoFactory daoFactory ) {
        super( daoFactory );
    }

    @Override
    public void createCVs() {
        super.createCVs();

        getCvObject( CvDatabase.class, CvDatabase.IMEX, CvDatabase.IMEX_MI_REF );
        getCvObject( CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY, CvXrefQualifier.IMEX_PRIMARY_MI_REF );
        getCvObject( CvXrefQualifier.class, "imex-secondary", "MI:0952" );
        getCvObject( CvXrefQualifier.class, "imex source" );
        getCvObject( CvTopic.class, "last-imex-assigned" );
        getCvObject( CvTopic.class, "imex curation", "MI:0959" );
        getCvObject( CvTopic.class, "full-coverage", "MI:0957" );
    }
}
