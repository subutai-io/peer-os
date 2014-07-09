package org.safehaus.subutai.ui.commandrunner;

import com.google.common.base.Strings;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.TextArea;

/**
 * Created by daralbaev on 7/9/14.
 */
public class TerminalControl extends TextArea {

	private String inputPrompt;
	private String username, currentPath, machineName;

	public TerminalControl() {
		username = (String) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("username");
		currentPath = "/";
		machineName = "";

		this.setSizeFull();
		this.setImmediate(true);
		this.setWordwrap(true);
		this.addStyleName("terminal");

		setInputPrompt();
		addShortcutListener(new ShortcutListener("Shortcut Name", ShortcutAction.KeyCode.ENTER, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				System.out.println("Enter");
				getCommand();
			}
		});
	}

	public void setInputPrompt() {
		inputPrompt = String.format("%s@%s:%s#", username, machineName, currentPath);
		if (Strings.isNullOrEmpty(getValue())) {
			setValue(String.format("%s", inputPrompt));
		} else {
			setValue(String.format("%s\n$s", getValue(), inputPrompt));
		}
		this.setCursorPosition(this.getValue().length());
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
		String value = getValue();
		System.out.println(value);

		if (Strings.isNullOrEmpty(value)) {
			String[] args = value.split(inputPrompt);
			value = args[args.length - 1];
		}

		System.out.println(value);
		return value;
	}
}
