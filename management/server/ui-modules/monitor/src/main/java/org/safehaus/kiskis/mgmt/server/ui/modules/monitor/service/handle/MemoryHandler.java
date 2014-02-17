package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle;

import org.elasticsearch.index.query.BoolQueryBuilder;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class MemoryHandler extends Handler {

    public MemoryHandler() {
        super("memory_used", "KB");
    }

    protected void setQueryBuilder(BoolQueryBuilder queryBuilder) {
        queryBuilder
                .must(termQuery("collectd_type", "memory"))
                .must(termQuery("plugin", "memory"))
                .must(termQuery("type_instance", "used"));
    }

    protected Double getValue(Map<String, Object> json) {
        return (Double) json.get("value") / 1024;
    }

}
