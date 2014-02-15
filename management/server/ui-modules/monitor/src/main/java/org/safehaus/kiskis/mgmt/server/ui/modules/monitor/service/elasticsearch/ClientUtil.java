package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.elasticsearch;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;

public class ClientUtil {

    private static TransportClient client;

    private static TransportClient getClient() {

        if (client != null) {
            return client;
        }

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("client.transport.ignore_cluster_name", true)
                .put("node.name", "TestNode")
                .build();

        client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("172.16.10.103", 9300));

        return client;
    }

    public static SearchHit[] execute(BoolQueryBuilder queryBuilder) {
        SearchResponse response = getClient().prepareSearch()
                .setQuery(queryBuilder)
                .setSize(20)
                .addSort("@timestamp", SortOrder.DESC)
                .execute().actionGet();

        return response.getHits().getHits();
    }
}
