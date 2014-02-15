package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.elasticsearch.ClientUtil;

import java.util.Map;
import java.util.logging.Logger;

public abstract class Handler {

    protected Logger log = Logger.getLogger(getClass().getName());
    protected String mainTitle;
    protected String yTitle;

    protected Handler(String mainTitle, String yTitle) {
        this.mainTitle = mainTitle;
        this.yTitle = yTitle;
    }

    protected abstract BoolQueryBuilder getQueryBuilder();

    protected abstract Map<String, Double> parseHits(SearchHit hits[]);

    public String getMainTitle() {
        return mainTitle;
    }

    public String getYTitle() {
        return yTitle;
    }

    public Map<String, Double> getData() {
        SearchHit hits[] = ClientUtil.execute(getQueryBuilder());
        return parseHits(hits);
    }

}
