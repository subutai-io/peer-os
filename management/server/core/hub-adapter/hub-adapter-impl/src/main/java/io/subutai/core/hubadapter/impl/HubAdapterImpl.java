package io.subutai.core.hubadapter.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.hubadapter.api.HubAdapter;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;

import static java.lang.String.format;

public class HubAdapterImpl implements HubAdapter
{
    private static final String ENVIRONMENTS = "/rest/v1/adapter/users/%s/environments";

    private static final String CONTAINERS = "/rest/v1/adapter/environments/%s/containers/%s";

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final DaoHelper daoHelper;

    private final HttpClient httpClient;

    private final IdentityManager identityManager;

    private final String peerId;


    public HubAdapterImpl( DaoManager daoManager, SecurityManager securityManager, PeerManager peerManager, IdentityManager identityManager ) throws Exception
    {
        daoHelper = new DaoHelper( daoManager );

        httpClient = new HttpClient( securityManager );

        this.identityManager = identityManager;

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


    private String getUserId()
    {
        User user = identityManager.getActiveUser();

        log.debug( "Active user: username={}, email={}", user.getUserName(), user.getEmail() );

        // For the admin, get peer owner data from Hub
        if ( user.getUserName().equals( "admin") )
        {
            return getOwnerId();
        }

        // Trick to get the user id in Hub. See also: EnvironmentUserHelper.
        if ( user.getEmail().contains( "@hub.subut.ai" ) )
        {
            return StringUtils.substringBefore( user.getEmail(), "@" );
        }

        log.debug( "Can't get proper user id for Hub" );

        return null;
    }


    public void uploadEnvironment( String json )
    {
        if ( !isRegistered() )
        {
            log.debug( "Peer not registered to Hub." );

            return;
        }

        String userId = getUserId();

        if ( userId == null )
        {
            return;
        }

        log.debug( "json: {}", json );

        httpClient.doPost( format( ENVIRONMENTS, userId ), json );
    }


    @Override
    public void removeEnvironment( String envId )
    {
        if ( !isRegistered() )
        {
            log.debug( "Peer not registered to Hub." );

            return;
        }

        String path = format( ENVIRONMENTS, getUserId() ) + "/" + envId;

        httpClient.doDelete( path );
    }


    //
    // REST API
    //


    @Override
    public String getUserEnvironmentsForPeer()
    {
        if ( !isRegistered() )
        {
            log.debug( "Peer not registered to Hub." );

            return null;
        }

        String userId = getUserId();

        if ( userId == null )
        {
            return null;
        }

        log.debug( "Peer registered to Hub. Getting environments for: user={}, peer={}", userId, peerId );

        return httpClient.doGet( format( ENVIRONMENTS, userId ) );
    }


    @Override
    public void destroyContainer( String envId, String containerId )
    {
        httpClient.doDelete( format( CONTAINERS, envId, containerId ) );
    }
}