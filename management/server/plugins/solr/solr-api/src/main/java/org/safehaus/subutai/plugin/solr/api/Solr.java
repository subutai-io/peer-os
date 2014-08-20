/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.solr.api;


import java.util.UUID;

import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ApiBase;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.EnvironmentBlueprint;


/**
 * @author dilshat
 */
public interface Solr extends ApiBase<SolrClusterConfig> {

    public UUID startNode( String clusterName, String lxcHostname );

    public UUID stopNode( String clusterName, String lxcHostname );

    public UUID checkNode( String clusterName, String lxcHostname );

    public UUID addNode( String clusterName );

    public UUID destroyNode( String clusterName, String lxcHostname );

    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment, final SolrClusterConfig config,
                                                         final ProductOperation po );

    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( SolrClusterConfig config );
}
