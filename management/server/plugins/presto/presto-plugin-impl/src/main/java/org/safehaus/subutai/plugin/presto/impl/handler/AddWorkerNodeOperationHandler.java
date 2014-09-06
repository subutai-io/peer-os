package org.safehaus.subutai.plugin.presto.impl.handler;

import com.google.common.collect.Sets;
import java.util.concurrent.atomic.AtomicBoolean;
import org.safehaus.subutai.common.protocol.*;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.*;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.Commands;
import org.safehaus.subutai.plugin.presto.impl.PrestoImpl;

public class AddWorkerNodeOperationHandler extends AbstractOperationHandler<PrestoImpl> {

    private final String lxcHostname;

    public AddWorkerNodeOperationHandler(PrestoImpl manager, String clusterName, String lxcHostname) {
        super(manager, clusterName);
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation(
                PrestoClusterConfig.PRODUCT_KEY,
                String.format("Adding node %s to %s", lxcHostname, clusterName));
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        PrestoClusterConfig config = manager.getCluster(clusterName);
        if(config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
            return;
        }

        if(manager.getAgentManager().getAgentByHostname(config.getCoordinatorNode().getHostname()) == null) {
            po.addLogFailed(String.format("Coordinator node %s is not connected\nOperation aborted",
                    config.getCoordinatorNode().getHostname()));
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname(lxcHostname);
        if(agent == null) {
            po.addLogFailed(String.format("New node %s is not connected\nOperation aborted", lxcHostname));
            return;
        }

        //check if node is in the cluster
        if(config.getWorkers().contains(agent)) {
            po.addLogFailed(String.format("Node %s already belongs to this cluster\nOperation aborted",
                    agent.getHostname()));
            return;
        }

        po.addLog("Checking prerequisites...");

        //check installed ksks packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand(Sets.newHashSet(agent));
        manager.getCommandRunner().runCommand(checkInstalledCommand);

        if(!checkInstalledCommand.hasCompleted()) {
            po.addLogFailed("Failed to check presence of installed ksks packages\nOperation aborted");
            return;
        }

        AgentResult result = checkInstalledCommand.getResults().get(agent.getUuid());

        if(result.getStdOut().contains(Commands.PACKAGE_NAME)) {
            po.addLogFailed(String.format("Node %s already has Presto installed\nOperation aborted", lxcHostname));
            return;
        } else if(!result.getStdOut().contains("ksks-hadoop")) {
            po.addLogFailed(String.format("Node %s has no Hadoop installation\nOperation aborted", lxcHostname));
            return;
        }

        config.getWorkers().add(agent);
        po.addLog("Updating db...");
        //save to db
        try {
            manager.getPluginDAO().saveInfo(PrestoClusterConfig.PRODUCT_KEY, config.getClusterName(), config);

            po.addLog("Cluster info updated in DB");
            //install presto

            po.addLog("Installing Presto...");
            Command installCommand = Commands.getInstallCommand(Sets.newHashSet(agent));
            manager.getCommandRunner().runCommand(installCommand);

            if(installCommand.hasSucceeded())
                po.addLog("Installation succeeded");
            else {
                po.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
                return;
            }

            po.addLog("Configuring worker...");
            Command configureWorkerCommand
                    = Commands.getSetWorkerCommand(config.getCoordinatorNode(), Sets.newHashSet(agent));
            manager.getCommandRunner().runCommand(configureWorkerCommand);

            if(configureWorkerCommand.hasSucceeded()) {
                po.addLog("Worker configured successfully\nStarting Presto on new node...");

                Command startCommand = Commands.getStartCommand(Sets.newHashSet(agent));
                final AtomicBoolean ok = new AtomicBoolean();
                manager.getCommandRunner().runCommand(startCommand, new CommandCallback() {

                    @Override
                    public void onResponse(Response response, AgentResult agentResult, Command command) {
                        if(agentResult.getStdOut().contains("Started")) {
                            ok.set(true);
                            stop();
                        }
                    }
                });

                if(ok.get())
                    po.addLogDone("Presto started successfully\nDone");
                else
                    po.addLogFailed(String.format("Failed to start Presto, %s", startCommand.getAllErrors()));
            } else
                po.addLogFailed(
                        String.format("Failed to configure worker, %s", configureWorkerCommand.getAllErrors()));
        } catch(DBException e) {
            po.addLogFailed("Could not update cluster info in DB! Please see logs\nOperation aborted");
        }
    }
}
