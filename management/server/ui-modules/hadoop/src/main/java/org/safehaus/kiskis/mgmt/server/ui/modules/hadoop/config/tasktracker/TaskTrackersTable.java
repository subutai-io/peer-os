package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config.tasktracker;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common.Tasks;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.operation.TaskTrackerConfiguration;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private TaskTrackersWindow parent;

    public TaskTrackersTable(TaskTrackersWindow parent, String clusterName) {
        this.parent = parent;
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
        container.addContainerProperty(REMOVE, Button.class, "");

        List<Agent> list = cluster.getTaskTrackers();
        for (Agent agent : list) {
            addOrderToContainer(container, agent);

            Task statusTask = Tasks.getTaskTrackerCommand(agent, "status");

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
        final Item item = container.getItem(itemId);

        item.getItemProperty(HOSTNAME).setValue(agent.getHostname());
        item.getItemProperty(STATUS).setValue("");

        Button button = new Button(REMOVE);
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                String status = (String) item.getItemProperty(STATUS).getValue();
                if (status.contains("NOT")) {
                    TaskTrackerConfiguration.removeNode(parent, clusterName, agent, false);
                } else {
                    TaskTrackerConfiguration.removeNode(parent, clusterName, agent, true);
                }
            }
        });

        item.getItemProperty(REMOVE).setValue(button);
    }

    public void refreshDataSource() {
        this.setContainerDataSource(getContainer());
    }

    private void findRow(UUID uuid, String stdOut) {
        Agent agent = HadoopModule.getAgentManager().getAgentByUUID(uuid);

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

}
