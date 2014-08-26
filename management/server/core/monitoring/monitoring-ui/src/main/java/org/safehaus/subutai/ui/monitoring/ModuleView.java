package org.safehaus.subutai.ui.monitoring;


import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.monitoring.Metric;
import org.safehaus.subutai.api.monitoring.Monitor;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.ui.monitoring.component.AgentTree;
import org.safehaus.subutai.ui.monitoring.util.UIUtil;

import java.util.Date;
import java.util.Map;
import java.util.Set;


public class ModuleView extends CustomComponent {

	private static final int MAX_SIZE = 500;
	private final Monitor monitor;
	private final AgentManager agentManager;
	private Chart chart;
	private AgentTree agentTree;
	private PopupDateField startDateField;
	private PopupDateField endDateField;
	private ListSelect metricListSelect;


	public ModuleView(Monitor monitor, AgentManager agentManager) {
		this.monitor = monitor;
		this.agentManager = agentManager;
		initContent();
	}


	private void initContent() {

		setHeight("100%");

		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		splitPanel.setSplitPosition(20);

		agentTree = new AgentTree(agentManager);
		splitPanel.setFirstComponent(agentTree);
		splitPanel.setSecondComponent(getMainLayout());

		setCompositionRoot(splitPanel);
	}


	private Layout getMainLayout() {

		AbsoluteLayout layout = new AbsoluteLayout();
		layout.setWidth(1200, Unit.PIXELS);
		layout.setHeight(1000, Unit.PIXELS);

		addNote(layout);
		addDateFields(layout);
		addMetricList(layout);
		addSubmitButton(layout);
		addChartLayout(layout);

		return layout;
	}


	private void addNote(AbsoluteLayout layout) {
		UIUtil.addLabel(layout, String.format("<i>Note: the chart displays only recent %s values.</i>", MAX_SIZE),
				"left: 20px; top: 10px;");
	}


	private void addDateFields(AbsoluteLayout layout) {

		Date endDate = new Date();
		Date startDate = DateUtils.addHours(endDate, -1);

		startDateField = UIUtil.addDateField(layout, "From:", "left: 20px; top: 50px;", startDate);
		endDateField = UIUtil.addDateField(layout, "To:", "left: 20px; top: 100px;", endDate);
	}


	private void addMetricList(AbsoluteLayout layout) {

		metricListSelect = UIUtil.addListSelect(layout, "Metric:", "left: 20px; top: 150px;", "200px", "270px");

		for (Metric metric : Metric.values()) {
			metricListSelect.addItem(metric);
		}
	}


	private void addSubmitButton(AbsoluteLayout layout) {

		Button button = UIUtil.getButton("Submit", "150px");

		button.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				submitButtonClicked();
			}
		});

		layout.addComponent(button, "left: 20px; top: 430px;");
	}


	private void addChartLayout(AbsoluteLayout layout) {

		AbsoluteLayout chartLayout = new AbsoluteLayout();
		chartLayout.setWidth(800, Unit.PIXELS);
		chartLayout.setHeight(400, Unit.PIXELS);
		chartLayout.setDebugId("chart");

		layout.addComponent(chartLayout, "left: 250px; top: 50px;");
	}


	private void submitButtonClicked() {

		String host = getSelectedNode();
		Metric metric = getSelectedMetric();

		if (validParams(host, metric)) {
			loadChart(host, metric);
		}
	}

	private String getSelectedNode() {

		Set<Agent> agents = agentTree.getSelectedAgents();

		return agents == null || agents.size() == 0 ? null : agents.iterator().next().getHostname();
	}

	private Metric getSelectedMetric() {
		return (Metric) metricListSelect.getValue();
	}

	private boolean validParams(String host, Metric metric) {

		boolean success = true;

		if (StringUtils.isEmpty(host)) {
			Notification.show("Please select a node");
			success = false;
		} else if (metric == null) {
			Notification.show("Please select a metric");
			success = false;
		}

		return success;
	}

	private void loadChart(String host, Metric metric) {

		if (chart == null) {
			chart = new Chart(MAX_SIZE);
		}

		Date startDate = startDateField.getValue();
		Date endDate = endDateField.getValue();

		Map<Date, Double> values = monitor.getData(host, metric, startDate, endDate);
		chart.load(host, metric, values);
	}
}
