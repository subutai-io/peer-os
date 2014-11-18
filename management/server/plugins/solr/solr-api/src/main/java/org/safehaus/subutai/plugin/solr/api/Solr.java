/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.solr.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;


/**
 * @author dilshat
 */
public interface Solr extends ApiBase<SolrClusterConfig>
{

    public UUID startNode( String clusterName, String lxcHostname );

    public UUID stopNode( String clusterName, String lxcHostname );

    public UUID checkNode( String clusterName, String lxcHostname );

    public UUID addNode( String clusterName, String lxcHostname );

    public UUID destroyNode( String clusterName, String lxcHostname );

    public UUID uninstallCluster( SolrClusterConfig config );

    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment, final SolrClusterConfig config,
                                                         final TrackerOperation po );

    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( SolrClusterConfig config );

    UUID configureEnvironmentCluster( SolrClusterConfig config );
}
