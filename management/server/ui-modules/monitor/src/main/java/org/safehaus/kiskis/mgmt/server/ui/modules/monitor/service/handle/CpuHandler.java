package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class CpuHandler extends Handler {

    public CpuHandler () {
        super("cpu_user", "");
    }

    protected void setQueryBuilder(BoolQueryBuilder queryBuilder) {
        queryBuilder
                .must(termQuery("name", "cpu_user"));
    }

    protected Map<String, Double> parseHits(SearchHit hits[]) {
        LinkedHashMap<String, Double> map = new LinkedHashMap<String, Double>();

        for (int i = hits.length-1; i >= 0; i--) {
            Map<String, Object> json = hits[i].getSource();
            map.put(
                    json.get("@timestamp").toString(),
                    (Double) json.get("val")
            );
        }

        return map;
    }

}
