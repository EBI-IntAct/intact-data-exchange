/*
 * Copyright (c) 2008 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
 
package uk.ac.ebi.intact.psimitab;

import org.junit.Test;
import org.junit.BeforeClass;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;
import junit.framework.Assert;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexWriter;
import uk.ac.ebi.intact.bridges.ontologies.OntologyDocument;
import uk.ac.ebi.intact.bridges.ontologies.iterator.OboOntologyIterator;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * OntologyNameFinder Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyNameFinderTest {

    private static File getTargetDirectory() {
        String outputDirPath = OntologyNameFinderTest.class.getResource( "/" ).getFile();
        Assert.assertNotNull( outputDirPath );
        File outputDir = new File( outputDirPath );
        // we are in test-classes, move one up
        outputDir = outputDir.getParentFile();
        Assert.assertNotNull( outputDir );
        Assert.assertTrue( outputDir.isDirectory() );
        Assert.assertEquals( "target", outputDir.getName() );
        return outputDir;
    }

    private static Directory ontologyDirectory;

    @BeforeClass
    public static void buildIndex() throws Exception {

        final URI uri = OntologyNameFinderTest.class.getResource( "/ontologies/go_slim.obo" ).toURI();
        Assert.assertNotNull( uri );
        File f = new File( getTargetDirectory(), "ontologyIndex" );
        ontologyDirectory = FSDirectory.getDirectory( f );
        OntologyIndexWriter writer = new OntologyIndexWriter( ontologyDirectory, true );

        OboOntologyIterator iterator = new OboOntologyIterator( "obo_slim_plant", uri.toURL() );
        while ( iterator.hasNext() ) {
            final OntologyDocument ontologyDocument = iterator.next();
            writer.addDocument( ontologyDocument );
        }

        writer.flush();
        writer.optimize();
        writer.close();
    }

    @Test
    public void getNameByIdentifier() throws IOException {
        OntologyNameFinder finder = new OntologyNameFinder( ontologyDirectory );
        final String name = finder.getNameByIdentifier( "GO:0045182" );
        Assert.assertNotNull( name );
        Assert.assertEquals( "translation regulator activity", name );
    }

    @Test
    public void isSupported() throws Exception {
        OntologyNameFinder finder = new OntologyNameFinder( null );
        finder.addDatabaseName( "GO" );
        Assert.assertTrue( finder.isOntologySupported( "go" ) );
        Assert.assertTrue( finder.isOntologySupported( "Go" ) );
        Assert.assertTrue( finder.isOntologySupported( "GO" ) );
        Assert.assertFalse( finder.isOntologySupported( "interpro" ) );
    }
}
