package org.safehaus.subutai.ui.hadoop.manager;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.subutai.ui.hadoop.manager.components.ClusterNode;

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
		grid.setRows(7);
		grid.setSizeFull();

		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setMargin(true);
		horizontalLayout.setSpacing(true);

		horizontalLayout.addComponent(getButtonRefresh());
		horizontalLayout.addComponent(getIndicator());

		buttonsLayout = new HorizontalLayout();
		buttonsLayout.setMargin(true);
		buttonsLayout.setSpacing(true);

		Embedded startedButton = new Embedded("", new ThemeResource("icons/buttons/start.png"));
		startedButton.setWidth(ClusterNode.ICON_SIZE, Unit.PIXELS);
		startedButton.setHeight(ClusterNode.ICON_SIZE, Unit.PIXELS);
		startedButton.setEnabled(false);
		buttonsLayout.addComponent(startedButton);
		buttonsLayout.addComponent(new Label("Started node"));

		Embedded stoppedButton = new Embedded("", new ThemeResource("icons/buttons/stop.png"));
		stoppedButton.setWidth(ClusterNode.ICON_SIZE, Unit.PIXELS);
		stoppedButton.setHeight(ClusterNode.ICON_SIZE, Unit.PIXELS);
		stoppedButton.setEnabled(false);
		buttonsLayout.addComponent(stoppedButton);
		buttonsLayout.addComponent(new Label("Stopped node"));

		grid.addComponent(horizontalLayout, 0, 0);
		grid.addComponent(getHadoopTable(), 0, 1, 0, 5);
		grid.addComponent(buttonsLayout, 0, 6);

		addComponent(grid);
	}

	private Button getButtonRefresh() {
		refreshButton = new Button("Refresh");
		refreshButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				table.refreshDataSource();
			}
		});

		return refreshButton;
	}

	private Embedded getIndicator() {
		indicator = new Embedded("", new ThemeResource("icons/indicator.gif"));
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
