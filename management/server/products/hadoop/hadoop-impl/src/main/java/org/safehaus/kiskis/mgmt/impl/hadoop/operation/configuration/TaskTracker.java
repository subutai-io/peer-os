package org.safehaus.kiskis.mgmt.impl.hadoop.operation.configuration;

import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hadoop.HadoopImpl;
import org.safehaus.kiskis.mgmt.impl.hadoop.Tasks;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;

import java.util.UUID;

/**
 * Created by daralbaev on 15.04.14.
 */
public class TaskTracker {
    private HadoopImpl parent;
    private Config config;

    public TaskTracker(HadoopImpl parent, Config config) {
        this.parent = parent;
        this.config = config;
    }

    public UUID status(final Agent agent) {

        final ProductOperation po
                = parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Getting status of clusters %s TaskTracker", agent.getHostname()));

        parent.getExecutor().execute(new Runnable() {

            public void run() {

                final Agent node = parent.getAgentManager().getAgentByHostname(agent.getHostname());
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", agent.getHostname()));
                    return;
                }

                Task task = Tasks.getJobTrackerCommand(agent, "status");
                parent.getTaskRunner().executeTaskNWait(task);

                NodeState nodeState = NodeState.UNKNOWN;
                if (task.isCompleted()) {
                    Result result = task.getResults().entrySet().iterator().next().getValue();
                    if (result.getStdOut() != null && result.getStdOut().contains("TaskTracker")) {
                        String[] array = result.getStdOut().split("\n");

                        for (String status : array) {
                            if (status.contains("TaskTracker")) {
                                String temp = status.
                                        replaceAll("JobTracker is ", "");
                                if (temp.toLowerCase().contains("not")) {
                                    nodeState = NodeState.STOPPED;
                                } else {
                                    nodeState = NodeState.RUNNING;
                                }
                            }
                        }
                    }
                }

                if (NodeState.UNKNOWN.equals(nodeState)) {
                    po.addLogFailed(String.format("Failed to check status of %s",
                            agent.getHostname()
                    ));
                } else {
                    po.addLogDone(String.format("DataNode of %s is %s",
                            agent.getHostname(),
                            nodeState
                    ));
                }
            }
        });

        return po.getId();

    }

    public UUID block(final Agent agent) {

        final ProductOperation po
                = parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Blocking TaskTracker of %s cluster", agent.getHostname()));

        parent.getExecutor().execute(new Runnable() {

            public void run() {

                final Agent node = parent.getAgentManager().getAgentByHostname(agent.getHostname());
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", agent.getHostname()));
                    return;
                }

                Task task = Tasks.getClearTaskTrackerTask(config, agent);
                parent.getTaskRunner().executeTaskNWait(task);

                task = Tasks.getIncludeTaskTrackerCommand(config, agent);
                parent.getTaskRunner().executeTaskNWait(task);

                task = Tasks.getRefreshTaskTrackerTask(config);
                parent.getTaskRunner().executeTaskNWait(task);

                config.getBlockedAgents().add(agent);
                if (parent.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog("Cluster info saved to DB");
                } else {
                    po.addLogFailed("Could not save cluster info to DB! Please see logs\n" +
                            "Blocking node aborted");
                }

                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    po.addLogDone(String.format("Task's operation %s finished", task.getDescription()));
                } else if (task.getTaskStatus() == TaskStatus.FAIL) {
                    po.addLogFailed(String.format("Task's operation %s failed", task.getDescription()));
                } else if (task.getTaskStatus() == TaskStatus.TIMEDOUT) {
                    po.addLogFailed(String.format("Task's operation %s timeout", task.getDescription()));
                }
            }
        });

        return po.getId();

    }

    public UUID unblock(final Agent agent) {

        final ProductOperation po
                = parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Unblocking TaskTracker of %s cluster", agent.getHostname()));

        parent.getExecutor().execute(new Runnable() {

            public void run() {

                final Agent node = parent.getAgentManager().getAgentByHostname(agent.getHostname());
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", agent.getHostname()));
                    return;
                }

                Task task = Tasks.getSetTaskTrackerTask(config, agent);
                parent.getTaskRunner().executeTaskNWait(task);

                task = Tasks.getExcludeTaskTrackerCommand(config, agent);
                parent.getTaskRunner().executeTaskNWait(task);

                task = Tasks.getStartTaskTrackerTask(agent);
                parent.getTaskRunner().executeTaskNWait(task);

                config.getBlockedAgents().remove(agent);
                if (parent.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog("Cluster info saved to DB");
                } else {
                    po.addLogFailed("Could not save cluster info to DB! Please see logs\n" +
                            "Blocking node aborted");
                }

                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    po.addLogDone(String.format("Task's operation %s finished", task.getDescription()));
                } else if (task.getTaskStatus() == TaskStatus.FAIL) {
                    po.addLogFailed(String.format("Task's operation %s failed", task.getDescription()));
                } else if (task.getTaskStatus() == TaskStatus.TIMEDOUT) {
                    po.addLogFailed(String.format("Task's operation %s timeout", task.getDescription()));
                }
            }
        });

        return po.getId();

    }
}
