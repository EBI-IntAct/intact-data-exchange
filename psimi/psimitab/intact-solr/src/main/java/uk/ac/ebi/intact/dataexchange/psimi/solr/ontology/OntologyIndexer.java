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
import org.apache.solr.common.SolrInputDocument;
import uk.ac.ebi.intact.bridges.ontologies.OntologyDocument;
import uk.ac.ebi.intact.bridges.ontologies.OntologyMapping;
import uk.ac.ebi.intact.bridges.ontologies.iterator.OboOntologyIterator;
import uk.ac.ebi.intact.bridges.ontologies.iterator.OntologyIterator;
import uk.ac.ebi.intact.bridges.ontologies.iterator.UniprotTaxonomyOntologyIterator;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrException;
import uk.ac.ebi.intact.dataexchange.psimi.solr.SolrLogger;

import java.net.URL;
import java.util.Enumeration;
import java.util.logging.LogManager;
import java.util.logging.Level;

/**
 * Indexes in a SOLR instance the ontologies passed as URL. The created index is useful
 * to do fast queries to find elements, parents and children in the indexed ontologies.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyIndexer {

    private SolrServer solrServer;

    public OntologyIndexer(SolrServer solrServer) {
        this.solrServer = solrServer;

        SolrLogger.readFromLog4j();
    }

    public void indexObo(OntologyMapping[] ontologyMappings) throws IntactSolrException {
       for (OntologyMapping om : ontologyMappings) {
           indexObo(om.getName(), om.getUrl());
       }
    }

    public void indexObo(String ontologyName, URL oboUrl) throws IntactSolrException {
        OntologyIterator oboIterator;
        try {
            oboIterator = new OboOntologyIterator(ontologyName, oboUrl);
        } catch (Throwable e) {
            throw new IntactSolrException("Problem creating OBO iterator for: "+ontologyName+" URL: "+oboUrl, e);
        }

        indexOntology(oboIterator);
    }

    public void indexUniprotTaxonomy() throws IntactSolrException {
        OntologyIterator ontologyIterator;
        try {
            ontologyIterator = new UniprotTaxonomyOntologyIterator();
        } catch (Throwable e) {
            throw new IntactSolrException("Problem creating default Uniprot taxonomy iterator", e);
        }

        indexOntology(ontologyIterator);
    }

    public void indexOntology(OntologyIterator ontologyIterator) {
        int i = 0;

        while (ontologyIterator.hasNext()) {
            OntologyDocument doc = ontologyIterator.next();
            index(doc);

            i++;

            if (i % 100 == 0) {
                commitSolr(false);
            }
        }

        commitSolr(true);
    }

    public void index(OntologyDocument ontologyDocument) throws IntactSolrException {
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

        try {
            solrServer.add(doc);
        } catch (Exception e) {
            throw new IntactSolrException("Problem adding ontology document to SOLR server: "+ontologyDocument, e);
        }
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
}
