/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.cassandra;


import java.util.UUID;

import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ApiBase;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.EnvironmentBlueprint;


/**
 * @author dilshat
 */
public interface Cassandra extends ApiBase<CassandraConfig> {

    UUID startAllNodes( String clusterName );

    UUID checkAllNodes( String clusterName );

    UUID stopAllNodes( String clusterName );

    UUID startCassandraService( String agentUUID );

    UUID stopCassandraService( String agentUUID );

    UUID statusCassandraService( String agentUUID );

    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment, CassandraConfig config,
                                                         ProductOperation po );

    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( CassandraConfig config );
}
