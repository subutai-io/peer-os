package org.safehaus.subutai.plugin.spark.impl.handler;


import java.util.concurrent.atomic.AtomicBoolean;

import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.Commands;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;

import com.google.common.collect.Sets;

public class AddSlaveNodeOperationHandler extends AbstractOperationHandler<SparkImpl> {

    private final String hostname;

    public AddSlaveNodeOperationHandler(SparkImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.hostname = hostname;
        productOperation = manager.getTracker().createProductOperation(SparkClusterConfig.PRODUCT_KEY,
                String.format("Adding node %s to %s", hostname, clusterName));
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        SparkClusterConfig config = manager.getCluster(clusterName);
        if(config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
            return;
        }

        if(manager.getAgentManager().getAgentByHostname(config.getMasterNode().getHostname()) == null) {
            po.addLogFailed(String
                    .format("Master node %s is not connected\nOperation aborted", config.getMasterNode().getHostname()));
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            po.addLogFailed(String.format("New node %s is not connected\nOperation aborted", hostname));
            return;
        }

        //check if node is in the cluster
        if(config.getSlaveNodes().contains(agent)) {
            po.addLogFailed(
                    String.format("Node %s already belongs to this cluster\nOperation aborted", agent.getHostname()));
            return;
        }

        // check if node is one of Hadoop cluster nodes
        if(!config.getHadoopNodes().contains(agent)) {
            po.addLogFailed("Node does not belong to Hadoop cluster");
            return;
        }

        po.addLog("Checking prerequisites...");

        boolean install = !agent.equals(config.getMasterNode());

        //check installed ksks packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand(Sets.newHashSet(agent));
        manager.getCommandRunner().runCommand(checkInstalledCommand);

        if(!checkInstalledCommand.hasCompleted()) {
            po.addLogFailed("Failed to check presence of installed ksks packages\nOperation aborted");
            return;
        }

        AgentResult result = checkInstalledCommand.getResults().get(agent.getUuid());

        if(result.getStdOut().contains("ksks-spark") && install) {
            po.addLogFailed(String.format("Node %s already has Spark installed\nOperation aborted", hostname));
            return;
        } else if(!result.getStdOut().contains("ksks-hadoop")) {
            po.addLogFailed(String.format("Node %s has no Hadoop installation\nOperation aborted", hostname));
            return;
        }

        config.getSlaveNodes().add(agent);

        po.addLog("Updating db...");
        try {
            //save to db
            manager.getPluginDAO().saveInfo(SparkClusterConfig.PRODUCT_KEY, config.getClusterName(), config);
            po.addLog("Cluster info updated in DB");
        } catch(DBException ex) {
            po.addLogFailed("Could not update cluster info in DB! Please see logs\nOperation aborted");
            return;
        }

        //install spark
        if(install) {
            po.addLog("Installing Spark...");
            Command installCommand = Commands.getInstallCommand(Sets.newHashSet(agent));
            manager.getCommandRunner().runCommand(installCommand);

            if(installCommand.hasSucceeded())
                po.addLog("Installation succeeded");
            else {
                po.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
                return;
            }
        }

        po.addLog("Setting master IP on slave...");
        Command setMasterIPCommand = Commands
                .getSetMasterIPCommand(config.getMasterNode(), Sets.newHashSet(agent));
        manager.getCommandRunner().runCommand(setMasterIPCommand);

        if(setMasterIPCommand.hasSucceeded()) {
            po.addLog("Master IP successfully set\nRegistering slave with master...");

            Command addSlaveCommand = Commands.getAddSlaveCommand(agent, config.getMasterNode());
            manager.getCommandRunner().runCommand(addSlaveCommand);

            if(addSlaveCommand.hasSucceeded()) {
                po.addLog("Registration succeeded\nRestarting master...");

                Command restartMasterCommand = Commands.getRestartMasterCommand(config.getMasterNode());
                final AtomicBoolean ok = new AtomicBoolean();
                manager.getCommandRunner().runCommand(restartMasterCommand, new CommandCallback() {

                    @Override
                    public void onResponse(Response response, AgentResult agentResult, Command command) {
                        if(agentResult.getStdOut().contains("starting")) {
                            ok.set(true);
                            stop();
                        }
                    }

                });

                if(ok.get()) {
                    po.addLog("Master restarted successfully\nStarting Spark on new node...");

                    Command startSlaveCommand = Commands.getStartSlaveCommand(agent);
                    ok.set(false);
                    manager.getCommandRunner().runCommand(startSlaveCommand, new CommandCallback() {

                        @Override
                        public void onResponse(Response response, AgentResult agentResult, Command command) {
                            if(agentResult.getStdOut().contains("starting")) {
                                ok.set(true);
                                stop();
                            }
                        }
                    });

                    if(ok.get())
                        po.addLogDone("Spark started successfully\nDone");
                    else
                        po.addLogFailed(
                                String.format("Failed to start Spark, %s", startSlaveCommand.getAllErrors()));

                } else
                    po.addLogFailed(
                            String.format("Master restart failed, %s", restartMasterCommand.getAllErrors()));

            } else
                po.addLogFailed(String.format("Registration failed, %s", addSlaveCommand.getAllErrors()));
        } else
            po.addLogFailed(String.format("Failed to set master IP, %s", setMasterIPCommand.getAllErrors()));

    }
}
