package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.search.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleComponent extends CustomComponent {

    private Chart chart;
    private ListSelect metricListSelect;

    public ModuleComponent() {
        setHeight("100%");
        setCompositionRoot( getLayout() );
    }

    public Layout getLayout() {

        AbsoluteLayout layout = new AbsoluteLayout();
        layout.setWidth(1000, Sizeable.UNITS_PIXELS);
        layout.setHeight(1000, Sizeable.UNITS_PIXELS);

        addMetricList(layout);
        addSubmitButton(layout);
        addChartLayout(layout);

        return layout;
    }

    private void addMetricList(AbsoluteLayout layout) {

        metricListSelect = UIUtil.addListSelect(layout, "Metric:", "left: 20px; top: 50px;", "150px", "270px");

        for ( Metric metric : Metric.values() ) {
            metricListSelect.addItem(metric);
        }
    }

    private void addSubmitButton(AbsoluteLayout layout) {

        Button startButton = UIUtil.getButton("Start", "150px");

        startButton.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                submitButtonClicked();
            }
        });

        layout.addComponent(startButton, "left: 20px; top: 330px;");
    }

    private void addChartLayout(AbsoluteLayout layout) {

        AbsoluteLayout chartLayout = new AbsoluteLayout();
        chartLayout.setWidth(800, Sizeable.UNITS_PIXELS);
        chartLayout.setHeight(400, Sizeable.UNITS_PIXELS);
        chartLayout.setDebugId("chart");

        layout.addComponent(chartLayout, "left: 200px; top: 20px;");
    }

    private void submitButtonClicked() {

        if (chart == null) {
            chart = new Chart( getWindow() );
        }

        chart.load( getSelectedNode(), getSelectedMetric() );
    }

    private String getSelectedNode() {
//        return StringUtils.defaultIfEmpty((String) nodeComboBox.getValue(), DEFAULT_NODE);
        return "py453399588";
    }

    private Metric getSelectedMetric() {
        return (Metric) metricListSelect.getValue();
    }
}