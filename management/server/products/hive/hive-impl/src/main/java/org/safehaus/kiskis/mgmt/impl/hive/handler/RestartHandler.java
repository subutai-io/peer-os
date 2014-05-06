package org.safehaus.kiskis.mgmt.impl.hive.handler;

import java.util.Arrays;
import java.util.HashSet;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.impl.hive.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class RestartHandler extends AbstractHandler {

    private final String hostname;

    public RestartHandler(HiveImpl manager, String clusterName, String hostname) {
        super(manager, clusterName, "Restart node " + hostname);
        this.hostname = hostname;
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

        String s = Commands.make(CommandType.RESTART, Product.HIVE);
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s).withTimeout(90),
                new HashSet<Agent>(Arrays.asList(agent)));
        manager.getCommandRunner().runCommand(cmd);

        AgentResult res = cmd.getResults().get(agent.getUuid());
        po.addLog(res.getStdOut());
        po.addLog(res.getStdErr());

        boolean ok = cmd.hasSucceeded();

        // if server node, restart Derby as well
        if(ok && agent.equals(config.getServer())) {

            s = Commands.make(CommandType.RESTART, Product.DERBY);
            cmd = manager.getCommandRunner().createCommand(
                    new RequestBuilder(s).withTimeout(90),
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
