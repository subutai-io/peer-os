package org.safehaus.kiskis.mgmt.impl.hive.handler;

import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.*;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hive.HiveImpl;
import org.safehaus.kiskis.mgmt.impl.hive.Product;
import org.safehaus.kiskis.mgmt.impl.hive.TaskFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class RestartHandler extends AbstractHandler {

    public RestartHandler(HiveImpl manager, String clusterName, ProductOperation po) {
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

        Task restartHiveTask = TaskFactory.restart(agent, Product.HIVE);
        manager.getTaskRunner().executeTaskNWait(restartHiveTask);

        Result res = restartHiveTask.getResults().get(agent.getUuid());
        po.addLog(res.getStdOut());
        po.addLog(res.getStdErr());

        boolean ok = restartHiveTask.isCompleted() && isZero(res.getExitCode());

        // if server node, restart Derby as well
        if(ok && agent.equals(config.getServer())) {

            Task restartDerbyTask = TaskFactory.restart(agent, Product.DERBY);
            manager.getTaskRunner().executeTaskNWait(restartDerbyTask);

            res = restartDerbyTask.getResults().get(agent.getUuid());
            po.addLog(res.getStdOut());
            po.addLog(res.getStdErr());

            ok = restartDerbyTask.isCompleted() && isZero(res.getExitCode());
        }

        if(ok) po.addLogDone("Done");
        else po.addLogFailed(null);

    }

}
