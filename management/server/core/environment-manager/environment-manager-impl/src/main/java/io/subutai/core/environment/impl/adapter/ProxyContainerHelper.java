package io.subutai.core.environment.impl.adapter;


import java.util.HashSet;
import java.util.Set;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.peer.api.PeerManager;


class ProxyContainerHelper
{
    private final PeerManager peerManager;


    ProxyContainerHelper( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    void setProxyToRemoteContainers( Set<ProxyEnvironmentContainer> envContainers )
    {
        Set<String> localContainerIds = getLocalContainerIds();

        Host proxyContainer = getProxyContainer( envContainers, localContainerIds );

        for ( ProxyEnvironmentContainer c : envContainers )
        {
//            if ( !localContainerIds.contains( c.getId() ) )
//            {
                c.setProxyContainer( proxyContainer );
//            }
        }
    }


    // Returns a first local container which will be used as to execute SSH commands to remote containers
    private Host getProxyContainer( Set<ProxyEnvironmentContainer> envContainers, Set<String> localContainerIds )
    {
        for ( ProxyEnvironmentContainer host : envContainers )
        {
            if ( localContainerIds.contains( host.getId() ) && host.getState() == ContainerHostState.RUNNING ) {
                return host;
            }
        }

        return null;
    }


    private Set<String> getLocalContainerIds()
    {
        HashSet<String> ids = new HashSet<>();

        for ( ResourceHost rh : peerManager.getLocalPeer().getResourceHosts() )
        {
            for ( ContainerHost ch : rh.getContainerHosts() )
            {
                ids.add( ch.getId() );
            }
        }

        return ids;
    }
}
