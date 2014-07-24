/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.commandrunner;


import com.google.common.base.Strings;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.*;
import org.safehaus.subutai.server.ui.component.AgentTree;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Disposable;
import org.safehaus.subutai.shared.protocol.Response;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.shared.protocol.enums.RequestType;
import org.safehaus.subutai.shared.protocol.settings.Common;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Command Runner UI - Terminal
 */
public class TerminalForm extends CustomComponent implements Disposable {

	final CommandRunner commandRunner;
	final AgentManager agentManager;
	private volatile int taskCount = 0;
	private ExecutorService executor;
	private AgentTree agentTree;
	private TerminalControl commandOutputTxtArea;
	//
	private TextField programTxtFld, workDirTxtFld, timeoutTxtFld;
	private ComboBox requestTypeCombo;
	private Label indicator;
	private VerticalLayout controls;


	public TerminalForm(final CommandRunner commandRunner, final AgentManager agentManager) {
		this.commandRunner = commandRunner;
		this.agentManager = agentManager;

		executor = Executors.newCachedThreadPool();

		setSizeFull();

		HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
		horizontalSplit.setSplitPosition(20, Unit.PERCENTAGE);
		horizontalSplit.setSizeFull();

		agentTree = new AgentTree(agentManager);

		horizontalSplit.setFirstComponent(agentTree);

		HorizontalSplitPanel gridLayout = new HorizontalSplitPanel();
		gridLayout.setSizeFull();
		gridLayout.setSplitPosition(80, Unit.PERCENTAGE);

		commandOutputTxtArea = new TerminalControl(this);

		controls = new VerticalLayout();
		controls.setSpacing(true);
		controls.setMargin(true);

		initProgram();
		initCommand();
		initTimeout();
		initRequestType();
		initIndicator();

		gridLayout.setFirstComponent(commandOutputTxtArea);
		gridLayout.setSecondComponent(controls);

		horizontalSplit.setSecondComponent(gridLayout);
		setCompositionRoot(horizontalSplit);
	}

	private void initProgram() {
		Label programLbl = new Label("Program");
		programTxtFld = new TextField();
		programTxtFld.setId("pwd");
		programTxtFld.setValue("pwd");
		programTxtFld.setWidth(200, Unit.PIXELS);
		controls.addComponent(programLbl);
		controls.addComponent(programTxtFld);
	}

	private void initCommand() {
		Label workDirLbl = new Label("Cwd");
		workDirTxtFld = new TextField();
		workDirTxtFld.setValue("/");
		controls.addComponent(workDirLbl);
		controls.addComponent(workDirTxtFld);
	}

	private void initTimeout() {
		Label timeoutLbl = new Label("Timeout");
		timeoutTxtFld = new TextField();
		timeoutTxtFld.setId("timeout_txt");
		timeoutTxtFld.setValue("30");
		timeoutTxtFld.setWidth(30, Unit.PIXELS);
		controls.addComponent(timeoutLbl);
		controls.addComponent(timeoutTxtFld);
	}

	private void initRequestType() {
		Label requestTypeLabel = new Label("Req Type");
		controls.addComponent(requestTypeLabel);
		requestTypeCombo = new ComboBox(null,
				Arrays.asList(RequestType.EXECUTE_REQUEST, RequestType.TERMINATE_REQUEST, RequestType.PS_REQUEST));
		requestTypeCombo.setImmediate(true);
		requestTypeCombo.setTextInputAllowed(false);
		requestTypeCombo.setNullSelectionAllowed(false);
		requestTypeCombo.setValue(RequestType.EXECUTE_REQUEST);
		requestTypeCombo.setWidth(200, Unit.PIXELS);
		controls.addComponent(requestTypeCombo);
	}

	private void initIndicator() {
		indicator = new Label();
		indicator.setId("terminal_indicator");
		indicator.setIcon(new ThemeResource("img/spinner.gif"));
		indicator.setContentMode(ContentMode.HTML);
		indicator.setHeight(11, Unit.PIXELS);
		indicator.setWidth(50, Unit.PIXELS);
		indicator.setVisible(false);
		controls.addComponent(indicator);
	}

	public void sendCommand(String command) {
		Set<Agent> agents = checkAgents();
		if (agents != null && validateInputs(command)) {
			RequestBuilder requestBuilder = new RequestBuilder(command);

			if (requestTypeCombo.getValue() == RequestType.TERMINATE_REQUEST) {
				requestBuilder.withPid(Integer.valueOf(programTxtFld.getValue()));
				requestBuilder.withType(RequestType.TERMINATE_REQUEST);
			} else if (requestTypeCombo.getValue() == RequestType.PS_REQUEST) {
				requestBuilder.withType(RequestType.PS_REQUEST);
			}

			requestBuilder.withTimeout(Integer.valueOf(timeoutTxtFld.getValue()));
			requestBuilder.withCwd(workDirTxtFld.getValue());

//			getUI().setPollInterval(Common.REFRESH_UI_SEC * 1000);
			createCommand(requestBuilder, agents);
		}
	}

	private Set<Agent> checkAgents() {
		Set<Agent> agents = agentTree.getSelectedAgents();
		if (agents.isEmpty()) {
			agents = null;
			show("Please, select nodes\\n");
		}

		return agents;
	}

	private boolean validateInputs(String command) {

		if (Strings.isNullOrEmpty(command)) {
			show("Please, enter command\\n");
			return false;
		}

		if (Strings.isNullOrEmpty(timeoutTxtFld.getValue()) || !Util.isNumeric(timeoutTxtFld.getValue())) {
			show("Please, enter integer timeout value\\n");
			return false;
		} else {
			int timeout = Integer.valueOf(timeoutTxtFld.getValue());
			if (timeout <= 0 || timeout > Common.MAX_COMMAND_TIMEOUT_SEC) {
				show("Please, enter timeout value between 0 and " + Common.MAX_COMMAND_TIMEOUT_SEC + "\\n");
			}
		}

		if (Strings.isNullOrEmpty(workDirTxtFld.getValue())) {
			show("Please, enter working directory\\n");
			return false;
		}

		return true;
	}

	private void createCommand(RequestBuilder requestBuilder, Set<Agent> agents) {
		final Command command = commandRunner.createCommand(requestBuilder, agents);
		final String[] output = {""};
		indicator.setVisible(true);
		taskCount++;
		executor.execute(new Runnable() {

			public void run() {
				commandRunner.runCommand(command, new CommandCallback() {

					@Override
					public void onResponse(Response response, AgentResult agentResult, Command command) {
						Agent agent = agentManager.getAgentByUUID(response.getUuid());
						String host = agent == null ? String.format("Offline[%s]", response.getUuid()) :
								agent.getHostname();
						StringBuilder out = new StringBuilder("host\n");

						if (!Strings.isNullOrEmpty(response.getStdOut())) {
							out.append(response.getStdOut());
						}
						if (!Strings.isNullOrEmpty(response.getStdErr())) {
							out.append(response.getStdErr());
						}

						output[0] += out.toString();
//						getUI().setPollInterval(Common.REFRESH_UI_SEC * 60000);
					}
				});

				taskCount--;
				if (taskCount == 0) {
					show(output[0]);
					indicator.setVisible(false);
				}
			}
		});
	}

	private void show(String notification) {
		commandOutputTxtArea.setOutputPrompt(notification);
	}

	public void dispose() {
		agentTree.dispose();
		executor.shutdown();
	}
}
