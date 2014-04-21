package org.safehaus.kiskis.mgmt.impl.hive.handler;

import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hive.HiveImpl;
import org.safehaus.kiskis.mgmt.impl.hive.Product;
import org.safehaus.kiskis.mgmt.impl.hive.TaskFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class StopHandler extends BaseHandler {

    public StopHandler(HiveImpl manager, String clusterName, ProductOperation po) {
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

        Task stopHiveTask = TaskFactory.stop(agent, Product.HIVE);
        manager.getTaskRunner().executeTaskNWait(stopHiveTask);

        Result res = stopHiveTask.getResults().get(agent.getUuid());
        po.addLog(res.getStdOut());
        po.addLog(res.getStdErr());

        boolean ok = stopHiveTask.isCompleted() && isZero(res.getExitCode());

        // if server node, stop Derby
        if(ok && agent.equals(config.getServer())) {

            Task stopDerbyTask = TaskFactory.stop(agent, Product.DERBY);
            manager.getTaskRunner().executeTaskNWait(stopDerbyTask);

            res = stopDerbyTask.getResults().get(agent.getUuid());
            po.addLog(res.getStdOut());
            po.addLog(res.getStdErr());

            ok = stopDerbyTask.isCompleted() && isZero(res.getExitCode());
        }

        if(ok) po.addLogDone("Done");
        else po.addLogFailed(null);

    }

}
