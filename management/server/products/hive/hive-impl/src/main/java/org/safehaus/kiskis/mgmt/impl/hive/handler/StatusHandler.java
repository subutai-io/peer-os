package org.safehaus.kiskis.mgmt.impl.hive.handler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hive.CommandType;
import org.safehaus.kiskis.mgmt.impl.hive.Commands;
import org.safehaus.kiskis.mgmt.impl.hive.HiveImpl;
import org.safehaus.kiskis.mgmt.impl.hive.Product;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class StatusHandler extends AbstractHandler {

    private final String hostname;
    private final ProductOperation po;

    public StatusHandler(HiveImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.hostname = hostname;
        this.po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                "Status check for " + hostname);
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
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

        boolean ok = true;
        // if server node, check Derby first
        if(agent.equals(config.getServer())) {

            String s = Commands.make(CommandType.STATUS, Product.DERBY);
            Command cmd = manager.getCommandRunner().createCommand(
                    new RequestBuilder(s),
                    new HashSet<>(Arrays.asList(agent)));
            manager.getCommandRunner().runCommand(cmd);

            AgentResult res = cmd.getResults().get(agent.getUuid());
            po.addLog(res.getStdOut());
            po.addLog(res.getStdErr());

            ok = cmd.hasSucceeded();
        }
        if(ok) {

            String s = Commands.make(CommandType.STATUS, Product.HIVE);
            Command cmd = manager.getCommandRunner().createCommand(
                    new RequestBuilder(s),
                    new HashSet(Arrays.asList(agent)));
            manager.getCommandRunner().runCommand(cmd);

            AgentResult res = cmd.getResults().get(agent.getUuid());
            po.addLog(res.getStdOut());
            po.addLog(res.getStdErr());

            ok = cmd.hasSucceeded();
        }

        if(ok) po.addLogDone("Done");
        else po.addLogFailed(null);
    }

}
