/**
 * Copyright 2012 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.dataexchange.psimi.solr.util;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public final class IntactSolrUtils {
    
    private IntactSolrUtils() {}
    
    public static SchemaInfo retrieveSchemaInfo(SolrServer solrServer) throws IOException {
        SchemaInfo schemaInfo = new SchemaInfo();

        if (solrServer instanceof CommonsHttpSolrServer) {
            final CommonsHttpSolrServer solr = (CommonsHttpSolrServer) solrServer;
            
            final String url = solr.getBaseURL()+"/admin/file/?file=schema.xml";
            final GetMethod method = new GetMethod(url);
            final int code = solr.getHttpClient().executeMethod(method);

            XPath xpath = XPathFactory.newInstance().newXPath();
            String expression = "/schema/fields/field";
            InputStream stream = method.getResponseBodyAsStream();
            InputSource inputSource = new InputSource(stream);

            try {
                NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);

                for (int i=0; i<nodes.getLength(); i++) {
                    final String fieldName = nodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
                    schemaInfo.addFieldName(fieldName);
                }

                stream.close();
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }

        } else {
            throw new IllegalArgumentException("Cannot get schema for SolrServer with class: "+solrServer.getClass().getName());
        }

        return schemaInfo;
    }


}
