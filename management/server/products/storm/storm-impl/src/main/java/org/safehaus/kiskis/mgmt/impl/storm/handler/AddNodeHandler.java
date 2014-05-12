package org.safehaus.kiskis.mgmt.impl.storm.handler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.storm.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.storm.CommandType;
import org.safehaus.kiskis.mgmt.impl.storm.Commands;
import org.safehaus.kiskis.mgmt.impl.storm.StormImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class AddNodeHandler extends AbstractHandler {

    private final ProductOperation po;
    private final String hostname;

    public AddNodeHandler(StormImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.po = manager.getTracker().createProductOperation(
                Config.PRODUCT_NAME, "Add node to cluster: " + hostname);
        this.hostname = hostname;
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    public void run() {
        Config config = manager.getCluster(clusterName);
        if(config == null) {
            po.addLogFailed(String.format("Cluster '%s' does not exist", clusterName));
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            po.addLogFailed(String.format("Node '%s' is not connected", hostname));
            return;
        }

        Set<Agent> set = new HashSet<Agent>();
        set.add(agent);

        // check if Storm is already installed
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.LIST)), set);
        manager.getCommandRunner().runCommand(cmd);
        boolean skipInstall = false;
        if(cmd.hasSucceeded()) {
            AgentResult res = cmd.getResults().get(agent.getUuid());
            if(res.getStdOut().contains(Commands.PACKAGE_NAME)) {
                skipInstall = true;
                po.addLog("Storm already installed on " + hostname);
            }
        } else {
            po.addLogFailed("Failed to check installed packages");
            return;
        }

        if(!skipInstall) {
            cmd = manager.getCommandRunner().createCommand(
                    new RequestBuilder(Commands.make(CommandType.INSTALL)), set);
            manager.getCommandRunner().runCommand(cmd);
            if(cmd.hasSucceeded())
                po.addLog("Storm successfully installed on " + hostname);
            else {
                po.addLogFailed("Failed to install Storm on " + hostname);
                return;
            }
        }

        // add node to collection and do configuration
        config.getSupervisors().add(agent);
        if(!configure(config)) {
            po.addLogFailed("Failed to configure node");
            return;
        }
        po.addLogDone("Node successfully configured");

        boolean b = manager.getDbManager().saveInfo(Config.PRODUCT_NAME,
                clusterName, config);
        if(b) po.addLogDone("Cluster info successfully saved");
        else po.addLogFailed("Failed to save cluster info");
    }

}
