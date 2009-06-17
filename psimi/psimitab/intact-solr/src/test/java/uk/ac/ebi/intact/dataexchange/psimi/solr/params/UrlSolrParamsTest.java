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
package uk.ac.ebi.intact.dataexchange.psimi.solr.params;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class UrlSolrParamsTest {

    @Test
    public void params1() throws Exception {
        String params = "q=*:*&sort=rigid asc&rows=30&fq=+dataset:(\"Cancer\")&fq=+go_expanded_id:(\"GO:0048511\")&start=0";

        UrlSolrParams solrParams = new UrlSolrParams(params);

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.add(solrParams);

        Assert.assertEquals("*:*", solrQuery.getQuery());
        Assert.assertEquals("rigid asc", solrQuery.getSortField());
        Assert.assertEquals(Integer.valueOf(30), solrQuery.getRows());
        Assert.assertEquals(Integer.valueOf(0), solrQuery.getStart());

        Assert.assertTrue(Arrays.asList(solrQuery.getFilterQueries()).contains("+go_expanded_id:(\"GO:0048511\")"));
        Assert.assertTrue(Arrays.asList(solrQuery.getFilterQueries()).contains("+dataset:(\"Cancer\")"));
    }
}
