package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.elasticsearch.ClientUtil;

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

    protected abstract Map<String, Double> parseHits(SearchHit hits[]);

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

}
