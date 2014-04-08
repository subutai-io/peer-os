
package org.safehaus.kiskis.mgmt.ui.monitor.view;

import com.vaadin.ui.Window;
import org.safehaus.kiskis.mgmt.ui.monitor.service.Metric;
import org.safehaus.kiskis.mgmt.ui.monitor.service.Query;
import org.safehaus.kiskis.mgmt.ui.monitor.util.FileUtil;
import org.safehaus.kiskis.mgmt.ui.monitor.util.JavaScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

class Chart {

    private static final Logger LOG = LoggerFactory.getLogger(Chart.class);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String CHART_TEMPLATE = FileUtil.getContent("js/chart.js");

    private final int maxSize;
    private final JavaScript javaScript;

    Chart(Window window, int maxSize) {
        this.maxSize = maxSize;
        javaScript = new JavaScript(window);
        loadScripts();
    }

    private void loadScripts() {
        javaScript.loadFile("js/jquery.min.js");
        javaScript.loadFile("js/highcharts.js");
    }

    void load(String host, Metric metric, Date startDate, Date endDate) {
        LOG.info("host: {}, metric: {}, startDate: {}, endDate: {}", host, metric, startDate, endDate);

        Map<Date, Double> values = Query.execute(host, metric.toString(), startDate, endDate);
        String data = toPoints(values);

        String chart = CHART_TEMPLATE
                .replace( "$mainTitle", String.format("%s for %s", metric, host) )
                .replace( "$yTitle", metric.getUnit() )
                .replace( "$data", data );

        javaScript.execute(chart);
    }

    private String toPoints(Map<Date, Double> values) {

        String str = "";
        int i = 0;

        for ( Date date : values.keySet() ) {
            if ( !str.isEmpty() ) {
                str += ", ";
            }

            // Pass date as string so we don't deal with timezone issues between javascript and the server
            str += String.format( "{ x: Date.parse('%s'), y: %s }", DATE_FORMAT.format(date), values.get(date) );

            i++;
            if (i > maxSize) {
                break;
            }
        }

        return String.format("[%s]", str);
    }

}
