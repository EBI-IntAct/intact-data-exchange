/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dbutil.cv;

/**
 * TODO comment this
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16-Feb-2006</pre>
 */
public class PsiLoaderException extends Exception {
    public PsiLoaderException( String message ) {
        super( message );
    }

    public PsiLoaderException( Throwable cause ) {
        super( cause );
    }

    public PsiLoaderException( String message, Throwable cause ) {
        super( message, cause );
    }
}