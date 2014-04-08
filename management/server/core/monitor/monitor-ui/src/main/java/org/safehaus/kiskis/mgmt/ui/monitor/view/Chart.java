
package org.safehaus.kiskis.mgmt.ui.monitor.view;

import com.vaadin.ui.Window;
import org.apache.commons.lang3.StringUtils;
import org.safehaus.kiskis.mgmt.ui.monitor.service.Metric;
import org.safehaus.kiskis.mgmt.ui.monitor.service.Query;
import org.safehaus.kiskis.mgmt.ui.monitor.util.FileUtil;
import org.safehaus.kiskis.mgmt.ui.monitor.util.JavaScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Chart {

    private final static Logger LOG = LoggerFactory.getLogger(Chart.class);

    private static final String CHART_TEMPLATE = FileUtil.getContent("js/chart.js");

    private JavaScript javaScript;

    Chart(Window window) {
        javaScript = new JavaScript(window);
        loadScripts();
    }

    private void loadScripts() {
        javaScript.loadFile("js/jquery.min.js");
        javaScript.loadFile("js/highcharts.js");
    }

    void load(String host, Metric metric) {
        LOG.info("host: {}; metric: {}", host, metric);

        String data = Query.execute(host, metric.toString(), 25);

        if ( StringUtils.isEmpty(data) ) {
            return;
        }

        String chart = CHART_TEMPLATE
                .replace( "$mainTitle", String.format("%s for %s", metric, host) )
                .replace( "$yTitle", metric.getUnit() )
                .replace( "$data", data );

        javaScript.execute(chart);
    }

}
