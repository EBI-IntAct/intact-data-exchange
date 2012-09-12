package uk.ac.ebi.intact.dataexchange.psimi.solr.ontology;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.hupo.psi.mi.psicquic.model.PsicquicSolrException;
import org.hupo.psi.mi.psicquic.model.PsicquicSolrServer;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearchResult;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearcher;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class will index in LUCENE ontology terms that are indexed in interaction index so ti can be retrieved easily by the website
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23/08/12</pre>
 */

public class InteractionOntologyLuceneIndexer {

    private static final Log log = LogFactory.getLog(InteractionOntologyLuceneIndexer.class);

    private OntologySearcher ontologySearcher;
    private IntactSolrSearcher interactionSearcher;
    private Map<InteractionOntologyTerm, InteractionOntologyTermResults> processedTerms;

    public InteractionOntologyLuceneIndexer(String ontologySolrUrl, String interactionOntologyUrl) {
        if (ontologySolrUrl == null){
            throw new IllegalArgumentException("The ontology solr url cannot bet null");
        }
        if (interactionOntologyUrl == null){
            throw new IllegalArgumentException("The interaction solr url cannot bet null");
        }

        this.ontologySearcher = new OntologySearcher(createSolrServer(ontologySolrUrl));
        this.interactionSearcher = new IntactSolrSearcher(createSolrServer(interactionOntologyUrl));
        this.processedTerms = new HashMap<InteractionOntologyTerm, InteractionOntologyTermResults>();
    }

    private HttpClient createHttpClient() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
                .getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory
                .getSocketFactory()));

        PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
        cm.setMaxTotal(138);
        cm.setDefaultMaxPerRoute(32);

        HttpClient httpClient = new DefaultHttpClient(cm);

        return httpClient;
    }

    private HttpSolrServer createSolrServer(String solrUrl) {
        HttpSolrServer ontologiesSolrServer = new HttpSolrServer(solrUrl, createHttpClient());

        ontologiesSolrServer.setConnectionTimeout(20000);
        ontologiesSolrServer.setSoTimeout(20000);
        ontologiesSolrServer.setAllowCompression(true);

        return ontologiesSolrServer;
    }

    public void loadAndIndexAllFacetFieldCounts(File directory) throws SolrServerException, PsicquicSolrException, IOException {

        this.processedTerms.clear();

        // open lucene directory
        if (!directory.exists()){
            directory.mkdirs();
        }
        IndexWriterConfig indexConfig = new IndexWriterConfig(Version.LUCENE_30, new StandardAnalyzer(Version.LUCENE_30));
        Directory luceneDirectory = FSDirectory.open(directory);

        final IndexWriter termIndexWriter = new IndexWriter(luceneDirectory, indexConfig);

        List<String> facetFieldsWithResults = initializeListOfFacetFieldsToRetrieve();
        int first = 0;
        int chunk = 50;

        Collection<FieldCount> fieldCounts = loadFieldCountsFor(first, chunk, facetFieldsWithResults.toArray(new String[]{}), facetFieldsWithResults);
        registerFieldCountResultsFor(fieldCounts);

        while (!facetFieldsWithResults.isEmpty()){
            first+=chunk;

            fieldCounts = loadFieldCountsFor(first, chunk, facetFieldsWithResults.toArray(new String[]{}), facetFieldsWithResults);
            registerFieldCountResultsFor(fieldCounts);
        }

        // index registered terms
        for (Map.Entry<InteractionOntologyTerm, InteractionOntologyTermResults> termResult : this.processedTerms.entrySet()){
            createAndIndexDocumentsFor(termResult.getKey(), termResult.getValue(), termIndexWriter);
        }

        termIndexWriter.commit();
    }

    private void registerFieldCountResultsFor(Collection<FieldCount> fieldCounts) throws SolrServerException {

        for (FieldCount fieldCount : fieldCounts){

            String db = fieldCount.getType() != null ? fieldCount.getType() : "psi-mi";

            LazyLoadedOntologyTerm term;
            // annotations anf feature type : only the name, no id is provided
            if (fieldCount.getSearchFieldName().equals(FieldNames.INTERACTOR_FEATURE)
                    || fieldCount.getSearchFieldName().equals(FieldNames.INTERACTION_ANNOTATIONS)){
                term = new LazyLoadedOntologyTerm(ontologySearcher, null, fieldCount.getValue());
            }
            else {
                term = new LazyLoadedOntologyTerm(ontologySearcher, fieldCount.getValue());
            }

            Set<OntologyTerm> parents = term.getAllParentsToRoot();

            // register term
            createAndRegisterInteractionTerm(fieldCount, db, term);

            // register parents
            for (OntologyTerm parent : parents){
                LazyLoadedOntologyTerm lazyParent = (LazyLoadedOntologyTerm) parent;
                createAndRegisterInteractionTerm(fieldCount, db, lazyParent);
            }
        }
    }

    private void createAndRegisterInteractionTerm(FieldCount fieldCount, String db, LazyLoadedOntologyTerm term) {
        // register the term
        InteractionOntologyTerm interactionTerm = new InteractionOntologyTerm(term.getName(), term.getId());
        registerOntologyTermResults(fieldCount, db, interactionTerm);

        // register synonyms
        if (term.getSynonymsStr() != null){
            for (String synonym : term.getSynonymsStr()){
                InteractionOntologyTerm termSynonym = new InteractionOntologyTerm(synonym, term.getId());
                registerOntologyTermResults(fieldCount, db, termSynonym);
            }
        }
    }

    private void registerOntologyTermResults(FieldCount fieldCount, String db, InteractionOntologyTerm interactionTerm) {
        if (this.processedTerms.containsKey(interactionTerm)){
            this.processedTerms.get(interactionTerm).addToCount(fieldCount.getCount());
        }
        else {
            InteractionOntologyTermResults results = new InteractionOntologyTermResults(db, fieldCount.getSearchFieldName(), fieldCount.getCount());
            this.processedTerms.put(interactionTerm, results);
        }
    }

    private void createAndIndexDocumentsFor(InteractionOntologyTerm ontologyTerm, InteractionOntologyTermResults results, IndexWriter termIndexWriter) throws SolrServerException, IOException {
        String value = ontologyTerm.getIdentifier();

        // index current term
        Document document = new Document();
        document.add(new Field("identifier", value, Field.Store.YES, Field.Index.NOT_ANALYZED));
        if (ontologyTerm.getName() != null) {
            document.add(new Field("label", ontologyTerm.getName(), Field.Store.YES, Field.Index.ANALYZED));
            document.add(new Field("label_sorted", ontologyTerm.getName(), Field.Store.NO, Field.Index.NOT_ANALYZED));
        }
        document.add(new Field("databaseLabel", results.getDatabaseLabel(), Field.Store.YES, Field.Index.ANALYZED));
        document.add(new Field("databaseLabel_sorted", results.getDatabaseLabel(), Field.Store.NO, Field.Index.NOT_ANALYZED));
        document.add(new Field("count", String.valueOf(results.getCount()), Field.Store.YES, Field.Index.NOT_ANALYZED));
        document.add(new Field("fieldName", results.getSearchField(), Field.Store.YES, Field.Index.NOT_ANALYZED));

        termIndexWriter.addDocument(document);
    }

    private List<String> initializeListOfFacetFieldsToRetrieve() {
        List<String> facetFieldsWithResults = new ArrayList<String>(9);
        facetFieldsWithResults.add(FieldNames.DETMETHOD_FACET);
        facetFieldsWithResults.add(FieldNames.TYPE_FACET);
        facetFieldsWithResults.add(FieldNames.BIOLOGICAL_ROLE_FACET);
        facetFieldsWithResults.add(FieldNames.INTERACTOR_TYPE_FACET);
        facetFieldsWithResults.add(FieldNames.INTERACTOR_DET_METHOD_FACET);
        facetFieldsWithResults.add(FieldNames.INTERACTION_ANNOTATIONS_FACET);
        facetFieldsWithResults.add(FieldNames.INTERACTOR_FEATURE_FACET);
        facetFieldsWithResults.add(FieldNames.INTERACTION_XREF_FACET);
        facetFieldsWithResults.add(FieldNames.INTERACTOR_XREF_FACET);

        return  facetFieldsWithResults;
    }

    private Collection<FieldCount> loadFieldCountsFor(int first, int max, String[] facetFieldNames, List<String> fieldsWithResults) throws SolrServerException, PsicquicSolrException {

        fieldsWithResults.clear();

        IntactSolrSearchResult result = interactionSearcher.searchWithFacets("*", 0, 0, PsicquicSolrServer.RETURN_TYPE_MITAB27, null, facetFieldNames, first, max);

        List<FacetField> facetFields = result.getFacetFieldList();

        Collection<FieldCount> facetFieldCounts = new ArrayList<FieldCount>(facetFields.size());

        for (FacetField facetField : facetFields){

            // add detection methods fields
            if (FieldNames.DETMETHOD_FACET.equals(facetField.getName())){

                List<FacetField.Count> facetCounts = facetField.getValues();

                // we reached the max number of results
                if (facetCounts != null && facetCounts.size() == max){
                    fieldsWithResults.add(FieldNames.DETMETHOD_FACET);
                }

                collectFacetCountsFor(facetFieldCounts, facetCounts, FieldNames.DETMETHOD);
            }
            // add interaction type fields
            else if (FieldNames.TYPE_FACET.equals(facetField.getName())){

                List<FacetField.Count> facetCounts = facetField.getValues();

                // we reached the max number of results
                if (facetCounts != null && facetCounts.size() == max){
                    fieldsWithResults.add(FieldNames.TYPE_FACET);
                }

                collectFacetCountsFor(facetFieldCounts, facetCounts, FieldNames.TYPE);
            }
            // add biological role fields
            else if (FieldNames.BIOLOGICAL_ROLE_FACET.equals(facetField.getName())){

                List<FacetField.Count> facetCounts = facetField.getValues();

                // we reached the max number of results
                if (facetCounts != null && facetCounts.size() == max){
                    fieldsWithResults.add(FieldNames.BIOLOGICAL_ROLE_FACET);
                }

                collectFacetCountsFor(facetFieldCounts, facetCounts, FieldNames.BIOLOGICAL_ROLE);
            }
            // add interactor types fields
            else if (FieldNames.INTERACTOR_TYPE_FACET.equals(facetField.getName())){

                List<FacetField.Count> facetCounts = facetField.getValues();

                // we reached the max number of results
                if (facetCounts != null && facetCounts.size() == max){
                    fieldsWithResults.add(FieldNames.INTERACTOR_TYPE_FACET);
                }

                collectFacetCountsFor(facetFieldCounts, facetCounts, FieldNames.INTERACTOR_TYPE);
            }
            // add biological role fields
            else if (FieldNames.INTERACTOR_DET_METHOD_FACET.equals(facetField.getName())){

                List<FacetField.Count> facetCounts = facetField.getValues();

                // we reached the max number of results
                if (facetCounts != null && facetCounts.size() == max){
                    fieldsWithResults.add(FieldNames.INTERACTOR_DET_METHOD_FACET);
                }

                collectFacetCountsFor(facetFieldCounts, facetCounts, FieldNames.INTERACTOR_DET_METHOD);
            }
            // add annotation fields for interaction
            else if (FieldNames.INTERACTION_ANNOTATIONS_FACET.equals(facetField.getName())){

                List<FacetField.Count> facetCounts = facetField.getValues();

                // we reached the max number of results
                if (facetCounts != null && facetCounts.size() == max){
                    fieldsWithResults.add(FieldNames.INTERACTION_ANNOTATIONS_FACET);
                }

                collectFacetCountsFor(facetFieldCounts, facetCounts, FieldNames.INTERACTION_ANNOTATIONS);
            }
            // add feature type fields for interactors
            else if (FieldNames.INTERACTOR_FEATURE_FACET.equals(facetField.getName())){

                List<FacetField.Count> facetCounts = facetField.getValues();

                // we reached the max number of results
                if (facetCounts != null && facetCounts.size() == max){
                    fieldsWithResults.add(FieldNames.INTERACTOR_FEATURE_FACET);
                }

                collectFacetCountsFor(facetFieldCounts, facetCounts, FieldNames.INTERACTOR_FEATURE);
            }
            // add interactionXrefs
            else if (FieldNames.INTERACTION_XREF_FACET.equals(facetField.getName())){

                List<FacetField.Count> facetCounts = facetField.getValues();

                // we reached the max number of results
                if (facetCounts != null && facetCounts.size() == max){
                    fieldsWithResults.add(FieldNames.INTERACTION_XREF_FACET);
                }

                collectFacetCountsFor(facetFieldCounts, facetCounts, FieldNames.INTERACTION_XREF);
            }
            // add interactor
            else if (FieldNames.INTERACTOR_XREF_FACET.equals(facetField.getName())){

                List<FacetField.Count> facetCounts = facetField.getValues();

                // we reached the max number of results
                if (facetCounts != null && facetCounts.size() == max){
                    fieldsWithResults.add(FieldNames.INTERACTOR_XREF_FACET);
                }

                collectFacetCountsFor(facetFieldCounts, facetCounts, FieldNames.INTERACTOR_XREF);
            }
        }

        return facetFieldCounts;
    }

    private void collectFacetCountsFor(Collection<FieldCount> facetFieldCounts, List<FacetField.Count> facetCounts, String searchFieldName) {
        if (facetCounts != null && !facetCounts.isEmpty()){
            for (FacetField.Count count : facetCounts){
                FieldCount fieldCount = new FieldCount(count.getName(), count.getCount(), searchFieldName);
                facetFieldCounts.add(fieldCount);
            }
        }
    }

    public void shutDown(){
        this.ontologySearcher.shutdown();
        this.interactionSearcher.shutdown();
    }

    private static class FieldCount {

        private String field;
        private String type;
        private String value;
        private String searchFieldName;
        private long count;

        private FieldCount(String field, long count, String searchFieldName) {
            this.field = field;

            if (field.contains(":")){
                int position = this.field.indexOf(":");
                this.type = field.substring(0, position);
                this.value = field.substring(position+1);
            }
            else {
                this.type = null;
                this.value = field;
            }

            this.count = count;
            this.searchFieldName = searchFieldName;
        }

        public String getField() {
            return field;
        }

        public long getCount() {
            return count;
        }

        public String getSearchFieldName() {
            return searchFieldName;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }
}

