/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.util;

import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.Xref;

import java.util.Iterator;

/**
 * TODO comment this
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29-Jun-2005</pre>
 */
public class ToolBox {

    public static String getPsiReference( CvObject cv ) {

        if ( cv == null ) {
            throw new IllegalArgumentException( "the given CvObject must not be null." );
        }

        for ( Iterator iterator = cv.getXrefs().iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();

            if ( CvDatabase.PSI_MI.equals( xref.getCvDatabase().getShortLabel() ) ) {
                return xref.getPrimaryId();
            }
        }

        return null;
    }
}
