package org.safehaus.subutai.plugin.flume.impl;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.Node;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.*;

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
    public ConfigBase setup() throws ClusterSetupException
    {

        checkConfig();

        if(environment == null)
            throw new ClusterSetupException("Environment not specified");

        if(environment.getNodes() == null || environment.getNodes().isEmpty())
            throw new ClusterSetupException("Environment has no nodes");

        Set<Agent> flumeNodes = new HashSet<>(), allNodes = new HashSet<>();
        for(Node n : environment.getNodes()) {
            allNodes.add(n.getAgent());
            if(n.getTemplate().getProducts().contains(Commands.PACKAGE_NAME))
                flumeNodes.add(n.getAgent());
        }
        if(flumeNodes.isEmpty())
            throw new ClusterSetupException("Environment has no nodes with Flume installed");

        config.setNodes(flumeNodes);
        config.setHadoopNodes(allNodes);

        for(Agent a : config.getNodes()) {
            if(manager.getAgentManager().getAgentByHostname(a.getHostname()) == null)
                throw new ClusterSetupException("Node is not connected: " + a.getHostname());
        }

        po.addLog("Saving to db...");
        manager.getPluginDao().saveInfo(FlumeConfig.PRODUCT_KEY,
                config.getClusterName(), config);
        po.addLog("Cluster info successfully saved");

        return config;
    }

}
