package org.safehaus.kiskis.mgmt.impl.sqoop.handler;

import org.safehaus.kiskis.mgmt.api.sqoop.setting.ExportSetting;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.sqoop.SqoopImpl;

public class ExportHandler extends AbstractHandler {

    private ExportSetting settings;

    public ExportHandler(SqoopImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    public ExportSetting getSettings() {
        return settings;
    }

    public void setSettings(ExportSetting settings) {
        this.settings = settings;
    }

    public void run() {
        // TODO:
    }

}
