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
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.OrganismTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.TissueTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class OrganismTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public OrganismTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( OrganismTest.class );
    }

    public void testProcess_ok() {

        XrefTag xrefCellType = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
        CellTypeTag cellType = new CellTypeTag( xrefCellType, "shortlabel" );

        XrefTag xrefTissue = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
        TissueTag tissue = new TissueTag( xrefTissue, "shortlabel" );

        OrganismTag organism = new OrganismTag( "1234", cellType, tissue );

        assertNotNull( organism );
        assertEquals( "1234", organism.getTaxId() );
        assertEquals( tissue, organism.getTissue() );
        assertEquals( cellType, organism.getCellType() );
    }

    public void testProcess_ok_only_taxid() {

        OrganismTag organism = new OrganismTag( "1234" );

        assertNotNull( organism );
        assertEquals( "1234", organism.getTaxId() );
    }

    public void testProcess_ok_empty_taxid() {

        OrganismTag organism = null;

        try {
            organism = new OrganismTag( "" );
            fail( "should not allow to build an organism with an empty taxid." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( organism );
    }

    public void testProcess_ok_null_taxid() {

        OrganismTag organism = null;

        try {
            organism = new OrganismTag( null );
            fail( "should not allow to build an organism with a null taxid." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( organism );
    }

    public void testProcess_ok_non_integer_taxid() {

        OrganismTag organism = null;

        try {
            organism = new OrganismTag( "abc" );
            fail( "should not allow to build an organism with a non integer taxid." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( organism );
    }
}