package io.subutai.core.hubmanager.impl.environment;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;


abstract class Helper
{
    protected final Logger log = LoggerFactory.getLogger( getClass() );

    protected final LocalPeer localPeer;


    protected Helper( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    abstract void execute( PeerEnvironmentDto dto ) throws Exception;


    protected String getFirstResourceHostId( LocalPeer localPeer )
    {
        for ( ResourceHost rh : localPeer.getResourceHosts() )
        {
            return rh.getId();
        }

        return null;
    }

}
