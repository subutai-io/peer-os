package io.subutai.core.bazaarmanager.impl.adapter;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Environment;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.environment.api.EnvironmentEventListener;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.bazaarmanager.api.BazaarManager;
import io.subutai.core.bazaarmanager.api.RestClient;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.dao.ConfigDataService;
import io.subutai.core.bazaarmanager.impl.dao.ConfigDataServiceImpl;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.bazaar.share.common.BazaaarAdapter;

import static java.lang.String.format;


public class BazaaarAdapterImpl extends HostListener implements BazaaarAdapter, EnvironmentEventListener
{
    private static final String USER_ENVIRONMENTS_URL = "/rest/v1/adapter/users/%s/environments";

    private static final String PEER_ENVIRONMENTS_URL = "/rest/v1/adapter/peer/environments";

    private static final String DELETED_ENVIRONMENTS_URL = "/rest/v1/adapter/peer/deleted/environments";

    private static final String CONTAINERS_URL = "/rest/v1/adapter/environments/%s/containers/%s";

    private static final String ENVIRONMENT_SSHKEY_URL = "/rest/v1/adapter/environments/%s/ssh-key";

    private static final String CONTAINERS_STATE_URL = "/rest/v1/adapter/environments/%s/containers/%s/%s";

    private static final String CONTAINERS_HOSTNAME_URL = "/rest/v1/adapter/environments/%s/containers/%s/hostname/%s";


    private final Logger log = LoggerFactory.getLogger( getClass() );


    private final ConfigDataService configDataService;


    private final IdentityManager identityManager;

    private final String peerId;

    private final LocalPeer localPeer;


    public BazaaarAdapterImpl( DaoManager daoManager, PeerManager peerManager, IdentityManager identityManager )

    {
        configDataService = new ConfigDataServiceImpl( daoManager );

        this.identityManager = identityManager;

        localPeer = peerManager.getLocalPeer();

        peerId = peerManager.getLocalPeer().getId();
    }


    private RestClient getRestClient()
    {
        return getbazaarmanager().getRestClient();
    }


    private BazaarManager getbazaarmanager()
    {
        return ServiceLocator.lookup( BazaarManager.class );
    }


    private boolean isRegistered()
    {
        return configDataService.isPeerRegisteredToBazaar( peerId );
    }


    private String getOwnerId()
    {
        return configDataService.getPeerOwnerId( peerId );
    }


    private String getUserId()
    {
        User user = identityManager.getActiveUser();

        if ( user == null )
        {
            return null;
        }

        log.debug( "Active user: username={}, email={}", user.getUserName(), user.getEmail() );

        // For the admin, get peer owner data frombazaar
        if ( IdentityManager.ADMIN_USERNAME.equals( user.getUserName() ) )
        {
            return getOwnerId();
        }

        // Trick to get the user id inbazaar. See also: EnvironmentUserHelper.
        if ( user.getEmail().contains( BazaarManager.BAZAAR_EMAIL_SUFFIX ) )
        {
            return StringUtils.substringBefore( user.getEmail(), "@" );
        }

        log.debug( "Can't get proper user id for Bazaar" );

        return null;
    }


    private String getUserIdWithCheck()
    {
        if ( !isRegistered() )
        {
            log.debug( "Peer not registered to Bazaar" );

            return null;
        }

        return getUserId();
    }


    @Override
    public void uploadEnvironment( String json )
    {
        String userId = getUserIdWithCheck();

        if ( userId != null )
        {
            log.debug( "json: {}", json );

            getRestClient().post( format( USER_ENVIRONMENTS_URL, userId ), json );
        }
    }


    @Override
    public boolean uploadPeerOwnerEnvironment( final String json )
    {
        //obtainbazaar peer owner id
        String userId = getOwnerId();

        RestResult restResult = getRestClient().post( format( USER_ENVIRONMENTS_URL, userId ), json );

        return restResult.isSuccess();
    }


    @Override
    public void removeEnvironment( String envId )
    {
        String userId = getUserIdWithCheck();

        // in case user is tenant manager we pass 0 as user id tobazaar
        if ( userId == null && isRegistered() && ( identityManager.isTenantManager() || identityManager
                .isSystemUser() ) )
        {
            userId = "0";
        }

        if ( userId != null )
        {
            String path = format( USER_ENVIRONMENTS_URL, userId ) + "/" + envId;

            getRestClient().delete( path );
        }
    }


    @Override
    public void removeSshKey( final String envId, final String sshKey )
    {
        String path = format( ENVIRONMENT_SSHKEY_URL, envId ) + "/remove";

        getRestClient().post( path, sshKey );
    }


    @Override
    public void addSshKey( final String envId, final String sshKey )
    {
        String path = format( ENVIRONMENT_SSHKEY_URL, envId ) + "/add";
        getRestClient().post( path, sshKey );
    }


    @Override
    public String getUserEnvironmentsForPeer()
    {
        String userId = getUserIdWithCheck();

        if ( userId != null )
        {
            log.debug( "Peer registered to Bazaar. Getting environments for: user={}, peer={}", userId, peerId );

            return getRestClient().get( format( USER_ENVIRONMENTS_URL, userId ), String.class ).getEntity();
        }

        return null;
    }


    @Override
    public String getDeletedEnvironmentsForPeer()
    {
        return getRestClient().get( DELETED_ENVIRONMENTS_URL, String.class ).getEntity();
    }


    @Override
    public String getAllEnvironmentsForPeer()
    {
        return getRestClient().get( PEER_ENVIRONMENTS_URL, String.class ).getEntity();
    }


    @Override
    public void destroyContainer( String envId, String containerId )
    {
        getRestClient().delete( format( CONTAINERS_URL, envId, containerId ) );
    }


    @Override
    public void notifyContainerDiskUsageExcess( String peerId, String envId, String contId, long diskUsage,
                                                boolean containerWasStopped )
    {
        RestResult result = getRestClient().post( String
                .format( "/rest/v1/peers/%s/environments/%s/containers/%s/disk_usage/%d/%s", peerId, envId, contId,
                        diskUsage, containerWasStopped ), null );

        if ( !result.isSuccess() )
        {
            log.error( "Error notifying Bazaar about container disk usage excess: HTTP {} - {}", result.getStatus(),
                    result.getReasonPhrase() );
        }
    }


    private void onContainerStateChange( String envId, String contId, String state )
    {
        if ( !isRegistered() )
        {
            return;
        }

        log.info( "onContainerStateChange: envId={}, contId={}, state={}", envId, contId, state );

        getRestClient().post( format( CONTAINERS_STATE_URL, envId, contId, state ), null );
    }


    private void onContainerHostnameChange( String envId, String contId, String hostname )
    {
        if ( !isRegistered() )
        {
            return;
        }

        log.info( "onContainerHostnameChange: envId={}, contId={}, hostname={}", envId, contId, hostname );

        getRestClient().post( format( CONTAINERS_HOSTNAME_URL, envId, contId, hostname ), null );
    }


    //environment events


    @Override
    public void onEnvironmentCreated( final Environment environment )
    {
        getbazaarmanager().schedulePeerMetrics();
    }


    @Override
    public void onEnvironmentGrown( final Environment environment, final Set<EnvironmentContainerHost> newContainers )
    {
        getbazaarmanager().schedulePeerMetrics();
    }


    @Override
    public void onContainerDestroyed( final Environment environment, final String containerId )
    {
        destroyContainer( environment.getId(), containerId );

        getbazaarmanager().schedulePeerMetrics();
    }


    @Override
    public void onEnvironmentDestroyed( final String environmentId )
    {
        getbazaarmanager().schedulePeerMetrics();
    }


    @Override
    public void onContainerStarted( final Environment environment, final String containerId )
    {
        onContainerStateChange( environment.getId(), containerId, "start" );

        getbazaarmanager().schedulePeerMetrics();
    }


    @Override
    public void onContainerStopped( final Environment environment, final String containerId )
    {
        onContainerStateChange( environment.getId(), containerId, "stop" );

        getbazaarmanager().schedulePeerMetrics();
    }


    @Override
    public void onContainerStateChanged( final ContainerHostInfo containerInfo, final ContainerHostState previousState,
                                         final ContainerHostState currentState )
    {
        if ( currentState == ContainerHostState.RUNNING || currentState == ContainerHostState.STOPPED )
        {
            try
            {
                ContainerHost containerHost = localPeer.getContainerHostById( containerInfo.getId() );

                onContainerStateChange( containerHost.getEnvironmentId().getId(), containerInfo.getId(),
                        currentState == ContainerHostState.RUNNING ? "start" : "stop" );
            }
            catch ( HostNotFoundException e )
            {
                //ignore
            }
        }
    }


    @Override
    public void onContainerHostnameChanged( final ContainerHostInfo containerInfo, final String previousHostname,
                                            final String currentHostname )
    {
        try
        {
            ContainerHost containerHost = localPeer.getContainerHostById( containerInfo.getId() );

            onContainerHostnameChange( containerHost.getEnvironmentId().getId(), containerInfo.getId(),
                    currentHostname );
        }
        catch ( HostNotFoundException e )
        {
            //ignore
        }
    }
}