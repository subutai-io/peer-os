package org.safehaus.subutai.plugin.sqoop.impl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.sqoop.api.SetupType;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.plugin.sqoop.api.setting.ExportSetting;
import org.safehaus.subutai.plugin.sqoop.api.setting.ImportSetting;
import org.safehaus.subutai.plugin.sqoop.impl.handler.*;

public class SqoopImpl extends SqoopBase {

    @Override
    public UUID installCluster(SqoopConfig config) {
        ProductOperation po = tracker.createProductOperation(SqoopConfig.PRODUCT_KEY,
                "Install Sqoop on " + config.getClusterName());
        InstallHandler h = new InstallHandler(this, config.getClusterName(), po);
        h.setConfig(config);
        executor.execute(h);
        return po.getId();
    }

    @Override
    public UUID installCluster(SqoopConfig config, HadoopClusterConfig hadoopConfig) {
        ProductOperation po = tracker.createProductOperation(SqoopConfig.PRODUCT_KEY,
                "Install Sqoop with Hadoop: " + config.getClusterName());
        InstallHandler h = new InstallHandler(this, config.getClusterName(), po);
        h.setConfig(config);
        h.setHadoopConfig(hadoopConfig);
        executor.execute(h);
        return h.getTrackerId();
    }

    @Override
    public UUID uninstallCluster(String clusterName) {
        return null;
    }

    @Override
    public List<SqoopConfig> getClusters() {
        try {
            return pluginDao.getInfo(SqoopConfig.PRODUCT_KEY, SqoopConfig.class);
        } catch(DBException ex) {
            logger.error("Failed to get installation infos", ex);
        }
        return Collections.emptyList();
    }

    @Override
    public SqoopConfig getCluster(String clusterName) {
        try {
            return pluginDao.getInfo(SqoopConfig.PRODUCT_KEY, clusterName, SqoopConfig.class);
        } catch(DBException ex) {
            logger.error("Failed to get installation info for ", clusterName, ex);
        }
        return null;
    }

    @Override
    public UUID isInstalled(String clusterName, String hostname) {
        ProductOperation po = tracker.createProductOperation(SqoopConfig.PRODUCT_KEY,
                "Check Sqoop package on " + hostname);
        CheckHandler h = new CheckHandler(this, clusterName, po);
        h.setHostname(hostname);
        executor.execute(h);
        return po.getId();
    }

    @Override
    public UUID destroyNode(String clusterName, String hostname) {
        ProductOperation po = tracker.createProductOperation(SqoopConfig.PRODUCT_KEY,
                "Destroy node " + hostname);
        DestroyNodeHandler h = new DestroyNodeHandler(this, clusterName, po);
        h.setHostname(hostname);
        executor.execute(h);
        return po.getId();
    }

    @Override
    public UUID exportData(ExportSetting settings) {
        ProductOperation po = tracker.createProductOperation(SqoopConfig.PRODUCT_KEY,
                "Export data. Node: " + settings.getHostname());
        ExportHandler h = new ExportHandler(this, settings.getClusterName(), po);
        h.setSettings(settings);
        executor.execute(h);
        return po.getId();
    }

    @Override
    public UUID importData(ImportSetting settings) {
        ProductOperation po = tracker.createProductOperation(SqoopConfig.PRODUCT_KEY,
                "Import data. Node: " + settings.getHostname());
        ImportHandler h = new ImportHandler(this, settings.getClusterName(), po);
        h.setSettings(settings);
        executor.execute(h);
        return po.getId();
    }

    @Override
    public ClusterSetupStrategy getClusterSetupStrategy(Environment env, SqoopConfig config, ProductOperation po) {
        if(config.getSetupType() == SetupType.OVER_HADOOP)
            return new SetupStrategyOverHadoop(this, config, po);
        else if(config.getSetupType() == SetupType.WITH_HADOOP) {
            SetupStrategyWithHadoop s = new SetupStrategyWithHadoop(this, config, po);
            s.setEnvironment(env);
            return s;
        }
        return null;
    }

}
