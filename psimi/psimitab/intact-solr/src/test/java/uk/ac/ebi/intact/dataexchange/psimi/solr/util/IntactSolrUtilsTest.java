/**
 * Copyright 2012 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.dataexchange.psimi.solr.util;

import org.junit.Test;
import uk.ac.ebi.intact.dataexchange.psimi.solr.AbstractSolrTestCase;
import uk.ac.ebi.intact.dataexchange.psimi.solr.CoreNames;

import static org.junit.Assert.assertTrue;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactSolrUtilsTest extends AbstractSolrTestCase {

    @Test
    public void testRetrieveSchemaInfo() throws Exception {
        final SchemaInfo schemaInfo = IntactSolrUtils.retrieveSchemaInfo(getSolrJettyRunner().getSolrServer(CoreNames.CORE_PUB));



        assertTrue(schemaInfo.hasFieldName("line"));
        assertTrue(schemaInfo.hasFieldName("mitab"));
        assertTrue(schemaInfo.hasFieldName("idA"));
        assertTrue(schemaInfo.hasFieldName("idB"));
        assertTrue(schemaInfo.hasFieldName("altidA"));
        assertTrue(schemaInfo.hasFieldName("altidB"));
        assertTrue(schemaInfo.hasFieldName("detmethod"));
        assertTrue(schemaInfo.hasFieldName("pubauth"));
        assertTrue(schemaInfo.hasFieldName("pubid"));
        assertTrue(schemaInfo.hasFieldName("taxidA"));
        assertTrue(schemaInfo.hasFieldName("taxidB"));
        assertTrue(schemaInfo.hasFieldName("type"));
        assertTrue(schemaInfo.hasFieldName("source"));
        assertTrue(schemaInfo.hasFieldName("interaction_id"));
        assertTrue(schemaInfo.hasFieldName("confidence"));
        assertTrue(schemaInfo.hasFieldName("id"));
        assertTrue(schemaInfo.hasFieldName("identifier"));
        assertTrue(schemaInfo.hasFieldName("altid"));
        assertTrue(schemaInfo.hasFieldName("alias"));
        assertTrue(schemaInfo.hasFieldName("taxid"));
        assertTrue(schemaInfo.hasFieldName("species"));
        assertTrue(schemaInfo.hasFieldName("pkey"));
        assertTrue(schemaInfo.hasFieldName("experimentalRoleA"));
        assertTrue(schemaInfo.hasFieldName("experimentalRoleB"));
        assertTrue(schemaInfo.hasFieldName("experimentalRole"));
        assertTrue(schemaInfo.hasFieldName("biologicalRoleA"));
        assertTrue(schemaInfo.hasFieldName("biologicalRoleB"));
        assertTrue(schemaInfo.hasFieldName("biologicalRole"));
        assertTrue(schemaInfo.hasFieldName("propertiesA"));
        assertTrue(schemaInfo.hasFieldName("propertiesB"));
        assertTrue(schemaInfo.hasFieldName("properties"));
        assertTrue(schemaInfo.hasFieldName("typeA"));
        assertTrue(schemaInfo.hasFieldName("typeB"));
        assertTrue(schemaInfo.hasFieldName("interactorType"));
        assertTrue(schemaInfo.hasFieldName("hostOrganism"));
        assertTrue(schemaInfo.hasFieldName("expansion"));
        assertTrue(schemaInfo.hasFieldName("dataset"));
        assertTrue(schemaInfo.hasFieldName("annotationA"));
        assertTrue(schemaInfo.hasFieldName("annotationB"));
        assertTrue(schemaInfo.hasFieldName("annotation"));
        assertTrue(schemaInfo.hasFieldName("parameterA"));
        assertTrue(schemaInfo.hasFieldName("parameterB"));
        assertTrue(schemaInfo.hasFieldName("parameter"));
        assertTrue(schemaInfo.hasFieldName("parameterInteraction"));
        assertTrue(schemaInfo.hasFieldName("go"));
        assertTrue(schemaInfo.hasFieldName("interpro"));
        assertTrue(schemaInfo.hasFieldName("psi-mi"));
        assertTrue(schemaInfo.hasFieldName("chebi"));
        assertTrue(schemaInfo.hasFieldName("rigid"));
        assertTrue(schemaInfo.hasFieldName("geneName"));
        assertTrue(schemaInfo.hasFieldName("relevancescore"));
        assertTrue(schemaInfo.hasFieldName("evidences"));
        assertTrue(schemaInfo.hasFieldName("spell"));
        assertTrue(schemaInfo.hasFieldName("uniprotkb"));
        assertTrue(schemaInfo.hasFieldName("intact-miscore"));
        assertTrue(schemaInfo.hasFieldName("timestamp"));
    }
}
