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

    public UUID startNameNode(Config config);

    public UUID stopNameNode(Config config);

    public UUID restartNameNode(Config config);

    public UUID statusNameNode(Config config);

    public UUID statusSecondaryNameNode(Config config);

    public UUID statusDataNode(Agent agent);

    public UUID startJobTracker(Config config);

    public UUID stopJobTracker(Config config);

    public UUID restartJobTracker(Config config);

    public UUID statusJobTracker(Config config);

    public UUID statusTaskTracker(Agent agent);

    public UUID addNode(String clusterName);

    public UUID blockDataNode(Config config, Agent agent);

    public UUID blockTaskTracker(Config config, Agent agent);

    public UUID unblockDataNode(Config config, Agent agent);

    public UUID unblockTaskTracker(Config config, Agent agent);

    /**
     * Returns list of configurations of installed clusters
     *
     * @return - list of configurations of installed clusters
     */
    public List<Config> getClusters();

    public Config getCluster(String clusterName);
}
