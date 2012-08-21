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

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.hupo.psi.calimocho.io.IllegalFieldException;
import org.hupo.psi.calimocho.io.IllegalRowException;
import org.hupo.psi.calimocho.tab.io.IllegalColumnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.intact.bridges.ontologies.OntologyMapping;
import uk.ac.ebi.intact.bridges.ontologies.iterator.OntologyIterator;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.SolrDocumentConverter;
import uk.ac.ebi.intact.dataexchange.psimi.solr.failure.FailFastFailureHandling;
import uk.ac.ebi.intact.dataexchange.psimi.solr.failure.FailureHandlingStrategy;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologyIndexer;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;

import java.io.*;
import java.net.MalformedURLException;
import java.util.Arrays;

/**
 * Indexes information into a SOLR server.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactSolrIndexer {

    private Logger log = LoggerFactory.getLogger(IntactSolrIndexer.class);

    private SolrServer solrServer;
    private HttpSolrServer ontologySolrServer;
    private SolrDocumentConverter converter;

    private int timesToRetry = 100;

    private FailureHandlingStrategy failureHandlingStrategy = new FailFastFailureHandling();

    //////////////////
    // Constructors

    public IntactSolrIndexer(String solrServerUrl) throws MalformedURLException {
        this(new HttpSolrServer(solrServerUrl));
    }

    public IntactSolrIndexer(SolrServer solrServer) {
        this.solrServer = solrServer;
        this.converter = new SolrDocumentConverter(solrServer);

        SolrLogger.readFromLog4j();
    }

    public IntactSolrIndexer(String solrServerUrl, String ontologySolrServerUrl) throws MalformedURLException {
        this(new HttpSolrServer(solrServerUrl), new HttpSolrServer(ontologySolrServerUrl));
    }

    public IntactSolrIndexer(SolrServer solrServer, HttpSolrServer ontologySolrServer) {
        this(solrServer);
        this.ontologySolrServer = ontologySolrServer;
        this.converter = new SolrDocumentConverter(solrServer, new OntologySearcher(ontologySolrServer));
    }

    ///////////////////////////
    // Getters and Setters

    public FailureHandlingStrategy getFailureHandlingStrategy() {
        return failureHandlingStrategy;
    }

    public void setFailureHandlingStrategy( FailureHandlingStrategy failureHandlingStrategy ) {
        this.failureHandlingStrategy = failureHandlingStrategy;
    }

    /////////////////////
    // Indexing

    public void indexOntologies(OntologyMapping[] ontologyMappings) throws IntactSolrException {
        if (ontologySolrServer == null) {
            throw new IllegalStateException("To index an ontology, an ontology SolrServer must be passed to the constructor");
        }
        
        if (log.isInfoEnabled()) log.info("Indexing ontologies: "+ Arrays.asList(ontologyMappings));
        
        OntologyIndexer ontologyIndexer = new OntologyIndexer(ontologySolrServer);
        ontologyIndexer.indexObo(ontologyMappings);
    }

    public void indexOntology(OntologyIterator ontologyIterator) throws IntactSolrException {
        if (ontologySolrServer == null) {
            throw new IllegalStateException("To index an ontology, an ontology SolrServer must be passed to the constructor");
        }

        OntologyIndexer ontologyIndexer = new OntologyIndexer(ontologySolrServer);
        ontologyIndexer.indexOntology(ontologyIterator);
    }

    public int indexMitab(File mitabFile, boolean hasHeader) throws IOException, IntactSolrException {
        FileInputStream inputStream = new FileInputStream(mitabFile);
        int num = 0;
        try{
            num = indexMitab(inputStream, hasHeader);
        }
        finally {
            inputStream.close();
        }
        return num;
    }

    public int indexMitab(File mitabFile, boolean hasHeader, Integer firstLine, Integer batchSize) throws IOException, IntactSolrException {
        FileInputStream inputStream = new FileInputStream(mitabFile);
        int num = 0;
        try{
            num = indexMitab(new FileInputStream(mitabFile), hasHeader, firstLine, batchSize);
        }
        finally {
            inputStream.close();
        }
        return num;
    }

    public int indexMitabFromClasspath(String resourceUrl, boolean hasHeader) throws IOException, IntactSolrException {
        return indexMitabFromClasspath(resourceUrl, hasHeader, null, null);
    }

    public int indexMitabFromClasspath(String resourceUrl, boolean hasHeader, Integer firstLine, Integer batchSize) throws IOException, IntactSolrException {
        InputStream resourceStream = IntactSolrIndexer.class.getResourceAsStream(resourceUrl);

        if (resourceStream == null) throw new IntactSolrException("Resource not found in the classpath: "+resourceUrl);

        int num = 0;

        try{
            num= indexMitab(resourceStream, hasHeader, firstLine, batchSize);
        }
        finally {
            resourceStream.close();
        }
        return num;
    }

    /**
     * Indexes a MITAB formatted input stream into the database.
     * @param mitabStream The stream to index
     * @param hasHeader Whether the data has header or not
     * @return Count of indexed lines
     * @throws IOException Thrown if there is a problem reading the stream
     * @throws IntactSolrException Thrown if there is a problem indexing the data
     */
    public int indexMitab(InputStream mitabStream, boolean hasHeader) throws IOException, IntactSolrException {
        return indexMitab(mitabStream, hasHeader, null, null);
    }

    /**
     * Indexes a MITAB formatted input stream into the database.
     * @param mitabStream The stream to index
     * @param hasHeader Whether the data has header or not
     * @param firstLine The first line to process, being line 0 the first line in the file ignoring the header
     * @param batchSize Number of lines to process
     * @return Count of indexed lines
     * @throws IOException Thrown if there is a problem reading the stream
     * @throws IntactSolrException Thrown if there is a problem indexing the data
     */
    public int indexMitab(InputStream mitabStream, boolean hasHeader, Integer firstLine, Integer batchSize) throws IOException, IntactSolrException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(mitabStream));

        int num = 0;
        try{
            num = indexMitab(reader, hasHeader, firstLine, batchSize);
        }
        finally {
            reader.close();
        }

        return num;
    }

    /**
     * Indexes MITAB data using a Reader into the database.
     * @param reader The reader to use
     * @param hasHeader Whether the data has header or not
     * @return Count of indexed lines
     * @throws IOException Thrown if there is a problem reading
     * @throws IntactSolrException Thrown if there is a problem indexing the data
     */
    public int indexMitab(BufferedReader reader, boolean hasHeader) throws IOException, IntactSolrException {
        return indexMitab(reader, hasHeader, null, null);
    }

    /**
     * Indexes MITAB data using a Reader into the database.
     * @param reader The reader to use
     * @param hasHeader Whether the data has header or not
     * @param firstLine The first line to process, being line 0 the first line in the file ignoring the header
     * @param batchSize Number of lines to process
     * @return Count of indexed lines
     * @throws IOException Thrown if there is a problem reading
     * @throws IntactSolrException Thrown if there is a problem indexing the data
     */
    public int indexMitab(BufferedReader reader, boolean hasHeader, Integer firstLine, Integer batchSize) throws IOException, IntactSolrException {
        int lineCount = 0;

        int first = (firstLine == null)? 0 : firstLine;
        int lastRes = (batchSize == null)? Integer.MAX_VALUE : batchSize;
        int end = first + lastRes;

        if (hasHeader) {
            first++;
            if (end != Integer.MAX_VALUE) end++;
        }

        if ( log.isDebugEnabled() ) log.debug( "Processing from " + first + ".." + end );

        int processed = 0;

        String line;

        while ((line = reader.readLine()) != null) {

            try {

                if (lineCount >= first && lineCount < end) {

                    addSolrDocument(line, timesToRetry);

                    processed++;

                    if (lineCount > 0 && lineCount % 100 == 0) {
                        commitSolr(false, timesToRetry);
                    }

                    if (log.isDebugEnabled() && processed % 1000 == 0) {
                        if (log.isDebugEnabled()) log.debug("Processed: "+processed);
                    }
                }

                if (lineCount >= end) {
                    if ( log.isDebugEnabled() ) log.debug( "Reached the end of the chunk to be processed (lineCount:"+
                                                           lineCount+", end:"+ end +"), stopping here. " );
                    break;
                }

            } catch( Throwable t ) {
                failureHandlingStrategy.handleFailure( t, line, lineCount );
            }

            lineCount++;
        }

        commitSolr(true, timesToRetry);

        return processed;
    }

    private void addSolrDocument(String line, int retriesLeft) throws IOException {
        try {
           SolrInputDocument inputDocument = converter.toSolrDocument(line);
           solrServer.add(inputDocument);

        } catch (SolrServerException e) {

            handleAddDocumentException( e, line, retriesLeft );

        } catch (SolrException e) {

            handleAddDocumentException( e, line, retriesLeft );
        } catch (IllegalFieldException e) {
            handleAddDocumentException( e, line, retriesLeft );
        } catch (IllegalColumnException e) {
            handleAddDocumentException( e, line, retriesLeft );
        } catch (IllegalRowException e) {
            handleAddDocumentException( e, line, retriesLeft );
        }
    }

    private void handleAddDocumentException( Exception e, String line, int retriesLeft ) throws IOException {
        if (retriesLeft > 0) {
            if (log.isErrorEnabled())
                log.error("Error adding document to the server. Retrying in 10 seconds. Times to retry: " + retriesLeft + ". Line in process: "+line, e);

            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e1) {
                log.error("Interrupted thread", e1);
            }

            addSolrDocument(line, retriesLeft - 1);

        } else {
            throw new IntactSolrException("Cannot add the document to the server, after retrying " + timesToRetry + " times");
        }
    }

    private void commitSolr(boolean optimize, int retriesLeft) throws IOException, IntactSolrException {
        try {

            if (optimize) {
                solrServer.optimize();

                // Recommendations are that all searchers are closed and reopened when Solr index is optimized
                // as files on the file system can be deleted and create NFS Stale File Handles.
//                this.converter
                
            }
            else {
                solrServer.commit();
            }

        } catch ( SolrServerException e ) {

            handleCommitException( e, optimize, retriesLeft );

        } catch ( SolrException e ) {

            handleCommitException( e, optimize, retriesLeft );
        }
    }

    private void handleCommitException( Exception e, boolean optimize, int retriesLeft ) throws IOException {
        if (retriesLeft > 0) {
            if (log.isErrorEnabled())
                log.error("Error committing. Retrying in 10 seconds. Times to retry: " + retriesLeft, e);

            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e1) {
                log.error("Interrupted thread", e1);
            }

            commitSolr(optimize, retriesLeft - 1);

        } else {
            throw new IntactSolrException("Cannot add the document to the server, after retrying " + timesToRetry + " times");
        }
    }

    public int getTimesToRetry() {
        return timesToRetry;
    }

    public void setTimesToRetry(int timesToRetry) {
        this.timesToRetry = timesToRetry;
    }

    public void shutdown(){
        if (ontologySolrServer != null){
            ontologySolrServer.shutdown();
        }
        if (solrServer != null && solrServer instanceof HttpSolrServer){
            HttpSolrServer httpsolrserver = (HttpSolrServer) solrServer;
            httpsolrserver.shutdown();
        }
    }
}
