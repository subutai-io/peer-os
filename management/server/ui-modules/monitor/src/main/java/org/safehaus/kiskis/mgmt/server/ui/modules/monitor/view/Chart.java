
package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view;

import com.vaadin.ui.Window;
import org.codehaus.jackson.JsonNode;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle.Handler;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle.Metric;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.search.Format;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.search.Query;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.util.FileUtil;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.util.JavaScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Random;

class Chart {

    private final static Logger LOG = LoggerFactory.getLogger(Chart.class);

    private static final String CHART_TEMPLATE = FileUtil.getContent("js/chart.js");

    private JavaScript javaScript;
    private Timer timer;

    Chart(Window window) {
        javaScript = new JavaScript(window);
        loadScripts();
    }

    private void loadScripts() {
        javaScript.loadFile("js/jquery.min.js");
        javaScript.loadFile("js/highcharts.js");
    }

    void load(String host, Metric metric) {

        String data = Query.execute("py453399588", "cpu_user", 20);
        LOG.info("data: {}", data);

        String chart = CHART_TEMPLATE
                .replace( "$mainTitle", String.format("%s for %s", metric, host) )
                .replace("$yTitle", metric.getTitleY())
                .replace( "$data", data )
                .replace( "$data", data );

        javaScript.execute(chart);
        startTimer();
    }

    private void startTimer() {

        if (timer != null) {
            timer.interrupt();
        }

        timer = new Timer(this);
        timer.start();
    }

    void push() {
        String data = Query.execute("py453399588", "cpu_user", 1);
        LOG.info("data: {}", data);

        String script = String.format("setData(%s);", data);
        javaScript.execute(script);
    }

}
