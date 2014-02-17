package org.safehaus.kiskis.mgmt.server.ui.modules.monitor;

import com.vaadin.ui.Component;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.elasticsearch.ClientUtil;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view.ModuleComponent;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class Test {

    public static void main(String args[]) {

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("client.transport.ignore_cluster_name", true)
                .put("node.name", "TestNode")
                .build();

        TransportClient client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("172.16.10.103", 9300));

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                //.must(termQuery("host", "node1"))
                .must(termQuery("name", "cpu_user"));

                /*.must(termQuery("collectd_type", "memory"))
                .must(termQuery("plugin", "memory"))
                .must(termQuery("type_instance", "used"));*/

        SearchResponse response = client.prepareSearch()
                .setQuery(queryBuilder)
                .setSize(20)
                .addSort("@timestamp", SortOrder.DESC)
                .execute().actionGet();

        System.out.println(response);
    }

}
