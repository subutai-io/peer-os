package org.safehaus.kiskis.mgmt.server.ui.modules.lxc.forms;

import com.google.common.base.Strings;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.LxcModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.util.ParseResult;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 11/30/13
 * Time: 6:56 PM
 */
public class LxcTable extends Table {
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

    public static final String INFO_LXC = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": \":source\",\n" +
            "\t    \"uuid\": \":uuid\",\n" +
            "\t    \"taskUuid\": \":task\",\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"/usr/bin/lxc-info\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"-n\",\":lxc-host-name\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String DESTROY_LXC = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": \":source\",\n" +
            "\t    \"uuid\": \":uuid\",\n" +
            "\t    \"taskUuid\": \":task\",\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"/usr/bin/lxc-destroy\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"-n\",\":lxc-host-name\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String START_LXC = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": \":source\",\n" +
            "\t    \"uuid\": \":uuid\",\n" +
            "\t    \"taskUuid\": \":task\",\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"/usr/bin/lxc-start\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"-n\",\":lxc-host-name\", \"-d\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String STOP_LXC = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": \":source\",\n" +
            "\t    \"uuid\": \":uuid\",\n" +
            "\t    \"taskUuid\": \":task\",\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"/usr/bin/lxc-stop\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"-n\",\":lxc-host-name\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    private Agent agent;
    private IndexedContainer container;

    private Task listTask;
    private Task infoTask;
    private Task destroyTask;
    private Task startTask;
    private Task stopTask;

    public static final int REQUEST = 0, RESPONSE = 1, OUTPUT_SIZE = 2;
    private List<ParseResult> output;

    public LxcTable() {
        this.setCaption(" LXC containers");
        this.setContainerDataSource(getContainer(new String[]{}));

        this.setWidth("100%");
        this.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        this.setPageLength(10);
        this.setSelectable(true);
        this.setImmediate(true);
    }

    private IndexedContainer getContainer(String[] lxcs) {
        container = new IndexedContainer();

        // Create the container properties
        container.addContainerProperty("name", String.class, "");
        container.addContainerProperty("Start", Button.class, "");
        container.addContainerProperty("Stop", Button.class, "");
        container.addContainerProperty("Destroy", Button.class, "");

        if (lxcs.length > 0) {
            infoTask = createTask("Info lxc container");
            // Create some orders
            for (String lxc : lxcs) {
                if (!Strings.isNullOrEmpty(lxc.trim()) && !lxc.trim().equals("base-container")) {
                    addOrderToContainer(container, lxc.trim());
                    createRequest(INFO_LXC, infoTask, lxc);
                }
            }
        }

        return container;
    }

    private void addOrderToContainer(Container container, final String lxc) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);

        item.getItemProperty("name").setValue(lxc);

        Button buttonStart = new Button("Start");
        buttonStart.setEnabled(false);
        buttonStart.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                startTask = createTask("Start lxc container");
                createRequest(START_LXC, startTask, lxc);
            }
        });
        item.getItemProperty("Start").setValue(buttonStart);

        Button buttonStop = new Button("Stop");
        buttonStop.setEnabled(false);
        buttonStop.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                stopTask = createTask("Stop lxc container");
                createRequest(STOP_LXC, stopTask, lxc);
            }
        });
        item.getItemProperty("Stop").setValue(buttonStop);

        Button buttonDestroy = new Button("Destroy");
        buttonDestroy.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                destroyTask = createTask("Destroy lxc container");
                createRequest(DESTROY_LXC, destroyTask, lxc);
            }
        });
        item.getItemProperty("Destroy").setValue(buttonDestroy);
    }

    private Task createTask(String description) {
        Task task = new Task();
        task.setTaskStatus(TaskStatus.NEW);
        task.setDescription(description);
        if (getCommandManager() != null) {
            getCommandManager().saveTask(task);
        }

        return task;
    }

    private void createRequest(final String command, Task task, String lxc) {
        String json = command;
        json = json.replaceAll(":task", task.getUuid().toString());
        json = json.replaceAll(":source", LxcModule.MODULE_NAME);

        json = json.replaceAll(":uuid", agent.getUuid().toString());
        json = json.replaceAll(":requestSequenceNumber", task.getIncrementedReqSeqNumber().toString());
        json = json.replaceAll(":lxc-host-name", lxc);

        Request request = CommandJson.getRequest(json);
        if (getCommandManager() != null) {
            getCommandManager().executeCommand(new Command(request));
        }
    }

    public void outputResponse(Response response) {
        if (listTask != null && response.getTaskUuid().compareTo(listTask.getUuid()) == 0) {
            output = parseResponse(listTask);
            if (output != null) {
                for (ParseResult pr : output) {
                    refreshDataSource(pr);
                }
            }

        } else if (infoTask != null && response.getTaskUuid().compareTo(infoTask.getUuid()) == 0) {
            output = parseResponse(infoTask);
            if (output != null) {
                for (ParseResult pr : output) {
                    findRow(pr);
                }
            }
        } else if (destroyTask != null && response.getTaskUuid().compareTo(destroyTask.getUuid()) == 0) {
            output = parseResponse(destroyTask);
            if (output != null) {
                for (ParseResult pr : output) {
                    findRow(pr);
                }
            }
            listTask = createTask("List lxc container");
            createRequest(LIST_LXC, listTask, null);
        } else if (startTask != null && response.getTaskUuid().compareTo(startTask.getUuid()) == 0) {
            output = parseResponse(startTask);
            if (output != null) {
                for (ParseResult pr : output) {
                    findRow(pr);
                }
            }
            listTask = createTask("List lxc container");
            createRequest(LIST_LXC, listTask, null);
        } else if (stopTask != null && response.getTaskUuid().compareTo(stopTask.getUuid()) == 0) {
            output = parseResponse(stopTask);
            if (output != null) {
                for (ParseResult pr : output) {
                    findRow(pr);
                }
            }
            listTask = createTask("List lxc container");
            createRequest(LIST_LXC, listTask, null);
        }
    }

    private void findRow(ParseResult parseResult) {
        String lxc = parseResult.getRequest().getArgs().get(1);

        for (Object itemId : container.getItemIds()) {
            Item item = container.getItem(itemId);

            String name = (String) item.getItemProperty("name").getValue();
            if (name.equals(lxc)) {
                Button buttonStart = (Button) item.getItemProperty("Start").getValue();
                Button buttonStop = (Button) item.getItemProperty("Stop").getValue();

                String out = parseResult.getResponse().getStdOut();
                out = out.trim();

                if (out.split("\\n").length > 0) {
                    out = out.split("\\n")[0].trim();
                    if (out.split(":").length == 2) {
                        out = out.split(":")[1].trim();
                        if (out.equals("STOPPED")) {
                            buttonStart.setEnabled(true);
                            buttonStop.setEnabled(false);
                        } else {
                            buttonStop.setEnabled(true);
                            buttonStart.setEnabled(false);
                        }
                    }
                }
            }
        }
    }

    private List<ParseResult> parseResponse(Task task) {
        List<ParseResult> result = new ArrayList<ParseResult>();
        if (getCommandManager() != null) {
            boolean isSuccess = true;
            List<Request> requests = getCommandManager().getCommands(task.getUuid());
            Integer responseCount = getCommandManager().getResponseCount(task.getUuid());

            if (requests.size() == responseCount) {
                for (Request request : requests) {
                    Response response = getCommandManager().getResponse(
                            task.getUuid(),
                            request.getRequestSequenceNumber());

                    if (response == null) {
                        return null;
                    } else {
                        if (response.getType().equals(ResponseType.EXECUTE_TIMEOUTED)) {
                            isSuccess = false;

                            getWindow().showNotification(
                                    "Error",
                                    response.getStdErr(),
                                    Window.Notification.TYPE_ERROR_MESSAGE);
                        } else {
                            if (response.getType().equals(ResponseType.EXECUTE_RESPONSE_DONE)) {
                                if (response.getExitCode() == 0) {
                                    ParseResult pr = new ParseResult(request, response);
                                    result.add(pr);

                                    if (!Strings.isNullOrEmpty(response.getStdErr().trim())) {
                                        getWindow().showNotification(
                                                "Warning",
                                                response.getStdErr(),
                                                Window.Notification.TYPE_TRAY_NOTIFICATION);
                                    }
                                } else {
                                    getWindow().showNotification(
                                            "Error",
                                            response.getStdErr(),
                                            Window.Notification.TYPE_ERROR_MESSAGE);
                                }
                            }
                        }
                    }
                }

                if (isSuccess) {
                    task.setTaskStatus(TaskStatus.SUCCESS);
                } else {
                    task.setTaskStatus(TaskStatus.FAIL);
                }
                getCommandManager().saveTask(task);
            }
        }

        return result;
    }


    private void refreshDataSource(ParseResult parseResult) {
        String[] lxcs = parseResult.getResponse().getStdOut().split("\\n");
        this.setCaption(agent.getHostname() + " LXC containers");
        this.setContainerDataSource(getContainer(lxcs));
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
        listTask = createTask("List lxc container");
        createRequest(LIST_LXC, listTask, null);
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
