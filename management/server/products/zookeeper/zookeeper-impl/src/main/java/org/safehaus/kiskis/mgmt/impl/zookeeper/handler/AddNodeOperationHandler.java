package org.safehaus.kiskis.mgmt.impl.zookeeper.handler;

import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.zookeeper.Config;
import org.safehaus.kiskis.mgmt.impl.zookeeper.Commands;
import org.safehaus.kiskis.mgmt.impl.zookeeper.ZookeeperImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dilshat on 5/7/14.
 */
public class AddNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
    private final ProductOperation po;

    public AddNodeOperationHandler(ZookeeperImpl manager, String clusterName) {
        super(manager, clusterName);
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Adding node to %s", clusterName));

    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        final Config config = manager.getCluster(clusterName);
        if (config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
            return;
        }

        try {

            //create lxc
            po.addLog("Creating lxc container...");

            Map<Agent, Set<Agent>> lxcAgentsMap = manager.getLxcManager().createLxcs(1);

            Agent lxcAgent = lxcAgentsMap.entrySet().iterator().next().getValue().iterator().next();

            config.getNodes().add(lxcAgent);

            po.addLog(String.format("Lxc container created successfully\nInstalling %s...", Config.PRODUCT_KEY));

            //install
            Command installCommand = Commands.getInstallCommand(Util.wrapAgentToSet(lxcAgent));
            manager.getCommandRunner().runCommand(installCommand);

            if (installCommand.hasCompleted()) {
                po.addLog("Installation succeeded\nUpdating db...");
                //update db
                if (manager.getDbManager().saveInfo(Config.PRODUCT_KEY, clusterName, config)) {
                    po.addLog("Cluster info updated in DB\nUpdating settings...");

                    //update settings
                    Command updateSettingsCommand = Commands.getUpdateSettingsCommand(config.getZkName(), config.getNodes());
                    manager.getCommandRunner().runCommand(updateSettingsCommand);

                    if (updateSettingsCommand.hasSucceeded()) {
                        po.addLog("Settings updated\nRestarting cluster...");
                        //restart all nodes
                        Command restartCommand = Commands.getRestartCommand(config.getNodes());
                        final AtomicInteger count = new AtomicInteger();
                        manager.getCommandRunner().runCommand(restartCommand, new CommandCallback() {
                            @Override
                            public void onResponse(Response response, AgentResult agentResult, Command command) {
                                if (agentResult.getStdOut().contains("STARTED")) {
                                    if (count.incrementAndGet() == config.getNodes().size()) {
                                        stop();
                                    }
                                }
                            }
                        });
                        if (count.get() == config.getNodes().size()) {
                            po.addLogDone("Cluster restarted successfully\nDone");
                        } else {
                            po.addLogFailed(String.format("Failed to restart cluster, %s", restartCommand.getAllErrors()));
                        }
                    } else {
                        po.addLogFailed(
                                String.format(
                                        "Settings update failed, %s.\nPlease update settings manually and restart the cluster",
                                        updateSettingsCommand.getAllErrors())
                        );
                    }
                } else {
                    po.addLogFailed("Error while updating cluster info in DB. Check logs. Use LXC Module to cleanup\nFailed");
                }
            } else {
                po.addLogFailed(String.format("Installation failed, %s\nUse LXC Module to cleanup",
                        installCommand.getAllErrors()));
            }

        } catch (LxcCreateException ex) {
            po.addLogFailed(ex.getMessage());
        }
    }
}
