package org.safehaus.kiskis.mgmt.impl.hadoop.operation.configuration;

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
 * Created by daralbaev on 14.04.14.
 */
public class SecondaryNameNode {
    private HadoopImpl parent;
    private Config config;

    public SecondaryNameNode(HadoopImpl parent, Config config) {
        this.parent = parent;
        this.config = config;
    }

    public UUID status() {

        final ProductOperation po
                = parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Getting status of clusters %s Secondary NameNode", config.getClusterName()));

        parent.getExecutor().execute(new Runnable() {

            public void run() {
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
                    return;
                }

                final Agent node = parent.getAgentManager().getAgentByHostname(config.getSecondaryNameNode().getHostname());
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", config.getSecondaryNameNode().getHostname()));
                    return;
                }

                Task task = Tasks.getNameNodeCommandTask(config.getSecondaryNameNode(), "status");
                parent.getTaskRunner().executeTaskNWait(task);

                NodeState nodeState = NodeState.UNKNOWN;
                if (task.isCompleted()) {
                    Result result = task.getResults().entrySet().iterator().next().getValue();
                    if (result.getStdOut() != null && result.getStdOut().contains("NameNode")) {
                        String[] array = result.getStdOut().split("\n");

                        for (String status : array) {
                            if (status.contains("SecondaryNameNode")) {
                                String temp = status.
                                        replaceAll("SecondaryNameNode is ", "");
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
                            config.getSecondaryNameNode().getHostname()
                    ));
                } else {
                    po.addLogDone(String.format("Secondary NameNode of %s is %s",
                            config.getSecondaryNameNode().getHostname(),
                            nodeState
                    ));
                }
            }
        });

        return po.getId();

    }
}
