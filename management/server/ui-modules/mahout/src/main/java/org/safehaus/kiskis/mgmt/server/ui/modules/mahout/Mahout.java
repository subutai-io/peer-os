package org.safehaus.kiskis.mgmt.server.ui.modules.mahout;

import com.vaadin.ui.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AsyncTaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

public class Mahout implements Module {

    public static final String MODULE_NAME = "Mahout";
    private AsyncTaskRunner taskRunner;

    public void setTaskRunner(AsyncTaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public static class ModuleComponent extends CustomComponent {

        private final TextArea commandOutputTxtArea;
        private final AgentManager agentManager;
        private final Label indicator;
        private final AsyncTaskRunner taskRunner;
        private volatile int taskCount = 0;

        public ModuleComponent(final AsyncTaskRunner taskRunner) {
            agentManager = ServiceLocator.getService(AgentManager.class);
            this.taskRunner = taskRunner;
            setHeight("100%");
            GridLayout grid = new GridLayout(20, 10);
            grid.setSizeFull();
            grid.setMargin(true);
            grid.setSpacing(true);

            commandOutputTxtArea = new TextArea("Commands output");
            commandOutputTxtArea.setSizeFull();
            commandOutputTxtArea.setImmediate(true);
            commandOutputTxtArea.setWordwrap(false);
            grid.addComponent(commandOutputTxtArea, 0, 3, 19, 9);

            Label logo = MgmtApplication.createImage("mahout.png", 200, 100);
            grid.addComponent(logo, 0, 0, 7, 2);
            Button checkBtn = new Button("Check");
            grid.addComponent(checkBtn, 16, 2, 16, 2);
            final Button installBtn = new Button("Install");
            grid.addComponent(installBtn, 17, 2, 17, 2);
            Button uninstallBtn = new Button("Uninstall");
            grid.addComponent(uninstallBtn, 18, 2, 18, 2);

            indicator = MgmtApplication.createImage("indicator.gif", 50, 11);
            indicator.setVisible(false);
            grid.addComponent(indicator, 19, 2, 19, 2);

            setCompositionRoot(grid);

            installBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Set<Agent> agents = Util.filterLxcAgents(MgmtApplication.getSelectedAgents());

                    if (agents.isEmpty()) {
                        commandOutputTxtArea.setValue("Please, select lxc node(s)");
                    } else {
                        commandOutputTxtArea.setValue("Installing Mahout ...\n");
                        Task checkTask = Tasks.getCheckTask(agents);
                        final Map<UUID, StringBuilder> outs = new HashMap<UUID, StringBuilder>();
                        for (Agent agent : agents) {
                            outs.put(agent.getUuid(), new StringBuilder());
                        }

                        executeTask(checkTask, new TaskCallback() {
                            final Set<Agent> eligibleAgents = new HashSet<Agent>();

                            @Override
                            public void onResponse(Task task, Response response) {
                                if (response != null && response.getUuid() != null
                                        && outs.get(response.getUuid()) != null) {

                                    StringBuilder sb = outs.get(response.getUuid());

                                    if (!Util.isStringEmpty(response.getStdOut())) {
                                        sb.append(response.getStdOut());
                                    }

                                    if (Util.isFinalResponse(response)) {
                                        if (sb.indexOf("ksks-mahout") > -1) {
                                            addOutput(String.format("%s: %s\n", getHostname(response), "Mahout is already installed. Omitting node from installation set"));
                                        } else if (sb.indexOf("ksks-hadoop") == -1) {
                                            addOutput(String.format("%s: %s\n", getHostname(response), "Hadoop is not installed. Omitting node from installation set"));
                                        } else {
                                            Agent agent = agentManager.getAgentByUUID(response.getUuid());
                                            if (agent != null) {
                                                eligibleAgents.add(agent);
                                            } else {
                                                addOutput(String.format("%s: %s\n", getHostname(response), "Agent is offline. Omitting node from installation set"));
                                            }
                                        }
                                    }
                                }
                                if (task.isCompleted()) {
                                    if (eligibleAgents.isEmpty()) {
                                        addOutput(String.format("%s\n", "No nodes eligible for installation. Installation aborted"));
                                    } else {
                                        //run installation 
                                        Task installTask = Tasks.getInstallTask(eligibleAgents);
                                        final Map<UUID, StringBuilder> errs = new HashMap<UUID, StringBuilder>();
                                        for (Agent agent : eligibleAgents) {
                                            errs.put(agent.getUuid(), new StringBuilder());
                                        }
                                        executeTask(installTask, new TaskCallback() {

                                            @Override
                                            public void onResponse(Task task, Response response) {
                                                if (response != null && response.getUuid() != null
                                                        && errs.get(response.getUuid()) != null) {

                                                    StringBuilder sb = errs.get(response.getUuid());

                                                    if (!Util.isStringEmpty(response.getStdErr())) {
                                                        sb.append(response.getStdErr());
                                                    }

                                                    if (Util.isFinalResponse(response)) {
                                                        if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE) {
                                                            if (response.getExitCode() == 0) {
                                                                addOutput(String.format("%s: %s\n", getHostname(response), "Installation done"));
                                                            } else {
                                                                addOutput(String.format("%s: %s: %s\n", getHostname(response), "Installation failed", sb.toString()));
                                                            }
                                                        } else if (response.getType() == ResponseType.EXECUTE_TIMEOUTED) {
                                                            addOutput(String.format("%s: %s\n", getHostname(response), "Command timed out"));
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                }
            });

            checkBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Set<Agent> agents = Util.filterLxcAgents(MgmtApplication.getSelectedAgents());

                    if (agents.isEmpty()) {
                        commandOutputTxtArea.setValue("Please, select lxc node(s)");
                    } else {
                        commandOutputTxtArea.setValue("Checking if Mahout is installed ...\n");
                        Task checkTask = Tasks.getCheckTask(agents);
                        final Map<UUID, StringBuilder> outs = new HashMap<UUID, StringBuilder>();
                        for (Agent agent : agents) {
                            outs.put(agent.getUuid(), new StringBuilder());
                        }
                        executeTask(checkTask, new TaskCallback() {

                            @Override
                            public void onResponse(Task task, Response response) {

                                if (response != null && response.getUuid() != null
                                        && outs.get(response.getUuid()) != null) {

                                    StringBuilder sb = outs.get(response.getUuid());

                                    if (!Util.isStringEmpty(response.getStdOut())) {
                                        sb.append(response.getStdOut());
                                    }

                                    if (Util.isFinalResponse(response)) {
                                        if (sb.indexOf("ksks-mahout") > -1) {
                                            addOutput(String.format("%s: %s\n", getHostname(response), "Mahout is installed"));
                                        } else {
                                            addOutput(String.format("%s: %s\n", getHostname(response), "Mahout is not installed"));
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            });

            uninstallBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Set<Agent> agents = Util.filterLxcAgents(MgmtApplication.getSelectedAgents());

                    if (agents.isEmpty()) {
                        commandOutputTxtArea.setValue("Please, select lxc node(s)");
                    } else {
                        commandOutputTxtArea.setValue("Uninstalling Mahout ...\n");
                        Task uninstallTask = Tasks.getUninstallTask(agents);
                        final Map<UUID, StringBuilder> outs = new HashMap<UUID, StringBuilder>();
                        final Map<UUID, StringBuilder> errs = new HashMap<UUID, StringBuilder>();
                        for (Agent agent : agents) {
                            outs.put(agent.getUuid(), new StringBuilder());
                            errs.put(agent.getUuid(), new StringBuilder());
                        }
                        executeTask(uninstallTask, new TaskCallback() {

                            @Override
                            public void onResponse(Task task, Response response) {

                                if (response != null && response.getUuid() != null
                                        && outs.get(response.getUuid()) != null
                                        && errs.get(response.getUuid()) != null) {

                                    StringBuilder out = outs.get(response.getUuid());
                                    StringBuilder err = errs.get(response.getUuid());

                                    if (!Util.isStringEmpty(response.getStdOut())) {
                                        out.append(response.getStdOut());
                                    }
                                    if (!Util.isStringEmpty(response.getStdErr())) {
                                        err.append(response.getStdErr());
                                    }

                                    if (Util.isFinalResponse(response)) {
                                        if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE) {
                                            if (response.getExitCode() == 0) {
                                                if (out.indexOf("Package ksks-mahout is not installed, so not removed") == -1) {
                                                    addOutput(String.format("%s: %s\n", getHostname(response), "Uninstallation done"));
                                                } else {
                                                    addOutput(String.format("%s: %s\n", getHostname(response), "Mahout is not installed, so not removed"));
                                                }
                                            } else {
                                                addOutput(String.format("%s: %s: %s\n", getHostname(response), "Uninstallation failed", err.toString()));
                                            }
                                        } else if (response.getType() == ResponseType.EXECUTE_TIMEOUTED) {
                                            addOutput(String.format("%s: %s\n", getHostname(response), "Command timed out"));
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            });

        }

        private String getHostname(Response response) {
            Agent agent = agentManager.getAgentByUUID(response.getUuid());
            return agent == null
                    ? String.format("Offline[%s]", response.getUuid()) : agent.getHostname();
        }

        private void executeTask(Task task, final TaskCallback callback) {
            indicator.setVisible(true);
            taskCount++;
            taskRunner.executeTask(task, new TaskCallback() {

                @Override
                public void onResponse(Task task, Response response) {
                    callback.onResponse(task, response);
                    if (task.isCompleted()) {
                        taskCount--;
                        if (taskCount == 0) {
                            indicator.setVisible(false);
                        }
                    }
                }
            });
        }

        private void addOutput(String output) {
            if (!Util.isStringEmpty(output)) {
                commandOutputTxtArea.setValue(
                        String.format("%s\n%s",
                                commandOutputTxtArea.getValue(),
                                output));
                commandOutputTxtArea.setCursorPosition(commandOutputTxtArea.getValue().toString().length() - 1);
            }
        }

    }

    @Override
    public String getName() {
        return Mahout.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent(taskRunner);
    }

}
