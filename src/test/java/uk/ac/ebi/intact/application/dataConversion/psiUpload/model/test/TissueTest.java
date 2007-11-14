/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.TissueTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class TissueTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public TissueTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( TissueTest.class );
    }


    public void testProcess_ok() {

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
        TissueTag tissue = new TissueTag( xref, "shortlabel" );
        assertNotNull( tissue );
        assertEquals( xref, tissue.getPsiDefinition() );
        assertEquals( "shortlabel", tissue.getShortlabel() );
    }


    public void testProcess_ok_no_xref() {

        TissueTag tissue = new TissueTag( null, "shortlabel" );
        assertNotNull( tissue );
        assertEquals( null, tissue.getPsiDefinition() );
        assertEquals( "shortlabel", tissue.getShortlabel() );
    }


//    public void testProcess_error_no_xref() {
//
//        try {
//            new TissueTag( null, "shortlabel" );
//            fail( "tissue without an Xref should not have been permitted." );
//        } catch ( IllegalArgumentException e ) {
//            // ok
//        }
//    }

//    public void testProcess_error_no_xref() {
//
//        try {
//            XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
//            new TissueTag( xref, "shortlabel" );
//            fail( "tissue without no PSI Xref should not have been permitted." );
//        } catch ( IllegalArgumentException e ) {
//            // ok
//        }
//    }


    public void testProcess_error_no_shortlabel() {

        try {
            XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
            new TissueTag( xref, "" );
            fail( "tissue without a shortlabel should not have been permitted." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }
    }
}
