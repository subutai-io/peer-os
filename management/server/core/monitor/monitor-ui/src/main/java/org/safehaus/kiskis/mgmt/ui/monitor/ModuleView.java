package org.safehaus.kiskis.mgmt.ui.monitor;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.safehaus.kiskis.mgmt.api.monitor.Metric;
import org.safehaus.kiskis.mgmt.api.monitor.Monitor;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.ui.monitor.util.UIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class ModuleView extends CustomComponent {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleView.class);

    private static final int MAX_SIZE = 500;

    private Chart chart;

    private final Monitor monitor;

    private PopupDateField startDateField;
    private PopupDateField endDateField;
    private ListSelect metricListSelect;

    public ModuleView(Monitor monitor) {
        this.monitor = monitor;

        setHeight("100%");
        setCompositionRoot( getLayout() );
    }

    public Layout getLayout() {

        AbsoluteLayout layout = new AbsoluteLayout();
        layout.setWidth(1000, Sizeable.UNITS_PIXELS);
        layout.setHeight(1000, Sizeable.UNITS_PIXELS);

        addNote(layout);
        addDateFields(layout);
        addMetricList(layout);
        addSubmitButton(layout);
        addChartLayout(layout);

        return layout;
    }

    private void addNote(AbsoluteLayout layout) {
        UIUtil.addLabel(layout, String.format("<i>Note: the chart displays only recent %s values.</i>", MAX_SIZE), "left: 20px; top: 10px;");
    }

    private void addDateFields(AbsoluteLayout layout) {

        Date endDate = new Date();
        Date startDate = DateUtils.addHours(endDate, -1);

        startDateField = UIUtil.addDateField(layout, "From:", "left: 20px; top: 50px;", startDate);
        endDateField = UIUtil.addDateField(layout, "To:", "left: 20px; top: 100px;", endDate);
    }

    private void addMetricList(AbsoluteLayout layout) {

        metricListSelect = UIUtil.addListSelect(layout, "Metric:", "left: 20px; top: 150px;", "150px", "270px");

        for ( Metric metric : Metric.values() ) {
            metricListSelect.addItem(metric);
        }
    }

    private void addSubmitButton(AbsoluteLayout layout) {

        Button button = UIUtil.getButton("Submit", "150px");

        button.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                submitButtonClicked();
            }
        });

        layout.addComponent(button, "left: 20px; top: 430px;");
    }

    private void addChartLayout(AbsoluteLayout layout) {

        AbsoluteLayout chartLayout = new AbsoluteLayout();
        chartLayout.setWidth(800, Sizeable.UNITS_PIXELS);
        chartLayout.setHeight(400, Sizeable.UNITS_PIXELS);
        chartLayout.setDebugId("chart");

        layout.addComponent(chartLayout, "left: 200px; top: 50px;");
    }

    private void submitButtonClicked() {

        String host = getSelectedNode();
        Metric metric = getSelectedMetric();

        if ( validParams(host, metric) ) {
            loadChart(host, metric);
        }

//        if ( !validParams(host, metric) ) {
//            return;
//        }

//        if (chart == null) {
//            chart = new Chart(getWindow(), MAX_SIZE);
//        }
//
//        Date startDate = (Date) startDateField.getValue();
//        Date endDate = (Date) endDateField.getValue();
//
//        chart.load(host, metric, startDate, endDate);
    }

    private void loadChart(String host, Metric metric) {

        if (chart == null) {
            chart = new Chart(getWindow(), MAX_SIZE);
        }

        Date startDate = (Date) startDateField.getValue();
        Date endDate = (Date) endDateField.getValue();

        LOG.info("host: {}, metric: {}, startDate: {}, endDate: {}", host, metric, startDate, endDate);

        Map<Date, Double> values = monitor.getData(host, metric, startDate, endDate);
        chart.load(host, metric, values);
    }

    private boolean validParams(String host, Metric metric) {

        boolean success = true;

        if ( StringUtils.isEmpty(host) ) {
            getWindow().showNotification("Please select a node");
            success = false;
        } else if (metric == null) {
            getWindow().showNotification("Please select a metric");
            success = false;
        }

        return success;
    }

    private String getSelectedNode() {

        Set<Agent> agents = MgmtApplication.getSelectedAgents();

        return agents == null || agents.size() == 0
                ? null
                : agents.iterator().next().getHostname();
    }

    private Metric getSelectedMetric() {
        return (Metric) metricListSelect.getValue();
    }
}