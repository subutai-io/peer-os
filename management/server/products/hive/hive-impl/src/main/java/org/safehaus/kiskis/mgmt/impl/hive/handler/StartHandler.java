package org.safehaus.kiskis.mgmt.impl.hive.handler;

import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hive.HiveImpl;
import org.safehaus.kiskis.mgmt.impl.hive.Product;
import org.safehaus.kiskis.mgmt.impl.hive.TaskFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class StartHandler extends AbstractHandler {

    public StartHandler(HiveImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    public void run() {
        Config config = getClusterConfig();
        if(config == null) {
            po.addLogFailed(String.format("Cluster '%s' does not exist",
                    clusterName));
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            po.addLogFailed(String.format("Node '%s' is not connected", hostname));
            return;
        }

        boolean ok = true;

        // if server node, start Derby first
        if(agent.equals(config.getServer())) {
            Task startDerbyTask = TaskFactory.start(agent, Product.DERBY);
            manager.getTaskRunner().executeTaskNWait(startDerbyTask);

            Result res = startDerbyTask.getResults().get(agent.getUuid());
            po.addLog(res.getStdOut());
            po.addLog(res.getStdErr());

            ok = startDerbyTask.isCompleted() && isZero(res.getExitCode());
        }
        if(ok) {
            Task startHiveTask = TaskFactory.start(agent, Product.HIVE);
            manager.getTaskRunner().executeTaskNWait(startHiveTask);

            Result res = startHiveTask.getResults().get(agent.getUuid());
            po.addLog(res.getStdOut());
            po.addLog(res.getStdErr());

            ok = startHiveTask.isCompleted() && isZero(res.getExitCode());
        }

        if(ok) po.addLogDone("Done");
        else po.addLogFailed(null);
    }

}
