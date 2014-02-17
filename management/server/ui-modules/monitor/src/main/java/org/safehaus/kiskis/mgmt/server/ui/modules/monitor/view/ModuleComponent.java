package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle.*;

import java.util.logging.Logger;

public class ModuleComponent extends CustomComponent {

    private static final String DEFAULT_NODE = "node1";
    private static final String DEFAULT_METRIC = "MEMORY";
    private final Logger log = Logger.getLogger(ModuleComponent.class.getName());
    private Chart chart;

    private ComboBox nodeComboBox;
    private ComboBox metricComboBox;

    public ModuleComponent() {
        setHeight("100%");
        setCompositionRoot(getLayout());
    }

    public Layout getLayout() {

        AbsoluteLayout layout = new AbsoluteLayout();
        layout.setWidth(1000, Sizeable.UNITS_PIXELS);
        layout.setHeight(1000, Sizeable.UNITS_PIXELS);

        nodeComboBox = UIUtil.getComboBox("Nodes", DEFAULT_NODE, "node2", "172.16.10.109", "172.16.10.110", "bigdata");
        layout.addComponent(nodeComboBox, "left: 10px; top: 50px;");

        metricComboBox = UIUtil.getComboBox("Metric",
                Metric.MEMORY.toString(),
                Metric.CPU.toString(),
                Metric.DISK.toString(),
                Metric.NETWORK.toString()
        );
        layout.addComponent(metricComboBox, "left: 10px; top: 100px;");

        Button submitButton = UIUtil.getButton("Submit", 150);
        submitButton.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                handleSubmit();
            }
        });

        layout.addComponent(submitButton, "left: 10px; top: 150px;");

        AbsoluteLayout chartLayout = new AbsoluteLayout();
        chartLayout.setWidth(800, Sizeable.UNITS_PIXELS);
        chartLayout.setHeight(300, Sizeable.UNITS_PIXELS);
        chartLayout.setDebugId("chart");
        layout.addComponent(chartLayout, "left: 200px; top: 10px;");

        return layout;
    }

    private void handleSubmit() {

        if (chart == null) {
            chart = new Chart(getWindow());
        }

        Handler handler = HandlerFactory.getHandler(getSelectedMetric());
        chart.load(handler, getSelectedNode());
    }

    private String getSelectedNode() {
        return StringUtils.defaultIfEmpty((String) nodeComboBox.getValue(), DEFAULT_NODE);
    }

    private Metric getSelectedMetric() {
        String metric = StringUtils.defaultIfEmpty((String) metricComboBox.getValue(), DEFAULT_METRIC);
        return Metric.valueOf(metric);
    }
}