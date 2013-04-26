/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dataexchange.imex.idassigner.helpers;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.InstitutionXref;


/**
 * Utility methods for Institution.
 *
 * @author Arnaud Ceol
 * @version $Id
 */
public class InstitutionHelper {

	
	/**
	 * Get the PSI-MI id of the institution running the database.
	 * @return PSI-MI id of the institution
	 */
	public static String getMyInstitutionPsiId() {
		Institution institution = IntactContext.getCurrentInstance().getInstitution();
		
		for (InstitutionXref xref : institution.getXrefs()) {
			if (CvDatabase.PSI_MI_MI_REF.equals(xref.getCvDatabase().getIdentifier())) {
				return xref.getPrimaryId();
			}
		}
		
		return null;		
	}
	
}