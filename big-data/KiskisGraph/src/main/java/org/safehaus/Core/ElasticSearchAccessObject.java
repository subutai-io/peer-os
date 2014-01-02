package org.safehaus.Core;
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

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.Date;

import static org.elasticsearch.index.query.QueryBuilders.queryString;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.search.sort.SortBuilders.fieldSort;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class ElasticSearchAccessObject {
    private TransportClient client = null;
    public ElasticSearchAccessObject()
    {
        connect();
    }
    public ElasticSearchAccessObject(String host, int port)
    {
        connect(host,port);
    }
    public ElasticSearchAccessObject(String host, int port, String clusterName)
    {
        connect(host,port, clusterName);
    }
    public void connect(String host, int port) {
        //Create Client
        Settings settings = ImmutableSettings.settingsBuilder().put("elasticsearch", "localtestsearch").build();
        TransportClient client = new TransportClient(settings);
        client = client.addTransportAddress(new InetSocketTransportAddress(host, port));
        this.setClient(client);

    }
    public void connect(String host, int port, String clusterName) {
        //Create Client
        Settings settings = ImmutableSettings.settingsBuilder().put(clusterName, "localtestsearch").build();
        TransportClient client = new TransportClient(settings);
        client = client.addTransportAddress(new InetSocketTransportAddress(host, port));
        this.setClient(client);

    }
    public void connect() {
        //Create Client
        Settings settings = ImmutableSettings.settingsBuilder().put("elasticsearch", "localtestsearch").build();
        TransportClient client = new TransportClient(settings);
        client = client.addTransportAddress(new InetSocketTransportAddress("10.0.3.168", 9300));
        this.setClient(client);

    }
    public String executeSampleQuery()
    {
        int size = 1000;
        CountResponse response6 = getClient().prepareCount("logstash*")
                .setQuery(QueryBuilders.boolQuery()
                        .must(termQuery("collectd_type", "memory"))
                        .must(termQuery("host", "testlog")))
                .execute().actionGet();
//        System.out.println("Count for response6: " + response6.getCount());


        if(size>response6.getCount())
            size=(int)response6.getCount();
        SearchResponse response2 = getClient().prepareSearch("logstash*")
                .setQuery(QueryBuilders.boolQuery()
                        .must(termQuery("collectd_type", "memory"))
                        .must(termQuery("host", "testlog")))
                .addSort(fieldSort("@timestamp"))
                .setFrom((int) (response6.getCount() - size)).setSize(size)
                .execute().actionGet();
        String sampleResult = "";
        for(int i = 0; i< size; i++)
        {
            sampleResult += response2.getHits().getHits()[i].getId();
//            System.out.println("timestamp: " + response2.getHits().getHits()[i].getSource().get("@timestamp")+", id: " + response2.getHits().getHits()[i].getId() +", "+response2.getHits().getHits()[i].getSource().get("value"));
        }
//        getClient().close();
//        Double deneme = (Double.parseDouble(response2.getHits().getHits()[0].getSource().get("value").toString()));
//        System.out.println( String.format("%f", deneme));
        return "Count: "+ response6.getCount()+"\nID List of search (index:logstash*, collectd_type:memory, host:testlog):\n " + sampleResult;
    }
    public SearchResponse executeMemoryQuery(int lastIndex, Timestamp beginTime, Timestamp endTime)
    {
        if(lastIndex == -1)
        {
            lastIndex = 0;
        }
        SearchResponse response = client.prepareSearch("logstash*")
                .setQuery(QueryBuilders.boolQuery()
                        .must(termQuery("collectd_type", "memory"))
                        .must(termQuery("type_instance","free"))
                        .must(termQuery("host", "testlog"))
                )
                        //Show last hour data
                .setFilter(FilterBuilders.rangeFilter("@timestamp").from(beginTime).to(endTime))
                .addSort(fieldSort("@timestamp"))
                .setFrom(lastIndex).setSize(999999)
                .execute().actionGet();

        System.out.println("New query is sent and the total result count is: "+response.getHits().getTotalHits());
        System.out.println("New query is sent with the following parameters: lastIndex:" + lastIndex);
//        client.close();
        return response;

    }

    public ArrayList<String> getHosts(){
        ArrayList<String> hosts = new ArrayList<String>();

        CountResponse countResponse = client.prepareCount("logstash*")
                .setQuery(QueryBuilders.boolQuery()
                        .must(queryString("path:*"))
                        .must(queryString("host:*")))
                .execute().actionGet();


        SearchResponse searchResponse = client.prepareSearch("logstash*")
                .setQuery(QueryBuilders.boolQuery()
                        .must(queryString("path:*"))
                        .must(queryString("host:*")))
                .setFrom(0).setSize((int) countResponse.getCount())
                .execute().actionGet();

        for(int i = 0; i<countResponse.getCount(); i++)
        {
            String temp = (String) searchResponse.getHits().getHits()[i].getSource().get("host");

            if( !(exists(hosts, temp))){
                hosts.add(temp);
            }
        }
        return hosts;
    }

    public ArrayList<LogResponse> getLogs(int lastIndex, Timestamp beginTime, Timestamp endTime){
        if(lastIndex == -1)
        {
            lastIndex = 0;
        }
        ArrayList<LogResponse> list = new ArrayList<LogResponse>();

        SearchResponse searchResponse = client.prepareSearch("logstash*")
                .setQuery(QueryBuilders.boolQuery()
                        .must(queryString("path:*log"))
                        .must(queryString("host:*")))
                .setFilter(FilterBuilders.rangeFilter("@timestamp").from(beginTime).to(endTime))
                .setFrom((int)lastIndex).setSize(999999)
                .execute().actionGet();

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

    public TransportClient getClient() {
        return client;
    }

    public void setClient(TransportClient client) {
        this.client = client;
    }
}
