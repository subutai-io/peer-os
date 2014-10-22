package org.safehaus.subutai.core.peer.impl;


import javax.naming.NamingException;

import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;


/**
 * Management host implementation.
 */
public class ManagementHost extends Host
{
    private static final String DEFAULT_MANAGEMENT_HOSTNAME = "management";


    @Override
    public void invoke( final PeerCommandMessage commandMessage ) throws PeerException
    {
        getPeerManager().invoke( commandMessage );
    }


    private PeerManager getPeerManager() throws PeerException
    {
        PeerManager peerManager = null;
        try
        {
            ServiceLocator.getServiceNoCache( PeerManager.class );
        }
        catch ( NamingException e )
        {
            throw new PeerException( e.toString() );
        }

        return peerManager;
    }


    @Override
    public String getHostname()
    {
        return DEFAULT_MANAGEMENT_HOSTNAME;
    }
}
