package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.util.FileUtil;

import java.util.Map;
import java.util.logging.Logger;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class ModuleComponent extends CustomComponent {

    private final Logger log = Logger.getLogger(ModuleComponent.class.getName());

    private boolean loaded;

    public ModuleComponent() {
        setHeight("100%");
        setCompositionRoot(getLayout());
    }

    public Layout getLayout() {

        AbsoluteLayout layout = new AbsoluteLayout();
        layout.setWidth(1000, Sizeable.UNITS_PIXELS);
        layout.setHeight(1000, Sizeable.UNITS_PIXELS);

        Button button = new Button("Test");
        button.setWidth(120, Sizeable.UNITS_PIXELS);

        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                getWindow().executeJavaScript("console.log(1)");
                loadScripts();
                loadScript("js/chart.js");
                getWindow().executeJavaScript("console.log(2)");
                testData();
            }
        });

        layout.addComponent(button, "left: 30px; top: 50px;");

        AbsoluteLayout layout2 = new AbsoluteLayout();
        layout2.setWidth(800, Sizeable.UNITS_PIXELS);
        layout2.setHeight(300, Sizeable.UNITS_PIXELS);
        layout2.setDebugId("subdiv");

        layout.addComponent(layout2, "left: 200px; top: 10px;");

        return layout;
    }

    private void testData() {

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("client.transport.ignore_cluster_name", true)
                .put("node.name", "TestNode")
                .build();

        TransportClient client = new TransportClient(settings);
        client = client.addTransportAddress(new InetSocketTransportAddress("172.16.10.103", 9300));

        BoolQueryBuilder queryBuilder =  QueryBuilders.boolQuery()
                .must(termQuery("host", "node1"))
                .must(termQuery("collectd_type", "memory"))
                .must(termQuery("plugin", "memory"))
                .must(termQuery("type_instance", "used"));

        SearchResponse response = client.prepareSearch()
                .setQuery(queryBuilder)
                .setSize(10)
                .addSort("@timestamp", SortOrder.DESC)
                .execute().actionGet();

        //System.out.println(response);
        SearchHit hits[] = response.getHits().getHits();
        log.info("count: " + hits.length);

        for (int i = hits.length-1; i >= 0; i--) {
            //for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> json = hits[i].getSource();
            log.info(json.get("@timestamp") + ": " + json.get("value"));
        }

        client.close();
    }

    private void loadScripts() {
        log.info("loaded: " + loaded);

        if (loaded) {
            return;
        }

        loadScript("js/jquery.min.js");
        loadScript("js/highcharts.js");

        loaded = true;
    }

    private void loadScript(String filePath) {
        getWindow().executeJavaScript(FileUtil.getContent(filePath));
    }
}