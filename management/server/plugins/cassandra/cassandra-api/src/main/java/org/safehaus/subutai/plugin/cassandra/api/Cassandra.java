package org.safehaus.subutai.plugin.cassandra.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;


public interface Cassandra extends ApiBase<CassandraClusterConfig>
{

    UUID startCluster( String clusterName );

    UUID checkCluster( String clusterName );

    UUID stopCluster( String clusterName );

    UUID startService( String clusterName, String hostname );

    UUID stopService( String clusterName, String hostname );

    UUID statusService( String clusterName, String hostname );

    UUID addNode( String clusterName, String nodetype );

    UUID destroyNode( String clusterName, UUID containerId );

    UUID checkNode( String clusterName, String hostname );

    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment, CassandraClusterConfig config,
                                                         TrackerOperation po );

    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( CassandraClusterConfig config );

    UUID configureEnvironmentCluster( CassandraClusterConfig config );
}