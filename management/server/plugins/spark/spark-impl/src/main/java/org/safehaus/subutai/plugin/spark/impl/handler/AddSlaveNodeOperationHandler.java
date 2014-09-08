package org.safehaus.subutai.plugin.spark.impl.handler;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.*;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.*;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.spark.api.SetupType;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.*;

public class AddSlaveNodeOperationHandler extends AbstractOperationHandler<SparkImpl> {

    private final String hostname;

    public AddSlaveNodeOperationHandler(SparkImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.hostname = hostname;
        productOperation = manager.getTracker().createProductOperation(SparkClusterConfig.PRODUCT_KEY,
                String.format("Adding node %s to %s", (hostname != null ? hostname : ""), clusterName));
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
            po.addLogFailed(String.format("Master node %s is not connected\nOperation aborted",
                    config.getMasterNode().getHostname()));
            return;
        }

        try {
            Agent agent;
            if(config.getSetupType() == SetupType.OVER_HADOOP)
                agent = setupHost(config);
            else if(config.getSetupType() == SetupType.WITH_HADOOP)
                agent = addHost(config);
            else
                throw new ClusterSetupException("No setup type");

            config.getSlaveNodes().add(agent);

            po.addLog("Updating db...");
            manager.getPluginDAO().saveInfo(SparkClusterConfig.PRODUCT_KEY,
                    config.getClusterName(), config);
            po.addLog("Cluster info updated in DB");

        } catch(ClusterSetupException ex) {
            po.addLogFailed("Failede to add node: " + ex.getMessage());
        } catch(LxcCreateException ex) {
            po.addLogFailed("Failede to add node: " + ex.getMessage());
        } catch(DBException ex) {
            po.addLogFailed("Failed to save info: " + ex.getMessage());
        }

    }

    private Agent setupHost(SparkClusterConfig config) throws ClusterSetupException {
        ProductOperation po = productOperation;

        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null)
            throw new ClusterSetupException("Node is not connected");

        //check if node is in the cluster
        if(config.getSlaveNodes().contains(agent))
            throw new ClusterSetupException("Node already belongs to this cluster");

        po.addLog("Checking prerequisites...");

        boolean install = !agent.equals(config.getMasterNode());

        //check installed packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand(Sets.newHashSet(agent));
        manager.getCommandRunner().runCommand(checkInstalledCommand);

        if(!checkInstalledCommand.hasCompleted())
            throw new ClusterSetupException("Failed to check installed packages\nOperation aborted");

        AgentResult result = checkInstalledCommand.getResults().get(agent.getUuid());

        if(result.getStdOut().contains(Commands.PACKAGE_NAME) && install)
            throw new ClusterSetupException("Node already has Spark installed\nOperation aborted");
        else if(!result.getStdOut().contains("ksks-hadoop"))
            throw new ClusterSetupException("Node has no Hadoop installation\nOperation aborted");

        if(install) {
            po.addLog("Installing Spark...");
            Command installCommand = Commands.getInstallCommand(Sets.newHashSet(agent));
            manager.getCommandRunner().runCommand(installCommand);

            if(installCommand.hasSucceeded())
                po.addLog("Installation succeeded");
            else
                throw new ClusterSetupException("Installation failed: " + installCommand.getAllErrors());
        }

        SetupHelper helper = new SetupHelper(manager, config, po);
        helper.configureMasterIP(Sets.newHashSet(agent));

        registerSlave(agent, config);
        restartMaster(config);
        startSlave(agent);

        return agent;
    }

    private Agent addHost(SparkClusterConfig config) throws ClusterSetupException, LxcCreateException {

        Set<Agent> set = manager.getContainerManager().clone(
                SparkClusterConfig.TEMPLATE_NAME, 1, null);
        if(set.isEmpty())
            throw new ClusterSetupException("Failed to create container");
        if(set.size() != 1)
            throw new ClusterSetupException("Inconsistent state: cloned more than one container");

        SetupHelper sh = new SetupHelper(manager, config, productOperation);
        sh.configureMasterIP(set);

        Agent agent = set.iterator().next();

        registerSlave(agent, config);
        restartMaster(config);
        startSlave(agent);

        return agent;
    }

    private void registerSlave(Agent agent, SparkClusterConfig config) throws ClusterSetupException {
        productOperation.addLog("Registering slave with master...");

        Command cmd = Commands.getAddSlaveCommand(agent, config.getMasterNode());
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasSucceeded())
            productOperation.addLog("Registration succeeded");
        else
            throw new ClusterSetupException("Registration failed:" + cmd.getAllErrors());
    }

    private void restartMaster(SparkClusterConfig config) throws ClusterSetupException {
        productOperation.addLog("Restarting master...");

        Command cmd = Commands.getRestartMasterCommand(config.getMasterNode());
        final AtomicBoolean ok = new AtomicBoolean();
        manager.getCommandRunner().runCommand(cmd, new CommandCallback() {

            @Override
            public void onResponse(Response response, AgentResult agentResult, Command command) {
                if(agentResult.getStdOut().contains("starting")) {
                    ok.set(true);
                    stop();
                }
            }
        });

        if(ok.get())
            productOperation.addLog("Master restarted successfully");
        else
            throw new ClusterSetupException("Master restart failed: " + cmd.getAllErrors());
    }

    private void startSlave(Agent agent) throws ClusterSetupException {
        productOperation.addLog("Starting Spark on new node...");

        final AtomicBoolean ok = new AtomicBoolean();
        Command startSlaveCommand = Commands.getStartSlaveCommand(agent);
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
            productOperation.addLog("Spark started successfully\nDone");
        else
            throw new ClusterSetupException("Failed to start Spark: " + startSlaveCommand.getAllErrors());

    }
}
