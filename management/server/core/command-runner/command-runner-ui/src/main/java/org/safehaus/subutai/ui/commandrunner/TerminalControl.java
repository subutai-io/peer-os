package org.safehaus.subutai.ui.commandrunner;

import com.google.common.base.Strings;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.TextArea;
import org.safehaus.subutai.shared.protocol.FileUtil;

/**
 * Created by daralbaev on 7/9/14.
 */
public class TerminalControl extends CssLayout {

	private TextArea commandPrompt;
	private String inputPrompt;
	private String username, currentPath, machineName;

	public TerminalControl() {
		username = (String) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("username");
		currentPath = "/";
		machineName = "";

		initCommandPrompt();
		addComponent(commandPrompt);

		this.setSizeFull();
	}

	private void initCommandPrompt() {
		commandPrompt = new TextArea();
		commandPrompt.setId("terminal");
		commandPrompt.setSizeFull();
		commandPrompt.setImmediate(true);
		commandPrompt.addStyleName("terminal");
		setInputPrompt();
		focusPrompt();

		JavaScript.getCurrent().execute(FileUtil.getContent("js/jquery-1.7.1.min.js", this));
		JavaScript.getCurrent().execute(FileUtil.getContent("js/jquery.terminal-min.js", this));
		JavaScript.getCurrent().execute(FileUtil.getContent("js/jquery.mousewheel-min.js", this));
	}

	public void setInputPrompt() {
		inputPrompt = String.format("%s@%s:%s#", username, machineName, currentPath);
		if (Strings.isNullOrEmpty(commandPrompt.getValue())) {
			commandPrompt.setValue(String.format("%s", inputPrompt));
		} else {
			commandPrompt.setValue(String.format("%s\n%s", commandPrompt.getValue(), inputPrompt));
		}
	}

	public void focusPrompt() {
		commandPrompt.setCursorPosition(commandPrompt.getValue().length());
		commandPrompt.focus();
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
