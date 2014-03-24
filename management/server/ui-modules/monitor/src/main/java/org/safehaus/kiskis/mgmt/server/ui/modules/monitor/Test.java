package org.safehaus.kiskis.mgmt.server.ui.modules.monitor;

import com.vaadin.ui.Component;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.FacetBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.elasticsearch.ClientUtil;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle.Handler;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle.HandlerFactory;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle.Metric;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view.ModuleComponent;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import org.elasticsearch.index.query.FilterBuilders.*;

import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class Test {

    public static void main_(String[] args) {

        Handler handler = HandlerFactory.getHandler(Metric.CPU);
        Map<String, Double> data = handler.getData("node1");
        System.out.println(data);
    }

    public static void main(String args[]) {

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("client.transport.ignore_cluster_name", true)
//                .put("node.name", "MonitoringNode")
                .put("node.name", "Franz Kafka")
                .build();

        TransportClient client = new TransportClient(settings).addTransportAddress( new InetSocketTransportAddress("172.16.10.108", 9300) );

/*
        FacetBuilder facet = FacetBuilders.termsFacet("f")
//                .field("host")
                .field("log_host")
                .size(100);
*/

/*        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                //.must(termQuery("host", "node1"))
                .must(termQuery("name", "cpu_user"));

        SearchResponse searchResponse = client.prepareSearch()
                .must(termQuery("host", node));
//                .setQuery()
                .addFacet(facet)
                .setSize(30)
                .execute().actionGet();

        System.out.println(searchResponse);*/

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
//               .must(termQuery("host", "py453399588")) // logs host
               .must(termQuery("log_host", "py453399588"))
                .must(termQuery("name", "cpu_nice"));
//                .must(termQuery("name", "cpu_system"));
//                .should(termQuery("host", "py453399588"));
//                .must(termQuery("log_host", "management"));
//                .must(termQuery("log_host.exact", "py453399588"));

        BoolFilterBuilder filterBuilder = FilterBuilders.boolFilter()
//                .must(FilterBuilders.termFilter("log_host", "py453399588-lxc-spark1")); doesn't work
                .must(FilterBuilders.termFilter("name", "cpu_system"));
//                .must(FilterBuilders.termFilter("name", "cpu_nice"));
//                .mustNot(FilterBuilders.rangeFilter("age").from("10").to("20"))
//                .should(FilterBuilders.termFilter("tag", "sometag"))
//                .should(FilterBuilders.termFilter("tag", "sometagtag"));


//        QueryBuilder qb = QueryBuilders.fuzzyQuery("message", "DHCPREQUEST");
        QueryBuilder qb = QueryBuilders.matchQuery("host", "py453399588");

        FacetBuilder facet = FacetBuilders.termsFacet("f")
                .field("name")
                .size(100);

        SearchResponse response = client.prepareSearch()
                .setQuery(queryBuilder)
//                .setQuery(qb)
//                .setFilter(filterBuilder)
//                .addFacet(facet)
                .setSize(5)
                .addSort("@timestamp", SortOrder.DESC)
                .execute().actionGet();

//        System.out.println(response);

        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> json = hit.getSource();
            System.out.println(json);
        }


    }

}
