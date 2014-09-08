/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.oozie.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;


/**
 * @author dilshat
 */
public interface Oozie extends ApiBase<OozieClusterConfig> {

    UUID startServer( OozieClusterConfig config );

    UUID stopServer( OozieClusterConfig config );

    UUID checkServerStatus( OozieClusterConfig config );

    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment, OozieClusterConfig config,
                                                         ProductOperation po );

    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( OozieClusterConfig config );

    UUID addNode( String clustername, String lxchostname, String nodetype );

    UUID destroyNode( String clustername, String lxchostname, String nodetype );
}
