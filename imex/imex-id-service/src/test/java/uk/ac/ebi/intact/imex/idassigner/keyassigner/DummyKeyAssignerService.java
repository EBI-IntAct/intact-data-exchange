/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.imex.idassigner.keyassigner;


import uk.ac.ebi.intact.imex.idservice.id.IMExRange;
import uk.ac.ebi.intact.imex.idservice.keyassigner.KeyAssignerServiceException;
import uk.ac.ebi.intact.imex.idservice.keyassigner.KeyAssignerService;

/**
 * Dummy version of the KeyAssignerService.
 * <p/>
 * This could be used in test so it doesn't involve any Network Connections.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: DummyKeyAssignerService.java 8158 2007-04-18 11:49:07Z skerrien $
 * @since <pre>16-May-2006</pre>
 */
public class DummyKeyAssignerService implements KeyAssignerService {

    ////////////////////////
    // Instnace variables

    private long currentId = 1;
    private long submissionId = 1;

    /**
     * Constructor allowing to set the starting key and the starting submissionId.
     *
     * @param startKey          the first key that will be returned by the key assigner
     * @param startSubmissionId the first submission id.
     */
    public DummyKeyAssignerService( long startKey, long startSubmissionId ) {
        currentId = startKey;
        submissionId = startSubmissionId;
    }

    /**
     * Simulates the request of 'howMany' Id from the service.
     * <p/>
     * the submission id gets incremented by 1 after each call.
     *
     * @param howMany
     *
     * @return
     *
     * @throws KeyAssignerServiceException
     */
    public synchronized IMExRange getAccessions( int howMany ) throws KeyAssignerServiceException {

        long subId = submissionId;
        submissionId++;

        long from = currentId;
        long to = currentId + howMany - 1;

        currentId += howMany;

        return new IMExRange( subId, from, to, "testPartner" );
    }

    public long getLastAccessionReturned() {
        return currentId - 1;
    }

    public long getLastSubmissionId() {
        return submissionId - 1;
    }
}