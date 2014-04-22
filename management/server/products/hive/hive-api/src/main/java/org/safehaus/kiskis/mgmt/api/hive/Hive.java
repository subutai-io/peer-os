package org.safehaus.kiskis.mgmt.api.hive;

import java.util.List;
import java.util.UUID;

public interface Hive {

    public UUID installCluster(Config config);

    public UUID uninstallCluster(String clusterName);

    public UUID statusCheck(String clusterName, String hostname);

    public UUID startNode(String clusterName, String hostname);

    public UUID stopNode(String clusterName, String hostname);

    public UUID restartNode(String clusterName, String hostname);

    public UUID addNode(String clusterName, String hostname);

    public UUID destroyNode(String clusterName, String hostname);

    /**
     * Returns list of configurations of installed clusters
     *
     * @return - list of configurations of installed clusters
     *
     */
    public List<Config> getClusters();
}
