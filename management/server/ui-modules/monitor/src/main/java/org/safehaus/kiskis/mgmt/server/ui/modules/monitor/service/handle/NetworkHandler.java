package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle;

import org.elasticsearch.index.query.BoolQueryBuilder;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class NetworkHandler extends Handler {

    public NetworkHandler () {
        super("if_packets", "rx + tx");
    }

    protected void setQueryBuilder(BoolQueryBuilder queryBuilder) {
        queryBuilder
                .must(termQuery("host", "node1"))
                .must(termQuery("collectd_type", "if_packets"));
    }

    protected Double getValue(Map<String, Object> json) {
        Integer rx = (Integer) json.get("rx");
        Integer tx = (Integer) json.get("tx");

        return (double) (rx + tx);
    }
}
