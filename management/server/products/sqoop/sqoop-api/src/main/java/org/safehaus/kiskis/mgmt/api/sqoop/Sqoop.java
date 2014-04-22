package org.safehaus.kiskis.mgmt.api.sqoop;

import java.util.List;
import java.util.UUID;

public interface Sqoop {

    public UUID installCluster(Config config);

    public UUID uninstallCluster(String clusterName);

    public UUID statusCheck(String clusterName, String hostname);

    /**
     * Returns list of configurations of installed clusters
     *
     * @return - list of configurations of installed clusters
     *
     */
    public List<Config> getClusters();
}
