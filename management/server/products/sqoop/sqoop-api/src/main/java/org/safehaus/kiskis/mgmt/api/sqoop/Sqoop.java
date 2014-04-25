package org.safehaus.kiskis.mgmt.api.sqoop;

import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.sqoop.setting.ExportSetting;
import org.safehaus.kiskis.mgmt.api.sqoop.setting.ImportSetting;

public interface Sqoop {

    public UUID installCluster(Config config);

    public UUID removeCluster(String clusterName);

    public UUID isInstalled(String clusterName, String hostname);

    public UUID addNode(String clusterName, String hostname);

    public UUID destroyNode(String clusterName, String hostname);

    /**
     * Returns list of configurations of installed clusters
     *
     * @return - list of configurations of installed clusters
     *
     */
    public List<Config> getClusters();

    public UUID exportData(ExportSetting settings);

    public UUID importData(ImportSetting settings);
}
