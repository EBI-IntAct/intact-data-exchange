/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
 * limitations under the License.
 */
package uk.ac.ebi.intact.psimitab;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;
import uk.ac.ebi.intact.bridges.ontologies.OntologyDocument;
import uk.ac.ebi.intact.bridges.ontologies.OntologyHits;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexSearcher;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Enables autocompletion of CV term based on their identifier.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.2
 */
public class OntologyNameFinder {

    private static final Log log = LogFactory.getLog( OntologyNameFinder.class );

    private OntologyIndexSearcher searcher;

    private Set<String> ontologyNames;

    public OntologyNameFinder( OntologyIndexSearcher searcher ) {

        ontologyNames = new HashSet<String>();

        if ( searcher == null ) {
            log.warn( "The given OntologyIndexSeacher is null, name autocompletion will be disabled." );
        } else {
            this.searcher = searcher;
        }
    }

    public void addOntologyName( String name ) {
        if ( name == null ) {
            throw new IllegalArgumentException( "You must give a non null ontology name" );
        }
        ontologyNames.add( name.toLowerCase() );
    }

    /**
     * verify is te given ontology name is supported by the finder, the name is not case sensitive.
     *
     * @param name
     * @return true of the ontology is supposrted.
     */
    public boolean isOntologySupported( String name ) {
        return name != null && ontologyNames.contains( name.toLowerCase() );
    }

    public String getNameByIdentifier( String identifier ) throws IOException {
        if ( searcher != null ) {
            final OntologyHits ontologyHits = searcher.searchByChildId( identifier );
            if ( ontologyHits.length() > 0 ) {
                final OntologyDocument doc = ontologyHits.doc( 0 );
                return doc.getChildName();
            }
        }
        return null;
    }
}