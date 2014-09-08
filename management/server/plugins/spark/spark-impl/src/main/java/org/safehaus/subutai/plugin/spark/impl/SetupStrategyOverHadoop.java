package org.safehaus.subutai.plugin.spark.impl;

import java.util.Iterator;
import java.util.Set;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

public class SetupStrategyOverHadoop extends SetupBase implements ClusterSetupStrategy {

    public SetupStrategyOverHadoop(ProductOperation po, SparkImpl sparkManager, SparkClusterConfig config) {
        super(po, sparkManager, config);
    }

    @Override
    public ConfigBase setup() throws ClusterSetupException {
        check();
        install();
        return config;
    }

    private void check() throws ClusterSetupException {

        String m = "Malformed configuration: ";
        if(config.getClusterName() == null || config.getClusterName().isEmpty())
            throw new ClusterSetupException(m + "cluster name not specified");
        if(manager.getCluster(config.getClusterName()) != null)
            throw new ClusterSetupException(m + String.format(
                    "cluster %s already exists", config.getClusterName()));
        if(config.getMasterNode() == null)
            throw new ClusterSetupException(m + "master node not specified");
        if(config.getSlaveNodes().isEmpty())
            throw new ClusterSetupException(m + "no slave nodes");

        // check if nodes are connected
        if(manager.agentManager.getAgentByHostname(config.getMasterNode().getHostname()) == null)
            throw new ClusterSetupException("Master node is not connected");
        for(Agent a : config.getSlaveNodes()) {
            if(manager.agentManager.getAgentByHostname(a.getHostname()) == null)
                throw new ClusterSetupException("Not all slave nodes are connected");
        }

        po.addLog("Checking prerequisites...");

        //check installed ksks packages
        Set<Agent> allNodes = config.getAllNodes();
        Command checkInstalledCommand = Commands.getCheckInstalledCommand(allNodes);
        manager.getCommandRunner().runCommand(checkInstalledCommand);

        if(!checkInstalledCommand.hasCompleted())
            throw new ClusterSetupException(
                    "Failed to check presence of installed ksks packages\nInstallation aborted");
        for(Iterator<Agent> it = allNodes.iterator(); it.hasNext();) {
            Agent node = it.next();

            AgentResult result = checkInstalledCommand.getResults().get(node.getUuid());
            if(result.getStdOut().contains(Commands.PACKAGE_NAME)) {
                po.addLog(String.format("Node %s already has Spark installed. Omitting this node from installation",
                        node.getHostname()));
                config.getSlaveNodes().remove(node);
                it.remove();
            } else if(!result.getStdOut().contains("ksks-hadoop")) {
                po.addLog(String.format("Node %s has no Hadoop installation. Omitting this node from installation",
                        node.getHostname()));
                config.getSlaveNodes().remove(node);
                it.remove();
            }
        }

        if(config.getSlaveNodes().isEmpty())
            throw new ClusterSetupException("No nodes eligible for installation\nInstallation aborted");
        if(!allNodes.contains(config.getMasterNode()))
            throw new ClusterSetupException("Master node was omitted\nInstallation aborted");
    }

    private void install() throws ClusterSetupException {
        po.addLog("Updating db...");
        //save to db
        try {
            manager.getPluginDAO().saveInfo(SparkClusterConfig.PRODUCT_KEY, config.getClusterName(),
                    config);
            po.addLog("Cluster info saved to DB\nInstalling Spark...");
            //install spark
            Command installCommand = Commands.getInstallCommand(config.getAllNodes());
            manager.getCommandRunner().runCommand(installCommand);

            if(installCommand.hasSucceeded()) {
                po.addLog("Installation succeeded");

                SetupHelper helper = new SetupHelper(manager, config, po);
                helper.configureMaster();
                helper.registerSlaves();
                helper.startCluster();

            } else
                throw new ClusterSetupException("Installation failed: "
                        + installCommand.getAllErrors());
        } catch(DBException e) {
            throw new ClusterSetupException(
                    "Could not save cluster info to DB! Please see logs\nInstallation aborted");
        }
    }
}
