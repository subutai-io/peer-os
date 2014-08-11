/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.elasticsearch2;

import org.safehaus.subutai.shared.protocol.ApiBase;

import java.util.UUID;

/**
 * @author dilshat
 */
public interface Cassandra extends ApiBase<Config> {

    UUID startAllNodes(String clusterName);

    UUID checkAllNodes(String clusterName);

    UUID stopAllNodes(String clusterName);

    UUID startCassandraService(String agentUUID);

    UUID stopCassandraService(String agentUUID);

    UUID statusCassandraService(String agentUUID);
}
