package org.safehaus.kiskis.mgmt.server.ui.modules.solr;

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

public class Solr implements Module {

    public static final String MODULE_NAME = "Solr";
    private AsyncTaskRunner taskRunner;

    public void setTaskRunner(AsyncTaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public static class ModuleComponent extends CustomComponent {

        private final TextArea commandOutputTxtArea;
        private final AgentManager agentManager;
        private final Label indicator;
        private final AsyncTaskRunner taskRunner;
        private final Button startBtn;
        private final Button stopBtn;
        private final Button checkBtn;
        private final Button installBtn;
        private final Button uninstallBtn;
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

                                    StringBuilder out = outs.get(response.getUuid());

                                    if (!Util.isStringEmpty(response.getStdOut())) {
                                        out.append(response.getStdOut());
                                    }

                                    if (Util.isFinalResponse(response)) {
                                        if (out.indexOf("ksks-solr") > -1) {
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

                                                    StringBuilder err = errs.get(response.getUuid());

                                                    if (!Util.isStringEmpty(response.getStdErr())) {
                                                        err.append(response.getStdErr());
                                                    }

                                                    if (Util.isFinalResponse(response)) {
                                                        if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE) {
                                                            if (response.getExitCode() == 0) {
                                                                addOutput(String.format("%s: %s\n", getHostname(response), "Installation done"));
                                                            } else {
                                                                addOutput(String.format("%s: %s: %s\n", getHostname(response), "Installation failed", err.toString()));
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
                        commandOutputTxtArea.setValue("\nPlease, select lxc node(s)");
                    } else {
                        commandOutputTxtArea.setValue("\nChecking if Solr is installed ...\n");
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

                                    StringBuilder out = outs.get(response.getUuid());

                                    if (!Util.isStringEmpty(response.getStdOut())) {
                                        out.append(response.getStdOut());
                                    }

                                    if (Util.isFinalResponse(response)) {
                                        if (out.indexOf("ksks-solr") > -1) {
                                            addOutput(String.format("%s: %s\n", getHostname(response), "Solr is installed"));
                                        } else {
                                            addOutput(String.format("%s: %s\n", getHostname(response), "Solr is not installed"));
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
                        commandOutputTxtArea.setValue("\nPlease, select lxc node(s)");
                    } else {
                        commandOutputTxtArea.setValue("\nUninstalling Solr ...\n");
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
                                                if (out.indexOf("Package ksks-solr is not installed, so not removed") == -1) {
                                                    addOutput(String.format("%s: %s\n", getHostname(response), "Uninstallation done"));
                                                } else {
                                                    addOutput(String.format("%s: %s\n", getHostname(response), "Solr is not installed, so not removed"));
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
                            public void onResponse(Task task, Response response) {

                                if (task.isCompleted()) {
                                    //run status check task
                                    final Map<UUID, StringBuilder> outs = new HashMap<UUID, StringBuilder>();
                                    final Map<UUID, StringBuilder> errs = new HashMap<UUID, StringBuilder>();
                                    for (Agent agent : agents) {
                                        outs.put(agent.getUuid(), new StringBuilder());
                                        errs.put(agent.getUuid(), new StringBuilder());
                                    }

                                    Task statusTask = Tasks.getStatusTask(agents);

                                    executeTask(statusTask, new TaskCallback() {

                                        @Override
                                        public void onResponse(Task task, Response response) {
                                            if (response != null
                                                    && response.getUuid() != null
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
                                                    if (err.length() > 0) {
                                                        addOutput(String.format("%s: %s\n", getHostname(response), err.toString()));
                                                    } else if (out.length() > 0) {
                                                        addOutput(String.format("%s: %s\n", getHostname(response), out.toString()));
                                                    }
                                                }

                                            }
                                        }
                                    });

                                }
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
                        final Map<UUID, StringBuilder> errs = new HashMap<UUID, StringBuilder>();
                        for (Agent agent : agents) {
                            errs.put(agent.getUuid(), new StringBuilder());
                        }

                        executeTask(stopTask, new TaskCallback() {

                            @Override
                            public void onResponse(Task task, Response response) {
                                if (response != null && response.getUuid() != null
                                        && errs.get(response.getUuid()) != null) {

                                    StringBuilder err = errs.get(response.getUuid());

                                    if (!Util.isStringEmpty(response.getStdErr())) {
                                        err.append(response.getStdErr());
                                    }

                                    if (Util.isFinalResponse(response)) {
                                        if (err.length() > 0) {
                                            addOutput(String.format("%s: %s\n", getHostname(response), err.toString()));
                                        } else {
                                            addOutput(String.format("%s: %s\n", getHostname(response), "Stop Solr done"));
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
            startBtn.setEnabled(false);
            stopBtn.setEnabled(false);
            checkBtn.setEnabled(false);
            installBtn.setEnabled(false);
            uninstallBtn.setEnabled(false);
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
                            startBtn.setEnabled(true);
                            stopBtn.setEnabled(true);
                            checkBtn.setEnabled(true);
                            installBtn.setEnabled(true);
                            uninstallBtn.setEnabled(true);
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
        return Solr.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent(taskRunner);
    }

}
