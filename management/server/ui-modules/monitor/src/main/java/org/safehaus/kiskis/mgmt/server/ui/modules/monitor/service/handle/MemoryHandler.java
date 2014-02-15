package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class MemoryHandler extends Handler {

    public MemoryHandler() {
        super("Memory Usage", "KB");
    }

    protected BoolQueryBuilder getQueryBuilder() {
        return QueryBuilders.boolQuery()
                .must(termQuery("host", "node1"))
                .must(termQuery("collectd_type", "memory"))
                .must(termQuery("plugin", "memory"))
                .must(termQuery("type_instance", "used"));
    }

    protected Map<String, Double> parseHits(SearchHit hits[]) {
        LinkedHashMap<String, Double> map = new LinkedHashMap<String, Double>();

        for (int i = hits.length-1; i >= 0; i--) {
            Map<String, Object> json = hits[i].getSource();
            map.put(
                    json.get("@timestamp").toString(),
                    (Double) json.get("value") / 1024
            );
        }

        return map;
    }

}
