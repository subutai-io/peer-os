package org.safehaus.subutai.plugin.hive.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.*;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;

class SetupStrategyOverHadoop extends HiveSetupStrategy {

    public SetupStrategyOverHadoop(HiveImpl manager, HiveConfig config, ProductOperation po) {
        super(manager, config, po);
    }

    @Override
    public ConfigBase setup() throws ClusterSetupException {

        checkConfig();

        //check if nodes are connected
        String serverHostname = config.getServer().getHostname();
        if(manager.agentManager.getAgentByHostname(serverHostname) == null)
            throw new ClusterSetupException("Server node is not connected ");
        for(Agent a : config.getClients()) {
            if(manager.agentManager.getAgentByHostname(a.getHostname()) == null)
                throw new ClusterSetupException(String.format(
                        "Node %s is not connected", a.getHostname()));
        }

        HadoopClusterConfig hc = manager.hadoopManager.getCluster(config.getHadoopClusterName());
        if(hc == null)
            throw new ClusterSetupException("Could not find Hadoop cluster "
                    + config.getHadoopClusterName());

        Set<Agent> allNodes = new HashSet<>(config.getClients());
        allNodes.add(config.getServer());

        if(!hc.getAllNodes().containsAll(allNodes))
            throw new ClusterSetupException("Not all nodes belong to Hadoop cluster "
                    + config.getHadoopClusterName());
        config.setHadoopNodes(new HashSet<>(hc.getAllNodes()));

        // check if already installed
        String s = Commands.make(CommandType.LIST, Product.HIVE);
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s), allNodes);
        manager.getCommandRunner().runCommand(cmd);

        if(!cmd.hasCompleted()) {
            String m = "Failed to check installed packages";
            po.addLog(m);
            throw new ClusterSetupException(m + ": " + cmd.getAllErrors());
        }

        String hadoop_pack = Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME;
        for(Agent a : allNodes) {
            AgentResult res = cmd.getResults().get(a.getUuid());

            if(res.getStdOut().contains(Product.HIVE.getPackageName()))
                throw new ClusterSetupException(String.format(
                        "Node %s already has Hive installed",
                        a.getHostname()));
            else if(!res.getStdOut().contains(hadoop_pack))
                throw new ClusterSetupException(String.format(
                        "Node %s has no Hadoop installation.",
                        a.getHostname()));
            else if(a.equals(config.getServer()))
                if(res.getStdOut().contains(Product.DERBY.getPackageName()))
                    throw new ClusterSetupException("Server node already has Derby installed");
        }

        // installation of server
        po.addLog("Installing server...");
        for(Product p : new Product[]{Product.HIVE, Product.DERBY}) {
            s = Commands.make(CommandType.INSTALL, p);
            cmd = manager.commandRunner.createCommand(
                    new RequestBuilder(s).withTimeout(120),
                    new HashSet<>(Arrays.asList(config.getServer())));
            manager.commandRunner.runCommand(cmd);
            if(!cmd.hasSucceeded())
                throw new ClusterSetupException(String.format(
                        "Failed to install %s on server node", p.toString()));
        }
        po.addLog("Server installation completed");

        // configure Hive server
        configureServer();

        po.addLog("Installing clients...");
        s = Commands.make(CommandType.INSTALL, Product.HIVE);
        cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s).withTimeout(120), config.getClients());
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasSucceeded()) {
            po.addLog("Installation completed");
            configureClients();

            po.addLog("Saving to db...");
            try {
                manager.getPluginDao().saveInfo(HiveConfig.PRODUCT_KEY,
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
