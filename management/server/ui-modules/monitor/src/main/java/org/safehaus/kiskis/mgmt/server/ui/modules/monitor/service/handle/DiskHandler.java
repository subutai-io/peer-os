package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle;

import org.elasticsearch.index.query.BoolQueryBuilder;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class DiskHandler extends Handler {

    public DiskHandler () {
        super("disk_time", "read + write");
    }

    protected void setQueryBuilder(BoolQueryBuilder queryBuilder) {
        queryBuilder
                .must(termQuery("host", "node1"))
                .must(termQuery("collectd_type", "disk_time"));
    }

    protected Double getValue(Map<String, Object> json) {
        Integer read = (Integer) json.get("read");
        Integer write = (Integer) json.get("write");

        return (double) (read + write);
    }
}
