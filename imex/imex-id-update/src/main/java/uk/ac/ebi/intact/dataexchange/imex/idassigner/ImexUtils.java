package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

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
}
