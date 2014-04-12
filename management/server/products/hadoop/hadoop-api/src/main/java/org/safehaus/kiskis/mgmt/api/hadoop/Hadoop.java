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

    public boolean statusNameNode(Config config);

    public boolean statusSecondaryNameNode(Config config);

    public boolean statusDataNode(Agent agent);

    public UUID startJobTracker(Config config);

    public UUID stopJobTracker(Config config);

    public UUID restartJobTracker(Config config);

    public boolean statusJobTracker(Config config);

    public boolean statusTaskTracker(Agent agent);

    /**
     * Returns list of configurations of installed clusters
     *
     * @return - list of configurations of installed clusters
     */
    public List<Config> getClusters();
}
