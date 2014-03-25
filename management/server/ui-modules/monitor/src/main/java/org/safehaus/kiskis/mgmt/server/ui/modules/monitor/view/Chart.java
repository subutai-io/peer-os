
package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view;

import com.vaadin.ui.Window;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.search.Metric;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.search.Query;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.util.FileUtil;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.util.JavaScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Chart {

    private final static Logger LOG = LoggerFactory.getLogger(Chart.class);

    private static final String CHART_TEMPLATE = FileUtil.getContent("js/chart.js");

    private JavaScript javaScript;

    private Timer timer;

    private String host;
    private Metric metric;

    Chart(Window window) {
        javaScript = new JavaScript(window);
        loadScripts();
    }

    private void loadScripts() {
        javaScript.loadFile("js/jquery.min.js");
        javaScript.loadFile("js/highcharts.js");
    }

    void load(String host, Metric metric) {

        LOG.info("metric: {}", metric);

        if (metric == null) {
            return;
        }

//        if (true) {
//            return;
//        }

        this.host = host;
        this.metric = metric;

        String data = Query.execute(host, metric.toString(), 20);
        LOG.info("data: {}", data);

        String chart = CHART_TEMPLATE
                .replace( "$mainTitle", String.format("%s for %s", metric, host) )
                .replace( "$yTitle", metric.getUnit() )
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
        String data = Query.execute(host, metric.toString(), 1);
        LOG.info("data: {}", data);

        String script = String.format("setData(%s);", data);
        javaScript.execute(script);
    }

}
