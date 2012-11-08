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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.solr.common.params.MultiMapSolrParams;

import java.util.Collection;
import java.util.Map;

/**
 * Extends the SolrParams class, so it holds the solr params. This class
 * gets the params names and values from the URL.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class UrlSolrParams extends MultiMapSolrParams {

    public UrlSolrParams(String urlParams) {
        super(createMultiMap(urlParams));
    }

    private static Map<String, String[]> createMultiMap(String urlParams) {
        Multimap<String, String> map = getQueryMap(urlParams);

        Map<String, Collection<String>> colMap = map.asMap();

        Map<String, String[]> arrMap = Maps.newHashMapWithExpectedSize(colMap.size());

        for (Map.Entry<String, Collection<String>> entry : colMap.entrySet()) {
            arrMap.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
        }

        return arrMap;
    }

    private static Multimap<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Multimap<String, String> map = HashMultimap.create();

        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }
}
