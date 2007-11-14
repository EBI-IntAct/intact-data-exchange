/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.*;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ProteinInteractorTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public ProteinInteractorTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( ProteinInteractorTest.class );
    }

    public void testProcess_ok() {

        XrefTag xrefCellType = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
        CellTypeTag cellType = new CellTypeTag( xrefCellType, "shortlabel" );

        XrefTag xrefTissue = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
        TissueTag tissue = new TissueTag( xrefTissue, "shortlabel" );

        OrganismTag organism = new OrganismTag( "1234", cellType, tissue );

        XrefTag xrefUniprot = new XrefTag( XrefTag.PRIMARY_REF, "P12345", "uniprot" );

        ProteinInteractorTag proteinInteractor = new ProteinInteractorTag( xrefUniprot, organism );


        assertNotNull( proteinInteractor );
        assertEquals( xrefUniprot, proteinInteractor.getPrimaryXref() );
        assertEquals( organism, proteinInteractor.getOrganism() );
    }


    public void testProcess_ok_no_organism() {

        XrefTag xrefUniprot = new XrefTag( XrefTag.PRIMARY_REF, "P12345", "uniprot" );

        ProteinInteractorTag proteinInteractor = new ProteinInteractorTag( xrefUniprot, null );

        assertNotNull( proteinInteractor );
        assertEquals( xrefUniprot, proteinInteractor.getPrimaryXref() );
        assertEquals( null, proteinInteractor.getOrganism() );
    }


    public void testProcess_no_uniprot_xref() {

        XrefTag xrefUniprot = new XrefTag( XrefTag.PRIMARY_REF, "uniparc", "UPI:00000000000000012" );

        ProteinInteractorTag proteinInteractor = null;
        try {
            proteinInteractor = new ProteinInteractorTag( xrefUniprot, null );
            fail( "Should not allow to create a proteinInteractor without a uniprot Xref." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( proteinInteractor );
    }


    public void testProcess_error_xref_null() {

        ProteinInteractorTag proteinInteractor = null;
        try {
            proteinInteractor = new ProteinInteractorTag( null, null );
            fail( "Should not allow to create a proteinInteractor with a null uniprot Xref." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( proteinInteractor );
    }
}