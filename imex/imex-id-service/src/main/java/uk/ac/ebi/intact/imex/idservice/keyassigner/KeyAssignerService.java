/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.imex.idservice.keyassigner;

import uk.ac.ebi.intact.imex.idservice.id.IMExRange;


/**
 * Contract of a KeyAssignerService
 *
 * @author Arnaud Ceol
 */
public interface KeyAssignerService {

    /**
     * Request an IMEx IDs from the Key Assigner.
     *
     * @return an IMExId.
     *
     * @throws KeyAssignerServiceException
     *
     */
    public IMExRange getAccessions( int howMany ) throws KeyAssignerServiceException;
}