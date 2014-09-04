package org.safehaus.subutai.plugin.storm.impl.handler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.RequestBuilder;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.storm.api.StormConfig;
import org.safehaus.subutai.plugin.storm.impl.CommandType;
import org.safehaus.subutai.plugin.storm.impl.Commands;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;

public class AddNodeHandler extends AbstractHandler {

    private final String hostname;

    public AddNodeHandler(StormImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.productOperation = manager.getTracker().createProductOperation(
                StormConfig.PRODUCT_NAME, "Add node to cluster: "
                + (hostname != null ? hostname : "new container will be created"));
        this.hostname = hostname;
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        StormConfig config = manager.getCluster(clusterName);
        if(config == null) {
            po.addLogFailed(String.format("Cluster '%s' does not exist", clusterName));
            return;
        }

        Agent agent;
        if(hostname != null) {
            agent = manager.getAgentManager().getAgentByHostname(hostname);
            if(agent == null) {
                po.addLogFailed(String.format("Node '%s' is not connected", hostname));
                return;
            }
        } else {
            po.addLog("Creating container for new node...");
            Helper helper = new Helper(manager);
            try {
                agent = helper.createContainer();
                if(agent == null)
                    throw new LxcCreateException("returned value is null");
            } catch(LxcCreateException ex) {
                po.addLogFailed("Failed to create container: " + ex.getMessage());
                return;
            }
            po.addLog("Container created. Host name is " + agent.getHostname());
        }

        Set<Agent> set = new HashSet<>();
        set.add(agent);

        po.addLog("Installing Storm...");
        String s = Commands.make(CommandType.INSTALL);
        int t = (int)TimeUnit.MINUTES.toSeconds(25);
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s).withTimeout(t), set);
        manager.getCommandRunner().runCommand(cmd);
        if(cmd.hasSucceeded())
            po.addLog("Storm successfully installed on " + agent.getHostname());
        else {
            po.addLogFailed("Failed to install Storm on " + agent.getHostname());
            return;
        }

        // add node to collection and do configuration
        config.getSupervisors().add(agent);
        if(!configure(config)) {
            po.addLogFailed("Failed to configure node");
            return;
        }
        po.addLogDone("Node successfully configured");

        try {
            manager.getPluginDao().saveInfo(StormConfig.PRODUCT_NAME,
                    clusterName, config);
            po.addLogDone("Cluster info successfully saved");
        } catch(DBException ex) {
            manager.getLogger().error("Failed to save in db", ex);
            po.addLogFailed("Failed to save cluster info");
        }
    }

}
