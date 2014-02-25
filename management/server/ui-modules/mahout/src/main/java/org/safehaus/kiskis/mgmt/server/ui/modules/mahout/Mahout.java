package org.safehaus.kiskis.mgmt.server.ui.modules.mahout;

import com.vaadin.ui.*;
import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

public class Mahout implements Module {

    public static final String MODULE_NAME = "Mahout";
    private TaskRunner taskRunner;

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public static class ModuleComponent extends CustomComponent {

        private final TextArea commandOutputTxtArea;
        private final AgentManager agentManager;
        private final Label indicator;
        private final TaskRunner taskRunner;
        private final Button checkBtn;
        private final Button installBtn;
        private final Button uninstallBtn;
        private volatile int taskCount = 0;

        public ModuleComponent(final TaskRunner taskRunner) {
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
            grid.addComponent(commandOutputTxtArea, 0, 1, 19, 9);

            Label logo = MgmtApplication.createImage("mahout.png", 200, 100);
            grid.addComponent(logo, 0, 0, 7, 0);
            checkBtn = new Button("Check");
            grid.addComponent(checkBtn, 16, 0, 16, 0);
            installBtn = new Button("Install");
            grid.addComponent(installBtn, 17, 0, 17, 0);
            uninstallBtn = new Button("Uninstall");
            grid.addComponent(uninstallBtn, 18, 0, 18, 0);

            indicator = MgmtApplication.createImage("indicator.gif", 50, 11);
            indicator.setVisible(false);
            grid.addComponent(indicator, 14, 0, 15, 0);

            grid.setComponentAlignment(checkBtn, Alignment.MIDDLE_CENTER);
            grid.setComponentAlignment(installBtn, Alignment.MIDDLE_CENTER);
            grid.setComponentAlignment(uninstallBtn, Alignment.MIDDLE_CENTER);
            grid.setComponentAlignment(indicator, Alignment.MIDDLE_CENTER);

            setCompositionRoot(grid);

            installBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Set<Agent> agents = Util.filterLxcAgents(MgmtApplication.getSelectedAgents());

                    if (agents.isEmpty()) {
                        commandOutputTxtArea.setValue("\nPlease, select lxc node(s)");
                    } else {
                        commandOutputTxtArea.setValue("\nInstalling Mahout ...\n");
                        Task checkTask = Tasks.getCheckTask(agents);
                        executeTask(checkTask, new TaskCallback() {
                            final Set<Agent> eligibleAgents = new HashSet<Agent>();

                            @Override
                            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

                                if (task.getData() == TaskType.CHECK) {
                                    if (Util.isFinalResponse(response)) {
                                        if (stdOut.indexOf("ksks-mahout") > -1) {
                                            addOutput(String.format("%s: %s\n", getHostname(response), "Mahout is already installed. Omitting node from installation set"));
                                        } else if (stdOut.indexOf("ksks-hadoop") == -1) {
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
                                    if (task.isCompleted()) {
                                        if (eligibleAgents.isEmpty()) {
                                            addOutput(String.format("%s\n", "No nodes eligible for installation. Installation aborted"));
                                        } else {
                                            //run installation 
                                            return Tasks.getInstallTask(eligibleAgents);
                                        }
                                    }
                                } else {
                                    if (Util.isFinalResponse(response)) {
                                        if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE) {
                                            if (response.getExitCode() == 0) {
                                                addOutput(String.format("%s: %s\n", getHostname(response), "Installation done"));
                                            } else {
                                                addOutput(String.format("%s: %s: %s\n", getHostname(response), "Installation failed", stdErr));
                                            }
                                        } else if (response.getType() == ResponseType.EXECUTE_TIMEOUTED) {
                                            addOutput(String.format("%s: %s\n", getHostname(response), "Command timed out"));
                                        }
                                    }
                                }

                                return null;
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
                        commandOutputTxtArea.setValue("\nPlease, select lxc node(s)");
                    } else {
                        commandOutputTxtArea.setValue("\nChecking if Mahout is installed ...\n");
                        Task checkTask = Tasks.getCheckTask(agents);
                        executeTask(checkTask, new TaskCallback() {

                            @Override
                            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

                                if (Util.isFinalResponse(response)) {
                                    if (stdOut.indexOf("ksks-mahout") > -1) {
                                        addOutput(String.format("%s: %s\n", getHostname(response), "Mahout is installed"));
                                    } else {
                                        addOutput(String.format("%s: %s\n", getHostname(response), "Mahout is not installed"));
                                    }
                                }

                                return null;
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
                        commandOutputTxtArea.setValue("\nPlease, select lxc node(s)");
                    } else {
                        commandOutputTxtArea.setValue("\nUninstalling Mahout ...\n");
                        Task uninstallTask = Tasks.getUninstallTask(agents);
                        executeTask(uninstallTask, new TaskCallback() {

                            @Override
                            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

                                if (Util.isFinalResponse(response)) {
                                    if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE) {
                                        if (response.getExitCode() == 0) {
                                            if (stdOut.indexOf("Package ksks-mahout is not installed, so not removed") == -1) {
                                                addOutput(String.format("%s: %s\n", getHostname(response), "Uninstallation done"));
                                            } else {
                                                addOutput(String.format("%s: %s\n", getHostname(response), "Mahout is not installed, so not removed"));
                                            }
                                        } else {
                                            addOutput(String.format("%s: %s: %s\n", getHostname(response), "Uninstallation failed", stdErr));
                                        }
                                    } else if (response.getType() == ResponseType.EXECUTE_TIMEOUTED) {
                                        addOutput(String.format("%s: %s\n", getHostname(response), "Command timed out"));
                                    }
                                }

                                return null;
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
            checkBtn.setEnabled(false);
            installBtn.setEnabled(false);
            uninstallBtn.setEnabled(false);
            indicator.setVisible(true);
            taskCount++;
            taskRunner.executeTask(task, new TaskCallback() {

                @Override
                public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                    Task nextTask = callback.onResponse(task, response, stdOut, stdErr);
                    if (task.isCompleted() && nextTask == null) {
                        taskCount--;
                        if (taskCount == 0) {
                            indicator.setVisible(false);
                            checkBtn.setEnabled(true);
                            installBtn.setEnabled(true);
                            uninstallBtn.setEnabled(true);
                        }
                    }

                    return nextTask;
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
