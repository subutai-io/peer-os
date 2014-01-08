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
package org.safehaus.core;

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.elasticsearch.index.query.QueryBuilders.queryString;
import static org.elasticsearch.search.sort.SortBuilders.fieldSort;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class ElasticSearchAccessObject {
    private Logger logger = Logger.getLogger("ElasticSearchQueryLogger");
    private String host = "localhost";
    private int port = 9300;
    TransportClient client;
    private String clusterName = "elasticsearch";
    public ElasticSearchAccessObject()
    {
        host = Configuration.getConfiguration("elasticsearch.properties", "host");
        port = Integer.parseInt(Configuration.getConfiguration("elasticsearch.properties", "port"));
        clusterName = Configuration.getConfiguration("elasticsearch.properties", "clusterName");
    }
    public TransportClient connect() {
        //Create Client
        Settings settings = ImmutableSettings.settingsBuilder().put(clusterName, "localtestsearch").build();
        if(client != null)
            client.close();
        client = new TransportClient(settings);
        client = client.addTransportAddress(new InetSocketTransportAddress(host, port));
        return  client;
    }

    public SearchResponse executeQuery(BoolQueryBuilder queryBuilders, int lastIndex, Timestamp beginTime, Timestamp endTime)  {
        if (queryBuilders == null)
        {
            logger.log(Level.SEVERE, "Query cannot be NULL");
            return null;
        }
        client = connect();

        if(lastIndex == -1)
        {
            lastIndex = 0;
        }

        SearchRequestBuilder requestBuilder = client.prepareSearch("logstash*");
        requestBuilder.setFilter(FilterBuilders.rangeFilter("@timestamp").from(beginTime).to(endTime));
        requestBuilder.addSort(fieldSort("@timestamp"));
        requestBuilder.setFrom(lastIndex).setSize(999999);
        requestBuilder.setQuery(queryBuilders);
       logger.log(Level.INFO, "Full Query: " + requestBuilder.toString());

        SearchResponse response= requestBuilder.execute().actionGet();
        logger.log(Level.INFO, "New query is sent with lastIndex: " + lastIndex +" and the total result count is: "+response.getHits().getTotalHits());

        client.close();
        return response;
    }
    public ArrayList<String> getHosts(){
        ArrayList<String> hosts = new ArrayList<String>();

        client = connect();
        CountResponse countResponse = client.prepareCount("logstash*")
                .setQuery(QueryBuilders.boolQuery()
                        .must(queryString("log_host:*")))
                .execute().actionGet();


        SearchResponse searchResponse = client.prepareSearch("logstash*")
                .setQuery(QueryBuilders.boolQuery()
                        .must(queryString("log_host:*")))
                .setFrom(0).setSize((int) countResponse.getCount())
                .execute().actionGet();

        for(int i = 0; i<searchResponse.getHits().getHits().length; i++)
        {
            String temp = (String) searchResponse.getHits().getHits()[i].getSource().get("log_host");

            if( !(exists(hosts, temp))){
                hosts.add(temp);
            }
        }
        client.close();
        return hosts;
    }

    public ArrayList<LogResponse> getLogs(int lastIndex, Timestamp beginTime, Timestamp endTime){
        if(lastIndex == -1)
        {
            lastIndex = 0;
        }
        client = connect();
        ArrayList<LogResponse> list = new ArrayList<LogResponse>();

        BoolQueryBuilder queryBuilders =  QueryBuilders.boolQuery()
                                            .must(queryString("path:*log"))
                                            .must(queryString("host:*"));

        SearchRequestBuilder requestBuilder = client.prepareSearch("logstash*");
        requestBuilder.setFilter(FilterBuilders.rangeFilter("@timestamp").from(beginTime).to(endTime));
        requestBuilder.addSort(fieldSort("@timestamp"));
        requestBuilder.setFrom(lastIndex).setSize(999999);
        requestBuilder.setQuery(queryBuilders);
        logger.log(Level.INFO, "Full Query: " + requestBuilder.toString());

        SearchResponse searchResponse= requestBuilder.execute().actionGet();
        logger.log(Level.INFO, "New query is sent with lastIndex: " + lastIndex +" and the total result count is: "+searchResponse.getHits().getTotalHits());

        int diff = searchResponse.getHits().getHits().length;

        for(int i = 0; i<diff; i++)
        {
            LogResponse tmp = new LogResponse();
            tmp.setMessage((String) searchResponse.getHits().getHits()[i].getSource().get("message"));
            tmp.setHost((String) searchResponse.getHits().getHits()[i].getSource().get("host"));
            tmp.setPath((String) searchResponse.getHits().getHits()[i].getSource().get("path"));
            tmp.setTimestamp((String) searchResponse.getHits().getHits()[i].getSource().get("@timestamp"));
            tmp.setVersion((String) searchResponse.getHits().getHits()[i].getSource().get("@version"));
            tmp.setType((String) searchResponse.getHits().getHits()[i].getSource().get("type"));
            list.add(tmp);

        }

        client.close();
        return list;
    }

    public boolean exists(ArrayList<String> list, String target){
        if(list.size() == 0){
            return false;
        }
        else{
            for(int i=0; i<list.size(); i++){
                if(list.get(i).equals(target))
                    return true;
            }
        }
        return false;
    }

}
