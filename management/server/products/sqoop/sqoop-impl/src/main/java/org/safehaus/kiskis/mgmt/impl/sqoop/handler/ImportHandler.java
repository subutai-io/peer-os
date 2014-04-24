package org.safehaus.kiskis.mgmt.impl.sqoop.handler;

import org.safehaus.kiskis.mgmt.api.sqoop.setting.ImportSetting;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.sqoop.SqoopImpl;

public class ImportHandler extends AbstractHandler {

    private ImportSetting settings;

    public ImportHandler(SqoopImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    public ImportSetting getSettings() {
        return settings;
    }

    public void setSettings(ImportSetting settings) {
        this.settings = settings;
    }

    public void run() {
        // TODO:
    }

}
