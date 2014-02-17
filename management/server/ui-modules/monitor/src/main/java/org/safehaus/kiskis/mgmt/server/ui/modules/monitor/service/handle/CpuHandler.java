package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle;

import org.elasticsearch.index.query.BoolQueryBuilder;
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

    protected Double getValue(Map<String, Object> json) {
        return (Double) json.get("val");
    }
}
