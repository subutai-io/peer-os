/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.accumulo.api;


import java.util.UUID;

import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.protocol.ApiBase;


/**
 * @author dilshat
 */
public interface Accumulo extends ApiBase<AccumuloClusterConfig> {

    public UUID installCluster( ZookeeperClusterConfig config, HadoopClusterConfig hadoopClusterConfig,
                                AccumuloClusterConfig accumuloClusterConfig );

    public UUID startCluster( String clusterName );

    public UUID stopCluster( String clusterName );

    public UUID checkNode( String clusterName, String lxcHostname );

    public UUID addNode( String clusterName, String lxcHostname, NodeType nodeType );

    public UUID destroyNode( String clusterName, String lxcHostname, NodeType nodeType );

    public UUID addProperty( String clusterName, String propertyName, String propertyValue );

    public UUID removeProperty( String clusterName, String propertyName );
}
