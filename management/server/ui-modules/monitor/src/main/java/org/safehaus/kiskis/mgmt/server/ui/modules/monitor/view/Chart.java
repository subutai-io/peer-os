package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view;

import com.vaadin.ui.Window;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle.Handler;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.util.FileUtil;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.util.JavaScript;

import java.util.*;

class Chart {

    private static final String CHART = FileUtil.getContent("js/chart.js");

    private JavaScript javaScript;

    Chart(Window window) {
        javaScript = new JavaScript(window);
        loadScripts();
    }

    void load(Handler handler, String node) {

        Map<String, Double> data = handler.getData(node);

        String chart = CHART
                .replace("${mainTitle}", handler.getMainTitle())
                .replace("${yTitle}", handler.getYTitle())
                .replace("${categories}", format(new ArrayList(data.keySet()), "'%s'") )
                .replace("${values}", format(data.values(), "%s") );

        javaScript.execute(chart);
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

}
