package org.safehaus.kiskis.mgmt.impl.hive.handler;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hive.HiveImpl;
import org.safehaus.kiskis.mgmt.impl.hive.TaskFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class DestroyNodeHandler extends BaseHandler {

    public DestroyNodeHandler(HiveImpl manager, String clusterName, ProductOperation po) {
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

        if(config.getClients().size() == 1) {
            po.addLog("This is the last node in cluster. Destroy cluster instead");
            return;
        }

        Set<Agent> set = new HashSet<Agent>(2);
        set.add(agent);
        Task task = TaskFactory.uninstallClient(set);
        manager.getTaskRunner().executeTaskNWait(task);
        if(task.getTaskStatus() == TaskStatus.SUCCESS) {
            Result res = task.getResults().get(agent.getUuid());
            if(res.getExitCode() != null && res.getExitCode() == 0) {
                po.addLog("Hive client removed");

                config.getClients().remove(agent);

                po.addLog("Update cluster info...");
                if(manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config))
                    po.addLogDone("Cluster info successfully updated");
                else
                    po.addLogFailed("Failed to update cluster info");

            } else {
                po.addLogFailed("Failed to remove Hive client: " + res.getStdErr());
            }
        } else {
            po.addLogFailed("Failed to remove node: " + task.getFirstError());
        }
    }

}
