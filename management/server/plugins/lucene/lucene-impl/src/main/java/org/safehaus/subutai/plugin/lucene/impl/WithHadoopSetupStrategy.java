package org.safehaus.subutai.plugin.lucene.impl;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.Node;
import org.safehaus.subutai.plugin.lucene.api.Config;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ConfigBase;


class WithHadoopSetupStrategy extends LuceneSetupStrategy {

    Environment environment;

    public WithHadoopSetupStrategy( LuceneImpl manager, Config config, ProductOperation po ) {
        super(manager, config, po);
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment env) {
        this.environment = env;
    }

    @Override
    public ConfigBase setup() throws ClusterSetupException {

        checkConfig();

        if(environment == null)
            throw new ClusterSetupException("Environment not specified");

        if(environment.getNodes() == null || environment.getNodes().isEmpty())
            throw new ClusterSetupException("Environment has no nodes");

        Set<Agent> luceneNodes = new HashSet<>(), allNodes = new HashSet<>();
        for(Node n : environment.getNodes()) {
            allNodes.add(n.getAgent());
            if(n.getTemplate().getProducts().contains(Commands.PACKAGE_NAME))
                luceneNodes.add(n.getAgent());
        }
        if(luceneNodes.isEmpty())
            throw new ClusterSetupException("Environment has no nodes with Flume installed");

        config.setNodes(luceneNodes);
        config.setHadoopNodes(allNodes);

        for(Agent a : config.getNodes()) {
            if(manager.getAgentManager().getAgentByHostname(a.getHostname()) == null)
                throw new ClusterSetupException("Node is not connected: " + a.getHostname());
        }

        po.addLog("Saving to db...");
        try {
            manager.getPluginDao().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config);
            po.addLog("Cluster info successfully saved");
        } catch(DBException ex) {
            throw new ClusterSetupException("Failed to save cluster info: " + ex.getMessage());
        }

        return config;
    }

}
