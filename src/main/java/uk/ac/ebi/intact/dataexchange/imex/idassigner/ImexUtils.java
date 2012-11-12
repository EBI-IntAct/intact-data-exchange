package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persistence.dao.XrefDao;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.XrefUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO document this !
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.1
 */
public class ImexUtils {

    public static Xref getPrimaryImexId( AnnotatedObject ao ) {
        final Collection<PublicationXref> xrefs = AnnotatedObjectUtils.searchXrefs( ao,
                                                                                    CvDatabase.IMEX_MI_REF,
                                                                                    CvXrefQualifier.IMEX_PRIMARY_MI_REF );
        switch ( xrefs.size() ) {
            case 0:
                return null;
            case 1:
                return xrefs.iterator().next();
            default:
                throw new IllegalStateException( "Found " + xrefs.size() + " IMEx primary ids on " +
                                                 ao.getClass().getSimpleName() + ": " + ao.getShortLabel() +
                                                 "(" + ao.getAc() + ")" );
        }
    }

    public static void addImexPrimaryRefTo(AnnotatedObject object, String imexId){

        if (imexId != null){

            boolean hasImexId = false;
            boolean isUpdated = false;

            Collection<Xref> refs = new ArrayList<Xref>(object.getXrefs());
            XrefDao refDao = IntactContext.getCurrentInstance().getDaoFactory().getXrefDao();

            for (Xref ref : refs){
                if (ref.getCvDatabase() != null && ref.getCvDatabase().getIdentifier().equals(CvDatabase.IMEX_MI_REF)){
                    if (ref.getCvXrefQualifier() != null && ref.getCvXrefQualifier().getIdentifier().equals(CvXrefQualifier.IMEX_PRIMARY_MI_REF)){
                        // the imex id is already there
                        if (ref.getPrimaryId().equals(imexId) && !hasImexId){
                            hasImexId = true;
                        }
                        // the imex id is there twice, delete duplicated imex primary ref
                        else if (ref.getPrimaryId().equals(imexId) && hasImexId){
                            object.removeXref(ref);
                            refDao.delete(ref);
                            isUpdated = true;
                        }
                        // the imex primary ref is not valid, delete it
                        else {
                            object.removeXref(ref);
                            refDao.delete(ref);
                            isUpdated = true;
                        }
                    }
                }
            }

            if (!hasImexId){
                DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

                CvDatabase imex = daoFactory.getCvObjectDao( CvDatabase.class ).getByPsiMiRef( CvDatabase.IMEX_MI_REF );
                if ( imex == null ) {
                    throw new IllegalArgumentException( "Could not find CV term: imex" );
                }

                CvXrefQualifier imexPrimary = daoFactory.getCvObjectDao( CvXrefQualifier.class ).getByPsiMiRef( CvXrefQualifier.IMEX_PRIMARY_MI_REF );
                if ( imexPrimary == null ) {
                    throw new IllegalArgumentException( "Could not find CV term: imexPrimary" );
                }

                Xref xref = XrefUtils.createIdentityXref(object, imexId, imexPrimary, imex);
                refDao.persist(xref);

                object.addXref(xref);
                isUpdated = true;
            }

            if (isUpdated){
                IntactContext.getCurrentInstance().getDaoFactory().getAnnotatedObjectDao(AnnotatedObject.class).update(object);
            }
        }
    }
}
