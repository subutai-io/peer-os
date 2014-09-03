package org.safehaus.subutai.plugin.sqoop.impl;

import java.util.List;
import java.util.UUID;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public UUID uninstallCluster(String clusterName) {
        return null;
    }

    @Override
    public List<SqoopConfig> getClusters() {
        return dbManager.getInfo(SqoopConfig.PRODUCT_KEY, SqoopConfig.class);
    }

    @Override
    public SqoopConfig getCluster(String clusterName) {
        return dbManager.getInfo(SqoopConfig.PRODUCT_KEY, clusterName, SqoopConfig.class);
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
    public UUID addNode(String clusterName, String hostname) {
        ProductOperation po = tracker.createProductOperation(SqoopConfig.PRODUCT_KEY,
                "Add new node " + hostname);
        AddNodeHandler h = new AddNodeHandler(this, clusterName, po);
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
