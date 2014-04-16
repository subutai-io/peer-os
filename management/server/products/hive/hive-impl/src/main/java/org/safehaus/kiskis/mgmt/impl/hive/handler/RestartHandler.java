package org.safehaus.kiskis.mgmt.impl.hive.handler;

import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hive.HiveImpl;
import org.safehaus.kiskis.mgmt.impl.hive.TaskFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class RestartHandler extends BaseHandler {

    public RestartHandler(HiveImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    public void run() {
        if(config == null) {
            po.addLogFailed(String.format("Cluster '%s' does not exist",
                    config.getClusterName()));
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            po.addLogFailed(String.format("Node '%s' is not connected", hostname));
            return;
        }

        Task task = TaskFactory.restart(agent);
        manager.getTaskRunner().executeTaskNWait(task);
        if(task.getTaskStatus() == TaskStatus.SUCCESS) {
            Result res = task.getResults().get(agent.getUuid());
            if(res.getExitCode() != null && res.getExitCode() == 0)
                po.addLogDone(String.format("Node '%s' restarted", hostname));
            else
                po.addLogFailed(String.format("Failed to restart node '%s': %s",
                        hostname, res.getStdErr()));
        } else {
            po.addLogFailed(String.format("Failed to restart node '%s': %s",
                    hostname, task.getFirstError()));
        }
    }

}
