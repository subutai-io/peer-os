package org.safehaus.kiskis.mgmt.api.hadoop;

import java.util.List;
import java.util.UUID;

/**
 * Created by daralbaev on 02.04.14.
 */
public interface Hadoop {
    public UUID installCluster(Config config);

    public UUID uninstallCluster(String clusterName);

    public UUID createNodes(Config config);

    /**
     * Returns list of configurations of installed clusters
     *
     * @return - list of configurations of installed clusters
     */
    public List<Config> getClusters();
}
