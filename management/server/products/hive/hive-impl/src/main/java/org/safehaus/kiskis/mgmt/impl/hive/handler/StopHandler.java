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

public class StopHandler extends AbstractHandler {

    private final String hostname;
    private final ProductOperation po;

    public StopHandler(HiveImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.hostname = hostname;
        this.po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                "Stop node " + hostname);
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
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
        manager.getCommandRunner().runCommand(cmd);

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
