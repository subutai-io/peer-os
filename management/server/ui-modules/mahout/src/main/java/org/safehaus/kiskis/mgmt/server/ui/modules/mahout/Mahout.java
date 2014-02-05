package org.safehaus.kiskis.mgmt.server.ui.modules.mahout;

import com.vaadin.ui.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AsyncTaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
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
            grid.addComponent(commandOutputTxtArea, 0, 0, 19, 8);

            Button checkBtn = new Button("Check");
            grid.addComponent(checkBtn, 16, 9, 16, 9);
            final Button installBtn = new Button("Install");
            grid.addComponent(installBtn, 17, 9, 17, 9);
            Button removeBtn = new Button("Remove");
            grid.addComponent(removeBtn, 18, 9, 18, 9);

            indicator = MgmtApplication.createImage("indicator.gif", 50, 11);
            indicator.setVisible(false);
            grid.addComponent(indicator, 19, 9, 19, 9);

            setCompositionRoot(grid);

            installBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Set<Agent> agents = Util.filterLxcAgents(MgmtApplication.getSelectedAgents());

                    if (agents.isEmpty()) {
                        show("Please, select lxc node(s)");
                    } else {
                        Task task = new Task();
                        for (Agent agent : agents) {
                            Request request = getRequestTemplate();
                            request.setUuid(agent.getUuid());
                            task.addRequest(request);
                        }
                        indicator.setVisible(true);
                        taskCount++;
                        taskRunner.executeTask(task, new TaskCallback() {

                            @Override
                            public void onResponse(Task task, Response response) {
                                if (response != null && response.getUuid() != null) {
                                    Agent agent = agentManager.getAgentByUUID(response.getUuid());
                                    String host = agent == null
                                            ? String.format("Offline[%s]", response.getUuid()) : agent.getHostname();

                                    StringBuilder out = new StringBuilder(host).append(":\n");
                                    if (!Util.isStringEmpty(response.getStdOut())) {
                                        out.append(response.getStdOut()).append("\n");
                                    }
                                    if (!Util.isStringEmpty(response.getStdErr())) {
                                        out.append(response.getStdErr()).append("\n");
                                    }
                                    if (Util.isFinalResponse(response)) {
                                        if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE) {
                                            out.append("Exit code: ").append(response.getExitCode()).append("\n\n");
                                        } else {
                                            out.append("Command timed out").append("\n\n");
                                        }
                                    }
                                    addOutput(out.toString());
                                }

                                if (task.isCompleted()) {
                                    taskCount--;
                                    if (taskCount == 0) {
                                        indicator.setVisible(false);
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
                        show("Please, select lxc node(s)");
                    } else {
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
                                            addOutput(String.format("%s: %s", getHostname(response), "Mahout is installed"));
                                        } else {
                                            addOutput(String.format("%s: %s", getHostname(response), "Mahout is not installed"));
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            });

            removeBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
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
                        String.format("%s%s",
                                commandOutputTxtArea.getValue(),
                                output));
                commandOutputTxtArea.setCursorPosition(commandOutputTxtArea.getValue().toString().length() - 1);
            }
        }

        private void show(String notification) {
            getWindow().showNotification(notification);
        }

        public static Request getRequestTemplate() {
            return CommandFactory.newRequest(
                    RequestType.EXECUTE_REQUEST, // type
                    null, //                        !! agent uuid
                    MODULE_NAME, //     source
                    null, //                        !! task uuid 
                    1, //                           !! request sequence number
                    "/", //                         cwd
                    "pwd", //                        program
                    OutputRedirection.RETURN, //    std output redirection 
                    OutputRedirection.RETURN, //    std error redirection
                    null, //                        stdout capture file path
                    null, //                        stderr capture file path
                    "root", //                      runas
                    null, //                        arg
                    null, //                        env vars
                    30); //  
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
