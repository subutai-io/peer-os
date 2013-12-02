package org.safehaus.kiskis.mgmt.server.ui.modules.lxc.forms;

import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.LxcModule;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 12/1/13
 * Time: 5:56 PM
 */
@SuppressWarnings("serial")
public class LxcManageForm extends VerticalLayout {

    public static final String LIST_LXC = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": \":source\",\n" +
            "\t    \"uuid\": \":uuid\",\n" +
            "\t    \"taskUuid\": \":task\",\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"/usr/bin/lxc-ls\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    private Agent physicalAgent;
    private Button buttonRefresh;
    private Task listTask;
    private LxcTable table;

    public LxcManageForm() {
        setSpacing(true);

        Panel panel = new Panel("Manage LXC containers");
        panel.addComponent(getRefreshButton());
        panel.addComponent(getLxcTable());

        addComponent(panel);
    }

    private LxcTable getLxcTable() {
        table = new LxcTable();

        return table;
    }

    private Button getRefreshButton() {
        buttonRefresh = new Button("Refresh LXC containers");
        buttonRefresh.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Set<Agent> agents = AppData.getSelectedAgentList();
                if (agents != null && agents.size() > 0) {
                    Set<Agent> physicalAgents = new HashSet<Agent>();
                    for (Agent agent : agents) {
                        if (!agent.isIsLXC()) {
                            physicalAgent = agent;
                            physicalAgents.add(agent);
                        }
                    }

                    if (physicalAgents.size() != 1) {
                        getWindow().showNotification("Select only one physical agent");
                    } else {
                        createTask();
                    }
                }
            }
        });

        return buttonRefresh;
    }

    public void outputResponse(Response response) {
        if (listTask != null && response.getTaskUuid().compareTo(listTask.getUuid()) == 0) {
            parseResponse();
        } else {
            table.outputResponse(response);
        }
    }

    private void createTask() {
        listTask = new Task();
        listTask.setTaskStatus(TaskStatus.NEW);
        listTask.setDescription("List lxc container");
        if (getCommandManager() != null) {
            getCommandManager().saveTask(listTask);
        }

        createRequest();
    }

    private void createRequest() {
        String jsonTemplate = LIST_LXC;
        jsonTemplate = jsonTemplate.replaceAll(":task", listTask.getUuid().toString());
        jsonTemplate = jsonTemplate.replaceAll(":source", LxcModule.MODULE_NAME);

        String json = jsonTemplate.replaceAll(":uuid", physicalAgent.getUuid().toString());
        json = json.replaceAll(":requestSequenceNumber", listTask.getIncrementedReqSeqNumber().toString());

        Request request = CommandJson.getRequest(json);
        if (getCommandManager() != null) {
            getCommandManager().executeCommand(new Command(request));
        }

        buttonRefresh.setEnabled(false);
    }

    private void parseResponse() {
        if (getCommandManager() != null) {
            boolean isSuccess = true;
            List<Request> requests = getCommandManager().getCommands(listTask.getUuid());

            for (Request request : requests) {
                Response response = getCommandManager().getResponse(
                        listTask.getUuid(),
                        request.getRequestSequenceNumber());
                if (response == null) {
                    return;
                } else {
                    if (response.getType().equals(ResponseType.EXECUTE_TIMEOUTED)) {
                        isSuccess = false;

                        getWindow().showNotification(
                                "Error",
                                response.getStdErr(),
                                Window.Notification.TYPE_ERROR_MESSAGE);

                        buttonRefresh.setEnabled(true);
                    } else if (response.getType().equals(ResponseType.EXECUTE_RESPONSE_DONE)) {
                        if (response.getExitCode() == 0) {
                            getLxcTable(response.getStdOut());
                            buttonRefresh.setEnabled(true);
                        } else {
                            getWindow().showNotification(
                                    "Error",
                                    response.getStdErr(),
                                    Window.Notification.TYPE_ERROR_MESSAGE);

                            buttonRefresh.setEnabled(true);
                        }
                    }
                }
            }

            if (isSuccess) {
                listTask.setTaskStatus(TaskStatus.SUCCESS);
            } else {
                listTask.setTaskStatus(TaskStatus.FAIL);
            }
            getCommandManager().saveTask(listTask);
        }
    }

    private void getLxcTable(String output) {
        String[] lxcs = output.split("\\n");
        table.refreshDataSource(physicalAgent, lxcs);
    }

    public CommandManagerInterface getCommandManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(LxcModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
            if (serviceReference != null) {
                return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
