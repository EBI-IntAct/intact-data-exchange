/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.CellTypeTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class CellTypeTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public CellTypeTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( CellTypeTest.class );
    }


    public void testProcess_ok() {

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
        CellTypeTag cellType = new CellTypeTag( xref, "shortlabel" );
        assertNotNull( cellType );
        assertEquals( xref, cellType.getPsiDefinition() );
        assertEquals( "shortlabel", cellType.getShortlabel() );
    }


    public void testProcess_ok_no_xref() {

        CellTypeTag cellType = new CellTypeTag( null, "shortlabel" );
        assertNotNull( cellType );
        assertEquals( null, cellType.getPsiDefinition() );
        assertEquals( "shortlabel", cellType.getShortlabel() );
    }


//    public void testProcess_error_no_xref() {
//
//        try {
//            new CellTypeTag( null, "shortlabel" );
//            fail( "cellType without an Xref should not have been permitted." );
//        } catch ( IllegalArgumentException e ) {
//            // ok
//        }
//    }

//    public void testProcess_error_no_xref() {
//
//        try {
//            XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
//            CellTypeTag cellType = new CellTypeTag( xref, "shortlabel" );
//            fail( "cellType without no PSI Xref should not have been permitted." );
//        } catch ( IllegalArgumentException e ) {
//            // ok
//        }
//    }


    public void testProcess_error_no_shortlabel() {

        try {
            XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
            new CellTypeTag( xref, "" );
            fail( "cellType without a shortlabel should not have been permitted." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }
    }
}
