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
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.HostOrganismTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.TissueTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class HostOrganismTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public HostOrganismTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( HostOrganismTest.class );
    }

    public void testProcess_ok() {

        XrefTag xrefCellType = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
        CellTypeTag cellType = new CellTypeTag( xrefCellType, "shortlabel" );

        XrefTag xrefTissue = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
        TissueTag tissue = new TissueTag( xrefTissue, "shortlabel" );

        HostOrganismTag hostOrganism = new HostOrganismTag( "1234", cellType, tissue );

        assertNotNull( hostOrganism );
        assertEquals( "1234", hostOrganism.getTaxId() );
        assertEquals( tissue, hostOrganism.getTissue() );
        assertEquals( cellType, hostOrganism.getCellType() );
    }

    public void testProcess_ok_only_taxid() {

        HostOrganismTag hostOrganism = new HostOrganismTag( "1234" );

        assertNotNull( hostOrganism );
        assertEquals( "1234", hostOrganism.getTaxId() );
    }

    public void testProcess_ok_empty_taxid() {

        HostOrganismTag hostOrganism = null;

        try {
            hostOrganism = new HostOrganismTag( "" );
            fail( "should not allow to build a hostOrganism with an empty taxid." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( hostOrganism );
    }

    public void testProcess_ok_null_taxid() {

        HostOrganismTag hostOrganism = null;

        try {
            hostOrganism = new HostOrganismTag( null );
            fail( "should not allow to build a hostOrganism with a null taxid." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( hostOrganism );
    }

    public void testProcess_ok_non_integer_taxid() {

        HostOrganismTag hostOrganism = null;

        try {
            hostOrganism = new HostOrganismTag( "abc" );
            fail( "should not allow to build a hostOrganism with a non integer taxid." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( hostOrganism );
    }
}