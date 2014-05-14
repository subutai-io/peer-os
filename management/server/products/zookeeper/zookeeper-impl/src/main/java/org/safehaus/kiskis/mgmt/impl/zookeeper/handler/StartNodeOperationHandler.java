package org.safehaus.kiskis.mgmt.impl.zookeeper.handler;

import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.api.zookeeper.Config;
import org.safehaus.kiskis.mgmt.impl.zookeeper.Commands;
import org.safehaus.kiskis.mgmt.impl.zookeeper.ZookeeperImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dilshat on 5/7/14.
 */
public class StartNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
    private final ProductOperation po;
    private final String lxcHostname;

    public StartNodeOperationHandler(ZookeeperImpl manager, String clusterName, String lxcHostname) {
        super(manager, clusterName);
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Starting node %s in %s", lxcHostname, clusterName));
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        Config config = manager.getCluster(clusterName);
        if (config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
            return;
        }

        final Agent node = manager.getAgentManager().getAgentByHostname(lxcHostname);
        if (node == null) {
            po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
            return;
        }

        if (!config.getNodes().contains(node)) {
            po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName));
            return;
        }

        po.addLog("Starting node...");

        Command startCommand = Commands.getStartCommand(Util.wrapAgentToSet(node));
        final AtomicBoolean ok = new AtomicBoolean();
        manager.getCommandRunner().runCommand(startCommand, new CommandCallback() {
            @Override
            public void onResponse(Response response, AgentResult agentResult, Command command) {
                if (agentResult.getStdOut().contains("STARTED")) {
                    ok.set(true);
                    stop();
                }
            }
        });

        if (ok.get()) {
            po.addLogDone(String.format("Node on %s started", lxcHostname));
        } else {
            po.addLogFailed(String.format("Failed to start node %s. %s",
                    lxcHostname, startCommand.getAllErrors()
            ));
        }
    }
}
