package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.component;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;

import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 12/1/13 Time: 1:38 AM
 */
public class NodesWindow extends Window {

    private final Table table;
    private IndexedContainer container;
    private final List<UUID> list;
    private final CommandManagerInterface commandManager;

    /**
     *
     * @param caption
     * @param list
     * @param commandManager
     */
    public NodesWindow(String caption, List<UUID> list, CommandManagerInterface commandManager) {
        this.list = list;
        setCaption(caption);
        setSizeUndefined();
        setWidth("600px");
        setHeight("400px");

        table = new Table("", getCassandraContainer());
        table.setSizeFull();

        table.setPageLength(10);
        table.setImmediate(true);

        addComponent(table);
        this.commandManager = commandManager;
    }

    private IndexedContainer getCassandraContainer() {
        container = new IndexedContainer();

        // Create the container properties
        container.addContainerProperty("hostname", String.class, "");
        container.addContainerProperty("uuid", UUID.class, "");
        container.addContainerProperty("Start", Button.class, "");
        container.addContainerProperty("Stop", Button.class, "");

        // Create some orders
        for (UUID uuid : list) {
            addOrderToContainer(container, getAgentManager().getAgent(uuid));
        }

        return container;
    }

    private void addOrderToContainer(Container container, final Agent agent) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);
        item.getItemProperty("hostname").setValue(agent.getHostname());
        item.getItemProperty("uuid").setValue(agent.getUuid());

        Button startButton = new Button("Start");
        startButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                Task task = new Task();
                task.setDescription("Starting node");
                task.setTaskStatus(TaskStatus.NEW);
                int reqSeqNumber = task.getIncrementedReqSeqNumber();

                Command command = (Command) CommandFactory.createRequest(
                        RequestType.EXECUTE_REQUEST,
                        agent.getUuid(),
                        "CassandraNodeStart",
                        task.getUuid(),
                        reqSeqNumber,
                        "/",
                        "service cassandra start",
                        OutputRedirection.RETURN,
                        OutputRedirection.RETURN,
                        null,
                        null,
                        "root",
                        null,
                        null);
                commandManager.executeCommand(command);
            }
        });
        Button stopButton = new Button("Start");
        stopButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                Task task = new Task();
                task.setDescription("Starting node");
                task.setTaskStatus(TaskStatus.NEW);
                int reqSeqNumber = task.getIncrementedReqSeqNumber();

                Command command = (Command) CommandFactory.createRequest(
                        RequestType.EXECUTE_REQUEST,
                        agent.getUuid(),
                        "CassandraNodeStart",
                        task.getUuid(),
                        reqSeqNumber,
                        "/",
                        "service cassandra stop",
                        OutputRedirection.RETURN,
                        OutputRedirection.RETURN,
                        null,
                        null,
                        "root",
                        null,
                        null);
                commandManager.executeCommand(command);
                getWindow().showNotification("Stop cassandra cluster");
            }
        });
//        Button destroyButton = new Button("Destroy");
//        destroyButton.addListener(new Button.ClickListener() {
//
//            @Override
//            public void buttonClick(Button.ClickEvent event) {
//                getWindow().showNotification("Destroy cassandra node");
//            }
//        });

        item.getItemProperty("Start").setValue(startButton);
        item.getItemProperty("Stop").setValue(stopButton);
//        item.getItemProperty("Destroy").setValue(destroyButton);
    }

    public static AgentManagerInterface getAgentManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(NodesWindow.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(AgentManagerInterface.class.getName());
            if (serviceReference != null) {
                return AgentManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
