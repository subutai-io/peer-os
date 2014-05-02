package org.safehaus.kiskis.mgmt.impl.hive.handler;

import java.util.Arrays;
import java.util.HashSet;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hive.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class StopHandler extends AbstractHandler {

    public StopHandler(HiveImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    public void run() {
        Config config = manager.getCluster(clusterName);
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

        String s = Commands.make(CommandType.STOP, Product.HIVE);
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s), new HashSet<Agent>(Arrays.asList(agent)));

        AgentResult res = cmd.getResults().get(agent.getUuid());
        po.addLog(res.getStdOut());
        po.addLog(res.getStdErr());

        boolean ok = cmd.hasSucceeded();

        // if server node, stop Derby
        if(ok && agent.equals(config.getServer())) {

            s = Commands.make(CommandType.STOP, Product.DERBY);
            cmd = manager.getCommandRunner().createCommand(new RequestBuilder(s),
                    new HashSet<Agent>(Arrays.asList(agent)));
            manager.getCommandRunner().runCommand(cmd);

            res = cmd.getResults().get(agent.getUuid());
            po.addLog(res.getStdOut());
            po.addLog(res.getStdErr());

            ok = cmd.hasSucceeded();
        }

        if(ok) po.addLogDone("Done");
        else po.addLogFailed(null);

    }

}
