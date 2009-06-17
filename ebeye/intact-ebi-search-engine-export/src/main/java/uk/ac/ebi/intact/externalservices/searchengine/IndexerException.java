/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.externalservices.searchengine;

/**
 * TODO comment this
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01-Feb-2007</pre>
 */
public class IndexerException extends Exception {


    public IndexerException( Throwable cause ) {
        super( cause );
    }

    public IndexerException( String message, Throwable cause ) {
        super( message, cause );
    }
}