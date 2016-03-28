package io.subutai.core.hubadapter.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.hubadapter.api.HubAdapter;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;

import static java.lang.String.format;

public class HubAdapterImpl implements HubAdapter
{
    private static final String USER_ENVIRONMENTS = "/rest/v1/adapter/users/%s/environments";

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final DaoHelper daoHelper;

    private final HttpClient httpClient;

    private final String peerId;


    public HubAdapterImpl( DaoManager daoManager, SecurityManager securityManager, PeerManager peerManager ) throws Exception
    {
        daoHelper = new DaoHelper( daoManager );

        httpClient = new HttpClient( securityManager );

        peerId = peerManager.getLocalPeer().getId();
    }


    private boolean isRegistered()
    {
        return daoHelper.isPeerRegisteredToHub( peerId );
    }


    private String getOwnerId()
    {
        return daoHelper.getPeerOwnerId( peerId );
    }


    @Override
    public String getUserEnvironmentsForPeer()
    {
        if ( !isRegistered() )
        {
            log.debug( "Peer not registered to Hub." );

            return null;
        }

        log.debug( "Peer registered to Hub. Getting environments for: user={}, peer={}", getOwnerId(), peerId );

        return httpClient.doGet( format( USER_ENVIRONMENTS, getOwnerId() ) );
    }
}