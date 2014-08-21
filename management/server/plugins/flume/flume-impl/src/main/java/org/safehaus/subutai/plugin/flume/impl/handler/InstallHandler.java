package org.safehaus.subutai.plugin.flume.impl.handler;

import org.safehaus.subutai.plugin.flume.impl.FlumeImpl;
import org.safehaus.subutai.plugin.flume.impl.Commands;
import org.safehaus.subutai.plugin.flume.impl.CommandType;
import java.util.Iterator;
import org.safehaus.subutai.api.commandrunner.*;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;

public class InstallHandler extends AbstractOperationHandler<FlumeImpl> {

    private final FlumeConfig config;

    public InstallHandler(FlumeImpl manager, FlumeConfig config) {
        super(manager, config.getClusterName());
        this.config = config;
        this.productOperation = manager.getTracker().createProductOperation(
                FlumeConfig.PRODUCT_KEY, "Install Flume cluster " + config.getClusterName());
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        if(clusterName == null || clusterName.isEmpty() || config.getNodes() == null || config.getNodes().isEmpty()) {
            po.addLogFailed("Invalid configuration");
            return;
        }
        if(manager.getCluster(config.getClusterName()) != null) {
            po.addLogFailed("Cluster already exists: " + clusterName);
            return;
        }

        //check if node agent is connected
        for(Iterator<Agent> it = config.getNodes().iterator(); it.hasNext();) {
            Agent node = it.next();
            if(manager.getAgentManager().getAgentByHostname(node.getHostname()) != null)
                continue;
            po.addLog(String.format(
                    "Node %s is not connected. Omitting this node from installation",
                    node.getHostname()));
            it.remove();
        }
        if(config.getNodes().isEmpty()) {
            po.addLogFailed("No nodes eligible for installation. Operation aborted");
            return;
        }

        po.addLog("Checking prerequisites...");
        //check installed ksks packages
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.STATUS)),
                config.getNodes());
        manager.getCommandRunner().runCommand(cmd);
        if(!cmd.hasSucceeded()) {
            po.addLogFailed("Failed to check installed packages");
            return;
        }

        for(Iterator<Agent> it = config.getNodes().iterator(); it.hasNext();) {
            Agent node = it.next();
            AgentResult result = cmd.getResults().get(node.getUuid());

            if(result.getStdOut().contains("ksks-flume")) {
                po.addLog(String.format(
                        "Node %s already has Flume installed. Omitting this node from installation",
                        node.getHostname()));
                it.remove();
            } else if(!result.getStdOut().contains("ksks-hadoop")) {
                po.addLog(String.format(
                        "Node %s has no Hadoop installation. Omitting this node from installation",
                        node.getHostname()));
                it.remove();
            }
        }

        if(config.getNodes().isEmpty()) {
            po.addLogFailed("No nodes eligible for installation. Operation aborted");
            return;
        }

        po.addLog("Updating db...");
        boolean b = manager.getDbManager().saveInfo(FlumeConfig.PRODUCT_KEY,
                config.getClusterName(), config);
        if(!b) {
            po.addLogFailed("Failed to save cluster info!");
            return;
        }
        po.addLog("Cluster info successfully saved\nInstalling Flume...");

        String s = Commands.make(CommandType.INSTALL);
        cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s).withTimeout(90),
                config.getNodes());
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasSucceeded())
            po.addLogDone("Installation succeeded\nDone");
        else {
            po.addLog(cmd.getAllErrors());
            po.addLogFailed("Installation failed");
        }

    }

}
