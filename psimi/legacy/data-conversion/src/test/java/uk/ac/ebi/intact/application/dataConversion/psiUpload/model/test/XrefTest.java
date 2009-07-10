/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class XrefTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public XrefTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( XrefTest.class );
    }


    public void testProcess() {

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF,
                                    "11805826", "pubmed", "mySecondaryId", "version1" );
        assertNotNull( xref );
        assertEquals( true, xref.isPrimaryRef() );
        assertEquals( false, xref.isSecondaryRef() );
        assertEquals( "11805826", xref.getId() );
        assertEquals( "pubmed", xref.getDb() );
        assertEquals( "mySecondaryId", xref.getSecondary() );
        assertEquals( "version1", xref.getVersion() );


        xref = new XrefTag( XrefTag.SECONDARY_REF,
                            "11805826", "pubmed", "mySecondaryId", "version1" );
        assertNotNull( xref );
        assertEquals( false, xref.isPrimaryRef() );
        assertEquals( true, xref.isSecondaryRef() );
        assertEquals( "11805826", xref.getId() );
        assertEquals( "pubmed", xref.getDb() );
        assertEquals( "mySecondaryId", xref.getSecondary() );
        assertEquals( "version1", xref.getVersion() );

        // check on giving a wrong type
        try {
            xref = new XrefTag( (short) 10,
                                "11805826", "pubmed", "sec", "ver" );
            fail( "The Xref type must be either PRIMARY or SECONDARY" );
        } catch ( IllegalArgumentException iae ) {
        }

        try {
            xref = new XrefTag( XrefTag.PRIMARY_REF,
                                null, "pubmed", "sec", "ver" );
            fail( "The Xref id must be given" );
        } catch ( IllegalArgumentException iae ) {
        }

        try {
            xref = new XrefTag( XrefTag.PRIMARY_REF,
                                "11805826", null, "sec", "ver" );
            fail( "The Xref db must be given" );
        } catch ( IllegalArgumentException iae ) {
        }
    }
}
