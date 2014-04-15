package org.safehaus.kiskis.mgmt.impl.hadoop.operation.Configuration;

import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
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
                String.format("Getting status of clusters %s DataNode", config.getClusterName()));

        parent.getExecutor().execute(new Runnable() {

            public void run() {
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
                    return;
                }

                final Agent node = parent.getAgentManager().getAgentByHostname(agent.getHostname());
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", agent.getHostname()));
                    return;
                }

                Task task = Tasks.getNameNodeCommandTask(agent, "status");
                parent.getTaskRunner().executeTask(task);

                NodeState nodeState = NodeState.UNKNOWN;
                if (task.isCompleted()) {
                    Result result = task.getResults().entrySet().iterator().next().getValue();
                    if (result.getStdOut().contains("DataNode")) {
                        String[] array = result.getStdOut().split("\n");

                        for (String status : array) {
                            if (status.contains("DataNode")) {
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
                    po.addLogFailed(String.format("Failed to check status of %s, %s",
                            config.getClusterName(),
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
}
