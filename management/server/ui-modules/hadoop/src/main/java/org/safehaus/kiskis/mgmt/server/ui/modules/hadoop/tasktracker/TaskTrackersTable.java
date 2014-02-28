package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.tasktracker;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Table;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.install.Commands;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common.TaskUtil;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/30/13 Time: 6:56 PM
 */
public class TaskTrackersTable extends Table {

    public static final String HOSTNAME = "hostname",
            STATUS = "status",
            REMOVE = "remove";
    private final String clusterName;
    private IndexedContainer container;
    private HadoopClusterInfo cluster;

    public TaskTrackersTable(String clusterName) {
        this.clusterName = clusterName;

        this.setCaption(" Task Trackers");
        this.setContainerDataSource(getContainer());

        this.setWidth("100%");
        this.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        this.setPageLength(10);
        this.setSelectable(true);
        this.setImmediate(true);
    }

    private IndexedContainer getContainer() {
        this.cluster = HadoopDAO.getHadoopClusterInfo(clusterName);
        container = new IndexedContainer();

        // Create the container properties
        container.addContainerProperty(HOSTNAME, String.class, "");
        container.addContainerProperty(STATUS, String.class, "");
//        container.addContainerProperty(REMOVE, Button.class, "");

//        statusTask = RequestUtil.createTask("Status data node from Hadoop Cluster");
//        // Create some orders
//        List<Agent> list = cluster.getTaskTrackers();
//        for (Agent agent : list) {
//            addOrderToContainer(container, agent);
//
//            HashMap<String, String> map = new HashMap<String, String>();
//            map.put(":source", HadoopModule.MODULE_NAME);
//            map.put(":uuid", agent.getUuid().toString());
//            RequestUtil.createRequest(getCommandManager(), Commands.STATUS_DATA_NODE, statusTask, map);
//        }
//        
        List<Agent> list = cluster.getTaskTrackers();
        for (Agent agent : list) {
            addOrderToContainer(container, agent);

            HashMap<String, String> map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", agent.getUuid().toString());
            Task statusTask = TaskUtil.getTask(Commands.STATUS_DATA_NODE, map);

            HadoopModule.getTaskRunner().executeTask(statusTask, new TaskCallback() {

                Map<UUID, String> statuses = new HashMap<UUID, String>();

                @Override
                public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

                    if (Util.isFinalResponse(response)) {
                        statuses.put(response.getUuid(), stdOut);
                    }
                    if (task.isCompleted() && task.getTaskStatus() == TaskStatus.SUCCESS) {
                        for (Map.Entry<UUID, String> status : statuses.entrySet()) {
                            findRow(status.getKey(), status.getValue());
                        }
                    }

                    return null;
                }
            });
        }

        return container;
    }

    private void addOrderToContainer(Container container, final Agent agent) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);

        item.getItemProperty(HOSTNAME).setValue(agent.getHostname());
        item.getItemProperty(STATUS).setValue("");

        /*Button buttonRemove = new Button("Remove");
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
         RequestUtil.createRequest(getCommandManager(), Commands.REMOVE_DATA_NODE, removeTask, map);

         if (!agent.getListIP().isEmpty()) {
         map = new HashMap<String, String>();
         map.put(":source", HadoopModule.MODULE_NAME);
         map.put(":uuid", master.getUuid().toString());
         map.put(":IP", agent.getHostname());
         RequestUtil.createRequest(getCommandManager(), Commands.EXCLUDE_DATA_NODE, removeTask, map);
         }

         map = new HashMap<String, String>();
         map.put(":source", HadoopModule.MODULE_NAME);
         map.put(":uuid", master.getUuid().toString());
         RequestUtil.createRequest(getCommandManager(), Commands.REFRESH_DATA_NODES, removeTask, map);

         map = new HashMap<String, String>();
         map.put(":source", HadoopModule.MODULE_NAME);
         map.put(":uuid", agent.getUuid().toString());
         RequestUtil.createRequest(getCommandManager(), Commands.STOP_DATA_NODE, removeTask, map);
         }
         });
         item.getItemProperty(REMOVE).setValue(buttonRemove);*/
    }

    public void refreshDataSource() {
        this.setContainerDataSource(getContainer());
    }

    private void findRow(UUID uuid, String stdOut) {
        Agent agent = getAgentManager().getAgentByUUID(uuid);

        for (Object itemId : container.getItemIds()) {
            Item item = container.getItem(itemId);

            String name = (String) item.getItemProperty(HOSTNAME).getValue();

            if (name.equals(agent.getHostname())) {
                item.getItemProperty(STATUS).setValue(parseDataNodeStatus(stdOut));
            }
        }
    }

    private String parseDataNodeStatus(String response) {
        String[] array = response.split("\n");

        for (String status : array) {
            if (status.contains("TaskTracker")) {
                return status.replaceAll("TaskTracker is ", "");
            }
        }

        return "";
    }

    public AgentManager getAgentManager() {
        return ServiceLocator.getService(AgentManager.class);
    }
}
