/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */

package uk.ac.ebi.intact.imex.idassigner.keyassigner;

import junit.framework.JUnit4TestAdapter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import uk.ac.ebi.intact.imex.idservice.id.IMExRange;
import uk.ac.ebi.intact.imex.idservice.keyassigner.KeyAssignerServiceException;
import uk.ac.ebi.intact.imex.idservice.keyassigner.KeyAssignerService;

/**
 * DummyKeyAssignerService Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: DummyKeyAssignerServiceTest.java 4871 2006-05-18 08:21:32Z skerrien $
 * @since <pre>05/16/2006</pre>
 */
public class DummyKeyAssignerServiceTest {

    ////////////////////////////////
    // Compatibility with JUnit 3

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter( DummyKeyAssignerServiceTest.class );
    }

    /////////////////////
    // Tests

    @Test
    public void getAccessions() {
        KeyAssignerService service = new DummyKeyAssignerService( 1, 1 );
        try {
            IMExRange range = service.getAccessions( 5 );

            assertEquals( 1L, range.getSubmissionId() );
            assertEquals( 1L, range.getFrom() );
            assertEquals( 5L, range.getTo() );

            range = service.getAccessions( 2 );

            assertEquals( 2L, range.getSubmissionId() );
            assertEquals( 6L, range.getFrom() );
            assertEquals( 7L, range.getTo() );

            range = service.getAccessions( 10 );

            assertEquals( 3L, range.getSubmissionId() );
            assertEquals( 8L, range.getFrom() );
            assertEquals( 17L, range.getTo() );

        } catch ( KeyAssignerServiceException e ) {
            fail();
        }
    }
}
