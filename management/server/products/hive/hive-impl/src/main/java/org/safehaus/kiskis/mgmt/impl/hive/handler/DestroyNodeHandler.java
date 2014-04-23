package org.safehaus.kiskis.mgmt.impl.hive.handler;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hive.HiveImpl;
import org.safehaus.kiskis.mgmt.impl.hive.TaskFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class DestroyNodeHandler extends AbstractHandler {

    public DestroyNodeHandler(HiveImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    public void run() {
        Config config = getClusterConfig();
        if(config == null) {
            po.addLogFailed(String.format("Cluster '%s' does not exist", clusterName));
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

        Result res = task.getResults().get(agent.getUuid());
        po.addLog(res.getStdOut());
        po.addLog(res.getStdErr());

        boolean ok = task.isCompleted() && isZero(res.getExitCode());
        if(ok) {
            config.getClients().remove(agent);
            po.addLog("Done");

            po.addLog("Update cluster info...");
            if(manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config))
                po.addLogDone("Cluster info successfully updated");
            else
                po.addLogFailed("Failed to update cluster info");

        }
    }

}
