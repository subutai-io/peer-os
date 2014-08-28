package org.safehaus.subutai.ui.sqoop.manager;

import com.vaadin.ui.*;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.plugin.sqoop.api.setting.CommonSetting;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.ui.sqoop.SqoopUI;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class ImportExportBase extends VerticalLayout {

	protected String clusterName;
	protected Agent agent;
	protected List<Field> fields = new ArrayList<>();
	AbstractTextField connStringField = UIUtil.getTextField("Connection string:", 300);
	AbstractTextField tableField = UIUtil.getTextField("Table name:", 300);
	AbstractTextField usernameField = UIUtil.getTextField("Username:", 300);
	AbstractTextField passwordField = UIUtil.getTextField("Password:", 300, true);
	AbstractTextField optionalParams = UIUtil.getTextField("Optional parameters:", 300);
	TextArea logTextArea = UIUtil.getTextArea("Logs:", 600, 200);

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
		reset();
	}

	void reset() {
		for (Field f : this.fields) {
			if (f instanceof AbstractTextField) f.setValue("");
			else if (f instanceof CheckBox) f.setValue(false);
		}
	}

	abstract CommonSetting makeSettings();

	void init() {
		logTextArea.setValue("");
		logTextArea.setHeight(100, Unit.PERCENTAGE);

		fields.add(connStringField);
		fields.add(tableField);
		fields.add(usernameField);
		fields.add(passwordField);
		fields.add(optionalParams);
		fields.add(logTextArea);
	}

	void addComponents(List<Component> components) {
		GridLayout grid = new GridLayout(2, components.size());
		grid.setSpacing(true);
		grid.setMargin(true);
		for (int i = 0; i < components.size(); i++) {
			grid.addComponent(components.get(i), 0, i);
		}
		String title = "<h1>Hostname: " + agent.getHostname() + "</h1>";
		grid.addComponent(UIUtil.getLabel(title, 100, Unit.PERCENTAGE), 1, 0);
		grid.addComponent(logTextArea, 1, 1, 1, components.size() - 1 - 1);

		addComponent(grid);
	}

	boolean checkFields() {
		if (!hasValue(connStringField, "Connection string not specified"))
			return false;
		// table check is done in subclasses
		if (!hasValue(usernameField, "Username not specified"))
			return false;
		if (!hasValue(passwordField, "Password not specified"))
			return false;
		// fields have value
		return true;
	}

	boolean hasValue(Field f, String errMessage) {
		if (f.getValue() == null || f.getValue().toString().isEmpty()) {
			appendLogMessage(errMessage);
			return false;
		}
		return true;
	}

	void appendLogMessage(String m) {
		if (m != null && m.length() > 0) {
			logTextArea.setValue(logTextArea.getValue() + "\n" + m);
			logTextArea.setCursorPosition(logTextArea.getValue().length());
		}
	}

	void setFieldsEnabled(boolean enabled) {
		for (Field f : this.fields) f.setEnabled(enabled);
	}

	void clearLogMessages() {
		logTextArea.setValue("");
	}

	void detachFromParent() {
		ComponentContainer parent = (ComponentContainer) getParent();
		parent.removeComponent(this);
	}

	protected interface OperationCallback {

		void onComplete();
	}

	protected class OperationWatcher implements Runnable {

		private final UUID trackId;
		private OperationCallback callback;

		public OperationWatcher(UUID trackId) {
			this.trackId = trackId;
		}

		public void setCallback(OperationCallback callback) {
			this.callback = callback;
		}

		@Override
		public void run() {
			String m = "";
			while (true) {
				ProductOperationView po = SqoopUI.getTracker().getProductOperation(
						SqoopConfig.PRODUCT_KEY, trackId);
				if (po == null) break;

				if (po.getLog() != null) {
					String logText = po.getLog().replace(m, "");
					m = po.getLog();
					if (!logText.isEmpty()) appendLogMessage(logText);
					if (po.getState() != ProductOperationState.RUNNING) break;
				}
				try {
					Thread.sleep(300);
				} catch (InterruptedException ex) {
					break;
				}
			}
			if (callback != null) callback.onComplete();
		}

	}

}
