package org.safehaus.kiskis.mgmt.api.flume;

import java.util.List;
import java.util.UUID;

public interface Flume {

    public UUID installCluster(Config config);

    public UUID uninstallCluster(String clusterName);

    public UUID startNode(String clusterName, String lxcHostname);

    public UUID stopNode(String clusterName, String lxcHostname);

    public UUID checkNode(String clusterName, String lxcHostname);

    public UUID addNode(String clusterName, String lxcHostname);

    public UUID destroyNode(String clusterName, String lxcHostname);

    /**
     * Returns list of configurations of installed clusters
     *
     * @return - list of configurations of installed clusters
     *
     */
    public List<Config> getClusters();
}
