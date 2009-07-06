/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.imex.idservice.keyassigner;

/**
 * Exception thrown by the KeyAssignerService.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: KeyAssignerServiceException.java 8019 2007-04-10 16:02:21Z skerrien $
 * @since <pre>11-May-2006</pre>
 */
public class KeyAssignerServiceException extends Exception {
    public KeyAssignerServiceException( String message ) {
        super( message );
    }

    public KeyAssignerServiceException( String message, Throwable cause ) {
        super( message, cause );
    }
}