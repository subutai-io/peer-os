package org.safehaus.kiskis.mgmt.impl.flume.handler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.flume.Config;
import org.safehaus.kiskis.mgmt.impl.flume.CommandType;
import org.safehaus.kiskis.mgmt.impl.flume.Commands;
import org.safehaus.kiskis.mgmt.impl.flume.FlumeImpl;
import org.safehaus.kiskis.mgmt.shared.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class AddNodeHandler extends AbstractOperationHandler<FlumeImpl> {

    private final String hostname;
    private final ProductOperation po;

    public AddNodeHandler(FlumeImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.hostname = hostname;
        this.po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                "Add node to cluster: " + clusterName);
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    public void run() {
        Config config = manager.getCluster(clusterName);
        if(config == null) {
            po.addLogFailed("Cluster does not exist: " + clusterName);
            return;
        }
        //check if node agent is connected
        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            po.addLogFailed("Node is not connected: " + hostname);
            return;
        }

        Set<Agent> set = new HashSet<Agent>(Arrays.asList(agent));

        po.addLog("Checking prerequisites...");
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.STATUS)), set);
        manager.getCommandRunner().runCommand(cmd);
        if(!cmd.hasSucceeded()) {
            po.addLogFailed("Failed to check installed packages");
            return;
        }

        AgentResult res = cmd.getResults().get(agent.getUuid());
        if(res.getStdOut().contains("ksks-flume")) {
            po.addLogFailed("Flume already installed on " + hostname);
            return;
        } else if(!res.getStdOut().contains("ksks-hadoop")) {
            po.addLogFailed("Hadoop not installed on " + hostname);
            return;
        }

        config.getNodes().add(agent);

        po.addLog("Updating db...");
        if(manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
            po.addLog("Cluster info updated in DB\nInstalling Flume...");

            cmd = manager.getCommandRunner().createCommand(
                    new RequestBuilder(Commands.make(CommandType.INSTALL)),
                    set);
            manager.getCommandRunner().runCommand(cmd);

            if(cmd.hasSucceeded())
                po.addLogDone("Installation succeeded");
            else {
                po.addLog(cmd.getAllErrors());
                po.addLogFailed("Installation failed");
            }
        } else
            po.addLogFailed("Failed to update cluster info");

    }

}
