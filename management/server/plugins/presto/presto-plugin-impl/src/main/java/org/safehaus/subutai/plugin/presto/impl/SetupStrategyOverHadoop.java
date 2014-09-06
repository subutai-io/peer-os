package org.safehaus.subutai.plugin.presto.impl;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;

public class SetupStrategyOverHadoop extends SetupHelper implements ClusterSetupStrategy {

    private Set<Agent> skipInstallation = new HashSet<>();

    public SetupStrategyOverHadoop(ProductOperation po, PrestoImpl manager,
            PrestoClusterConfig config) {
        super(po, manager, config);
    }

    @Override
    public PrestoClusterConfig setup() throws ClusterSetupException {
        check();
        install();
        return config;
    }

    private void check() throws ClusterSetupException {
        po.addLog("Checking prerequisites...");

        String m = "Malformed configuration: ";
        if(manager.getCluster(config.getClusterName()) != null)
            throw new ClusterSetupException(m + "Cluster already exists: " + config.getClusterName());
        if(config.getCoordinatorNode() == null)
            throw new ClusterSetupException(m + "Coordinator node is not specified");
        if(config.getWorkers() == null || config.getWorkers().isEmpty())
            throw new ClusterSetupException(m + "No workers nodes");

        checkConnected();

        //check installed packages
        Set<Agent> allNodes = config.getAllNodes();
        Command checkInstalledCommand = Commands.getCheckInstalledCommand(allNodes);
        manager.getCommandRunner().runCommand(checkInstalledCommand);

        if(!checkInstalledCommand.hasCompleted())
            throw new ClusterSetupException(
                    "Failed to check installed packages\nInstallation aborted");

        String hadoopPack = Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME;
        skipInstallation.clear();
        for(Agent node : allNodes) {
            AgentResult result = checkInstalledCommand.getResults().get(node.getUuid());
            if(result.getStdOut().contains(Commands.PACKAGE_NAME)) {
                skipInstallation.add(node);
                po.addLog(String.format("Node %s already has Presto installed. Omitting this node from installation",
                        node.getHostname()));
            } else if(!result.getStdOut().contains(hadoopPack))
                throw new ClusterSetupException(String.format(
                        "Node %s has no Hadoop installation", node.getHostname()));
        }

        if(config.getWorkers().isEmpty())
            throw new ClusterSetupException("No nodes eligible for installation\nInstallation aborted");
        if(!allNodes.contains(config.getCoordinatorNode()))
            throw new ClusterSetupException("Coordinator node was omitted\nInstallation aborted");
    }

    private void install() throws ClusterSetupException {
        po.addLog("Updating db...");
        //save to db
        try {
            manager.getPluginDAO().saveInfo(PrestoClusterConfig.PRODUCT_KEY,
                    config.getClusterName(),
                    config);

            po.addLog("Cluster info saved to DB");

        } catch(DBException e) {
            throw new ClusterSetupException(
                    "Could not save cluster info to DB! Please see logs\nInstallation aborted");
        }

        //install presto
        po.addLog("Installing Presto...");
        Set<Agent> installationSet = new HashSet<>(config.getAllNodes());
        installationSet.removeAll(skipInstallation);
        Command installCommand = Commands.getInstallCommand(installationSet);
        manager.getCommandRunner().runCommand(installCommand);

        if(installCommand.hasSucceeded()) {
            po.addLog("Installation succeeded");

            configureAsCoordinator(config.getCoordinatorNode());
            configureAsWorker(config.getWorkers(), config.getCoordinatorNode());
            startNodes(config.getAllNodes());

        } else throw new ClusterSetupException("Installation failed: "
                    + installCommand.getAllErrors());
    }
}
