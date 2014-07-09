package org.safehaus.subutai.ui.commandrunner;

import com.google.common.base.Strings;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

/**
 * Created by daralbaev on 7/9/14.
 */
public class TerminalControl extends VerticalLayout {

	private TextArea commandPrompt;
	private Button sendButton;
	private String inputPrompt;
	private String username, currentPath, machineName;

	public TerminalControl() {
		username = (String) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("username");
		currentPath = "/";
		machineName = "";

		commandPrompt = new TextArea();
		commandPrompt.setSizeFull();
		commandPrompt.setImmediate(true);
		commandPrompt.addStyleName("terminal");
		setInputPrompt();

		sendButton = new Button("");
		sendButton.setVisible(false);

		addComponent(sendButton);
		addComponent(commandPrompt);
		this.setSizeFull();
	}

	public void setInputPrompt() {
		inputPrompt = String.format("%s@%s:%s#", username, machineName, currentPath);
		if (Strings.isNullOrEmpty(commandPrompt.getValue())) {
			commandPrompt.setValue(String.format("%s", inputPrompt));
		} else {
			commandPrompt.setValue(String.format("%s\n$s", commandPrompt.getValue(), inputPrompt));
		}
		commandPrompt.setCursorPosition(commandPrompt.getValue().length());
		this.focus();
	}

	public String getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getCommand() {
		String value = commandPrompt.getValue();
		System.out.println(value);

		if (Strings.isNullOrEmpty(value)) {
			String[] args = value.split(inputPrompt);
			value = args[args.length - 1];
		}

		System.out.println(value);
		return value;
	}
}
