package org.safehaus.subutai.plugin.flume.impl;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ConfigBase;
import org.safehaus.subutai.shared.protocol.settings.Common;

class OverHadoopSetupStrategy extends FlumeSetupStrategy {

    public OverHadoopSetupStrategy(FlumeImpl manager, FlumeConfig config, ProductOperation po) {
        super(manager, config, po);
    }

    @Override
    public ConfigBase setup() throws ClusterSetupException {

        checkConfig();

        //check if node agents are connected
        for(Agent a : config.getNodes()) {
            if(manager.agentManager.getAgentByHostname(a.getHostname()) == null)
                throw new ClusterSetupException(String.format(
                        "Node %s is not connected", a.getHostname()));
        }

        HadoopClusterConfig hc = manager.hadoopManager.getCluster(config.getHadoopClusterName());
        if(hc == null)
            throw new ClusterSetupException("Could not find Hadoop cluster "
                    + config.getHadoopClusterName());

        if(!hc.getAllNodes().containsAll(config.getNodes()))
            throw new ClusterSetupException("Not all nodes belong to Hadoop cluster "
                    + config.getClusterName());

        po.addLog("Checking installed packages...");
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.STATUS)),
                config.getNodes());
        manager.getCommandRunner().runCommand(cmd);
        if(!cmd.hasSucceeded())
            throw new ClusterSetupException("Failed to check installed packages");

        String hadoop_pack = Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME;
        for(Agent a : config.getNodes()) {
            AgentResult result = cmd.getResults().get(a.getUuid());

            if(result.getStdOut().contains(Commands.PACKAGE_NAME))
                throw new ClusterSetupException(String.format(
                        "Node %s already has Flume installed.",
                        a.getHostname()));
            else if(!result.getStdOut().contains(hadoop_pack))
                throw new ClusterSetupException(String.format(
                        "Node %s has no Hadoop installation.",
                        a.getHostname()));
        }

        po.addLog("Installing Flume...");
        String s = Commands.make(CommandType.INSTALL);
        cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s).withTimeout(90),
                config.getNodes());
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasSucceeded()) {
            po.addLog("Installation succeeded");
            po.addLog("Saving to db...");
            try {
                manager.getPluginDao().saveInfo(FlumeConfig.PRODUCT_KEY,
                        config.getClusterName(), config);
                po.addLog("Cluster info successfully saved");
            } catch(DBException ex) {
                throw new ClusterSetupException("Failed to save cluster info: " + ex.getMessage());
            }
        } else
            throw new ClusterSetupException("Installation failed: " + cmd.getAllErrors());

        return config;
    }

}
