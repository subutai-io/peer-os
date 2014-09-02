package org.safehaus.subutai.ui.sqoop.manager;

import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import org.safehaus.subutai.api.sqoop.setting.ExportSetting;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.ui.sqoop.SqoopUI;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExportPanel extends ImportExportBase {

	AbstractTextField hdfsPathField = UIUtil.getTextField("HDFS file path:", 300);

	public ExportPanel() {
		init();
	}

	@Override
	public void setAgent(Agent agent) {
		super.setAgent(agent);
		init();
	}

	@Override
	ExportSetting makeSettings() {
		ExportSetting s = new ExportSetting();
		s.setClusterName(clusterName);
		s.setHostname(agent.getHostname());
		s.setConnectionString(connStringField.getValue());
		s.setTableName(tableField.getValue());
		s.setUsername(usernameField.getValue());
		s.setPassword(passwordField.getValue());
		s.setHdfsPath(hdfsPathField.getValue());
		s.setOptionalParameters(optionalParams.getValue());
		return s;
	}

	@Override
	final void init() {
		removeAllComponents();

		if (agent == null) {
			addComponent(UIUtil.getLabel("<h1>No node selected</h1>", 200));
			return;
		}

		super.init();
		fields.add(hdfsPathField);

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.addComponent(UIUtil.getButton("Export", 120,
				new Button.ClickListener() {

					@Override
					public void buttonClick(Button.ClickEvent event) {
						clearLogMessages();
						if (!checkFields()) return;
						setFieldsEnabled(false);
						ExportSetting es = makeSettings();
						final UUID trackId = SqoopUI.getManager().exportData(es);

						OperationWatcher watcher = new OperationWatcher(trackId);
						watcher.setCallback(new OperationCallback() {

							@Override
							public void onComplete() {
								setFieldsEnabled(true);
							}
						});
						SqoopUI.getExecutor().execute(watcher);
					}

				}));
		buttons.addComponent(UIUtil.getButton("Cancel", 120, new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {
				detachFromParent();
			}
		}));

		List<Component> ls = new ArrayList<>();
		ls.add(UIUtil.getLabel("<h1>Sqoop Export</h1>", 100, Unit.PERCENTAGE));
		ls.add(connStringField);
		ls.add(tableField);
		ls.add(usernameField);
		ls.add(passwordField);
		ls.add(hdfsPathField);
		ls.add(optionalParams);
		ls.add(buttons);

		addComponents(ls);
	}

	@Override
	boolean checkFields() {
		if (super.checkFields()) {
			if (!hasValue(tableField, "Table name not specified"))
				return false;
			if (!hasValue(hdfsPathField, "HDFS file path not specified"))
				return false;
			// every field has value
			return true;
		}
		return false;
	}

}
