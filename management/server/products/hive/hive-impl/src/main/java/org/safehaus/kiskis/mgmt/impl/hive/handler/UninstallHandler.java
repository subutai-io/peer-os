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

public class UninstallHandler extends BaseHandler {

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

        // STOP services
//        po.addLog("Stop Hive clients");
//        Task stopTask = TaskFactory.stop(config.getClients(), Product.HIVE);
//        manager.getTaskRunner().executeTaskNWait(stopTask);
//        if(stopTask.isCompleted()) {
//            for(Agent a : config.getClients()) {
//                Result res = stopTask.getResults().get(a.getUuid());
//                if(res.getExitCode() != null && res.getExitCode() == 0)
//                    po.addLog("Service stopped on " + a.getHostname() + ": " + res.getStdOut());
//                else
//                    po.addLog("Failed to stop on " + a.getHostname() + ": " + res.getStdErr());
//            }
//        } else {
//            po.addLog("Failed to stop services on client nodes: " + stopTask.getFirstError());
//        }
//
//        po.addLog("Stopping Hive server service...");
//        stopTask = TaskFactory.stop(config.getServer(), Product.HIVE);
//        manager.getTaskRunner().executeTaskNWait(stopTask);
//        po.addLog(stopTask.getTaskStatus().toString() + "\n");
//
//        po.addLog("Stopping Derby server service...");
//        stopTask = TaskFactory.stop(config.getServer(), Product.DERBY);
//        manager.getTaskRunner().executeTaskNWait(stopTask);
//        po.addLog(stopTask.getTaskStatus().toString() + "\n");

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
