/*
 * Copyright (c) 2008 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */

package uk.ac.ebi.intact.dataexchange.cvutils.services;

import org.junit.Test;
import org.junit.Before;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.Link;
import junit.framework.Assert;
import uk.ac.ebi.intact.dataexchange.cvutils.OboUtils;
import uk.ac.ebi.intact.dataexchange.cvutils.CvExporter;

import java.net.URL;
import java.util.Collection;
import java.io.File;

/**
 * OntologyMergerImpl Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyMergerImplTest {

    private URL getCarOntology() throws Exception {
        return OntologyMergerImplTest.class.getResource( "/ontologies/cars.obo" ).toURI().toURL();
    }

    private URL getThingsOntology() throws Exception {
        return OntologyMergerImplTest.class.getResource( "/ontologies/things.obo" ).toURI().toURL();
    }

    private URL getPsiMiOntology() throws Exception {
        return OntologyMergerImplTest.class.getResource( "/psi-mi25-next.obo" ).toURI().toURL();
    }

    private URL getBiosapiensDgiOntology() throws Exception {
        return OntologyMergerImplTest.class.getResource( "/Druggability.obo" ).toURI().toURL();
    }

    OBOSession carsOboSession;
    OBOSession thingsOboSession;
    OBOSession psimiOboSession;
    OBOSession biosapiensOboSession;


    @Before
    public void prepareOboSessions() throws Exception {
        carsOboSession = OboUtils.createOBOSession( getCarOntology() );
        Assert.assertNotNull( carsOboSession );
        thingsOboSession = OboUtils.createOBOSession( getThingsOntology() );
        Assert.assertNotNull( thingsOboSession );
        psimiOboSession = OboUtils.createOBOSession( getPsiMiOntology() );
        Assert.assertNotNull( psimiOboSession );
        biosapiensOboSession = OboUtils.createOBOSession( getBiosapiensDgiOntology() );
        Assert.assertNotNull( biosapiensOboSession );
    }

    private int countChildren( OBOObject term ) {
        int localCount = 0;
        for ( Link link : term.getChildren() ) {
            localCount += 1 + countChildren( (OBOObject) link.getChild() );
        }
        return localCount;
    }

    private OBOObject getChild( OBOObject term, String id, String name ) {
        if( id == null && name == null ) {
            throw new IllegalArgumentException( "either id or name should not be null." );
        }
        final Collection<Link> children = term.getChildren();
        for ( Link link : children ) {
            final OBOObject child = ( OBOObject ) link.getChild();
            if( id !=null && child.getID().equals( id ) ) {
                return child;
            }
            if( name !=null && child.getName().equals( name ) ) {
                return child;
            }
        }
        return null;
    }

    @Test
    public void merge_including_source_recursive() throws Exception {

        // we are merging all terms and subterms of cars under things->car
        OntologyMergeConfig config = new OntologyMergeConfig( "car:09003", "ID:0000001", true, true );

        OBOObject rootBeforeMerge = ( OBOObject )  thingsOboSession.getObject( "ID:0000001" );
        Assert.assertEquals(0, rootBeforeMerge.getChildren().size());

        OntologyMerger merger = new OntologyMergerImpl();
        final OBOSession mergedOboSession = merger.merge( config, carsOboSession, thingsOboSession );

        OBOObject root = ( OBOObject )  mergedOboSession.getObject( "ID:0000001" );
        Assert.assertNotNull( root );

        //all 9 terms including the source
        Assert.assertEquals( 9, countChildren( root ));

        //immediate child object is only one
        Assert.assertEquals(1, root.getChildren().size());


        OBOObject carTerm = ( OBOObject )  mergedOboSession.getObject( "car:09003" );
        Assert.assertNotNull( carTerm );
        Assert.assertNotNull( getChild(carTerm, "car:09004", "ford" ) );
        Assert.assertNotNull( getChild(carTerm, "car:09005", "vauxhall" ) );


    }

    @Test
    public void merge_excluding_source_recursive() throws Exception {

        // we are merging all terms and subterms of cars inder things->car
        OntologyMergeConfig config = new OntologyMergeConfig( "car:09003", "ID:0000001", true, false );
        OntologyMerger merger = new OntologyMergerImpl();
        final OBOSession mergedOboSession = merger.merge( config, carsOboSession, thingsOboSession );

        OBOObject root = ( OBOObject )  mergedOboSession.getObject( "ID:0000001" );
        Assert.assertNotNull( root );

        //8 terms excluding the source
        Assert.assertEquals( 8, countChildren( root ));

        //now it has four immediete child
        Assert.assertEquals(4, root.getChildren().size());

        Assert.assertNotNull( getChild(root, "car:09004", "ford" ) );
        Assert.assertNotNull( getChild(root, "car:09005", "vauxhall" ) );

    }

    @Test
    public void merge_excluding_source_non_recursive() throws Exception {

        // we are merging all terms and subterms of cars inder things->car
        OntologyMergeConfig config = new OntologyMergeConfig( "car:09003", "ID:0000001", false, false );
        OntologyMerger merger = new OntologyMergerImpl();
        final OBOSession mergedOboSession = merger.merge( config, carsOboSession, thingsOboSession );

        OBOObject root = ( OBOObject )  mergedOboSession.getObject( "ID:0000001" );
        Assert.assertNotNull( root );
        //non-recursive so no children added
        Assert.assertEquals( 0, countChildren( root ));

        Assert.assertEquals(0, root.getChildren().size());
        Assert.assertNull( getChild(root, "car:09004", "ford" ) );
        Assert.assertNull( getChild(root, "car:09005", "vauxhall") );

    }

    @Test
    public void countChildren() throws Exception {
        OBOObject root = (OBOObject) carsOboSession.getObject( "car:09003" );
        Assert.assertEquals( 8, countChildren( root ));
    }


    @Test
    public void mergeAndExportTest() throws Exception {

        OntologyMergeConfig config = new OntologyMergeConfig( "car:09003", "ID:0000001", true, true );
        OBOObject rootBeforeMerge = ( OBOObject ) thingsOboSession.getObject( "ID:0000001" );
        Assert.assertEquals( 0, rootBeforeMerge.getChildren().size() );

        OntologyMerger merger = new OntologyMergerImpl();
        final OBOSession mergedOboSession = merger.merge( config, carsOboSession, thingsOboSession );

        CvExporter downloadCv = new CvExporter();

        // Create temp directory
        File tempDir = new File( "temp" );
        tempDir.mkdir();
        File outFile = File.createTempFile( "test", ".obo", tempDir );
        downloadCv.writeOBOFile( mergedOboSession, outFile );
    }


     @Test
    public void mergeAndExportBiosapiensTest() throws Exception {

        OntologyMergeConfig config = new OntologyMergeConfig( "MI:0590", "BS:09003", true, true );
        OBOObject rootBeforeMerge = ( OBOObject ) biosapiensOboSession.getObject( "BS:09003" );
        Assert.assertEquals( 0, rootBeforeMerge.getChildren().size() );

        OntologyMerger merger = new OntologyMergerImpl();
        final OBOSession mergedOboSession = merger.merge( config, psimiOboSession, biosapiensOboSession );

        CvExporter downloadCv = new CvExporter();

        // Create temp directory
        File tempDir = new File( "temp" );
        tempDir.mkdir();
        File outFile = File.createTempFile( "psimibiosapiens", ".obo", tempDir );
        downloadCv.writeOBOFile( mergedOboSession, outFile );

    }



}
