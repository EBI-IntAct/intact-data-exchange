// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.model;

import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.Institution;

/**
 * Allow to create a Protein to which we can set an AC.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class TestableBioSource extends BioSource {

    public TestableBioSource() {
    
    }

    public TestableBioSource( String ac, Institution owner, String shortLabel, String taxid ) {

        super( owner, shortLabel, taxid );

        if ( ac == null ) {
            throw new IllegalArgumentException( "You must give a non null AC." );
        }
        this.ac = ac;
    }
}