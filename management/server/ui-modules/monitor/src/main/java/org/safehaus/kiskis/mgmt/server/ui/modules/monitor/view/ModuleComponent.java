package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.MemoryHandler;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.util.FileUtil;

import java.util.*;
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
                handleButton();
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

    private void handleButton() {

        loadScripts();

        String chart = FileUtil.getContent("js/chart.js");

        Map<String, Double> data = new MemoryHandler().testData();

        chart = chart.replace("${mainTitle}", "Memory Usage");
        chart = chart.replace("${yTitle}", "KB");
        chart = chart.replace("${categories}", formatData(data.keySet()));
        chart = chart.replace("${values}", formatData(data.values()));

        log.info("chart: " + chart);

        getWindow().executeJavaScript(chart);
    }

    private static String formatData(Collection<Double> data) {
        String s = "";

        for (Object v : data) {
            if (!s.isEmpty()) {
                s += ",";
            }

            s += v;
        }

        return s;
    }

    private static String formatData(Set<String> set) {
        String s = "";

        for (String v : set) {
            if (!s.isEmpty()) {
                s += ",";
            }

            s += String.format("'%s'", v);
        }

        return s;
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