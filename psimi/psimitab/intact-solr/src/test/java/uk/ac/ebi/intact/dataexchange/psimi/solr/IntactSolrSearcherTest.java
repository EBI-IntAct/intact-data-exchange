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
package uk.ac.ebi.intact.dataexchange.psimi.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.hupo.psi.mi.psicquic.model.PsicquicSolrException;
import org.hupo.psi.mi.psicquic.model.PsicquicSolrServer;
import org.junit.Assert;
import org.junit.Test;
import psidev.psi.mi.tab.model.*;
import uk.ac.ebi.intact.bridges.ontologies.OntologyMapping;
import uk.ac.ebi.intact.bridges.ontologies.iterator.OntologyIterator;
import uk.ac.ebi.intact.bridges.ontologies.iterator.UniprotTaxonomyOntologyIterator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * IntactSolrSearcher Tester.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactSolrSearcherTest extends AbstractSolrTestCase {

    @Test
    public void search() throws Exception  {
        assertCount(0L, "*:*");

        indexFromClasspath("/mitab_samples/intact200.txt", true);

        assertCount(200L, "*:*");
        assertCount(100L, "\"CHEBI:39112\"");
        //assertCount(183L, "experimentalRole:bait");
        //assertCount(197L, "experimentalRole:prey");
        assertCount(1L, "-pbiorole:\"unspecified role\"");
        assertCount(1L, "pxref:ENSG00000169047");
        //assertCount(183L, "experimentalRoleA:bait");
        assertCount(200L, "pubid:17721511");
    }

    @Test
    public void search_interactors1() throws Exception  {
        assertCount(0L, "*:*");

        indexFromClasspath("/mitab_samples/intact200.txt", true);

        //MI:0326(protein)	MI:0328(small molecule)
        SolrQuery query = new SolrQuery("*:*")
                .setRows(0)
                .setFacet(true)
                .setFacetMinCount(1)
                .setFacetLimit(Integer.MAX_VALUE)
                .addFacetField("intact_byInteractorType_mi0326")
                .addFacetField("intact_byInteractorType_mi0328");

        QueryResponse response = getSolrJettyRunner().getSolrServer(CoreNames.CORE_PUB).query(query);

        FacetField ffProt = response.getFacetField("intact_byInteractorType_mi0326");
        Assert.assertEquals(129, ffProt.getValueCount());
        
        FacetField ffSm = response.getFacetField("intact_byInteractorType_mi0328");
        Assert.assertEquals(5, ffSm.getValueCount());
    }

    @Test
    public void search_interactors_count() throws Exception  {
        assertCount(0L, "*:*");

        indexFromClasspath("/mitab_samples/intact200.txt", true);

        IntactSolrSearcher searcher = new IntactSolrSearcher(getSolrJettyRunner().getSolrServer(CoreNames.CORE_PUB));

        //MI:0326(protein)	MI:0328(small molecule)
        SolrQuery query = new SolrQuery("*:*")
                .setRows(0)
                .setFacet(true)
                .setFacetMinCount(1)
                .setFacetLimit(Integer.MAX_VALUE);

        Assert.assertEquals(new Integer(129), searcher.countAllInteractors(query, new String [] {"mi0326"}).get("intact_byInteractorType_mi0326"));
        Assert.assertEquals(new Integer(5), searcher.countAllInteractors(query, new String [] {"mi0328"}).get("intact_byInteractorType_mi0328"));
    }

    @Test
    public void search_interactors2() throws Exception {
        assertCount(0L, "*:*");

        indexFromClasspath("/mitab_samples/intact200.txt", true);

        SolrQuery query = new SolrQuery("*:*");

        IntactSolrSearcher searcher = new IntactSolrSearcher(getSolrJettyRunner().getSolrServer(CoreNames.CORE_PUB));

        Assert.assertEquals(129, searcher.searchInteractors(query, "MI:0326", 0, 130).size());
        Assert.assertEquals(5, searcher.searchInteractors(query, "MI:0328", 0, 6).size());

        // with pagination
        Assert.assertEquals(3, searcher.searchInteractors(query, "MI:0328", 0, 3).size());
        
        final IntactSolrSearchResult result1 = (IntactSolrSearchResult) searcher.search( "GRB2", null, null, null, null );
        assertEquals(3, result1.getNumberResults());

        SolrQuery query2 = new SolrQuery("GRB2*");
        final IntactSolrSearchResult result2 = (IntactSolrSearchResult) searcher.search( "GRB2*", null, null, null, null );
        assertEquals(3, result2.getNumberResults());



    }

    @Test
    public void delete_by_pmid() throws Exception {
        assertCount(0L, "*:*");

        indexFromClasspath("/mitab_samples/p20053.txt", true);

        final String miql = "pubid:11805826"; // Gavin et al.
        assertCount(17L, miql );

        final SolrServer server = getSolrJettyRunner().getSolrServer( CoreNames.CORE_PUB );
        server.deleteByQuery( miql );

        assertCount(17L, miql );

        server.commit();

        assertCount(0L, miql );
    }

    @Test
    public void search_integrityTest() throws Exception {
        String mitab = "uniprotkb:P35568|intact:EBI-517592\tuniprotkb:Q08345-2|intact:EBI-711903\tuniprotkb:IRS1(gene name)" +
                "\t-\tintact:irs1_human(shortLabel)\tuniprotkb:CAK II(isoform synonym)|uniprotkb:Short(isoform synonym)|intact:Q08345-2(shortLabel)" +
                "\tMI:0424(protein kinase assay)\tBantscheff et al. (2007)" +
                "\tpubmed:17721511\ttaxid:9606(human)\ttaxid:9606(human)\tMI:0217(phosphorylation)" +
                "\tMI:0469(intact)\tintact:EBI-1381086\t-\tspoke\t" +
                "MI:0502(enzyme target)\tMI:0501(enzyme)\tMI:0499(unspecified role)\tMI:0499(unspecified role)\tMI:0326(protein)\tMI:0326(protein)\tgo:\"GO:0030188\"|go:\"GO:0005159\"|" +
                "go:\"GO:0005158\"|interpro:IPR002404|" +
                "interpro:IPR001849|interpro:IPR011993|ensembl:ENSG00000169047|rcsb pdb:1IRS|rcsb pdb:1K3A|rcsb pdb:1QQG" +
                "\tintact:EBI-711879\t-\tcaution:note1\tcaution:note2\tdataset:\"happy data\"\ttaxid:-1(in vitro)\tkd:0.5\t-\t-\t-" +
                "\t-\t-\t-\t-\t-\t-\t-\t-\t-";

        Assert.assertEquals(42, mitab.split("\t").length);

        IntactSolrIndexer indexer = new IntactSolrIndexer(getSolrJettyRunner().getSolrServer(CoreNames.CORE_PUB),
                                                          (HttpSolrServer) getSolrJettyRunner().getSolrServer(CoreNames.CORE_ONTOLOGY_PUB));
        indexer.indexOntologies(new OntologyMapping[] {
                new OntologyMapping("go", IntactSolrIndexerTest.class.getResource("/META-INF/goslim_generic.obo"))});

        OntologyIterator taxonomyIterator = new UniprotTaxonomyOntologyIterator(IntactSolrSearcherTest.class.getResourceAsStream("/META-INF/hominidae-taxonomy.tsv"));
        indexer.indexOntology(taxonomyIterator);

        indexer.indexMitab(new ByteArrayInputStream(mitab.getBytes()), false);

        IntactSolrSearcher searcher = new IntactSolrSearcher(getSolrJettyRunner().getSolrServer(CoreNames.CORE_PUB));

        IntactSolrSearchResult result = (IntactSolrSearchResult) searcher.search("P35568", null, null, PsicquicSolrServer.RETURN_TYPE_MITAB27, null);

        BinaryInteraction binaryInteraction = result.getBinaryInteractionList().iterator().next();

        final Interactor ia = binaryInteraction.getInteractorA();
        final Interactor ib = binaryInteraction.getInteractorB();

        Assert.assertEquals("P35568", get(0, ia.getIdentifiers()).getIdentifier());
        Assert.assertEquals("uniprotkb", get(0, ia.getIdentifiers()).getDatabase());
        Assert.assertEquals("EBI-517592", get(1, ia.getIdentifiers()).getIdentifier());
        Assert.assertEquals("intact", get(1, ia.getIdentifiers()).getDatabase());
        Assert.assertEquals("Q08345-2", get(0, ib.getIdentifiers()).getIdentifier());
        Assert.assertEquals("uniprotkb", get(0, ib.getIdentifiers()).getDatabase());
        Assert.assertEquals("EBI-711903", get(1, ib.getIdentifiers()).getIdentifier());
        Assert.assertEquals("intact", get(1, ib.getIdentifiers()).getDatabase());
        Assert.assertEquals("IRS1", get(0, ia.getAlternativeIdentifiers()).getIdentifier());
        Assert.assertEquals("uniprotkb", get(0, ia.getAlternativeIdentifiers()).getDatabase());
        Assert.assertEquals("gene name", get(0, ia.getAlternativeIdentifiers()).getText());
        Assert.assertEquals(0, ib.getAlternativeIdentifiers().size());
        Assert.assertEquals("irs1_human", get(0, ia.getAliases()).getName());
        Assert.assertEquals("intact", get(0, ia.getAliases()).getDbSource());
        Assert.assertEquals("shortLabel", get(0, ia.getAliases()).getAliasType());
        Assert.assertEquals("CAK II", get(0, ib.getAliases()).getName());
        Assert.assertEquals("uniprotkb", get(0, ib.getAliases()).getDbSource());
        Assert.assertEquals("isoform synonym", get(0, ib.getAliases()).getAliasType());
        Assert.assertEquals("MI:0424", ((CrossReference) binaryInteraction.getDetectionMethods().get(0)).getIdentifier());
        Assert.assertEquals("psi-mi", ((CrossReference) binaryInteraction.getDetectionMethods().get(0)).getDatabase());
        Assert.assertEquals("protein kinase assay", ((CrossReference) binaryInteraction.getDetectionMethods().get(0)).getText());
        Assert.assertEquals("Bantscheff et al. (2007)", ((Author) binaryInteraction.getAuthors().get(0)).getName());
        Assert.assertEquals("17721511", ((CrossReference) binaryInteraction.getPublications().get(0)).getIdentifier());
        Assert.assertEquals("pubmed", ((CrossReference) binaryInteraction.getPublications().get(0)).getDatabase());
        Assert.assertEquals("9606", get(0, ia.getOrganism().getIdentifiers()).getIdentifier());
        Assert.assertEquals("taxid", get(0, ia.getOrganism().getIdentifiers()).getDatabase());
        Assert.assertEquals("human", get(0, ia.getOrganism().getIdentifiers()).getText());
        Assert.assertEquals("9606", get(0, ib.getOrganism().getIdentifiers()).getIdentifier());
        Assert.assertEquals("taxid", get(0, ib.getOrganism().getIdentifiers()).getDatabase());
        Assert.assertEquals("human", get(0, ib.getOrganism().getIdentifiers()).getText());
        Assert.assertEquals("MI:0217", ((CrossReference) binaryInteraction.getInteractionTypes().get(0)).getIdentifier());
        Assert.assertEquals("psi-mi", ((CrossReference) binaryInteraction.getInteractionTypes().get(0)).getDatabase());
        Assert.assertEquals("phosphorylation", ((CrossReference) binaryInteraction.getInteractionTypes().get(0)).getText());
        Assert.assertEquals("MI:0469", ((CrossReference) binaryInteraction.getSourceDatabases().get(0)).getIdentifier());
        Assert.assertEquals("psi-mi", ((CrossReference) binaryInteraction.getSourceDatabases().get(0)).getDatabase());
        Assert.assertEquals("intact", ((CrossReference) binaryInteraction.getSourceDatabases().get(0)).getText());
        Assert.assertEquals("EBI-1381086", ((CrossReference) binaryInteraction.getInteractionAcs().get(0)).getIdentifier());
        Assert.assertEquals("intact", ((CrossReference) binaryInteraction.getInteractionAcs().get(0)).getDatabase());
        Assert.assertEquals(0, binaryInteraction.getConfidenceValues().size());
        Assert.assertEquals(1, ia.getExperimentalRoles().size());
        Assert.assertEquals("MI:0499", get(0, ia.getExperimentalRoles()).getIdentifier());
        Assert.assertEquals("psi-mi", get(0, ia.getExperimentalRoles()).getDatabase());
        Assert.assertEquals("unspecified role", get(0, ia.getExperimentalRoles()).getText());
        Assert.assertEquals(1, ib.getExperimentalRoles().size());
        Assert.assertEquals("MI:0499", get(0, ib.getExperimentalRoles()).getIdentifier());
        Assert.assertEquals("psi-mi", get(0, ib.getExperimentalRoles()).getDatabase());
        Assert.assertEquals("unspecified role", get(0, ib.getExperimentalRoles()).getText());
        Assert.assertEquals(1, ia.getBiologicalRoles().size());
        Assert.assertEquals("MI:0502", get(0, ia.getBiologicalRoles()).getIdentifier());
        Assert.assertEquals("psi-mi", get(0, ia.getBiologicalRoles()).getDatabase());
        Assert.assertEquals("enzyme target", get(0, ia.getBiologicalRoles()).getText());
        Assert.assertEquals(1, ib.getBiologicalRoles().size());
        Assert.assertEquals("MI:0501", get(0, ib.getBiologicalRoles()).getIdentifier());
        Assert.assertEquals("psi-mi", get(0, ib.getBiologicalRoles()).getDatabase());
        Assert.assertEquals("enzyme", get(0, ib.getBiologicalRoles()).getText());
        Assert.assertEquals("GO:0030188", get(0, ia.getXrefs()).getIdentifier());
        Assert.assertEquals("go", get(0, ia.getXrefs()).getDatabase());
        //Assert.assertEquals("chaperone regulator activity", get(0, ia.getXrefs()).getText());
        // do not enrich the mitab xref value itself, only add parent terms and synonyms!!!!
        Assert.assertNull(get(0, ia.getXrefs()).getText());
        Assert.assertEquals(10, ia.getXrefs().size());
        Assert.assertEquals("EBI-711879", get(0, ib.getXrefs()).getIdentifier());
        Assert.assertEquals("intact", get(0, ib.getXrefs()).getDatabase());
        Assert.assertEquals(1, ib.getXrefs().size());
        Assert.assertEquals("MI:0326", get(0, ia.getInteractorTypes()).getIdentifier());
        Assert.assertEquals("psi-mi", get(0, ia.getInteractorTypes()).getDatabase());
        Assert.assertEquals("protein", get(0, ia.getInteractorTypes()).getText());
        Assert.assertEquals("MI:0326", get(0, ib.getInteractorTypes()).getIdentifier());
        Assert.assertEquals("psi-mi", get(0, ib.getInteractorTypes()).getDatabase());
        Assert.assertEquals("protein", get(0, ib.getInteractorTypes()).getText());
        Assert.assertEquals("-1", get(0, binaryInteraction.getHostOrganism().getIdentifiers()).getIdentifier());
        Assert.assertEquals("taxid", get(0, binaryInteraction.getHostOrganism().getIdentifiers()).getDatabase());
        Assert.assertEquals("in vitro", get(0, binaryInteraction.getHostOrganism().getIdentifiers()).getText());
        Assert.assertEquals(1, ia.getAnnotations().size());
        Assert.assertEquals("caution", get(0, ia.getAnnotations()).getTopic());
        Assert.assertEquals("note1", get(0, ia.getAnnotations()).getText());
        Assert.assertEquals(1, ib.getAnnotations().size());
        Assert.assertEquals("caution", get(0, ib.getAnnotations()).getTopic());
        Assert.assertEquals("note2", get(0, ib.getAnnotations()).getText());
        //Assert.assertEquals(1, ia.getParameters().size());
        //Assert.assertEquals("temperature", get(0, ia.getParameters()).getType());
        //Assert.assertEquals("1", get(0, ia.getParameters()).getValue());
        //Assert.assertEquals(1, ib.getParameters().size());
        //Assert.assertEquals("temperature", get(0, ib.getParameters()).getType());
        //Assert.assertEquals("2", get(0, ib.getParameters()).getValue());
        Assert.assertEquals(1, binaryInteraction.getParameters().size());
        Assert.assertEquals("kd", ((Parameter) binaryInteraction.getParameters().get(0)).getType());
        Assert.assertEquals("0.5", ((Parameter) binaryInteraction.getParameters().get(0)).getValue());
    }

    private <T> T get(int position, Collection<T> elements) {
        return new ArrayList<T>(elements).get(position);
    }

    private void assertCount(Number count, String searchQuery) throws IntactSolrException, SolrServerException, PsicquicSolrException {
        IntactSolrSearcher searcher = new IntactSolrSearcher(getSolrJettyRunner().getSolrServer(CoreNames.CORE_PUB));
        IntactSolrSearchResult result = (IntactSolrSearchResult) searcher.search(searchQuery, null, null, null, null);

        assertEquals(count, result.getNumberResults());
    }

    private void indexFromClasspath(String resource, boolean hasHeader) throws IOException, IntactSolrException {
        IntactSolrIndexer indexer = new IntactSolrIndexer(getSolrJettyRunner().getSolrServer(CoreNames.CORE_PUB));
        indexer.indexMitabFromClasspath(resource, hasHeader);
    }

    @Test
    public void compare_facetted_nonfacetted_interactors_search() throws Exception {
        assertCount( 0L, "*:*" );

        indexFromClasspath( "/mitab_samples/p20053.txt", true );

        SolrQuery query = new SolrQuery( "P20053" );

        IntactSolrSearcher searcher = new IntactSolrSearcher( getSolrJettyRunner().getSolrServer( CoreNames.CORE_PUB ) );

        Assert.assertEquals( 34, searcher.searchInteractors( query, "MI:0326", 0, 35 ).size());

        // use pagination
        Assert.assertEquals( 30, searcher.searchInteractors( query, "MI:0326", 4, 35 ).size());

        SolrQuery queryFacetted = new SolrQuery( "P20053" )
                .setRows( 0 )
                .setFacet( true )
                .setFacetMinCount( 1 )
                .setFacetLimit( Integer.MAX_VALUE )
                .addFacetField( "intact_byInteractorType_mi0326" )
                .addFacetField( "intact_byInteractorType_mi0328" );

        QueryResponse response = getSolrJettyRunner().getSolrServer( CoreNames.CORE_PUB ).query( queryFacetted );

        boolean ebi340 = false;
        boolean ebi421 = false;
        FacetField ffProt = response.getFacetField( "intact_byInteractorType_mi0326" );
        if ( ffProt != null && ffProt.getValues() != null ) {
            for ( FacetField.Count c : ffProt.getValues() ) {
                if ( c.getName().equals( "EBI-340" ) && c.getCount() == 8 ) {
                    ebi340 = true;
                }
                if ( c.getName().equals( "EBI-421" ) && c.getCount() == 5 ) {
                    ebi421 = true;
                }
            }
        }
        Assert.assertTrue( ebi340 );
        Assert.assertTrue( ebi421 );

        final IntactSolrSearchResult nonFacetResult1 = (IntactSolrSearchResult) searcher.search( "P20053 +EBI-340", null, null, null, null );
        Assert.assertEquals( 8, nonFacetResult1.getNumberResults() );

        final IntactSolrSearchResult nonFacetResult2 = (IntactSolrSearchResult) searcher.search( "P20053 +EBI-421" , null, null, null, null);
        Assert.assertEquals( 5, nonFacetResult2.getNumberResults() );

        final IntactSolrSearchResult nonFacetResult3 = (IntactSolrSearchResult) searcher.search( "P20053 +id:EBI-340", null, null, null, null );
        Assert.assertEquals( 8, nonFacetResult3.getNumberResults() );

        final IntactSolrSearchResult nonFacetResult4 = (IntactSolrSearchResult) searcher.search( "P20053 +id:EBI-421", null, null, null, null );
        Assert.assertEquals( 5, nonFacetResult4.getNumberResults() );
    }
}
