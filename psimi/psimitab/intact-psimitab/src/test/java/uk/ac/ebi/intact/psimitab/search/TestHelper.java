/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.psimitab.search;

import junit.framework.Assert;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.obo.dataadapter.OBOParseException;
import psidev.psi.mi.search.Searcher;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineException;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.intact.bridges.ontologies.OntologyDocument;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexWriter;
import uk.ac.ebi.intact.bridges.ontologies.iterator.OboOntologyIterator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * PSIMITAB Test Helper.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: TestHelper.java 801 2007-10-09 14:01:14Z skerrien $
 */
public abstract class TestHelper {

    public static Directory createIndexFromResource(String resourcePath) throws IOException, ConverterException, MitabLineException {
        InputStream is = TestHelper.class.getResourceAsStream(resourcePath);
        return Searcher.buildIndexInMemory(is, true, true, new IntactPsimiTabIndexWriter());
    }

    public static Directory createIndexFromResource(String resourcePath, Directory ontologyDirectory) throws IOException, ConverterException, MitabLineException {
        InputStream is = TestHelper.class.getResourceAsStream(resourcePath);
        return Searcher.buildIndexInMemory(is, true, true, new IntactPsimiTabIndexWriter(ontologyDirectory));
    }

    public static Directory createIndexFromLine(String line) throws IOException, ConverterException, MitabLineException {
        return Searcher.buildIndexInMemory(new ByteArrayInputStream(line.getBytes()), true, false, new IntactPsimiTabIndexWriter(new RAMDirectory()));
    }

    public static Directory createIndexFromLine(String line, Directory ontologyDirectory) throws IOException, ConverterException, MitabLineException {
        return Searcher.buildIndexInMemory(new ByteArrayInputStream(line.getBytes()), true, false, new IntactPsimiTabIndexWriter(ontologyDirectory));
    }

     public static Directory buildOntologiesIndex(Map<String,URL> urls) throws Exception {

        File f = new File( getTargetDirectory(), "ontologyIndex" );
        Directory ontologyDirectory = FSDirectory.getDirectory( f );
        OntologyIndexWriter writer = new OntologyIndexWriter( ontologyDirectory, true );

        for (Map.Entry<String,URL> entry : urls.entrySet()) {
            addOntologyToIndex( entry.getValue(), entry.getKey() ,writer );
        }

        writer.flush();
        writer.optimize();
        writer.close();

        return ontologyDirectory;
    }

    public static Directory buildDefaultOntologiesIndex() throws Exception {
        Map<String,URL> urls = new HashMap<String,URL>();
        urls.put("go", new URL("http://www.geneontology.org/ontology/gene_ontology_edit.obo"));
        urls.put("psi-mi", new URL("http://psidev.sourceforge.net/mi/rel25/data/psi-mi25.obo"));

        return buildOntologiesIndex(urls);
    }

    private static File getTargetDirectory() {
        String outputDirPath = IntActDocumentBuilderTest.class.getResource( "/" ).getFile();
        Assert.assertNotNull( outputDirPath );
        File outputDir = new File( outputDirPath );
        // we are in test-classes, move one up
        outputDir = outputDir.getParentFile();
        Assert.assertNotNull( outputDir );
        Assert.assertTrue( outputDir.isDirectory() );
        Assert.assertEquals( "target", outputDir.getName() );
        return outputDir;
    }


    private static void addOntologyToIndex( URL goUrl, String ontology, OntologyIndexWriter writer ) throws OBOParseException,
                                                                                                            IOException {
        OboOntologyIterator iterator = new OboOntologyIterator( ontology, goUrl );
        while ( iterator.hasNext() ) {
            final OntologyDocument ontologyDocument = iterator.next();
            writer.addDocument( ontologyDocument );
        }
    }
}
