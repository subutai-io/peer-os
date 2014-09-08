/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.cassandra;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;


/**
 * @author dilshat
 */
public interface Cassandra extends ApiBase<Config> {

    UUID startAllNodes( String clusterName );

    UUID checkAllNodes( String clusterName );

    UUID stopAllNodes( String clusterName );

    UUID startCassandraService( String agentUUID );

    UUID stopCassandraService( String agentUUID );

    UUID statusCassandraService( String agentUUID );
}
