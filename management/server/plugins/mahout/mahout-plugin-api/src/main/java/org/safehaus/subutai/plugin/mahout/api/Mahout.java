/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mahout.api;


import java.util.UUID;

import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.ProductOperation;


/**
 * @author dilshat
 */
public interface Mahout extends ApiBase<MahoutConfig> {

    public UUID addNode( String clusterName, String lxcHostname );

    public UUID destroyNode( String clusterName, String lxcHostname );

    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment, MahoutConfig config,
                                                         ProductOperation po );

    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( MahoutConfig config );
}
