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
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.hupo.psi.mi.psicquic.model.PsicquicSolrException;
import org.junit.Assert;
import org.junit.Test;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.bridges.ontologies.OntologyMapping;
import uk.ac.ebi.intact.bridges.ontologies.iterator.OntologyIterator;
import uk.ac.ebi.intact.bridges.ontologies.iterator.UniprotTaxonomyOntologyIterator;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.SolrDocumentConverter;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactSolrIndexerTest extends AbstractSolrTestCase {

    @Test
    public void indexMitabFromClasspath() throws Exception {
        getIndexer().indexMitabFromClasspath("/mitab_samples/intact200.txt", true);
        assertCount(200, "*:*");
    }

    @Test
    public void indexMitabFromClasspath2() throws Exception {
        getIndexer().indexMitabFromClasspath("/mitab_samples/intact200.txt", true, 10, 20);
        assertCount(20, "*:*");
    }
    
    @Test
    public void indexMitabFromClasspath3() throws Exception {
        getIndexer().indexMitabFromClasspath("/mitab_samples/intact200.txt", true, 190, 20);
        assertCount(10, "*:*");
    }

    @Test
    public void indexMitabFromClasspath4() throws Exception {
        getIndexer().indexMitabFromClasspath("/mitab_samples/intact200.txt", true);
        assertCount(1, "EBI-1380413");
        assertCount(36, "detmethod:\"binding\"");
        assertCount(1, "+detmethod:\"binding\" +EBI-1380413");
        assertCount(1, "detmethod:\"binding\" AND EBI-1380413");
        assertCount(36, "detmethod:\"binding\" OR EBI-1380413");
        assertCount(0, "detmethod:\"binding\" AND EBI-1381086");
        assertCount(37, "detmethod:\"binding\" OR EBI-1381086");
    }

    @Test
    public void index1() throws Exception {
        // mitab line with annotations
        String mitabLine = "uniprotkb:P16884\tuniprotkb:Q60824\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)" +
                              "\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739" +
                              "\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)" +
                              "\tintact:EBI-446356\t-\t-\tMI:0499(unspecified role)" +
                              "\tMI:0499(unspecified role)\tMI:0498(prey)\tMI:0496(bait)\tMI:0326(protein)\tMI:0326(protein)\tinterpro:IPR004829|\tgo:\"GO:0030246\"\t-\t-\t-\t-\tyeast:4932\t-\t-\t-\t-" +
                "\t-\t-\t-\t-\t-\t-\t-\t-\t-";

        getIndexer().indexMitab(new ByteArrayInputStream(mitabLine.getBytes()), false);

        assertCount(1, "*:*");
        assertCount(1, "id:P16884");
        assertCount(1, "identifier:P16884");
        assertCount(0, "id:Nfh");
        assertCount(1, "identifier:Nfh");
    }

    @Test
    public void toSolrDocument_goExpansion() throws Exception {
        getIndexer().indexOntologies(new OntologyMapping[] {
                new OntologyMapping("go", IntactSolrIndexerTest.class.getResource("/META-INF/goslim_generic.obo"))
        });

        String goTermToExpand = "GO:0030246";
        String psiMiTabLine = "uniprotkb:P16884\tuniprotkb:Q60824\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)" +
                              "\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739" +
                              "\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)" +
                              "\tintact:EBI-446356\t-\t-\tMI:0499(unspecified role)" +
                              "\tMI:0499(unspecified role)\tMI:0498(prey)\tMI:0496(bait)\tMI:0326(protein)\tMI:0326(protein)\tinterpro:IPR004829|\tgo:\""+goTermToExpand+"\"\t-\t-\t-\t-\tyeast:4932\t-\t-\t-\t-" +
                "\t-\t-\t-\t-\t-\t-\t-\t-\t-";

        OntologySearcher ontologySearcher = new OntologySearcher(getSolrJettyRunner().getSolrServerNoTimeOut(CoreNames.CORE_ONTOLOGY_PUB));

        final Set<String> ontologyNames = ontologySearcher.getOntologyNames();
        Assert.assertEquals(1, ontologyNames.size());
        Assert.assertEquals("go", ontologyNames.iterator().next());

        SolrDocumentConverter converter = new SolrDocumentConverter(getSolrServer(), ontologySearcher);

        SolrInputDocument doc = converter.toSolrDocument(psiMiTabLine);

        Collection<Object> expandedGoIds = doc.getFieldValues("pxrefB");

        Assert.assertEquals(13, expandedGoIds.size());
        Assert.assertTrue(expandedGoIds.contains(goTermToExpand));
        Assert.assertTrue(expandedGoIds.contains("GO:0003674"));
        Assert.assertTrue(expandedGoIds.contains("GO:0005488"));
    }

    @Test
    public void toSolrDocument_goDescriptionUpdate() throws Exception {
        getIndexer().indexOntologies(new OntologyMapping[] {
                new OntologyMapping("go", IntactSolrIndexerTest.class.getResource("/META-INF/goslim_generic.obo"))
        });

        String goTermToExpand = "go:\"GO:0030246\"(lalalala!)";
        String psiMiTabLine = "uniprotkb:P16884\tuniprotkb:Q60824\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)" +
                              "\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739" +
                              "\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)" +
                              "\tintact:EBI-446356\t-\t-\tMI:0499(unspecified role)" +
                              "\tMI:0499(unspecified role)\tMI:0498(prey)\tMI:0496(bait)\tMI:0326(protein)\tMI:0326(protein)\tinterpro:IPR004829|\t"+goTermToExpand+"\t-\t-\t-\t-\tyeast:4932\t-\t-\t-\t-" +
                "\t-\t-\t-\t-\t-\t-\t-\t-\t-";

        OntologySearcher ontologySearcher = new OntologySearcher(getSolrJettyRunner().getSolrServerNoTimeOut(CoreNames.CORE_ONTOLOGY_PUB));
        SolrDocumentConverter converter = new SolrDocumentConverter(getSolrServer(), ontologySearcher);

        SolrInputDocument doc = converter.toSolrDocument(psiMiTabLine);

        Collection<Object> expandedGoIds = doc.getFieldValues("pxrefB");
        Assert.assertEquals(14, expandedGoIds.size());
        Assert.assertTrue(expandedGoIds.contains("go:GO:0030246"));
        Assert.assertTrue(expandedGoIds.contains("go:GO:0030246"));
        Assert.assertTrue(expandedGoIds.contains("carbohydrate binding"));
        Assert.assertTrue(expandedGoIds.contains("selectin"));

        BinaryInteraction binaryInteraction = converter.toBinaryInteraction(doc);
        //Assert.assertEquals("carbohydrate binding", binaryInteraction.getInteractorB().getXrefs().iterator().next().getText());
        // do not enrich the mitab xref value itself, only add parent terms and synonyms!!!!
        Assert.assertEquals("lalalala!", binaryInteraction.getInteractorB().getXrefs().iterator().next().getText());
    }

    @Test
    public void toSolrDocument_taxidUpdate() throws Exception {
         String mitab = "uniprotkb:P35568|intact:EBI-517592\tuniprotkb:Q08345-2|intact:EBI-711903\tuniprotkb:IRS1(gene name)" +
                "\t-\tintact:irs1_human(shortLabel)\tuniprotkb:CAK II(isoform synonym)|uniprotkb:Short(isoform synonym)|intact:Q08345-2(shortLabel)" +
                "\tMI:0424(protein kinase assay)\tBantscheff et al. (2007)" +
                "\tpubmed:17721511\ttaxid:9606(human)\ttaxid:9606(human)\tMI:0217(phosphorylation)" +
                "\tMI:0469(intact)\tintact:EBI-1381086\t-\t-\t" +
                "MI:0499(unspecified role)\tMI:0502(enzyme target)\tMI:0501(enzyme)\tMI:0499(unspecified role)\tMI:0326(protein)\tMI:0326(protein)\tgo:\"GO:0030188\"|go:\"GO:0005159\"|" +
                "go:\"GO:0005158\"|interpro:IPR002404|" +
                "interpro:IPR001849|interpro:IPR011993|ensembl:ENSG00000169047|rcsb pdb:1IRS|rcsb pdb:1K3A|rcsb pdb:1QQG" +
                "\tintact:EBI-711879\t-\t-\t-\t-\ttaxid:-1(in vitro)\t-\t-\t-\t-\t-\t-\t-\t-\t-\t-\t-\t-\t-";

        OntologyIterator taxonomyIterator = new UniprotTaxonomyOntologyIterator(IntactSolrSearcherTest.class.getResourceAsStream("/META-INF/hominidae-taxonomy.tsv"));
        getIndexer().indexOntology(taxonomyIterator);

        OntologySearcher ontologySearcher = new OntologySearcher(getSolrJettyRunner().getSolrServerNoTimeOut(CoreNames.CORE_ONTOLOGY_PUB));
        SolrDocumentConverter converter = new SolrDocumentConverter(getSolrServer(), ontologySearcher);

        SolrInputDocument doc = converter.toSolrDocument(mitab);

        Collection<Object> expandedTaxidA = doc.getFieldValues("taxidA");
        Assert.assertTrue(expandedTaxidA.contains("taxid:314295"));
        Assert.assertTrue(expandedTaxidA.contains("taxid:207598"));
        Assert.assertTrue(expandedTaxidA.contains("taxid:9605"));
        Assert.assertTrue(expandedTaxidA.contains("taxid:9604"));
        Assert.assertTrue(expandedTaxidA.contains("taxid:9606"));
        Assert.assertTrue(expandedTaxidA.contains("Catarrhini"));
        Assert.assertTrue(expandedTaxidA.contains("Hominidae"));
        Assert.assertTrue(expandedTaxidA.contains("Homo"));
        Assert.assertTrue(expandedTaxidA.contains("Hominidae"));
        Assert.assertTrue(expandedTaxidA.contains("Human"));
    }

    @Test
    public void toSolrDocument_wildcard() throws Exception {
        String mitabLine = "uniprotkb:P16884\tuniprotkb:Q60824\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)" +
                              "\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739" +
                              "\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)" +
                              "\tintact:EBI-446356\t-\t-\tMI:0499(unspecified role)" +
                              "\tMI:0499(unspecified role)\tMI:0498(prey)\tMI:0496(bait)\tMI:0326(protein)\tMI:0326(protein)\tinterpro:IPR004829|\tGO:012345\t-\t-\t-\t-\tyeast:4932\t-\t-\t-\t-" +
                "\t-\t-\t-\t-\t-\t-\t-\t-\t-";

        getIndexer().indexMitab(new ByteArrayInputStream(mitabLine.getBytes()), false);

        assertCount(1, "Nefh*");
    }

    @Test
    public void toSolrDocument_imex() throws Exception {
        String mitabLine = "uniprotkb:P16884\tuniprotkb:Q60824\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)" +
                              "\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739" +
                              "\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)" +
                              "\tintact:EBI-446356|imex:IM-1234-1\t-\t-\tMI:0499(unspecified role)" +
                              "\tMI:0499(unspecified role)\tMI:0498(prey)\tMI:0496(bait)\tMI:0326(protein)\tMI:0326(protein)\tinterpro:IPR004829|\tGO:012345\t-\t-\t-\t-\tyeast:4932\t-\t-\t-\t-" +
                "\t-\t-\t-\t-\t-\t-\t-\t-\t-";
        
        getIndexer().indexMitab(new ByteArrayInputStream(mitabLine.getBytes()), false);

        assertCount(1, "IM-1234-1");
    }

    @Test
    public void toSolrDocument_taxonomy_expansion() throws Exception {
        String mitabLine = "uniprotkb:P16884\tuniprotkb:Q60824\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)" +
                              "\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739" +
                              "\ttaxid:9606(human)\ttaxid:9606(human)\tMI:0218(physical interaction)\tMI:0469(intact)" +
                              "\tintact:EBI-446356\t-\t-\tMI:0499(unspecified role)" +
                              "\tMI:0499(unspecified role)\tMI:0498(prey)\tMI:0496(bait)\tMI:0326(protein)\tMI:0326(protein)\tinterpro:IPR004829|\tGO:012345\t-\t-\t-\t-\tyeast:4932\t-\t-\t-\t-" +
                "\t-\t-\t-\t-\t-\t-\t-\t-\t-";

        OntologyIterator taxonomyIterator = new UniprotTaxonomyOntologyIterator(IntactSolrSearcherTest.class.getResourceAsStream("/META-INF/hominidae-taxonomy.tsv"));

        IntactSolrIndexer indexer = new IntactSolrIndexer(getSolrJettyRunner().getSolrServer(CoreNames.CORE_PUB), getSolrJettyRunner().getSolrServerNoTimeOut(CoreNames.CORE_ONTOLOGY_PUB));
        indexer.indexOntology(taxonomyIterator);
        indexer.indexMitab(new ByteArrayInputStream(mitabLine.getBytes()), false);

        SolrServer pubCore = getSolrJettyRunner().getSolrServer(CoreNames.CORE_PUB);
        QueryResponse queryResponse = pubCore.query(new SolrQuery("P16884"));

        SolrDocument doc = queryResponse.getResults().get(0);

        Assert.assertEquals(1, doc.getFieldValues("taxidA_o").size());
    }

    private void assertCount(Number count, String searchQuery) throws IntactSolrException, SolrServerException, PsicquicSolrException {
        IntactSolrSearcher searcher = new IntactSolrSearcher(getSolrJettyRunner().getSolrServer(CoreNames.CORE_PUB));
        IntactSolrSearchResult result = (IntactSolrSearchResult) searcher.search(searchQuery, null, null, null, null);

        assertEquals(count.longValue(), result.getNumberResults());
    }

    @Test
    // cannot retry like that
    public void retrying() throws Exception {
        getSolrJettyRunner().stop();

        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    getIndexer().indexMitabFromClasspath("/mitab_samples/intact200.txt", true);
                } catch (IOException e) {
                    Assert.fail("An IOException was thrown");
                }
                catch (IllegalStateException e){
                    Assert.fail("An IllegalStateException was thrown");
                }
            }
        };

        t.start();
        
        Thread.sleep(5*1000);

        getSolrJettyRunner().start();

        while (t.isAlive()) {
           Thread.sleep(500);
        }

        assertCount(200, "*:*");
    }
}