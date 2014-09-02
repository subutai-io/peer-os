package org.safehaus.subutai.plugin.cassandra.api;


import java.util.UUID;

import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.ProductOperation;


/**
 * @author dilshat
 */
public interface Cassandra extends ApiBase<CassandraConfig> {

    UUID startAllNodes( String clusterName );

    UUID checkAllNodes( String clusterName );

    UUID stopAllNodes( String clusterName );

    UUID startCassandraService( String clusterName, String agentUUID );

    UUID stopCassandraService( String clusterName, String agentUUID );

    UUID statusCassandraService( String clusterName, String agentUUID );

    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment, CassandraConfig config,
                                                         ProductOperation po );

    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( CassandraConfig config );
}