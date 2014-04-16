package org.safehaus.kiskis.mgmt.impl.hive.handler;

import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hive.HiveImpl;
import org.safehaus.kiskis.mgmt.impl.hive.TaskFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class UninstallHandler extends BaseHandler {

    public UninstallHandler(HiveImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    public void run() {
        if(config == null) {
            po.addLogFailed(String.format("Cluster '%s' does not exist",
                    config.getClusterName()));
            return;
        }

        po.addLog("Removing Hive client(s)...");
        Task task = TaskFactory.uninstallClient(config.getClients());
        manager.getTaskRunner().executeTaskNWait(task);

        if(task.isCompleted()) {
            for(Agent agent : config.getClients()) {
                Result res = task.getResults().get(agent.getUuid());
                if(res.getExitCode() != null && res.getExitCode() == 0)
                    po.addLog("Hive removed from node " + agent.getHostname());
                else
                    po.addLogFailed(String.format("Failed to remove Hive on '%s': %s",
                            agent.getHostname(), res.getStdErr()));
            }

        } else {
            po.addLogFailed("Failed to remove client(s): " + task.getFirstError());
            return;
        }

        po.addLog("Removing Hive server...");
        task = TaskFactory.uninstallServer(config.getServer());
        manager.getTaskRunner().executeTaskNWait(task);
        if(task.isCompleted()) {
            Result res = task.getResults().get(config.getServer().getUuid());
            if(res.getExitCode() != null && res.getExitCode() == 0) {

                po.addLog("Hive server removed successfully");

                po.addLog("Updating DB...");
                if(manager.getDbManager().deleteInfo(Config.PRODUCT_KEY, config.getClusterName()))
                    po.addLogDone("Cluster info deleted from DB");
                else
                    po.addLogFailed("Failed to delete cluster info");

            } else {
                po.addLogFailed("Failed to remove Hive server: " + res.getStdErr());
            }
        } else {
            po.addLogFailed("Failed to remove server: " + task.getFirstError());
        }

    }

}
