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
package uk.ac.ebi.intact.dataexchange.psimi.solr.ontology;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import uk.ac.ebi.intact.bridges.ontologies.OntologyDocument;
import uk.ac.ebi.intact.bridges.ontologies.OntologyMapping;
import uk.ac.ebi.intact.bridges.ontologies.iterator.OboOntologyIterator;
import uk.ac.ebi.intact.bridges.ontologies.iterator.OntologyIterator;
import uk.ac.ebi.intact.bridges.ontologies.iterator.UniprotTaxonomyOntologyIterator;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrException;
import uk.ac.ebi.intact.dataexchange.psimi.solr.SolrLogger;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

/**
 * Indexes in a SOLR instance the ontologies passed as URL. The created index is useful
 * to do fast queries to find elements, parents and children in the indexed ontologies.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyIndexer {

    private static final Log log = LogFactory.getLog( OntologyIndexer.class );

    private StreamingUpdateSolrServer solrServer;

    public OntologyIndexer(StreamingUpdateSolrServer solrServer) {
        this.solrServer = solrServer;

        SolrLogger.readFromLog4j();
    }

    public void indexObo(OntologyMapping[] ontologyMappings) throws IntactSolrException {
       indexObo(ontologyMappings, new DefaultDocumentFilter());
    }

    public void indexObo(OntologyMapping[] ontologyMappings, DocumentFilter documentFilter) throws IntactSolrException {
       for (OntologyMapping om : ontologyMappings) {
           indexObo(om.getName(), om.getUrl(), documentFilter);
       }
    }

    public void indexObo(String ontologyName, URL oboUrl) throws IntactSolrException {
        indexObo(ontologyName, oboUrl, new DefaultDocumentFilter());
    }

    public void indexObo(String ontologyName, URL oboUrl, DocumentFilter documentFilter) throws IntactSolrException {
        OntologyIterator oboIterator;

        if ( log.isInfoEnabled() ) log.info( "Starting to index " + ontologyName + " from " + oboUrl );

        try {
            oboIterator = new OboOntologyIterator(ontologyName, oboUrl);
        } catch (Throwable e) {
            throw new IntactSolrException("Problem creating OBO iterator for: "+ontologyName+" URL: "+oboUrl, e);
        }

        indexOntology(oboIterator, documentFilter);
    }

    public void indexUniprotTaxonomy() throws IntactSolrException {
        OntologyIterator ontologyIterator;
        try {
            if ( log.isInfoEnabled() ) log.info( "Starting to index uniprot taxonomy" );
            ontologyIterator = new UniprotTaxonomyOntologyIterator();
        } catch (Throwable e) {
            throw new IntactSolrException("Problem creating default Uniprot taxonomy iterator", e);
        }

        indexOntology(ontologyIterator);
    }

    public void indexOntology(OntologyIterator ontologyIterator) {
        indexOntology(ontologyIterator, new DefaultDocumentFilter());
    }

    public void indexOntology(OntologyIterator ontologyIterator, DocumentFilter documentFilter) {
        int i = 0;

//        while (ontologyIterator.hasNext()) {
//            OntologyDocument doc = ontologyIterator.next();
//            index(doc, documentFilter);


            Iterator<SolrInputDocument> iter = new SolrInputDocumentIterator(ontologyIterator, documentFilter);

            try {
                solrServer.add(iter);
            } catch (Throwable e) {
                throw new IntactSolrException("Problem indexing documents using iterator", e);
            }

//            i++;

//            if (i % 100 == 0) {
//                commitSolr(false);
//                if ( log.isInfoEnabled() ) log.info( i + " terms processed" );
//            }
//        }

        commitSolr(true);
        
//        if ( log.isInfoEnabled() ) log.info( "Completed processing of " + i + " terms." );
    }

    public void index(OntologyDocument ontologyDocument) throws IntactSolrException {
         index(ontologyDocument, new DefaultDocumentFilter());
    }

    public void index(OntologyDocument ontologyDocument, DocumentFilter documentFilter) throws IntactSolrException {
        if (documentFilter != null && !documentFilter.accept(ontologyDocument)) {
            return;
        }

        SolrInputDocument doc = createSolrInputDocument(ontologyDocument);

        try {
            solrServer.add(doc);
        } catch (Exception e) {
            throw new IntactSolrException("Problem adding ontology document to SOLR server: "+ontologyDocument, e);
        }
    }

    private SolrInputDocument createSolrInputDocument(OntologyDocument ontologyDocument) {
        SolrInputDocument doc = new SolrInputDocument();

        String uniqueKey = ontologyDocument.getOntology() + "_" + ontologyDocument.getParentId() + "_" + ontologyDocument.getChildId() + "_" + ontologyDocument.getRelationshipType();
        addField(doc, OntologyFieldNames.ID, uniqueKey, false);
        addField(doc, OntologyFieldNames.ONTOLOGY, ontologyDocument.getOntology(), false);
        addField(doc, OntologyFieldNames.PARENT_ID, ontologyDocument.getParentId(), false);
        addField(doc, OntologyFieldNames.PARENT_NAME, ontologyDocument.getParentName(), true);
        addField(doc, OntologyFieldNames.CHILD_ID, ontologyDocument.getChildId(), false);
        addField(doc, OntologyFieldNames.CHILD_NAME, ontologyDocument.getChildName(), true);
        addField(doc, OntologyFieldNames.RELATIONSHIP_TYPE, ontologyDocument.getRelationshipType(), false);
        addField(doc, OntologyFieldNames.CYCLIC, ontologyDocument.isCyclicRelationship(), false);
        return doc;
    }

    private void addField(SolrInputDocument doc, String fieldName, Object value, boolean addTextCopy) {
        if (value != null) {
            doc.addField(fieldName, value);

            if (addTextCopy) {
                doc.addField(fieldName+"_t", value);
            }
        }
    }

    private void commitSolr(boolean optimize) throws IntactSolrException {
        try {
            solrServer.commit();
            if (optimize) solrServer.optimize();
        } catch (Exception e) {
            throw new IntactSolrException("Problem during commit", e);
        }
    }

    private class SolrInputDocumentIterator implements Iterator<SolrInputDocument> {

        private OntologyIterator ontologyIterator;
        private DocumentFilter documentFilter;

        private OntologyDocument next;

        public SolrInputDocumentIterator(OntologyIterator ontologyIterator,DocumentFilter documentFilter) {
            this.ontologyIterator = ontologyIterator;
            this.documentFilter = documentFilter;
        }

        @Override
        public boolean hasNext() {
            boolean hasNext = ontologyIterator.hasNext();

            if (hasNext) {
                next = ontologyIterator.next();

                if (documentFilter != null && !documentFilter.accept(next)) {
                    return hasNext();
                }

            }

            return hasNext;
        }

        @Override
        public SolrInputDocument next() {
            return createSolrInputDocument(next);
        }

        @Override
        public void remove() {
            ontologyIterator.remove();
        }
    }
}
