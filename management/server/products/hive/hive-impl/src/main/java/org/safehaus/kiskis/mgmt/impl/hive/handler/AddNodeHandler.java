package org.safehaus.kiskis.mgmt.impl.hive.handler;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hive.HiveImpl;
import org.safehaus.kiskis.mgmt.impl.hive.Product;
import org.safehaus.kiskis.mgmt.impl.hive.TaskFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class AddNodeHandler extends BaseHandler {

    public AddNodeHandler(HiveImpl manager, String clusterName, ProductOperation po) {
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

        Task task = TaskFactory.checkPackages(agent);
        manager.getTaskRunner().executeTaskNWait(task);
        if(!task.isCompleted()) {
            po.addLogFailed("Failed to check installed packages");
            return;
        }
        Result res = task.getResults().get(agent.getUuid());
        boolean skipHive;
        if(skipHive = res.getStdOut().contains(Product.HIVE.getPackageName())) {
            po.addLog("Hive already installed on " + hostname);
        }

        config.getClients().add(agent);

        po.addLog("Update cluster info...");
        if(manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
            po.addLog("Cluster info updated");

            Set<Agent> set = new HashSet<Agent>(2);
            set.add(agent);
            task = TaskFactory.installClient(set, skipHive);
            manager.getTaskRunner().executeTaskNWait(task);
            if(task.getTaskStatus() == TaskStatus.SUCCESS) {
                po.addLogDone(String.format("Node '%s' successfully added",
                        hostname));
            } else {
                po.addLogFailed("Failed to add node: " + res.getStdErr());
            }
        } else {
            po.addLogFailed("Failed to update cluster info");
        }

    }

}
