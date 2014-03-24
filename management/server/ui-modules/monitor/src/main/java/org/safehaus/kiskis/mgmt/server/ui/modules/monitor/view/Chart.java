package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view;

import com.vaadin.ui.Window;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle.Handler;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.util.FileUtil;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.util.JavaScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Random;

class Chart {

    private final static Logger LOG = LoggerFactory.getLogger(Chart.class);

//    private static final String CHART_TEMPLATE = FileUtil.getContent("js/chart2.js");
    private static final String CHART_TEMPLATE = FileUtil.getContent("js/chart.js");

    private JavaScript javaScript;

    Chart(Window window) {
        javaScript = new JavaScript(window);
        loadScripts();
    }

    void load(Handler handler, String node) {

//        Map<String, Double> data = handler.getData(node);
//
//        String chart = CHART_TEMPLATE
//                .replace("${mainTitle}", handler.getMainTitle())
//                .replace("${yTitle}", handler.getYTitle())
//                .replace("${categories}", format( new ArrayList( data.keySet() ), "'%s'") )
//                .replace("${values}", format( data.values(), "%s" ) );

        String chart = CHART_TEMPLATE;

        javaScript.execute(chart);
        push();
    }

    private void loadScripts() {
        javaScript.loadFile("js/jquery.min.js");
        javaScript.loadFile("js/highcharts.js");
    }

    private static String format(Collection<? extends Object> list, String pattern) {
        String str = "";

        for (Object v : list) {
            if (!str.isEmpty()) {
                str += ",";
            }

            str += String.format(pattern, v);
        }

        return str;
    }

    private void pushValue() {
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(1000);

                Random random = new Random();
                double d = random.nextDouble();

                LOG.info("i: {}, d: {}", i, d);
                javaScript.execute( String.format("i = %s;", d) );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void push() {

        Thread thread = new Thread(){
            public void run() {
                pushValue();
            }
        };

        thread.start();
    }

}
