package io.subutai.core.hubmanager.impl.environment;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;


public class EnvironmentDestroyer
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final LocalPeer localPeer;

    public EnvironmentDestroyer( LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    public void test()
    {
        String envId = "11e3e4de-2bf9-45e6-98f4-f09d65a86700";

        destroy( new EnvironmentId( envId ) );
    }


    public void destroy( EnvironmentId envId )
    {
        try
        {
            // Removes vni and containers
            localPeer.cleanupEnvironment( envId );

            // Bug: p2p is not removed.
//            localPeer.removeP2PConnection( envId );

            // Bug: Error getting public key by fingerprint
            localPeer.removePeerEnvironmentKeyPair( envId );

            log.debug( "Environment destroyed successfully");
        }
        catch ( Exception e )
        {

            log.error( "Error to destroy environment", e );
        }
    }

}
