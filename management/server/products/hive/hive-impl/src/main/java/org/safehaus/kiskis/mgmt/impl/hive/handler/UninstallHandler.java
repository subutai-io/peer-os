package org.safehaus.kiskis.mgmt.impl.hive.handler;

import java.util.List;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hive.HiveImpl;
import org.safehaus.kiskis.mgmt.impl.hive.TaskFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class UninstallHandler extends AbstractHandler {

    public UninstallHandler(HiveImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    public void run() {
        Config config = getClusterConfig();
        if(config == null) {
            po.addLogFailed(String.format("Cluster '%s' does not exist",
                    clusterName));
            return;
        }

        // check server node
        if(!isNodeConnected(config.getServer().getHostname())) {
            po.addLogFailed(String.format("Server node '%s' is not connected",
                    config.getServer().getHostname()));
            return;
        }
        // check client nodes
        if(checkClientNodes(config, false) == 0) {
            po.addLogFailed("Connected client(s) not found");
            return;
        }

        po.addLog("Removing Hive client(s)...");
        Task task = TaskFactory.uninstallClient(config.getClients());
        manager.getTaskRunner().executeTaskNWait(task);

        if(task.isCompleted()) {
            for(Agent agent : config.getClients()) {
                Result res = task.getResults().get(agent.getUuid());
                if(isZero(res.getExitCode()))
                    po.addLog("Hive removed from node " + agent.getHostname());
                else
                    po.addLogFailed(String.format("Failed to remove Hive on '%s': %s",
                            agent.getHostname(), res.getStdErr()));
            }

        } else {
            po.addLogFailed("Failed to remove client(s): " + task.getFirstError());
            return;
        }

        List<Task> removeTasks = TaskFactory.uninstallServer(config.getServer());
        for(Task t : removeTasks) {
            po.addLog(t.getDescription());
            manager.getTaskRunner().executeTaskNWait(t);
            po.addLog(t.getDescription() + " " + t.getTaskStatus());
            if(task.getTaskStatus() != TaskStatus.SUCCESS) {
                po.addLogFailed(task.getFirstError());
                return;
            }

        }

        po.addLog("Updating DB...");
        if(manager.getDbManager().deleteInfo(Config.PRODUCT_KEY, config.getClusterName()))
            po.addLogDone("Cluster info deleted from DB");
        else
            po.addLogFailed("Failed to delete cluster info");

    }

}
