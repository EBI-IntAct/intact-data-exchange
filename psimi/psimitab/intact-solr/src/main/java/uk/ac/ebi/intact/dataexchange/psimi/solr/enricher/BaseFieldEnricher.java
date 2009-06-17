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
package uk.ac.ebi.intact.dataexchange.psimi.solr.enricher;

import psidev.psi.mi.tab.model.builder.Field;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;

import java.util.*;

import org.apache.solr.client.solrj.SolrServerException;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BaseFieldEnricher implements FieldEnricher {

    private Set<String> expandableOntologies;

    public BaseFieldEnricher() {
        expandableOntologies = new HashSet<String>( );
        createDefaultExpandableOntologies();
    }

    private void createDefaultExpandableOntologies() {
        expandableOntologies.add( FieldNames.DB_GO );
        expandableOntologies.add( FieldNames.DB_INTERPRO );
        expandableOntologies.add( FieldNames.DB_CHEBI );
        expandableOntologies.add( FieldNames.DB_PSIMI );
        expandableOntologies.add( "taxid" );
    }

    public Field enrich(Field field) throws Exception {
         return field;
    }

    public Collection<Field> getAllParents(Field field, boolean includeItself) throws SolrServerException {
        Collection<Field> fields = new ArrayList<Field>();
        if (includeItself) {
            fields.add(field);
        }
        return fields;
    }

     public boolean isExpandableOntology( String name ) {
        return ( expandableOntologies.contains( name ) );
    }



}