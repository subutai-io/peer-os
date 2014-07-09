package org.safehaus.subutai.ui.commandrunner;

import com.google.common.base.Strings;
import com.vaadin.event.FieldEvents;
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

		initSendButton();
		initCommandPrompt();

		addComponent(sendButton);
		addComponent(commandPrompt);
		focusPrompt();

		this.setSizeFull();
	}

	private void initSendButton() {
		sendButton = new Button("");
		sendButton.setVisible(false);
		sendButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				getCommand();
			}
		});
	}

	private void initCommandPrompt() {
		commandPrompt = new TextArea();
		commandPrompt.setSizeFull();
		commandPrompt.setImmediate(true);
		commandPrompt.addStyleName("terminal");
		setInputPrompt();

		commandPrompt.addTextChangeListener(new FieldEvents.TextChangeListener() {
			@Override
			public void textChange(FieldEvents.TextChangeEvent textChangeEvent) {
				getCommand();
			}
		});
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
