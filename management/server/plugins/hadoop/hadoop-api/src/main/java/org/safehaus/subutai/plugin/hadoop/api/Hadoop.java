package org.safehaus.subutai.plugin.hadoop.api;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;


public interface Hadoop extends ApiBase<HadoopClusterConfig>
{

    public UUID uninstallCluster( HadoopClusterConfig config );

    public UUID startNameNode( HadoopClusterConfig hadoopClusterConfig );

    public UUID stopNameNode( HadoopClusterConfig hadoopClusterConfig );

    public UUID statusNameNode( HadoopClusterConfig hadoopClusterConfig );

    public UUID statusSecondaryNameNode( HadoopClusterConfig hadoopClusterConfig );

    public UUID startDataNode( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public UUID stopDataNode( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public UUID statusDataNode( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public UUID startJobTracker( HadoopClusterConfig hadoopClusterConfig );

    public UUID stopJobTracker( HadoopClusterConfig hadoopClusterConfig );

    public UUID statusJobTracker( HadoopClusterConfig hadoopClusterConfig );

    public UUID startTaskTracker( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public UUID stopTaskTracker( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public UUID statusTaskTracker( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public UUID addNode( String clusterName, int nodeCount );

    public UUID addNode( String clusterName );

    public UUID destroyNode( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public UUID checkDecomissionStatus( HadoopClusterConfig hadoopClusterConfig );

    public UUID excludeNode( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public UUID includeNode( HadoopClusterConfig hadoopClusterConfig, String hostname );

    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment,
                                                         HadoopClusterConfig hadoopClusterConfig, TrackerOperation po );

    public org.safehaus.subutai.common.protocol.EnvironmentBlueprint getDefaultEnvironmentBlueprint(
            final HadoopClusterConfig config ) throws ClusterSetupException;
}
