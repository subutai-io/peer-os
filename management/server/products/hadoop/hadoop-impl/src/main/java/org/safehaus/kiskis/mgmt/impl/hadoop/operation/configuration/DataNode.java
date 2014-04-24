package org.safehaus.kiskis.mgmt.impl.hadoop.operation.configuration;

import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hadoop.Commands;
import org.safehaus.kiskis.mgmt.impl.hadoop.HadoopImpl;
import org.safehaus.kiskis.mgmt.impl.hadoop.Tasks;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;

import java.util.UUID;

/**
 * Created by daralbaev on 15.04.14.
 */
public class DataNode {
    private HadoopImpl parent;
    private Config config;

    public DataNode(HadoopImpl parent, Config config) {
        this.parent = parent;
        this.config = config;
    }

    public UUID status(final Agent agent) {

        final ProductOperation po
                = parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Getting status of clusters %s DataNode", agent.getHostname()));

        parent.getExecutor().execute(new Runnable() {

            public void run() {

                final Agent node = parent.getAgentManager().getAgentByHostname(agent.getHostname());
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", agent.getHostname()));
                    return;
                }

                Command getNameNodeCommand = Commands.getNameNodeCommand(agent, "status");
                HadoopImpl.getCommandRunner().runCommand(getNameNodeCommand);

                NodeState nodeState = NodeState.UNKNOWN;
                if (getNameNodeCommand.hasCompleted()) {
                    AgentResult result = getNameNodeCommand.getResults().get(agent.getUuid());
                    if (result.getStdOut() != null && result.getStdOut().contains("DataNode")) {
                        String[] array = result.getStdOut().split("\n");

                        for (String status : array) {
                            if (status.contains("DataNode")) {
                                String temp = status.
                                        replaceAll("DataNode is ", "");
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

                /*Task task = Tasks.getNameNodeCommandTask(agent, "status");
                parent.getTaskRunner().executeTaskNWait(task);

                NodeState nodeState = NodeState.UNKNOWN;
                if (task.isCompleted()) {
                    Result result = task.getResults().entrySet().iterator().next().getValue();
                    if (result.getStdOut() != null && result.getStdOut().contains("DataNode")) {
                        String[] array = result.getStdOut().split("\n");

                        for (String status : array) {
                            if (status.contains("DataNode")) {
                                String temp = status.
                                        replaceAll("DataNode is ", "");
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
                }*/
            }
        });

        return po.getId();

    }

    public UUID block(final Agent agent) {

        final ProductOperation po
                = parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Blocking DataNode of %s cluster", agent.getHostname()));

        parent.getExecutor().execute(new Runnable() {

            public void run() {

                final Agent node = parent.getAgentManager().getAgentByHostname(agent.getHostname());
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", agent.getHostname()));
                    return;
                }

                Task task = Tasks.getClearDataNodeTask(config, agent);
                parent.getTaskRunner().executeTaskNWait(task);

                task = Tasks.getIncludeNameNodeCommand(config, agent);
                parent.getTaskRunner().executeTaskNWait(task);

                task = Tasks.getRefreshNameNodeTask(config);
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
}
