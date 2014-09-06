package org.safehaus.subutai.plugin.presto.impl;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.*;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.Node;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;

public class SetupStrategyWithHadoop extends SetupHelper implements ClusterSetupStrategy {

    private Environment environment;

    public SetupStrategyWithHadoop(ProductOperation po, PrestoImpl manager, PrestoClusterConfig config) {
        super(po, manager, config);
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public ConfigBase setup() throws ClusterSetupException {

        if(manager.getCluster(config.getClusterName()) != null)
            throw new ClusterSetupException("Cluster already exists: " + config.getClusterName());

        if(environment == null)
            throw new ClusterSetupException("Environment not specified");

        if(environment.getNodes() == null || environment.getNodes().isEmpty())
            throw new ClusterSetupException("Environment has no nodes");

        config.getWorkers().clear();
        for(Node n : environment.getNodes()) {
            if(n.getTemplate().getProducts().contains(Commands.PACKAGE_NAME))
                if(config.getCoordinatorNode() == null)
                    config.setCoordinatorNode(n.getAgent());
                else
                    config.getWorkers().add(n.getAgent());
        }
        if(config.getCoordinatorNode() == null)
            throw new ClusterSetupException("Environment has no coordinator node");
        if(config.getWorkers().isEmpty())
            throw new ClusterSetupException("Environment has no Presto nodes");

        checkConnected();
        configureAsCoordinator(config.getCoordinatorNode());
        configureAsWorker(config.getWorkers(), config.getCoordinatorNode());
        startNodes(config.getAllNodes());

        po.addLog("Saving cluster info...");
        try {
            manager.getPluginDAO().saveInfo(PrestoClusterConfig.PRODUCT_KEY,
                    config.getClusterName(), config);
            po.addLog("Cluster info saved to DB");
        } catch(DBException e) {
            throw new ClusterSetupException("Failed to save cluster info: "
                    + e.getMessage());
        }
        return config;
    }

}
