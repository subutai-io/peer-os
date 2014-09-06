package org.safehaus.subutai.plugin.presto.impl.handler;

import com.google.common.collect.Sets;
import java.util.Set;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.*;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.*;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.api.SetupType;
import org.safehaus.subutai.plugin.presto.impl.Commands;
import org.safehaus.subutai.plugin.presto.impl.PrestoImpl;
import org.safehaus.subutai.plugin.presto.impl.SetupHelper;

public class AddWorkerNodeOperationHandler extends AbstractOperationHandler<PrestoImpl> {

    private final String hostname;

    public AddWorkerNodeOperationHandler(PrestoImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.hostname = hostname;
        productOperation = manager.getTracker().createProductOperation(
                PrestoClusterConfig.PRODUCT_KEY,
                String.format("Adding node %s to %s", hostname, clusterName));
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        PrestoClusterConfig config = manager.getCluster(clusterName);
        if(config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
            return;
        }

        if(manager.getAgentManager().getAgentByHostname(config.getCoordinatorNode().getHostname()) == null) {
            po.addLogFailed(String.format("Coordinator node %s is not connected",
                    config.getCoordinatorNode().getHostname()));
            return;
        }

        try {
            if(config.getSetupType() == SetupType.OVER_HADOOP)
                setupHost(config);
            else if(config.getSetupType() == SetupType.WITH_HADOOP)
                addHost(config);
            else throw new ClusterSetupException("No setup type");

            po.addLogDone(null);

        } catch(ClusterSetupException ex) {
            po.addLog(ex.getMessage());
            po.addLogFailed("Add worker node failed");
        } catch(LxcCreateException ex) {
            po.addLog(ex.getMessage());
            po.addLogFailed("Add worker node failed");
        }
    }

    void setupHost(PrestoClusterConfig config) throws ClusterSetupException {
        ProductOperation po = productOperation;

        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null)
            throw new ClusterSetupException("New node is not connected");

        //check if node is in the cluster
        if(config.getWorkers().contains(agent))
            throw new ClusterSetupException("Node already belongs to cluster" + clusterName);

        po.addLog("Checking prerequisites...");

        //check installed ksks packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand(Sets.newHashSet(agent));
        manager.getCommandRunner().runCommand(checkInstalledCommand);

        if(!checkInstalledCommand.hasCompleted())
            throw new ClusterSetupException("Failed to check installed packages");

        AgentResult result = checkInstalledCommand.getResults().get(agent.getUuid());
        boolean skipInstall = false;
        String hadoopPack = Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME;
        if(result.getStdOut().contains(Commands.PACKAGE_NAME)) {
            skipInstall = true;
            po.addLog("Node already has Presto installed");
        } else if(!result.getStdOut().contains(hadoopPack))
            throw new ClusterSetupException("Node has no Hadoop installation");

        config.getWorkers().add(agent);
        po.addLog("Updating db...");
        //save to db
        try {
            manager.getPluginDAO().saveInfo(PrestoClusterConfig.PRODUCT_KEY,
                    config.getClusterName(), config);
            po.addLog("Cluster info updated in DB");
        } catch(DBException e) {
            throw new ClusterSetupException("Could not update cluster info in DB: " + e.getMessage());
        }

        //install presto
        if(!skipInstall) {
            po.addLog("Installing Presto...");
            Command installCommand = Commands.getInstallCommand(Sets.newHashSet(agent));
            manager.getCommandRunner().runCommand(installCommand);

            if(installCommand.hasSucceeded())
                po.addLog("Installation succeeded");
            else
                throw new ClusterSetupException("Installation failed: " + installCommand.getAllErrors());
        }

        SetupHelper sh = new SetupHelper(po, manager, config);
        sh.configureAsWorker(Sets.newHashSet(agent), config.getCoordinatorNode());
        sh.startNodes(Sets.newHashSet(agent));

    }

    private void addHost(PrestoClusterConfig config)
            throws LxcCreateException, ClusterSetupException {

        Set<Agent> set = manager.getContainerManager().clone(
                PrestoClusterConfig.TEMAPLTE_NAME, 1, null);
        if(set.isEmpty())
            throw new ClusterSetupException("Failed to create container");

        Agent agent = set.iterator().next();
        SetupHelper sh = new SetupHelper(productOperation, manager, config);
        sh.configureAsWorker(Sets.newHashSet(agent), config.getCoordinatorNode());
    }
}
