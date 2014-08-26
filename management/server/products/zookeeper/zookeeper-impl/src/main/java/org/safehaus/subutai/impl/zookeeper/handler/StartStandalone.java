package org.safehaus.subutai.impl.zookeeper.handler;

import java.util.Arrays;
import java.util.HashSet;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.zookeeper.Config;
import org.safehaus.subutai.impl.zookeeper.Commands;
import org.safehaus.subutai.impl.zookeeper.ZookeeperImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.protocol.Agent;

public class StartStandalone extends AbstractOperationHandler<ZookeeperImpl> {

    private final String hostname;

    public StartStandalone(ZookeeperImpl manager, String hostname) {
        super(manager, null);
        this.hostname = hostname;
        this.productOperation = manager.getTracker().createProductOperation(
                Config.PRODUCT_KEY, "Start stand alone Zookeeper node");

    }

    @Override
    public void run() {
        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            productOperation.addLogFailed("Node is not connected");
            return;
        }

        productOperation.addLog("Starting node...");
        Command cmd = Commands.getStartCommand(new HashSet<>(Arrays.asList(agent)));
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasSucceeded()) {
            AgentResult ar = cmd.getResults().get(agent.getUuid());
            if(ar.getStdOut().contains("STARTED"))
                productOperation.addLogDone("Zookeeper started on " + hostname);
        }
        productOperation.addLogFailed("Failed to start: " + cmd.getAllErrors());
    }

}
