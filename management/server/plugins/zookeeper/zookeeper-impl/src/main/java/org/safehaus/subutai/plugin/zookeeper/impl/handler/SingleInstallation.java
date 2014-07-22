package org.safehaus.subutai.plugin.zookeeper.impl.handler;

import java.util.Arrays;
import java.util.HashSet;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.protocol.Agent;

public class SingleInstallation extends AbstractOperationHandler<ZookeeperImpl> {

    private final String hostName;

    public SingleInstallation(ZookeeperImpl manager, String hostName) {
        super(manager, null);
        this.hostName = hostName;
        this.productOperation = manager.getTracker().createProductOperation(
                ZookeeperClusterConfig.PRODUCT_KEY, "Installing on " + hostName);
    }

    @Override
    public void run() {
        Agent a = manager.getAgentManager().getAgentByHostname(hostName);
        Command cmd = Commands.getInstallCommand(new HashSet<>(Arrays.asList(a)));
        manager.getCommandRunner().runCommand(cmd);
        if(cmd.hasSucceeded()) productOperation.addLogDone("Installation succeeded");
        else productOperation.addLogFailed("Failed to install on " + hostName);
    }

}
