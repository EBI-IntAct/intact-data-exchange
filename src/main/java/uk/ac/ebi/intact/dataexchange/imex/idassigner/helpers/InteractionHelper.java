/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dataexchange.imex.idassigner.helpers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.intact.core.IntactException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persistence.dao.XrefDao;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.InteractorXref;
import uk.ac.ebi.intact.model.Xref;


/**
 * Utility methods for Interactions.
 * <p/>
 * <b><u>Note</u></b>: the difference between an IMEx ID and an IMEx primary ID is the following: <br/> <b>IMEx ID</b>:
 * id retreived from the IMEx key assigner, it will be shared across all IMEx partner (eg. IM-12345). <br/> <b>IMEx
 * primary ID</b>: the local identifier of the interaction in the originating database. (eg, in IntAct: EBI-983747)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: InteractionHelper.java 8019 2007-04-10 16:02:21Z skerrien $
 * @since <pre>11-May-2006</pre>
 */
public class InteractionHelper {

	public static final Log log = LogFactory.getLog(InteractionHelper.class);

    /**
     * Add an IMEx evidence ID to an Interaction. If the interaction already has an IMEx Id, do nothing.
     *
     * @param interaction the interaction
     * @param imexId      the IMEx evidence ID
     *
     * @return true if the IMEx ID was added successfully, false otherwise (eg. there was already one IMEx id).
     *
     * @throws uk.ac.ebi.intact.business.IntactException
     *
     */
    public static boolean addIMExId( Interaction interaction, String imexEvidenceId ) throws IntactException {  

		// check if we own the publication
		if (false == IntactContext.getCurrentInstance().getInstitution().equals(interaction.getOwner())) {
			throw new IntactException("This interaction is owned by "+interaction.getOwner().getShortLabel()+" and cannot be modified");
		}
		
		if (null == InstitutionHelper.getMyInstitutionPsiId()) {
			throw new IntactException("The institution running the database should have an associated PSI-MI id");
		}
		
		/**
		 * TODO: where should I check that??
		 */
//		if (false == InstitutionHelper.getMyInstitutionPsiId().equals(IMExHelper.getKeyAssignerdatabasePsiId())) {
//			throw new IntactException("The psi id of the institution running the database ("+InstitutionHelper.getMyInstitutionPsiId()+") is different from the psi id used to retrive IMEx ids ("+IMExHelper.getKeyAssignerdatabasePsiId()+")");
//		}
		
    	CvDatabase imex = CvHelper.getImex();

        String id = getIMExEvidenceId( interaction );

        if ( id == null ) {
            // add a new Xref
            Institution owner = IntactContext.getCurrentInstance().getInstitution();
            InteractorXref xref = new InteractorXref( owner, imex, imexEvidenceId, null );
            interaction.addXref( xref );

            DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
            XrefDao<InteractorXref> xdao = daoFactory.getXrefDao(InteractorXref.class);
            xdao.persist( xref );
            System.out.println( "Added IMEx ID( " + imexEvidenceId + " ) to interaction " + interaction.getAc() + " " + interaction.getShortLabel() );

            return true;

        } else {
            log.debug( "Interaction " + interaction.getAc() + " " + interaction.getShortLabel() +
                                " had already an IMEx ID: " + id + ". skip update." );
        }

        return false;
    }

    /**
     * Search for an IMEx evidence id. IMEx primary ids are skip.
     *
     * @param interaction the interaction.
     *
     * @return the IMEx evicdence id, or null if not found.
     */
    public static String getIMExEvidenceId( Interaction interaction ) {

        CvDatabase imex = CvHelper.getImex( );

        for ( Xref xref : interaction.getXrefs() ) {
            if ( imex.equals( xref.getCvDatabase() ) && xref.getCvXrefQualifier() != null && CvXrefQualifier.IMEX_EVIDENCE_MI_REF.equals(xref.getCvXrefQualifier()) ) {
                return xref.getPrimaryId();
            }
        }

        return null; // not found
    }

    /**
     * Answers the question: "has the given interaction got an IMEx EVIDENCE ID ?".
     *
     * @param interaction the interaction
     *
     * @return true if the interaction has an IMEx EVIDENCE ID.
     */
    public static boolean hasIMExId( Interaction interaction ) {
        return ( null != getIMExEvidenceId( interaction ) );
    }
}