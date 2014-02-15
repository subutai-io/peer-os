package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class MemoryHandler {

    private final Logger log = Logger.getLogger(MemoryHandler.class.getName());

    public Map<String, Double> testData() {

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("client.transport.ignore_cluster_name", true)
                .put("node.name", "TestNode")
                .build();

        TransportClient client = new TransportClient(settings);
        client = client.addTransportAddress(new InetSocketTransportAddress("172.16.10.103", 9300));

        BoolQueryBuilder queryBuilder =  QueryBuilders.boolQuery()
                .must(termQuery("host", "node1"))
                .must(termQuery("collectd_type", "memory"))
                .must(termQuery("plugin", "memory"))
                .must(termQuery("type_instance", "used"));

        SearchResponse response = client.prepareSearch()
                .setQuery(queryBuilder)
                .setSize(20)
                .addSort("@timestamp", SortOrder.DESC)
                .execute().actionGet();


        SearchHit hits[] = response.getHits().getHits();
        log.info("response: " + response);

        LinkedHashMap<String, Double> map = new LinkedHashMap<String, Double>();

        for (int i = hits.length-1; i >= 0; i--) {
            Map<String, Object> json = hits[i].getSource();
            map.put(
                    json.get("@timestamp").toString(),
                    (Double) json.get("value") / 1024
            );
        }

        client.close();

        return map;
    }

}
