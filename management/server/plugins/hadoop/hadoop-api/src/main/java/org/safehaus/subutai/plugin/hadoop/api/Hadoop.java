package org.safehaus.subutai.plugin.hadoop.api;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;


public interface Hadoop extends ApiBase<HadoopClusterConfig>
{


    public UUID startNameNode( HadoopClusterConfig hadoopClusterConfig );

    public UUID stopNameNode( HadoopClusterConfig hadoopClusterConfig );

    public UUID restartNameNode( HadoopClusterConfig hadoopClusterConfig );

    public UUID statusNameNode( HadoopClusterConfig hadoopClusterConfig );

    public UUID statusSecondaryNameNode( HadoopClusterConfig hadoopClusterConfig );

    public UUID startDataNode( HadoopClusterConfig hadoopClusterConfig, Agent agent );

    public UUID stopDataNode( HadoopClusterConfig hadoopClusterConfig, Agent agent );

    public UUID statusDataNode( HadoopClusterConfig hadoopClusterConfig, Agent agent );

    public UUID startJobTracker( HadoopClusterConfig hadoopClusterConfig );

    public UUID stopJobTracker( HadoopClusterConfig hadoopClusterConfig );

    public UUID restartJobTracker( HadoopClusterConfig hadoopClusterConfig );

    public UUID statusJobTracker( HadoopClusterConfig hadoopClusterConfig );

    public UUID startTaskTracker( HadoopClusterConfig hadoopClusterConfig, Agent agent );

    public UUID stopTaskTracker( HadoopClusterConfig hadoopClusterConfig, Agent agent );

    public UUID statusTaskTracker( HadoopClusterConfig hadoopClusterConfig, Agent agent );

    public UUID addNode( String clusterName, int nodeCount );

    public UUID addNode( String clusterName );

    public UUID blockDataNode( HadoopClusterConfig hadoopClusterConfig, Agent agent );

    public UUID destroyNode( HadoopClusterConfig hadoopClusterConfig, Agent agent );

    public UUID checkDecomissionStatus( HadoopClusterConfig hadoopClusterConfig );

    public UUID excludeNode( HadoopClusterConfig hadoopClusterConfig, Agent agent );

    public UUID includeNode( HadoopClusterConfig hadoopClusterConfig, Agent agent );

    public UUID blockTaskTracker( HadoopClusterConfig hadoopClusterConfig, Agent agent );

    public UUID unblockDataNode( HadoopClusterConfig hadoopClusterConfig, Agent agent );

    public UUID unblockTaskTracker( HadoopClusterConfig hadoopClusterConfig, Agent agent );

    public ClusterSetupStrategy getClusterSetupStrategy( TrackerOperation po, HadoopClusterConfig hadoopClusterConfig );

    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment,
                                                         HadoopClusterConfig hadoopClusterConfig, TrackerOperation po );

    public org.safehaus.subutai.common.protocol.EnvironmentBlueprint getDefaultEnvironmentBlueprint(
            final HadoopClusterConfig config ) throws ClusterSetupException;
}
