package org.safehaus.kiskis.mgmt.api.hadoop;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.List;
import java.util.UUID;

/**
 * Created by daralbaev on 02.04.14.
 */
public interface Hadoop {
    public UUID installCluster(Config config);

    public UUID uninstallCluster(String clusterName);

    public boolean startNameNode(Config config);

    public boolean stopNameNode(Config config);

    public boolean restartNameNode(Config config);

    public boolean statusNameNode(Config config);

    public boolean statusSecondaryNameNode(Config config);

    public boolean statusDataNode(Agent agent);

    public boolean startJobTracker(Config config);

    public boolean stopJobTracker(Config config);

    public boolean restartJobTracker(Config config);

    public boolean statusJobTracker(Config config);

    public boolean statusTaskTracker(Agent agent);

    /**
     * Returns list of configurations of installed clusters
     *
     * @return - list of configurations of installed clusters
     */
    public List<Config> getClusters();
}
