package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.elasticsearch.ClientUtil;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public abstract class Handler {

    protected String mainTitle;
    protected String yTitle;

    protected Handler(String mainTitle, String yTitle) {
        this.mainTitle = mainTitle;
        this.yTitle = yTitle;
    }

    protected abstract void setQueryBuilder(BoolQueryBuilder queryBuilder);

    protected BoolQueryBuilder getQueryBuilder(String node) {

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(termQuery("host", node));

        setQueryBuilder(queryBuilder);

        return queryBuilder;
    }

    public String getMainTitle() {
        return mainTitle;
    }

    public String getYTitle() {
        return yTitle;
    }

    public Map<String, Double> getData(String node) {
        SearchHit hits[] = ClientUtil.execute(getQueryBuilder(node));
        return parseHits(hits);
    }

    protected Map<String, Double> parseHits(SearchHit hits[]) {
        LinkedHashMap<String, Double> map = new LinkedHashMap<String, Double>();

        for (int i = hits.length-1; i >= 0; i--) {
            Map<String, Object> json = hits[i].getSource();
            map.put(
                    json.get("@timestamp").toString(),
                    getValue(json)
            );
        }

        return map;
    }

    protected Double getValue(Map<String, Object> json) {
        return (Double) json.get("value") / 1024;
    }

}
