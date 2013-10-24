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
package ExternalResources;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class SolrExternalResource extends MyExternalResource {
    private SolrServer solrEmbeddedServer;
    private String solrHome;
    public SolrExternalResource(String solrHome)
    {
        this.solrHome = solrHome;
    }
    @Override
    public void before()
    {
        System.setProperty("solr.solr.home", solrHome);
        File root = new File(solrHome);
        CoreContainer container = new CoreContainer();

        SolrConfig config = null;
        try {
            config = new SolrConfig(root + "/collection1",
                    "solrconfig.xml",null);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        CoreDescriptor descriptor = new CoreDescriptor(container,
                "collection1",root + "");
        SolrCore core = new SolrCore("collection1", root +
                "/../cores_data/collection1", config,  null, descriptor);
        container.register(core, false);
        container.load();
        solrEmbeddedServer = new EmbeddedSolrServer(container,"collection1");

    }
    @Override
    public void after()
    {
        System.out.println("In after method of " + this.getClass().getSimpleName());
        System.out.println("Trying to shutdown the solr embedded server manually!");
        getSolrEmbeddedServer().shutdown();
    }

    public SolrServer getSolrEmbeddedServer() {
        return solrEmbeddedServer;
    }

    public void setSolrEmbeddedServer(EmbeddedSolrServer solrEmbeddedServer) {
        this.solrEmbeddedServer = solrEmbeddedServer;
    }
}
