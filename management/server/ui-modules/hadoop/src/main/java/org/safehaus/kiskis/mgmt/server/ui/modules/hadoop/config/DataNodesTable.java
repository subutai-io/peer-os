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
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;

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
    public static final String REMOVE_NODE = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"hadoop-master-slave.sh\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"slaves\",\"clear\",\":slave-hostname\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";


    public static final String HOSTNAME = "hostname",
            STATUS = "status",
            REMOVE = "remove";
    private String clusterName;

    private HadoopClusterInfo cluster;
    private Task removeTask;

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
        IndexedContainer container = new IndexedContainer();

        // Create the container properties
        container.addContainerProperty(HOSTNAME, String.class, "");
        container.addContainerProperty(STATUS, String.class, "");
        container.addContainerProperty(REMOVE, Button.class, "");

        // Create some orders
        List<UUID> list = cluster.getDataNodes();
        for (UUID item : list) {
            Agent agent = getAgentManager().getAgent(item);
            addOrderToContainer(container, agent);
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
                map.put(":taskUuid", removeTask.getUuid().toString());
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", master.getUuid().toString());
                map.put(":requestSequenceNumber", removeTask.getIncrementedReqSeqNumber().toString());
                map.put(":slave-hostname", agent.getUuid().toString());

                RequestUtil.createRequest(getCommandManager(), REMOVE_NODE, map);
            }
        });
        item.getItemProperty(REMOVE).setValue(buttonRemove);
    }

    public void refreshDataSource() {
        this.setContainerDataSource(getContainer());
    }

    public void onCommand(Response response) {
        Task task = getCommandManager().getTask(response.getTaskUuid());
        List<ParseResult> list = getCommandManager().parseTask(task, true);
        task = getCommandManager().getTask(response.getTaskUuid());

        if (removeTask != null) {
            if (task.equals(removeTask)) {
                getCommandManager().saveHadoopClusterData(cluster);
                refreshDataSource();
            }
        }
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
