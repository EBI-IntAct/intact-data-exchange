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

import uk.ac.ebi.intact.bridges.ontologies.OntologyDocument;

/**
 * Default document filtering based on relationship types of documents.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class DefaultDocumentFilter implements DocumentFilter {

    /**
     * Used to exclude any document that does not have an "is_a" relationship or it is not a root node
     * (null relationship) or it is cyclic.
     * @param ontologyDocument
     * @return
     */
    public boolean accept(OntologyDocument ontologyDocument) {
        if (ontologyDocument.getRelationshipType() == null) {
            return true;
        }

        final String type = ontologyDocument.getRelationshipType();
        return (( type.equals("is_a") || type.equals("OBO_REL:is_a")) && !ontologyDocument.isCyclicRelationship());
    }
}
