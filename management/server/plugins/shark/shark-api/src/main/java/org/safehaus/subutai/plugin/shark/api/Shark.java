package org.safehaus.subutai.plugin.shark.api;


import java.util.UUID;
import org.safehaus.subutai.common.protocol.ApiBase;


public interface Shark extends ApiBase<SharkClusterConfig>
{

    public UUID addNode( String clusterName, String lxcHostname );


    public UUID destroyNode( String clusterName, String lxcHostname );


    public UUID actualizeMasterIP( String clusterName );


}

