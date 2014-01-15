/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package solrTest;

import ExternalResources.SolrExternalResource;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class TestSolrExternalResource {

    @ClassRule
    public static SolrExternalResource solrExternalResource = new SolrExternalResource("/opt/solr-4.4.0/example/solr");

    @Test
    public void testSolrExternal()
    {
        SolrInputDocument doc1 = new SolrInputDocument();
        doc1.addField( "id", "id3", 1.0f );
        doc1.addField( "name", "doc3", 1.0f );
        doc1.addField( "price", 30 );

        SolrInputDocument doc2 = new SolrInputDocument();
        doc2.addField( "id", "id4", 1.0f );
        doc2.addField( "name", "doc4", 1.0f );
        doc2.addField( "price", 40 );

        Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
        docs.add( doc1 );
        docs.add( doc2 );
        try {
            solrExternalResource.getSolrEmbeddedServer().add(docs);
            solrExternalResource.getSolrEmbeddedServer().commit();
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        SolrQuery solrQuery = new SolrQuery("*:*");
        SolrQuery solrQuery = new SolrQuery("doc3");
        try {
            QueryResponse response = solrExternalResource.getSolrEmbeddedServer().query(solrQuery);
            System.out.println("Matching documents for query: " + solrQuery.toString());
            for (SolrDocument document : response.getResults()) {
                System.out.println(document);
            }
            Assert.assertTrue(response.getResults().size() != 0);
        } catch (SolrServerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
