/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.shark.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;


/**
 * @author dilshat
 */
public interface Shark extends ApiBase<SharkClusterConfig>
{

    public UUID addNode( String clusterName, String lxcHostname );

    public UUID destroyNode( String clusterName, String lxcHostname );

    public UUID actualizeMasterIP( String clusterName );
}
