package org.safehaus.subutai.plugin.cassandra.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;


/**
 * @author dilshat
 */
public interface Cassandra extends ApiBase<CassandraConfig> {

    UUID startCluster( String clusterName );

    UUID checkCluster( String clusterName );

    UUID stopCluster( String clusterName );

    UUID startService( String clusterName, String agentUUID );

    UUID stopService( String clusterName, String agentUUID );

    UUID statusService( String clusterName, String agentUUID );

    UUID addNode( String clusterName, String lxchostname, String nodetype );

    UUID destroyNode( String clusterName, String lxchostname, String nodetype );

    UUID checkNode( String clustername, String lxchostname );

    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment, CassandraConfig config,
                                                         ProductOperation po );

    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( CassandraConfig config );
}