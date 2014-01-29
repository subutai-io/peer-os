package org.safehaus.kiskis.mgmt.server.ui.modules.pig;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

import java.util.Set;
import java.util.logging.Logger;

public class ModuleComponent extends CustomComponent implements CommandListener {

    private static final Logger LOG = Logger.getLogger(ModuleComponent.class.getName());
    private final TextArea commandOutputTxtArea;
    private final AgentManager agentManager;
    private final TaskRunner taskRunner = new TaskRunner();

    public ModuleComponent() {

        LOG.info("Creating ModuleComponent");

        agentManager = ServiceLocator.getService(AgentManager.class);

        setHeight("100%");
        GridLayout grid = new GridLayout(10, 10);
        grid.setSizeFull();
        grid.setMargin(true);
        grid.setSpacing(true);

        setCompositionRoot(grid);

        commandOutputTxtArea = new TextArea("Log:");
        commandOutputTxtArea.setSizeFull();
        commandOutputTxtArea.setImmediate(true);
        commandOutputTxtArea.setWordwrap(false);
        grid.addComponent(commandOutputTxtArea, 1, 0, 9, 9);



        Button sendBtn = new Button("Refresh");
        grid.addComponent(sendBtn, 0, 0);

        sendBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                Set<Agent> agents = MgmtApplication.getSelectedAgents();
                Task task = new Task();

                for (Agent agent : agents) {
                    Command cmd = getTemplate();
                    cmd.getRequest().setUuid(agent.getUuid());
                    cmd.getRequest().setProgram("pwd");
                    cmd.getRequest().setTimeout(30);
                    cmd.getRequest().setWorkingDirectory("/");

                    task.addCommand(cmd);
                }

                taskRunner.runTask(task, new TaskCallback() {

                    @Override
                    public void onResponse(Task task, Response response) {
                        LOG.info("[onResponse] task: " + response);
                        LOG.info("[onResponse] response: " + response);

                        /*LOG.info("task: " + task);
                        LOG.info("response: " + response);
                        LOG.info("---");*/

                        /*if (response != null && response.getUuid() != null) {
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
                        }*/

                    }
                });

            }
        });

        Button clearButton = new Button("Install");
        grid.addComponent(clearButton, 0, 1);

        clearButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                LOG.info("RemainingTaskCount: " + taskRunner.getRemainingTaskCount());
                //commandOutputTxtArea.setValue("install");
            }
        });

        Button removeButton = new Button("Remove");
        grid.addComponent(removeButton, 0, 2);

        removeButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                alert("remove");
                LOG.info("remove");
                commandOutputTxtArea.setValue("remove");
            }
        });
    }

    public static Command getTemplate() {
        return CommandFactory.createRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                Pig.MODULE_NAME, //     source
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

    private void alert(String message) {
        getWindow().showNotification(message);
   }

    @Override
    public void onCommand(Response response) {
        LOG.info("response: " + response);
        taskRunner.feedResponse(response);
    }

    @Override
    public String getName() {
        return Pig.MODULE_NAME;
    }

}