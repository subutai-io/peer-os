package org.safehaus.subutai.plugin.hadoop.ui.manager;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;

/**
 * Created by daralbaev on 12.04.14.
 */
public class Manager extends VerticalLayout {
	private HorizontalLayout horizontalLayout, buttonsLayout;
	private Embedded indicator;
	private Button refreshButton;
	private HadoopTable table;

	public Manager() {
		setSizeFull();

		GridLayout grid = new GridLayout();
		grid.setColumns(1);
		grid.setRows(11);
		grid.setSizeFull();
		grid.setMargin(true);
		grid.setSpacing(true);

		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSizeFull();
		horizontalLayout.setMargin(true);
		horizontalLayout.setSpacing(true);

		horizontalLayout.addComponent(getButtonRefresh());
		horizontalLayout.addComponent(getIndicator());

		buttonsLayout = new HorizontalLayout();
		buttonsLayout.setMargin(true);
		buttonsLayout.setSpacing(true);

		Embedded startedButton = new Embedded("", new ThemeResource("img/btn/play.png"));
		startedButton.setEnabled(false);
		buttonsLayout.addComponent(startedButton);
		buttonsLayout.setComponentAlignment(startedButton, Alignment.TOP_LEFT);

		Label startedNodeLabel = new Label("Started node");
		buttonsLayout.addComponent(startedNodeLabel);
		buttonsLayout.setComponentAlignment(startedNodeLabel, Alignment.MIDDLE_LEFT);

		Embedded stoppedButton = new Embedded("", new ThemeResource("img/btn/stop.png"));
		stoppedButton.setEnabled(false);
		buttonsLayout.addComponent(stoppedButton);
		buttonsLayout.setComponentAlignment(stoppedButton, Alignment.MIDDLE_LEFT);

		Label stoppedNodeLabel = new Label("Stopped node");
		buttonsLayout.addComponent(stoppedNodeLabel);
		buttonsLayout.setComponentAlignment(stoppedNodeLabel, Alignment.MIDDLE_LEFT);

		grid.addComponent(horizontalLayout, 0, 0);
		grid.addComponent(getHadoopTable(), 0, 1, 0, 9);
		grid.setComponentAlignment(table, Alignment.TOP_CENTER);
		grid.addComponent(buttonsLayout, 0, 10);
		grid.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

		addComponent(grid);
	}

	private Button getButtonRefresh() {
		refreshButton = new Button("Refresh");
		refreshButton.addStyleName("default");
		refreshButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				table.refreshDataSource();
			}
		});

		return refreshButton;
	}

	private Embedded getIndicator() {
		indicator = new Embedded("", new ThemeResource("img/spinner.gif"));
		indicator.setHeight(11, Unit.PIXELS);
		indicator.setWidth(50, Unit.PIXELS);
		indicator.setVisible(true);

		return indicator;
	}

	private HadoopTable getHadoopTable() {
		if (table == null) {
			table = new HadoopTable("Hadoop Clusters", indicator);
			table.setMultiSelect(false);
			table.setSizeFull();
		}

		return table;
	}
}
