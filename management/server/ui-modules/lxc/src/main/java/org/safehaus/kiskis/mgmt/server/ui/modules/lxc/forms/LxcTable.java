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
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 11/30/13
 * Time: 6:56 PM
 */
public class LxcTable extends Table {
    public static final int BUTTON_START = 0, BUTTON_STOP = 1, BUTTON_DESTROY = 2;
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

    private IndexedContainer container;
    private Task infoTask;
    private Agent agent;
    private HashMap<String, Button[]> buttons;

    public LxcTable() {
        buttons = new HashMap<String, Button[]>();
        this.setCaption("LXC containers");
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
            createTask();
            // Create some orders
            for (String lxc : lxcs) {
                if (!Strings.isNullOrEmpty(lxc.trim()) && !lxc.trim().equals("base-container")) {
                    addOrderToContainer(container, lxc.trim());
                    createRequest(lxc);
                }
            }
        }

        return container;
    }

    private void addOrderToContainer(Container container, String lxc) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);

        item.getItemProperty("name").setValue(lxc);

        Button[] buttonsArr = new Button[3];
        Button buttonStart = new Button("Start");
        buttonStart.setEnabled(false);
        item.getItemProperty("Start").setValue(buttonStart);
        buttonsArr[BUTTON_START] = buttonStart;

        Button buttonStop = new Button("Stop");
        buttonStop.setEnabled(false);
        item.getItemProperty("Stop").setValue(buttonStop);
        buttonsArr[BUTTON_STOP] = buttonStop;

        Button buttonDestroy = new Button("Destroy");
        buttonDestroy.setEnabled(false);
        item.getItemProperty("Destroy").setValue(buttonDestroy);
        buttonsArr[BUTTON_DESTROY] = buttonDestroy;

        buttons.put("lxc", buttonsArr);
    }

    public void refreshDataSource(Agent agent, String[] lxcs) {
        this.agent = agent;
        this.setContainerDataSource(getContainer(lxcs));
        System.out.println(container.getItemIds());
    }

    private void createTask() {
        infoTask = new Task();
        infoTask.setTaskStatus(TaskStatus.NEW);
        infoTask.setDescription("Info lxc container");
        if (getCommandManager() != null) {
            getCommandManager().saveTask(infoTask);
        }
    }

    private void createRequest(String lxc) {
        String json = INFO_LXC;
        json = json.replaceAll(":task", infoTask.getUuid().toString());
        json = json.replaceAll(":source", LxcModule.MODULE_NAME);

        json = json.replaceAll(":uuid", agent.getUuid().toString());
        json = json.replaceAll(":requestSequenceNumber", infoTask.getIncrementedReqSeqNumber().toString());
        json = json.replaceAll(":lxc-host-name", lxc);

        Request request = CommandJson.getRequest(json);
        if (getCommandManager() != null) {
            getCommandManager().executeCommand(new Command(request));
        }
    }

    public void outputResponse(Response response) {
        if (infoTask != null && response.getTaskUuid().compareTo(infoTask.getUuid()) == 0) {
            parseResponse();
        }
    }

    private void parseResponse() {

        if (getCommandManager() != null) {
            boolean isSuccess = true;
            List<Request> requests = getCommandManager().getCommands(infoTask.getUuid());

            for (Request request : requests) {
                Response response = getCommandManager().getResponse(
                        infoTask.getUuid(),
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
                    } else if (response.getType().equals(ResponseType.EXECUTE_RESPONSE_DONE)) {
                        if (response.getExitCode() == 0) {
                            String lxc = request.getArgs().get(1);
                            findRow(lxc);
                            if (buttons.containsKey(lxc)) {
                                buttons.get(lxc)[BUTTON_START].setEnabled(true);
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

            if (isSuccess) {
                infoTask.setTaskStatus(TaskStatus.SUCCESS);
            } else {
                infoTask.setTaskStatus(TaskStatus.FAIL);
            }
            getCommandManager().saveTask(infoTask);
        }
    }

    private void findRow(String lcx) {
        for (Object itemId : container.getItemIds()) {
            Item item = container.getItem(itemId);
            System.out.println(item.getItemProperty("Start").getValue());
        }
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
