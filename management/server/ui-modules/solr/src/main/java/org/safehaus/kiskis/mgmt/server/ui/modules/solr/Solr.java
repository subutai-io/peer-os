package org.safehaus.kiskis.mgmt.server.ui.modules.solr;

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

public class Solr implements Module {

    public static final String MODULE_NAME = "Solr";
    private TaskRunner taskRunner;

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public static class ModuleComponent extends CustomComponent {

        private final TextArea commandOutputTxtArea;
        private final AgentManager agentManager;
        private final Label indicator;
        private final TaskRunner taskRunner;
        private final Button startBtn;
        private final Button stopBtn;
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

            Label logo = MgmtApplication.createImage("solr.png", 200, 100);
            startBtn = new Button("Start");
            stopBtn = new Button("Stop");
            checkBtn = new Button("Check");
            installBtn = new Button("Install");
            uninstallBtn = new Button("Uninstall");

            indicator = MgmtApplication.createImage("indicator.gif", 50, 11);
            indicator.setVisible(false);

            grid.addComponent(logo, 0, 0, 7, 0);
            grid.addComponent(startBtn, 14, 0, 14, 0);
            grid.addComponent(stopBtn, 15, 0, 15, 0);
            grid.addComponent(checkBtn, 16, 0, 16, 0);
            grid.addComponent(installBtn, 17, 0, 17, 0);
            grid.addComponent(uninstallBtn, 18, 0, 18, 0);
            grid.addComponent(indicator, 12, 0, 13, 0);
            grid.addComponent(commandOutputTxtArea, 0, 1, 19, 9);

            grid.setComponentAlignment(startBtn, Alignment.MIDDLE_CENTER);
            grid.setComponentAlignment(stopBtn, Alignment.MIDDLE_CENTER);
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
                        commandOutputTxtArea.setValue("\nInstalling Solr ...\n");
                        Task checkTask = Tasks.getCheckTask(agents);

                        executeTask(checkTask, new TaskCallback() {
                            final Set<Agent> eligibleAgents = new HashSet<Agent>();

                            @Override
                            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

                                if (task.getData() == TaskType.CHECK) {
                                    if (Util.isFinalResponse(response)) {
                                        if (stdOut.indexOf("ksks-solr") > -1) {
                                            addOutput(String.format("%s: %s\n", getHostname(response), "Solr is already installed. Omitting node from installation set"));
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
                        commandOutputTxtArea.setValue("\nChecking if Solr is installed ...\n");
                        Task checkTask = Tasks.getCheckTask(agents);
                        executeTask(checkTask, new TaskCallback() {

                            @Override
                            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

                                if (Util.isFinalResponse(response)) {
                                    if (stdOut.indexOf("ksks-solr") > -1) {
                                        addOutput(String.format("%s: %s\n", getHostname(response), "Solr is installed"));
                                    } else {
                                        addOutput(String.format("%s: %s\n", getHostname(response), "Solr is not installed"));
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
                        commandOutputTxtArea.setValue("\nUninstalling Solr ...\n");
                        Task uninstallTask = Tasks.getUninstallTask(agents);
                        executeTask(uninstallTask, new TaskCallback() {

                            @Override
                            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

                                if (Util.isFinalResponse(response)) {
                                    if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE) {
                                        if (response.getExitCode() == 0) {
                                            if (stdOut.indexOf("Package ksks-solr is not installed, so not removed") == -1) {
                                                addOutput(String.format("%s: %s\n", getHostname(response), "Uninstallation done"));
                                            } else {
                                                addOutput(String.format("%s: %s\n", getHostname(response), "Solr is not installed, so not removed"));
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

            startBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    final Set<Agent> agents = Util.filterLxcAgents(MgmtApplication.getSelectedAgents());

                    if (agents.isEmpty()) {
                        commandOutputTxtArea.setValue("\nPlease, select lxc node(s)");
                    } else {
                        commandOutputTxtArea.setValue("\nStarting Solr ...\n");
                        Task startTask = Tasks.getStartTask(agents);

                        executeTask(startTask, new TaskCallback() {

                            @Override
                            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

                                if (task.getData() == TaskType.START && task.isCompleted()) {
                                    //run status check task
                                    return Tasks.getStatusTask(agents);

                                } else if (task.getData() == TaskType.STATUS) {
                                    if (Util.isFinalResponse(response)) {
                                        if (stdErr.length() > 0) {
                                            addOutput(String.format("%s: %s\n", getHostname(response), stdErr));
                                        } else if (stdOut.length() > 0) {
                                            addOutput(String.format("%s: %s\n", getHostname(response), stdOut));
                                        }
                                    }

                                }

                                return null;
                            }
                        });
                    }
                }
            });

            stopBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    final Set<Agent> agents = Util.filterLxcAgents(MgmtApplication.getSelectedAgents());

                    if (agents.isEmpty()) {
                        commandOutputTxtArea.setValue("\nPlease, select lxc node(s)");
                    } else {
                        commandOutputTxtArea.setValue("\nStopping Solr ...\n");

                        Task stopTask = Tasks.getStopTask(agents);
                        executeTask(stopTask, new TaskCallback() {

                            @Override
                            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

                                if (Util.isFinalResponse(response)) {
                                    if (stdErr.length() > 0) {
                                        addOutput(String.format("%s: %s\n", getHostname(response), stdErr));
                                    } else {
                                        addOutput(String.format("%s: %s\n", getHostname(response), "Stop Solr done"));
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
            startBtn.setEnabled(false);
            stopBtn.setEnabled(false);
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
                            startBtn.setEnabled(true);
                            stopBtn.setEnabled(true);
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
        return Solr.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent(taskRunner);
    }

}
