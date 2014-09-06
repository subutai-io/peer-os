package org.safehaus.subutai.plugin.spark.impl;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.spark.api.SetupType;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.handler.*;

public class SparkImpl extends SparkBase implements Spark {

    @Override
    public UUID installCluster(final SparkClusterConfig config) {

        Preconditions.checkNotNull(config, "Configuration is null");

        AbstractOperationHandler operationHandler = new InstallOperationHandler(this, config);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    @Override
    public UUID uninstallCluster(final String clusterName) {

        AbstractOperationHandler operationHandler = new UninstallOperationHandler(this, clusterName);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    @Override
    public List<SparkClusterConfig> getClusters() {
        try {
            return pluginDAO.getInfo(SparkClusterConfig.PRODUCT_KEY, SparkClusterConfig.class);
        } catch(DBException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public SparkClusterConfig getCluster(String clusterName) {
        try {
            return pluginDAO.getInfo(SparkClusterConfig.PRODUCT_KEY, clusterName, SparkClusterConfig.class);
        } catch(DBException e) {
            return null;
        }
    }

    @Override
    public UUID addSlaveNode(final String clusterName, final String lxcHostname) {

        AbstractOperationHandler operationHandler = new AddSlaveNodeOperationHandler(this, clusterName, lxcHostname);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    @Override
    public UUID destroySlaveNode(final String clusterName, final String lxcHostname) {

        AbstractOperationHandler operationHandler
                = new DestroySlaveNodeOperationHandler(this, clusterName, lxcHostname);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    @Override
    public UUID changeMasterNode(final String clusterName, final String newMasterHostname, final boolean keepSlave) {

        AbstractOperationHandler operationHandler
                = new ChangeMasterNodeOperationHandler(this, clusterName, newMasterHostname, keepSlave);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    @Override
    public UUID startNode(final String clusterName, final String lxcHostname, final boolean master) {

        AbstractOperationHandler operationHandler
                = new StartNodeOperationHandler(this, clusterName, lxcHostname, master);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    @Override
    public UUID stopNode(final String clusterName, final String lxcHostname, final boolean master) {

        AbstractOperationHandler operationHandler
                = new StopNodeOperationHandler(this, clusterName, lxcHostname, master);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    @Override
    public UUID checkNode(final String clusterName, final String lxcHostname) {

        AbstractOperationHandler operationHandler = new CheckNodeOperationHandler(this, clusterName, lxcHostname);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    @Override
    public ClusterSetupStrategy getClusterSetupStrategy(final ProductOperation po,
            final SparkClusterConfig config,
            final Environment environment) {

        Preconditions.checkNotNull(po, "Product operation is null");
        Preconditions.checkNotNull(config, "Spark cluster config is null");

        if(config.getSetupType() == SetupType.OVER_HADOOP)
            return new SetupStrategyOverHadoop(po, this, config);
        else if(config.getSetupType() == SetupType.WITH_HADOOP) {
            SetupStrategyWithHadoop s = new SetupStrategyWithHadoop(po, this, config);
            s.setEnvironment(environment);
            return s;
        }

        return null;
    }
}
