package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.Action;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Table;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config.datanode.DataNodesWindow;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config.tasktracker.TaskTrackersWindow;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.operation.Deletion;

import java.util.List;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/30/13 Time: 6:56 PM
 */
public class ClusterTable extends Table {

    private ClusterForm parent;

    static final Action ACTION_NAME_NODE = new Action("Edit name node and data trackers");
    static final Action ACTION_JOB_TRACKER = new Action("Edit job tracker and task trackers");
    static final Action ACTION_DELETE_CLUSTER = new Action("Delete cluster");
    static final Action[] ACTIONS = new Action[]{ACTION_NAME_NODE,
            ACTION_JOB_TRACKER, ACTION_DELETE_CLUSTER};

    private DataNodesWindow dataNodesWindow;
    private TaskTrackersWindow taskTrackersWindow;

    public ClusterTable(ClusterForm form) {

        this.parent = form;
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
                    dataNodesWindow = new DataNodesWindow(
                            (String) item.getItemProperty(HadoopClusterInfo.CLUSTER_NAME_LABEL).getValue());
                    getApplication().getMainWindow().addWindow(dataNodesWindow);
                } else if (ACTION_JOB_TRACKER == action) {
                    taskTrackersWindow = new TaskTrackersWindow(
                            (String) item.getItemProperty(HadoopClusterInfo.CLUSTER_NAME_LABEL).getValue());
                    getApplication().getMainWindow().addWindow(taskTrackersWindow);
                } else if (ACTION_DELETE_CLUSTER == action) {
                    Deletion.startDeletion((String) item.getItemProperty(
                            HadoopClusterInfo.CLUSTER_NAME_LABEL).getValue(), parent);
                }
            }

        });
    }

    private IndexedContainer getContainer() {
        IndexedContainer container = new IndexedContainer();

        // Create the container properties
        container.addContainerProperty(HadoopClusterInfo.CLUSTER_NAME_LABEL, String.class, "");
        container.addContainerProperty(HadoopClusterInfo.NAME_NODE_LABEL, String.class, "");
        container.addContainerProperty(HadoopClusterInfo.SECONDARY_NAME_NODE_LABEL, String.class, "");
        container.addContainerProperty(HadoopClusterInfo.JOB_TRACKER_LABEL, String.class, "");
        container.addContainerProperty(HadoopClusterInfo.REPLICATION_FACTOR_LABEL, Integer.class, "");
        container.addContainerProperty(HadoopClusterInfo.DATA_NODES_LABEL, Integer.class, "");
        container.addContainerProperty(HadoopClusterInfo.TASK_TRACKERS_LABEL, Integer.class, "");

        // Create some orders
        List<HadoopClusterInfo> cdList = HadoopDAO.getHadoopClusterInfo();
        for (HadoopClusterInfo cluster : cdList) {
            addOrderToContainer(container, cluster);
        }

        return container;
    }

    private void addOrderToContainer(Container container, HadoopClusterInfo cluster) {
        if (container != null && cluster != null) {
            Object itemId = container.addItem();
            Item item = container.getItem(itemId);

            if (item != null) {
                item.getItemProperty(HadoopClusterInfo.CLUSTER_NAME_LABEL).setValue(cluster.getClusterName());
                item.getItemProperty(HadoopClusterInfo.NAME_NODE_LABEL)
                        .setValue(cluster.getNameNode().getHostname());
                item.getItemProperty(HadoopClusterInfo.SECONDARY_NAME_NODE_LABEL)
                        .setValue(cluster.getSecondaryNameNode().getHostname());
                item.getItemProperty(HadoopClusterInfo.JOB_TRACKER_LABEL)
                        .setValue(cluster.getJobTracker().getHostname());
                item.getItemProperty(HadoopClusterInfo.REPLICATION_FACTOR_LABEL)
                        .setValue(cluster.getReplicationFactor());
                item.getItemProperty(HadoopClusterInfo.DATA_NODES_LABEL)
                        .setValue(cluster.getDataNodes().size());
                item.getItemProperty(HadoopClusterInfo.TASK_TRACKERS_LABEL)
                        .setValue(cluster.getTaskTrackers().size());
            }
        }
    }

    public void refreshDataSource() {
        this.setContainerDataSource(getContainer());
    }
}
