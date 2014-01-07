package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.util.HadoopCommands;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 11/30/13
 * Time: 6:56 PM
 */
public class DataNodesTable extends Table {

    public static final String HOSTNAME = "hostname",
            STATUS = "status",
            REMOVE = "remove";
    private String clusterName;
    private IndexedContainer container;

    private HadoopClusterInfo cluster;
    private Task removeTask, statusTask;

    public DataNodesTable(String clusterName) {
        this.clusterName = clusterName;

        this.setCaption(" Data Nodes");
        this.setContainerDataSource(getContainer());

        this.setWidth("100%");
        this.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        this.setPageLength(10);
        this.setSelectable(true);
        this.setImmediate(true);
    }

    private IndexedContainer getContainer() {
        this.cluster = getCommandManager().getHadoopClusterData(clusterName);
        container = new IndexedContainer();

        // Create the container properties
        container.addContainerProperty(HOSTNAME, String.class, "");
        container.addContainerProperty(STATUS, String.class, "");
        container.addContainerProperty(REMOVE, Button.class, "");

        statusTask = RequestUtil.createTask(getCommandManager(), "Status data node from Hadoop Cluster");
        // Create some orders
        List<UUID> list = cluster.getDataNodes();
        for (UUID item : list) {
            Agent agent = getAgentManager().getAgent(item);
            addOrderToContainer(container, agent);

            HashMap<String, String> map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", agent.getUuid().toString());
            RequestUtil.createRequest(getCommandManager(), HadoopCommands.STATUS_DATA_NODE, statusTask, map);
        }

        return container;
    }

    private void addOrderToContainer(Container container, final Agent agent) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);

        item.getItemProperty(HOSTNAME).setValue(agent.getHostname());
        item.getItemProperty(STATUS).setValue("");

        Button buttonRemove = new Button("Remove");
        buttonRemove.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Agent master = getAgentManager().getAgent(cluster.getNameNode());
                cluster = getCommandManager().getHadoopClusterData(clusterName);
                cluster.getDataNodes().remove(agent.getUuid());

                removeTask = RequestUtil.createTask(getCommandManager(), "Remove data node from Hadoop Cluster");

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", master.getUuid().toString());
                map.put(":slave-hostname", agent.getUuid().toString());
                RequestUtil.createRequest(getCommandManager(), HadoopCommands.REMOVE_DATA_NODE, removeTask, map);

                if (!agent.getListIP().isEmpty()) {
                    map = new HashMap<String, String>();
                    map.put(":source", HadoopModule.MODULE_NAME);
                    map.put(":uuid", master.getUuid().toString());
                    map.put(":IP", agent.getListIP().get(0));
                    RequestUtil.createRequest(getCommandManager(), HadoopCommands.EXCLUDE_DATA_NODE, removeTask, map);
                }

                map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", master.getUuid().toString());
                RequestUtil.createRequest(getCommandManager(), HadoopCommands.REFRESH_DATA_NODES, removeTask, map);

                map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());
                RequestUtil.createRequest(getCommandManager(), HadoopCommands.STOP_DATA_NODE, removeTask, map);
            }
        });
        item.getItemProperty(REMOVE).setValue(buttonRemove);
    }

    public void refreshDataSource() {
        this.setContainerDataSource(getContainer());
    }

    public void onCommand(Response response) {

        List<ParseResult> list = getCommandManager().parseTask(response.getTaskUuid(), true);
        Task task = getCommandManager().getTask(response.getTaskUuid());

        if (removeTask != null) {
            if (task.equals(removeTask)) {
                if (task.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                    getCommandManager().saveHadoopClusterData(cluster);
                }
                refreshDataSource();
            }
        }

        if (statusTask != null) {
            if (task.equals(statusTask)) {
                if (task.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                    for (ParseResult pr : list) {
                        findRow(pr);
                    }
                }
            }
        }
    }

    private void findRow(ParseResult parseResult) {
        Agent agent = getAgentManager().getAgent(parseResult.getRequest().getUuid());

        for (Object itemId : container.getItemIds()) {
            Item item = container.getItem(itemId);

            String name = (String) item.getItemProperty(HOSTNAME).getValue();

            if (name.equals(agent.getHostname())) {
                item.getItemProperty(STATUS).setValue(parseDataNodeStatus(parseResult.getResponse().getStdOut()));
            }
        }
    }

    private String parseDataNodeStatus(String response) {
        String[] array = response.split("\n");
        System.out.println(array.length);
        System.out.println(array[3]);
        if (array.length == 4) {
            return array[3].replaceAll("DataNode is ", "");
        }

        return "";
    }

    public CommandManagerInterface getCommandManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(HadoopModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
            if (serviceReference != null) {
                return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }

    public AgentManagerInterface getAgentManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(HadoopModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(AgentManagerInterface.class.getName());
            if (serviceReference != null) {
                return AgentManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
