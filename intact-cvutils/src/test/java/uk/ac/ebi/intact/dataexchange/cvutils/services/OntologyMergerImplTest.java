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

import java.net.URL;
import java.util.Collection;

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

    OBOSession carsOboSession;
    OBOSession thingsOboSession;

    @Before
    public void prepareOboSessions() throws Exception {
        carsOboSession = OboUtils.createOBOSession( getCarOntology() );
        Assert.assertNotNull( carsOboSession );
        thingsOboSession = OboUtils.createOBOSession( getThingsOntology() );
        Assert.assertNotNull( thingsOboSession );
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

        // we are merging all terms and subterms of cars inder things->car
        OntologyMergeConfig config = new OntologyMergeConfig( "car:09003", "ID:0000001", true, true );
        OntologyMerger merger = new OntologyMergerImpl();
        final OBOSession mergedOboSession = merger.merge( config, carsOboSession, thingsOboSession );

        OBOObject root = ( OBOObject )  mergedOboSession.getObject( "ID:0000001" );
        Assert.assertNotNull( root );
        Assert.assertEquals( 12, countChildren( root ));

        Assert.assertEquals(3, root.getChildren().size());
        Assert.assertNotNull( getChild(root, "ID:0000003", "house" ) );
        Assert.assertNotNull( getChild(root, "ID:0000002", "truck" ) );

        final OBOObject carRoot = getChild( root, "ID:0000001", "car" );
        Assert.assertNotNull( carRoot );
        Assert.assertEquals( 9, countChildren( carRoot ));
    }

    @Test
    public void merge_excluding_source_recursive() throws Exception {

        // we are merging all terms and subterms of cars inder things->car
        OntologyMergeConfig config = new OntologyMergeConfig( "car:09003", "ID:0000001", true, false );
        OntologyMerger merger = new OntologyMergerImpl();
        final OBOSession mergedOboSession = merger.merge( config, carsOboSession, thingsOboSession );

        OBOObject root = ( OBOObject )  mergedOboSession.getObject( "ID:0000001" );
        Assert.assertNotNull( root );
        Assert.assertEquals( 11, countChildren( root ));

        Assert.assertEquals(3, root.getChildren().size());
        Assert.assertNotNull( getChild(root, "ID:0000003", "house" ) );
        Assert.assertNotNull( getChild(root, "ID:0000002", "truck" ) );

        final OBOObject carRoot = getChild( root, "ID:0000001", "car" );
        Assert.assertNotNull( carRoot );
        Assert.assertEquals( 8, countChildren( carRoot ));
    }

    @Test
    public void merge_excluding_source_non_recursive() throws Exception {

        // we are merging all terms and subterms of cars inder things->car
        OntologyMergeConfig config = new OntologyMergeConfig( "car:09003", "ID:0000001", false, false );
        OntologyMerger merger = new OntologyMergerImpl();
        final OBOSession mergedOboSession = merger.merge( config, carsOboSession, thingsOboSession );

        OBOObject root = ( OBOObject )  mergedOboSession.getObject( "ID:0000001" );
        Assert.assertNotNull( root );
        Assert.assertEquals( 7, countChildren( root ));

        Assert.assertEquals(3, root.getChildren().size());
        Assert.assertNotNull( getChild(root, "ID:0000003", "house" ) );
        Assert.assertNotNull( getChild(root, "ID:0000002", "truck" ) );

        final OBOObject carRoot = getChild( root, "ID:0000001", "car" );
        Assert.assertNotNull( carRoot );
        Assert.assertEquals( 4, countChildren( carRoot ));
    }

    @Test
    public void countChildren() throws Exception {
        OBOObject root = (OBOObject) carsOboSession.getObject( "car:09003" );
        Assert.assertEquals( 8, countChildren( root ));
    }
}
