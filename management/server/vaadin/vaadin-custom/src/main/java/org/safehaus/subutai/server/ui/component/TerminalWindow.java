/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.server.ui.component;

import com.google.common.base.Strings;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.*;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Response;
import org.safehaus.subutai.shared.protocol.enums.ResponseType;

import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * @author dilshat
 */
public class TerminalWindow {

	private final Window window;
	private final TextArea commandOutputTxtArea;
	private volatile int taskCount = 0;

	public TerminalWindow(
			final Set<Agent> agents,
			final ExecutorService executor,
			final CommandRunner commandRunner,
			final AgentManager agentManager) {

		GridLayout grid = new GridLayout();
		grid.setColumns(1);
		grid.setRows(11);
		grid.setSizeFull();
		grid.setMargin(true);
		grid.setSpacing(true);

		window = new Window(String.format("Shell"), grid);
		window.setModal(true);
		window.setWidth(600, Unit.PIXELS);
		window.setHeight(400, Unit.PIXELS);

		commandOutputTxtArea = new TextArea("Commands output");
		commandOutputTxtArea.setRows(15);
		commandOutputTxtArea.setColumns(40);
		commandOutputTxtArea.setImmediate(true);
		commandOutputTxtArea.setWordwrap(true);

		HorizontalLayout controls = new HorizontalLayout();
		controls.setSpacing(true);

		Label lblCommand = new Label("Command");
		lblCommand.addStyleName("dark");

		final TextField txtCommand = new TextField();
		txtCommand.setWidth(250, Unit.PIXELS);
		txtCommand.setValue("pwd");

		final Button clearBtn = new Button("Clear");
		clearBtn.addStyleName("default");

		final Button sendBtn = new Button("Send");
		sendBtn.addStyleName("default");

		final Label indicator = new Label();
		indicator.setIcon(new ThemeResource("img/spinner.gif"));
		indicator.setContentMode(ContentMode.HTML);
		indicator.setHeight(11, Unit.PIXELS);
		indicator.setWidth(50, Unit.PIXELS);
		indicator.setVisible(false);

		controls.addComponent(lblCommand);
		controls.addComponent(txtCommand);
		controls.addComponent(clearBtn);
		controls.addComponent(sendBtn);
		controls.addComponent(indicator);

		grid.addComponent(txtCommand, 0, 0, 0, 9);
		grid.setComponentAlignment(commandOutputTxtArea, Alignment.TOP_CENTER);
		grid.addComponent(controls, 0, 10);
		grid.setComponentAlignment(controls, Alignment.BOTTOM_CENTER);

		txtCommand.addShortcutListener(new ShortcutListener("Shortcut Name", ShortcutAction.KeyCode.ENTER, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				sendBtn.click();
			}
		});
		sendBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				if (!Strings.isNullOrEmpty(txtCommand.getValue())) {
					indicator.setVisible(true);
					taskCount++;
					final Command command = commandRunner.createCommand(new RequestBuilder(txtCommand.getValue()), agents);
					executor.execute(new Runnable() {
						@Override
						public void run() {
							commandRunner.runCommand(command, new CommandCallback() {

								@Override
								public void onResponse(Response response, AgentResult agentResult, Command command) {
									Agent agent = agentManager.getAgentByUUID(agentResult.getAgentUUID());
									String host = agent == null ? String.format("Offline[%s]", response.getUuid()) : agent.getHostname();
									StringBuilder out = new StringBuilder(host).append(":\n");
									if (!Strings.isNullOrEmpty(response.getStdOut())) {
										out.append(response.getStdOut()).append('\n');
									}
									if (!Strings.isNullOrEmpty(response.getStdErr())) {
										out.append(response.getStdErr()).append('\n');
									}
									if (response.isFinal()) {
										if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE) {
											out.append("Exit code: ").append(response.getExitCode()).append("\n\n");
										} else {
											out.append("Command timed out").append("\n\n");
										}
									}
									addOutput(out.toString());

								}
							});
							taskCount--;
							if (taskCount == 0) {
								indicator.setVisible(false);
							}
						}
					});
				} else {
					addOutput("Please enter command to run");
				}
			}
		});
		clearBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				commandOutputTxtArea.setValue("");
			}
		});
	}

	private void addOutput(String output) {
		if (!Strings.isNullOrEmpty(output)) {
			commandOutputTxtArea.setValue(String.format("%s%s", commandOutputTxtArea.getValue(), output));
			commandOutputTxtArea.setCursorPosition(commandOutputTxtArea.getValue().length() - 1);
		}
	}

	public Window getWindow() {
		return window;
	}
}
