/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.search.engine.SearchEngineException;
import psidev.psi.mi.search.engine.impl.FastSearchEngine;
import psidev.psi.mi.search.query.SearchQuery;
import psidev.psi.mi.tab.model.BinaryInteraction;

/**
 * TODO comment this!
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id: IntActFastSearchEngine.java 
 */
public class IntActFastSearchEngine extends FastSearchEngine {

    private static final Log log = LogFactory.getLog(IntActFastSearchEngine.class);
    private static final String WILDCARD = "*";
    private Directory indexDirectory;
	

    public IntActFastSearchEngine(Directory indexDirectory) throws IOException
    {
    	super(indexDirectory);
        this.indexDirectory = indexDirectory;
    }

    public IntActFastSearchEngine(String indexDirectory) throws IOException
    {
    	super(indexDirectory);
        this.indexDirectory = FSDirectory.getDirectory(indexDirectory);
    }


    public IntActFastSearchEngine(File indexDirectory) throws IOException
    {
        super(indexDirectory);
    	this.indexDirectory = FSDirectory.getDirectory(indexDirectory);
    }
    
   @Override
    public SearchResult search(SearchQuery searchQuery, Integer firstResult, Integer maxResults, Sort sort) throws SearchEngineException
    {
        if (searchQuery == null)
        {
            throw new NullPointerException("searchQuery cannot be null");
        }

        if (searchQuery.getQuery() == null) {
            throw new NullPointerException("SearchQuery object must contain a not null query");
        }
        
        if (!searchQuery.getClass().equals(IntActSearchQuery.class)){
        	searchQuery = new IntActSearchQuery(searchQuery);
        }

        if (log.isDebugEnabled()) log.debug("Searching=\""+ searchQuery.getQuery()+"\" (first="+firstResult+"/max="+maxResults+")");
        
        IndexSearcher indexSearcher = null;
        try {
            indexSearcher = new IndexSearcher(indexDirectory);
        } catch (Exception e) {
            throw new SearchEngineException(e);
        }

        if (firstResult == null) firstResult = 0;
        if (maxResults == null) maxResults = Integer.MAX_VALUE;

        if (searchQuery.getQuery().trim().equals(WILDCARD))
        {
            return searchAll(firstResult, maxResults);
        }

        long startTime = System.currentTimeMillis();

        Hits hits;
        Query query;

        try {
            // this does the actual lucene searchQuery
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new MultiFieldQueryParser(searchQuery.getFields(), analyzer);
            query = parser.parse(searchQuery.getQuery());

            if (log.isDebugEnabled()) log.debug("Expanded lucene query: " + query.toString());

            if (sort != null) {
                hits = indexSearcher.search(query, sort);
            } else {
                hits = indexSearcher.search(query);
            }

            if (log.isDebugEnabled()) log.debug("\tTime: " + (System.currentTimeMillis() - startTime) + "ms");
        }
        catch (Exception e) {
            closeIndexSearcher(indexSearcher);
            throw new SearchEngineException(e);
        }

        int totalCount = hits.length();

        if (totalCount < firstResult)
        {
            if (log.isDebugEnabled()) log.debug("\tNo hits. No results returned");

            closeIndexSearcher(indexSearcher);

            return new SearchResult(Collections.EMPTY_LIST, totalCount, firstResult, maxResults, searchQuery.getQuery().toString());
        }

        int maxIndex = Math.min(totalCount, firstResult+maxResults);

        if (log.isDebugEnabled()) log.debug("\tHits: "+hits.length()+". Will return from "+firstResult+" to "+maxIndex);

        List<BinaryInteraction> interactions = new ArrayList<BinaryInteraction>();

        for (int i=firstResult; i<maxIndex; i++)
        {
            try
            {
                Document doc = hits.doc(i);
                BinaryInteraction interaction = IntActDocumentBuilder.createBinaryInteraction(doc);
                interactions.add(interaction);
            }
            catch (Exception e)
            {
                closeIndexSearcher(indexSearcher);
                throw new SearchEngineException(e);
            }
        }

        closeIndexSearcher(indexSearcher);
        
        return new SearchResult(interactions, totalCount, firstResult, maxResults, query.toString());
    }
    
   @Override
    public SearchResult searchAll(Integer firstResult, Integer maxResults) throws SearchEngineException
    {
        IndexSearcher indexSearcher = null;
        try {
            indexSearcher = new IndexSearcher(indexDirectory);
        } catch (Exception e) {
            throw new SearchEngineException(e);
        }

        IndexReader reader = indexSearcher.getIndexReader();

        int totalCount = reader.maxDoc();

        // this is a hack to ignore any header introduced in the index by mistake (first development versions)
        if (reader.isDeleted(0))
        {
            firstResult++;
            totalCount--;
        }

        if (firstResult > totalCount)
        {
            closeIndexReader(reader);
            closeIndexSearcher(indexSearcher);
            return new SearchResult(Collections.EMPTY_LIST, totalCount, firstResult, maxResults, WILDCARD);
        }

        int maxIndex = Math.min(totalCount, firstResult+maxResults);

        List<BinaryInteraction> interactions = new ArrayList<BinaryInteraction>();

        for (int i=firstResult; i<maxIndex; i++)
        {
            try
            {
                Document doc = reader.document(i);
                BinaryInteraction interaction = IntActDocumentBuilder.createBinaryInteraction(doc);
                interactions.add(interaction);
            }
            catch (Exception e)
            {
                closeIndexReader(reader);
                closeIndexSearcher(indexSearcher);
                throw new SearchEngineException(e);
            }
        }

        closeIndexReader(reader);
        closeIndexSearcher(indexSearcher);
        return new SearchResult(interactions, totalCount, firstResult, maxResults, WILDCARD);
    }

}
