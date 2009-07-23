package uk.ac.ebi.intact.dataexchange.psimi.solr;

import org.junit.Test;
import uk.ac.ebi.intact.bridges.ontologies.OntologyMapping;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.BinaryInteractionEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.FieldEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.OntologyBinaryInteractionEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.OntologyFieldEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;

import java.net.URL;

/**
 * test the browsing of ontology hierarchy with counting og interactions available per term.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.0
 */
public class OntologyTermBrowsingTest extends AbstractSolrTestCase {

    @Test
    public void indexAndBrowseGo_is_a() throws Exception {
        assertCountInteraction( 0, "*:*" );

        // index ontology - note that this ontology's relationship are exclusively of type is_a
        final URL goURL = OntologyTermBrowsingTest.class.getResource( "/META-INF/GO_protein_domain_binding.obo" );
        getIndexer().indexOntologies( new OntologyMapping[]{new OntologyMapping( "go", goURL )} );
        assertCountOntologyTerm( 74, "*:*" );

        // enrich interactions based on ontology indexed
        final OntologySearcher ontologySearcher = new OntologySearcher( getSolrJettyRunner().getSolrServer( CoreNames.CORE_ONTOLOGY_PUB ) );
        BinaryInteractionEnricher enricher = new OntologyBinaryInteractionEnricher( ontologySearcher );

        FieldEnricher fe = new OntologyFieldEnricher( ontologySearcher );

        getIndexer().indexMitabFromClasspath( "/mitab_samples/intact5_go_binding.txt", true );
        assertCountInteraction( 5, "*:*" );

        // we have 5 interactions, 3 annotated with terms part of the small ontology
        // 1. GO:0051401
        // 2. none
        // 3. none
        // 4. GO:0042169 and GO:0005515
        // 5. GO:0042169                 http://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0042169#ancchart

        // check interaction count when querying on GO term hierarchy
        assertCountInteraction( 1, "go_expanded_id:\"GO:0051401\"" ); // leaf term
        assertCountInteraction( 2, "go_expanded_id:\"GO:0042169\"" ); // leaf term
        assertCountInteraction( 3, "go_expanded_id:\"GO:0019904\"" ); // direct parent of GO:0051401 and GO:0042169
        assertCountInteraction( 3, "go_expanded_id:\"GO:0005515\"" ); // GO:0005515 is parent of GO:0051401 and GO:0042169
        assertCountInteraction( 3, "go_expanded_id:\"GO:0003674\"" ); // root of the ontology
    }

    @Test
    public void indexAndBrowseGo_part_of() throws Exception {
        assertCountInteraction( 0, "*:*" );

        // index ontology - note that this ontology's relationship are of type is_a and part_of
        // The ontology was built by taking the term 'nuclear exosome' (GO:0000788) and including all parents
        // You can view the hierarchy in OLS folowing the link below:
        // http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=GO&termId=GO%3A0000788&termName=nuclear%20nucleosome

        final URL goURL = OntologyTermBrowsingTest.class.getResource( "/META-INF/GO_nuclear_exosome.obo" );
        getIndexer().indexOntologies( new OntologyMapping[]{new OntologyMapping( "go", goURL )} );
        assertCountOntologyTerm( 44, "*:*" ); // 33 is_a + 1 leaf + 1 root

        // index interactions (field enrichement on "go" included)
        getIndexer().indexMitabFromClasspath( "/mitab_samples/intact5_go_nuclear_exosome.txt", true );
        assertCountInteraction( 5, "*:*" );

        // we have 5 interactions, 4 annotated with terms part of the small ontology
        // 1. GO:0000785 (chromatin)
        // 2. none
        // 3. GO:0005694 (chromosome)
        // 4. GO:0000786 (nucleosome)
        // 5. GO:0000788 (nuclear exosome)

        // Simplified hierarchy of term used in MITAB
        //
        //                                 interaction
        //                                    count
        //  GO:0005694 (chromosome)             4  (#1, #3, #4, #5)
        //    |        |
        //    |   GO:0000785 (chromatin)        3  (#3, #4, #5)
        //    |         |
        //   GO:0000786 (nucleosome)            2  (#4, #5)
        //               |
        //   GO:0000788 (nuclear exosome)       1  (#5)
        //
        // Full view saved in resources directory as GO_nuclear_exosome.PNG

        // check interaction count when querying on GO term hierarchy
        assertCountInteraction( 4, "go_expanded_id:\"GO:0005575\"" ); // cellular location - root of ontology
        assertCountInteraction( 4, "go_expanded_id:\"GO:0043226\"" ); // organelle
        assertCountInteraction( 1, "go_expanded_id:\"GO:0000788\"" ); // nuclear exosome

        assertCountInteraction( 2, "go_expanded_id:\"GO:0000786\"" ); // nucleosome

        assertCountInteraction( 3, "go_expanded_id:\"GO:0000785\"" ); // chromatin
        assertCountInteraction( 4, "go_expanded_id:\"GO:0005694\"" ); // chromosome
    }
}
