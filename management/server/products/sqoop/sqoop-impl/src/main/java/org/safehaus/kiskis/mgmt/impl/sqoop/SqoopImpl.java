package org.safehaus.kiskis.mgmt.impl.sqoop;

import org.safehaus.kiskis.mgmt.api.sqoop.Config;
import org.safehaus.kiskis.mgmt.api.sqoop.setting.ExportSetting;
import org.safehaus.kiskis.mgmt.api.sqoop.setting.ImportSetting;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.sqoop.handler.*;

import java.util.List;
import java.util.UUID;

public class SqoopImpl extends SqoopBase {

    public UUID installCluster(Config config) {
        ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
                "Install Sqoop cluster " + config.getClusterName());
        InstallHandler h = new InstallHandler(this, config.getClusterName(), po);
        h.setConfig(config);
        executor.execute(h);
        return po.getId();
    }

    public UUID uninstallCluster(String clusterName) {
        ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
                "Remove Sqoop cluster " + clusterName);
        RemoveHandler h = new RemoveHandler(this, clusterName, po);
        executor.execute(h);
        return po.getId();
    }

    public UUID isInstalled(String clusterName, String hostname) {
        ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
                "Check Sqoop package on " + hostname);
        CheckHandler h = new CheckHandler(this, clusterName, po);
        h.setHostname(hostname);
        executor.execute(h);
        return po.getId();
    }

    public UUID addNode(String clusterName, String hostname) {
        ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
                "Add new node " + hostname);
        AddNodeHandler h = new AddNodeHandler(this, clusterName, po);
        h.setHostname(hostname);
        executor.execute(h);
        return po.getId();
    }

    public UUID destroyNode(String clusterName, String hostname) {
        ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
                "Destroy node " + hostname);
        DestroyNodeHandler h = new DestroyNodeHandler(this, clusterName, po);
        h.setHostname(hostname);
        executor.execute(h);
        return po.getId();
    }

    public List<Config> getClusters() {
        return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
    }

    @Override
    public Config getCluster(String clusterName) {
        return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
    }

    public UUID exportData(ExportSetting settings) {
        ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
                "Export data. Node: " + settings.getHostname());
        ExportHandler h = new ExportHandler(this, settings.getClusterName(), po);
        h.setSettings(settings);
        executor.execute(h);
        return po.getId();
    }

    public UUID importData(ImportSetting settings) {
        ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
                "Import data. Node: " + settings.getHostname());
        ImportHandler h = new ImportHandler(this, settings.getClusterName(), po);
        h.setSettings(settings);
        executor.execute(h);
        return po.getId();
    }

}
