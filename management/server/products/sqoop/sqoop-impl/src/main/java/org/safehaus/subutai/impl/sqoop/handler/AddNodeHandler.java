package org.safehaus.subutai.impl.sqoop.handler;

import java.util.Arrays;
import java.util.HashSet;

import org.safehaus.subutai.api.sqoop.Config;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.impl.sqoop.CommandFactory;
import org.safehaus.subutai.impl.sqoop.CommandType;
import org.safehaus.subutai.impl.sqoop.SqoopImpl;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;

public class AddNodeHandler extends AbstractHandler {

    public AddNodeHandler(SqoopImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    @Override
    public void run() {
        Config config = getClusterConfig();
        if(config == null) {
            po.addLogFailed("Cluster does not exist: " + clusterName);
            return;
        }
        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            po.addLogFailed("Node is not connected");
            return;
        }

        // check if already installed
        String s = CommandFactory.build(CommandType.LIST, null);
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s),
                new HashSet<>(Arrays.asList(agent)));
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasSucceeded()) {
            AgentResult res = cmd.getResults().get(agent.getUuid());
            if(res.getStdOut().contains(CommandFactory.PACKAGE_NAME)) {
                po.addLog("Sqoop already installed on " + hostname);
                addNode(agent, config);

                boolean saved = saveConfig(config);
                if(saved) po.addLogDone(null);
                else po.addLogFailed(null);
                return;
            }
        } else {
            po.addLogFailed("Failed to check installed packages");
            return;
        }

        // installation
        s = CommandFactory.build(CommandType.INSTALL, null);
        cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s).withTimeout(60),
                new HashSet<>(Arrays.asList(agent)));
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasSucceeded()) {
            po.addLog("Sqoop installed on " + hostname);
            addNode(agent, config);

            boolean saved = saveConfig(config);
            if(saved) po.addLogDone(null);
            else po.addLogFailed(null);
        } else {
            po.addLogFailed(cmd.getAllErrors());
        }
    }

    private void addNode(Agent agent, Config config) {
        if(config.getNodes() == null) config.setNodes(new HashSet<Agent>(4));
        config.getNodes().add(agent);
    }

    private boolean saveConfig(Config config) {
        boolean b = manager.getDbManager().saveInfo(Config.PRODUCT_KEY,
                config.getClusterName(), config);
        po.addLog(b ? "Cluster info successfully saved"
                : "Failed to save cluster info");
        return b;
    }

}
