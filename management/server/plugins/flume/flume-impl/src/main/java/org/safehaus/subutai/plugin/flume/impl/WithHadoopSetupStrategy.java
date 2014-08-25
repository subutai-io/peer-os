package org.safehaus.subutai.plugin.flume.impl;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.Node;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.*;

class WithHadoopSetupStrategy extends FlumeSetupStrategy {

    Environment environment;

    public WithHadoopSetupStrategy(FlumeImpl manager, FlumeConfig config, ProductOperation po) {
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

        if(environment.getNodes().size() < config.getNodesCount())
            throw new ClusterSetupException(String.format(
                    "Environment has %d nodes instead of %d",
                    environment.getNodes().size(), config.getNodesCount()));

        Set<Agent> flumeNodes = new HashSet<>();
        for(Node n : environment.getNodes()) {
            if(n.getTemplate().getProducts().contains(Commands.PACKAGE_NAME))
                flumeNodes.add(n.getAgent());
        }
        if(flumeNodes.size() < config.getNodesCount())
            throw new ClusterSetupException(String.format(
                    "Environment has %d nodes with Flume installed instead of %d",
                    flumeNodes.size(), config.getNodesCount()));

        config.setNodes(flumeNodes);

        for(Agent a : config.getNodes()) {
            if(manager.agentManager.getAgentByHostname(a.getHostname()) == null)
                throw new ClusterSetupException("Node is not connected: " + a.getHostname());
        }

        po.addLog("Saving to db...");
        try {
            manager.getDbManager().saveInfo2(FlumeConfig.PRODUCT_KEY,
                    config.getClusterName(), config);
            po.addLog("Cluster info successfully saved");
        } catch(DBException ex) {
            throw new ClusterSetupException("Failed to save cluster info: " + ex.getMessage());
        }

        return config;
    }

}
