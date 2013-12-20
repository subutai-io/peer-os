package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.Action;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Table;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.shared.protocol.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 11/30/13
 * Time: 6:56 PM
 */
public class HadoopClusterTable extends Table {
    private IndexedContainer container;

    static final Action ACTION_NAME_NODE = new Action("Edit name node and data trackers");
    static final Action ACTION_JOB_TRACKER = new Action("Edit job tracker and task trackers");
    static final Action[] ACTIONS = new Action[]{ACTION_NAME_NODE,
            ACTION_JOB_TRACKER};

    public HadoopClusterTable() {
        this.setCaption(" Hadoop Clusters");
        this.setContainerDataSource(getContainer());

        this.setWidth("100%");
        this.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        this.setPageLength(10);
        this.setSelectable(true);
        this.setImmediate(true);

        // Actions (a.k.a context menu)
        addActionHandler(new Action.Handler() {
            @Override
            public Action[] getActions(Object target, Object sender) {
                return ACTIONS;
            }

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                Item item = getItem(target);
                if (ACTION_NAME_NODE == action) {
                    getWindow().showNotification(
                            "Selected cluster NN",
                            (String) item.getItemProperty(HadoopClusterInfo.CLUSTER_NAME_LABEL).getValue());
                } else if (ACTION_JOB_TRACKER == action) {
                    getWindow().showNotification(
                            "Selected cluster JB",
                            (String) item.getItemProperty(HadoopClusterInfo.CLUSTER_NAME_LABEL).getValue());
                }
            }

        });
    }

    private IndexedContainer getContainer() {
        container = new IndexedContainer();

        // Create the container properties
        container.addContainerProperty(HadoopClusterInfo.CLUSTER_NAME_LABEL, String.class, "");
        container.addContainerProperty(HadoopClusterInfo.NAME_NODE_LABEL, String.class, "");
        container.addContainerProperty(HadoopClusterInfo.SECONDARY_NAME_NODE_LABEL, String.class, "");
        container.addContainerProperty(HadoopClusterInfo.JOB_TRACKER_LABEL, String.class, "");
        container.addContainerProperty(HadoopClusterInfo.REPLICATION_FACTOR_LABEL, Integer.class, "");
        container.addContainerProperty(HadoopClusterInfo.DATA_NODES_LABEL, Integer.class, "");
        container.addContainerProperty(HadoopClusterInfo.TASK_TRACKERS_LABEL, Integer.class, "");

        // Create some orders
        List<HadoopClusterInfo> cdList = getCommandManager().getHadoopClusterData();
        for (HadoopClusterInfo cluster : cdList) {
            addOrderToContainer(container, cluster);
        }

        return container;
    }

    private void addOrderToContainer(Container container, HadoopClusterInfo cluster) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);

        item.getItemProperty(HadoopClusterInfo.CLUSTER_NAME_LABEL).setValue(cluster.getClusterName());
        item.getItemProperty(HadoopClusterInfo.NAME_NODE_LABEL)
                .setValue(getAgentManager().getAgent(cluster.getNameNode()).getHostname());
        item.getItemProperty(HadoopClusterInfo.SECONDARY_NAME_NODE_LABEL)
                .setValue(getAgentManager().getAgent(cluster.getSecondaryNameNode()).getHostname());
        item.getItemProperty(HadoopClusterInfo.JOB_TRACKER_LABEL)
                .setValue(getAgentManager().getAgent(cluster.getJobTracker()).getHostname());
        item.getItemProperty(HadoopClusterInfo.REPLICATION_FACTOR_LABEL)
                .setValue(cluster.getReplicationFactor());
        item.getItemProperty(HadoopClusterInfo.DATA_NODES_LABEL)
                .setValue(cluster.getDataNodes().size());
        item.getItemProperty(HadoopClusterInfo.TASK_TRACKERS_LABEL)
                .setValue(cluster.getTaskTrackers().size());
    }

    public void refreshDataSource() {
        this.setContainerDataSource(getContainer());
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
