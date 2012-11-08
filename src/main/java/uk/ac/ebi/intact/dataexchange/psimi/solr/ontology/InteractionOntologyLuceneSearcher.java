package uk.ac.ebi.intact.dataexchange.psimi.solr.ontology;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.DocIdBitSet;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

/**
 * This is a lucene seracher for searching interaction ontology results
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/08/12</pre>
 */

public class InteractionOntologyLuceneSearcher {

    private final Directory indexDirectory;
    private boolean highlight;

    public InteractionOntologyLuceneSearcher(File indexDirectory) throws IOException{
        this.indexDirectory = FSDirectory.open(indexDirectory);
    }

    public InteractionOntologyLuceneSearcher(File indexDirectory, boolean highlight) throws IOException{
        this.indexDirectory = FSDirectory.open(indexDirectory);
        this.highlight = highlight;
    }

    public InteractionOntologyTerm findById(String value) throws IOException, ParseException {
        if (value == null || value.trim().length() == 0) return null;

        if (!value.startsWith("\"")) {
            value = "\"" + value + "\"";
        }

        InteractionOntologyTerm term = null;

        Collection<InteractionOntologyTerm> terms = search(value, new WhitespaceAnalyzer(Version.LUCENE_30));

        if (!terms.isEmpty()) {
            term = terms.iterator().next();
        }

        return term;
    }

    public InteractionOntologyTerm findByName(String value) throws IOException, ParseException {
        if (value == null || value.trim().length() == 0) return null;

        if (!value.startsWith("\"")) {
            value = "\"" + value + "\"";
        }

        InteractionOntologyTerm term = null;

        Collection<InteractionOntologyTerm> terms = searchByName(value, new WhitespaceAnalyzer(Version.LUCENE_30));

        if (!terms.isEmpty()) {
            term = terms.iterator().next();
        }

        return term;
    }

    public List<InteractionOntologyTerm> search(String strQuery) throws IOException, ParseException {
        return search(strQuery, new StandardAnalyzer(Version.LUCENE_30));
    }

    public List<InteractionOntologyTerm> search(String strQuery, Analyzer analyzer) throws IOException, ParseException {
        QueryParser queryParser = new QueryParser(Version.LUCENE_30, "identifier", analyzer);
        return search(queryParser.parse(strQuery), new Sort(new SortField("count", SortField.INT, true)));
    }

    public List<InteractionOntologyTerm> searchByName(String strQuery, Analyzer analyzer) throws IOException, ParseException {
        QueryParser queryParser = new QueryParser(Version.LUCENE_30, "label", analyzer);
        return search(queryParser.parse(strQuery), new Sort(new SortField("count", SortField.INT, true)));
    }

    public List<InteractionOntologyTerm> search(Query query, Sort sort) throws IOException {
        List<InteractionOntologyTerm> terms = new ArrayList<InteractionOntologyTerm>();

        IndexReader indexReader = IndexReader.open(indexDirectory);
        final IndexSearcher searcher = new IndexSearcher(indexReader);

        Filter filter = new Filter() {
            @Override
            public DocIdSet getDocIdSet(IndexReader indexReader) throws IOException {
                final DocIdBitSet bitSet = new DocIdBitSet(new BitSet());
                bitSet.getBitSet().flip(0, searcher.getIndexReader().numDocs());
                return bitSet;
            }
        };

        final TopDocs hits = searcher.search(query, filter, 30, sort);

        Formatter formatter = new SimpleHTMLFormatter();
        Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query) );
        highlighter.setTextFragmenter(new SimpleFragmenter(20));

        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document document = searcher.getIndexReader().document(scoreDoc.doc);

            InteractionOntologyTerm term = createOntologyTerm(document, highlighter);
            terms.add(term);
        }

        searcher.close();

        return terms;
    }

    private InteractionOntologyTerm createOntologyTerm(Document document, Highlighter highlighter) throws IOException {
        String identifier = document.getFieldable("identifier").stringValue();
        String label = document.getFieldable("label").stringValue();
        String databaseLabel = document.getFieldable("databaseLabel").stringValue();
        String fieldName = document.getFieldable("fieldName").stringValue();

        int count = Integer.parseInt(document.getField("count").stringValue());

        if (isHighlight()) {
            label = highlightText("label", label, highlighter);
        }

        InteractionOntologyTerm term = new InteractionOntologyTerm(label, identifier);
        term.setResults(new InteractionOntologyTermResults(databaseLabel, fieldName, count));

        return term;
    }

    private String highlightText(String fieldName, String text, Highlighter highlighter) throws IOException {
        TokenStream tokenStream = new StandardAnalyzer(Version.LUCENE_30).tokenStream(fieldName, new StringReader(text));

        try {
            return highlighter.getBestFragments(tokenStream, text, 5, "...");
        } catch (Throwable e) {
            throw new IOException( e.getMessage() );
        }
    }

    public boolean isHighlight() {
        return highlight;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }
}
