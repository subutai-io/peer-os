package org.safehaus.subutai.plugin.spark.impl;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.Node;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

public class SetupStrategyWithHadoop extends SetupBase implements ClusterSetupStrategy {

    private Environment environment;

    public SetupStrategyWithHadoop(ProductOperation po, SparkImpl manager, SparkClusterConfig config) {
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

        config.getSlaveNodes().clear();
        for(Node n : environment.getNodes()) {
            if(n.getTemplate().getProducts().contains(Commands.PACKAGE_NAME))
                if(config.getMasterNode() == null)
                    config.setMasterNode(n.getAgent());
                else
                    config.getSlaveNodes().add(n.getAgent());
        }
        if(config.getMasterNode() == null)
            throw new ClusterSetupException("Environment has no master node");
        if(config.getSlaveNodes().isEmpty())
            throw new ClusterSetupException("Environment has no Spark nodes");

        checkConnected();

        // TODO: do configuration of nodes
        // TODO: start nodes
        //
        po.addLog("Saving cluster info...");
        try {
            manager.getPluginDAO().saveInfo(SparkClusterConfig.PRODUCT_KEY,
                    config.getClusterName(), config);
            po.addLog("Cluster info saved to DB");
        } catch(DBException e) {
            throw new ClusterSetupException("Failed to save cluster info: "
                    + e.getMessage());
        }
        return config;
    }

}
