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
package uk.ac.ebi.intact.task.mitab.index;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.batch.item.ItemStreamException;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.BinaryInteractionEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.OntologyCrossReferenceEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.task.mitab.BinaryInteractionItemProcessor;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyEnricherItemProcessor implements BinaryInteractionItemProcessor {

    private String ontologiesSolrUrl;

    private BinaryInteractionEnricher enricher;
    private boolean processOnlyInteractors = false;

    // settings SOLRServer
    private int maxTotalConnections = 128;
    private int defaultMaxConnectionsPerHost = 32;
    private int connectionTimeOut = 20000;
    private int soTimeOut = 20000;
    private boolean allowCompression = true;

    public BinaryInteraction process(BinaryInteraction item) throws Exception {
        if (enricher == null) {

            if (ontologiesSolrUrl == null) {
                throw new ItemStreamException("ontologiesSolrUrl is null");
            }

            HttpSolrServer ontologiesSolrServer = createSolrServer();
            OntologySearcher ontologySearcher = new OntologySearcher(ontologiesSolrServer);
            enricher = new OntologyCrossReferenceEnricher(ontologySearcher);
        }

        if (!processOnlyInteractors){
            enricher.enrich(item);
        }
        else {
            if (item.getInteractorA() != null){
                enricher.enrich(item.getInteractorA());
            }
            if (item.getInteractorB() != null){
                enricher.enrich(item.getInteractorB());
            }
        }

        return item;
    }

    private HttpSolrServer createSolrServer() {
        HttpSolrServer ontologiesSolrServer = new HttpSolrServer(ontologiesSolrUrl, createHttpClient());

        ontologiesSolrServer.setConnectionTimeout(connectionTimeOut);
        ontologiesSolrServer.setSoTimeout(soTimeOut);
        ontologiesSolrServer.setAllowCompression(allowCompression);
        return ontologiesSolrServer;
    }

    private HttpClient createHttpClient() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
                .getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory
                .getSocketFactory()));

        PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
        cm.setMaxTotal(maxTotalConnections);
        cm.setDefaultMaxPerRoute(defaultMaxConnectionsPerHost);

        HttpClient httpClient = new DefaultHttpClient(cm);

        return httpClient;
    }

    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }

    public void setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }

    public int getDefaultMaxConnectionsPerHost() {
        return defaultMaxConnectionsPerHost;
    }

    public void setDefaultMaxConnectionsPerHost(int defaultMaxConnectionsPerHost) {
        this.defaultMaxConnectionsPerHost = defaultMaxConnectionsPerHost;
    }

    public int getConnectionTimeOut() {
        return connectionTimeOut;
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    public int getSoTimeOut() {
        return soTimeOut;
    }

    public void setSoTimeOut(int soTimeOut) {
        this.soTimeOut = soTimeOut;
    }

    public boolean isAllowCompression() {
        return allowCompression;
    }

    public void setAllowCompression(boolean allowCompression) {
        this.allowCompression = allowCompression;
    }

    public void setOntologiesSolrUrl(String ontologiesSolrUrl) {
        this.ontologiesSolrUrl = ontologiesSolrUrl;
    }

    public void setEnricher(BinaryInteractionEnricher enricher) {
        this.enricher = enricher;
    }

    @Override
    public void onlyProcessInteractors(boolean onlyInteractors) {
        this.processOnlyInteractors = onlyInteractors;
    }
}
