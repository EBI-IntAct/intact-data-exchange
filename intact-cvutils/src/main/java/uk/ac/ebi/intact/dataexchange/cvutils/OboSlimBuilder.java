/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.dataexchange.cvutils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;
import java.util.Collection;
import java.util.Set;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.obo.datamodel.*;
import org.obo.datamodel.impl.DefaultObjectFactory;
import org.obo.datamodel.impl.OBOSessionImpl;
import org.obo.dataadapter.OBOParseException;
import org.bbop.dataadapter.DataAdapterException;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.CvDagObject;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvObjectOntologyBuilder;

/**
 * Obo Slim Builder - builds a simplified/lighter obo ontology based on a couple of given terms.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.3
 */
public class OboSlimBuilder {

    private static final Log log = LogFactory.getLog( OboSlimBuilder.class );

    private boolean includeParents;

    private boolean includeChildren;


    private URL ontologyURL;

    private Set<String> terms;

    //////////////////
    // Constructors

    public OboSlimBuilder() {
        terms = Sets.newHashSet();
    }

    ///////////////////
    // Getters and Setters

    public void setOboLocation( URL url ) {
        ontologyURL = url;
    }

    public URL getOntologyURL() {
        return ontologyURL;
    }

    public boolean isIncludeParents() {
        return includeParents;
    }

    public void setIncludeParents( boolean includeParents ) {
        this.includeParents = includeParents;
    }

    public void setIncludeChildren( boolean includeChildren ) {
        this.includeChildren = includeChildren;
    }

    public boolean isIncludeChildren() {
        return includeChildren;
    }

    public Set<String> getTerms() {
        return terms;
    }

    public void addTerm( String termId ) {
        terms.add( termId );
    }

    //////////////////////
    // Builder

    public void build( File file ) throws OBOParseException, IOException, DataAdapterException {

        if ( log.isDebugEnabled() ) {
            log.debug( "Building a slim ontology based on " + terms.size() + " given term(s)..." );
        }

        final OBOSession session = OboUtils.createOBOSession( ontologyURL );

        // Issue, that class is MI specific, no way to load ChEBI into it :(
        final List<OBOObject> allOboObjects = Lists.newArrayList();
        final boolean recursive = true;
        for ( String termId : terms ) {
            OBOObject oboObject = ( OBOObject ) session.getObject( termId );
            allOboObjects.add( oboObject );

            if( oboObject == null ) {
                throw new RuntimeException( "Could not find term " + termId + " in the given ontology." );
            }

            if( includeParents ) allOboObjects.addAll( OboUtils.findParents( oboObject, recursive ) );
            if( includeChildren ) allOboObjects.addAll( OboUtils.findChildren( oboObject, recursive ) );
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "Found " + allOboObjects.size() + " term(s), now saving your subset..." );
        }

        ObjectFactory objFactory = new DefaultObjectFactory();
        OBOSession session2export = new OBOSessionImpl( objFactory );

        // Transfers Session meta data such as synonym category, namespaces
        for ( SynonymCategory category : session.getSynonymCategories() ) {
            session2export.addSynonymCategory( category );
        }

        session2export.setDefaultNamespace( session.getDefaultNamespace() );
        for ( Namespace namespace : session.getNamespaces() ) {
            session2export.addNamespace( namespace );
        }

        for ( TermCategory category : session.getCategories() ) {
            session2export.addCategory( category );
        }

        // Add all selected OBO objects
        for ( OBOObject oboObject : allOboObjects ) {
            session2export.addObject( oboObject );
        }

        OboUtils.saveSession( session2export, file );
    }
}
