package io.subutai.core.hubmanager.impl.adapter;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Environment;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.environment.api.EnvironmentEventListener;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.hubmanager.api.HubManager;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.hub.share.common.HubAdapter;
import io.subutai.hub.share.json.JsonUtil;

import static java.lang.String.format;


//TODO use HubRestClient and ConfigDataServiceimpl instead of DaoHelper and HttpClient
public class HubAdapterImpl implements HubAdapter, EnvironmentEventListener, HostListener
{
    private static final String USER_ENVIRONMENTS_URL = "/rest/v1/adapter/users/%s/environments";

    private static final String PEER_ENVIRONMENTS_URL = "/rest/v1/adapter/peer/environments";

    private static final String DELETED_ENVIRONMENTS_URL = "/rest/v1/adapter/peer/deleted/environments";

    private static final String CONTAINERS_URL = "/rest/v1/adapter/environments/%s/containers/%s";

    private static final String ENVIRONMENT_SSHKEY_URL = "/rest/v1/adapter/environments/%s/ssh-key";

    private static final String CONTAINERS_STATE_URL = "/rest/v1/adapter/environments/%s/containers/%s/%s";

    private static final String CONTAINERS_HOSTNAME_URL = "/rest/v1/adapter/environments/%s/containers/%s/hostname/%s";

    private static final String PLUGIN_DATA_URL = "/rest/v1/adapter/users/%s/peers/%s/plugins/%s";

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

    private final DaoHelper daoHelper;

    private final HttpClient httpClient;

    private final IdentityManager identityManager;

    private final String peerId;

    private final LocalPeer localPeer;


    public HubAdapterImpl( DaoManager daoManager, SecurityManager securityManager, PeerManager peerManager,
                           IdentityManager identityManager ) throws HubManagerException
    {
        daoHelper = new DaoHelper( daoManager );

        httpClient = new HttpClient( securityManager );

        this.identityManager = identityManager;

        localPeer = peerManager.getLocalPeer();

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

        if ( user == null )
        {
            return null;
        }

        log.debug( "Active user: username={}, email={}", user.getUserName(), user.getEmail() );

        // For the admin, get peer owner data from Hub
        if ( IdentityManager.ADMIN_USERNAME.equals( user.getUserName() ) )
        {
            return getOwnerId();
        }

        // Trick to get the user id in Hub. See also: EnvironmentUserHelper.
        if ( user.getEmail().contains( HubManager.HUB_EMAIL_SUFFIX ) )
        {
            return StringUtils.substringBefore( user.getEmail(), "@" );
        }

        log.debug( "Can't get proper user id for Hub" );

        return null;
    }


    private String getUserIdWithCheck()
    {
        if ( !isRegistered() )
        {
            log.debug( "Peer not registered to Hub." );

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

            httpClient.doPost( format( USER_ENVIRONMENTS_URL, userId ), json );
        }
    }


    @Override
    public void uploadPeerOwnerEnvironment( final String json )
    {
        //obtain Hub peer owner id
        String userId = getOwnerId();

        httpClient.doPost( format( USER_ENVIRONMENTS_URL, userId ), json );
    }


    @Override
    public void removeEnvironment( String envId )
    {
        String userId = getUserIdWithCheck();

        // in case user is tenant manager we pass 0 as user id to Hub
        if ( userId == null && isRegistered() && ( identityManager.isTenantManager() || identityManager
                .isSystemUser() ) )
        {
            userId = "0";
        }

        if ( userId != null )
        {
            String path = format( USER_ENVIRONMENTS_URL, getUserId() ) + "/" + envId;

            httpClient.doDelete( path );
        }
    }


    @Override
    public void removeSshKey( final String envId, final String sshKey )
    {
        String path = format( ENVIRONMENT_SSHKEY_URL, envId ) + "/remove";

        httpClient.doPost( path, sshKey );
    }


    @Override
    public void addSshKey( final String envId, final String sshKey )
    {
        String path = format( ENVIRONMENT_SSHKEY_URL, envId ) + "/add";
        httpClient.doPost( path, sshKey );
    }


    @Override
    public String getUserEnvironmentsForPeer()
    {
        String userId = getUserIdWithCheck();

        if ( userId != null )
        {
            log.debug( "Peer registered to Hub. Getting environments for: user={}, peer={}", userId, peerId );

            return httpClient.doGet( format( USER_ENVIRONMENTS_URL, userId ) );
        }

        return null;
    }


    @Override
    public String getDeletedEnvironmentsForPeer()
    {
        return httpClient.doGet( DELETED_ENVIRONMENTS_URL );
    }


    @Override
    public String getAllEnvironmentsForPeer()
    {
        return httpClient.doGet( PEER_ENVIRONMENTS_URL );
    }


    @Override
    public void destroyContainer( String envId, String containerId )
    {
        httpClient.doDelete( format( CONTAINERS_URL, envId, containerId ) );
    }


    @Override
    public boolean uploadPluginData( String pluginKey, String key, Object data )
    {
        String userId = getUserIdWithCheck();

        if ( userId == null )
        {
            return false;
        }

        String json = gson.toJson( data );

        log.debug( "json: {}", json );

        String url = format( PLUGIN_DATA_URL, userId, peerId, pluginKey ) + "/data/" + key;

        String response = httpClient.doPost( url, json );

        // The data is for environment created on Hub. We save the plugin data there only,
        // i.e. no need to store in the local DB too.
        return "HUB_ENVIRONMENT".equals( response );
    }


    @Override
    public <T> List<T> getPluginData( String pluginKey, Class<T> clazz )
    {
        String userId = getUserIdWithCheck();

        if ( userId == null )
        {
            return Collections.emptyList();
        }

        log.debug( "userId={}, pluginKey={}, class={}", userId, pluginKey, clazz );

        String response = httpClient.doGet( format( PLUGIN_DATA_URL, userId, peerId, pluginKey ) );

        log.debug( "response: {}", response );

        if ( response == null )
        {
            return Collections.emptyList();
        }

        List<T> resultList = new ArrayList<>();

        try
        {
            ArrayList<String> dataList = JsonUtil.fromJson( response, ArrayList.class );

            for ( String data : dataList )
            {
                if ( StringUtils.isNotBlank( data ) )
                {
                    resultList.add( gson.fromJson( data, clazz ) );
                }
            }
        }
        catch ( IOException e )
        {
            log.error( "Error to parse json: ", e );
        }

        return resultList;
    }


    @Override
    public <T> T getPluginDataByKey( String pluginKey, String key, Class<T> clazz )
    {
        String userId = getUserIdWithCheck();

        if ( userId == null )
        {
            return null;
        }

        log.debug( "userId={}, pluginKey={}, key={}, class={}", userId, pluginKey, key, clazz );

        String url = format( PLUGIN_DATA_URL, userId, peerId, pluginKey ) + "/data/" + key;

        String response = httpClient.doGet( url );

        log.debug( "response: {}", response );

        return response != null ? gson.fromJson( response, clazz ) : null;
    }


    @Override
    public boolean deletePluginData( String pluginKey, String key )
    {
        String userId = getUserIdWithCheck();

        if ( userId == null )
        {
            return false;
        }

        log.debug( "userId={}, pluginKey={}, key={}", userId, pluginKey, key );

        String url = format( PLUGIN_DATA_URL, userId, peerId, pluginKey ) + "/data/" + key;

        String response = httpClient.doDelete( url );

        log.debug( "response: {}", response );

        return true;
    }


    private void onContainerStateChange( String envId, String contId, String state )
    {
        if ( !isRegistered() )
        {
            return;
        }

        log.info( "onContainerStateChange: envId={}, contId={}, state={}", envId, contId, state );

        httpClient.doPost( format( CONTAINERS_STATE_URL, envId, contId, state ), null );
    }


    private void onContainerHostnameChange( String envId, String contId, String hostname )
    {
        if ( !isRegistered() )
        {
            return;
        }

        log.info( "onContainerHostnameChange: envId={}, contId={}, hostname={}", envId, contId, hostname );

        httpClient.doPost( format( CONTAINERS_HOSTNAME_URL, envId, contId, hostname ), null );
    }


    //environment events


    @Override
    public void onEnvironmentCreated( final Environment environment )
    {
        //not used
    }


    @Override
    public void onEnvironmentGrown( final Environment environment, final Set<EnvironmentContainerHost> newContainers )
    {
        //not used
    }


    @Override
    public void onContainerDestroyed( final Environment environment, final String containerId )
    {
        destroyContainer( environment.getId(), containerId );
    }


    @Override
    public void onEnvironmentDestroyed( final String environmentId )
    {
        //not used
    }


    @Override
    public void onContainerStarted( final Environment environment, final String containerId )
    {
        onContainerStateChange( environment.getId(), containerId, "start" );
    }


    @Override
    public void onContainerStopped( final Environment environment, final String containerId )
    {
        onContainerStateChange( environment.getId(), containerId, "stop" );
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
    public void onContainerDestroyed( final ContainerHostInfo containerInfo )
    {
        try
        {
            ContainerHost containerHost = localPeer.getContainerHostById( containerInfo.getId() );

            destroyContainer( containerHost.getEnvironmentId().getId(), containerInfo.getId() );
        }
        catch ( HostNotFoundException e )
        {
            //ignore
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

            //TODO implement update of /etc/hosts and /root/.ssh/authorized_keys files on all the rest environment
            // containers in case this is Hub environment
        }
        catch ( HostNotFoundException e )
        {
            //ignore
        }
    }


    @Override
    public void onContainerNetInterfaceChanged( final ContainerHostInfo containerInfo,
                                                final HostInterfaceModel oldNetInterface,
                                                final HostInterfaceModel newNetInterface )
    {
        //not used
    }


    @Override
    public void onContainerNetInterfaceAdded( final ContainerHostInfo containerInfo,
                                              final HostInterfaceModel netInterface )
    {
        //not used
    }


    @Override
    public void onContainerNetInterfaceRemoved( final ContainerHostInfo containerInfo,
                                                final HostInterfaceModel netInterface )
    {
        // todo
    }


    @Override
    public void onHeartbeat( final ResourceHostInfo resourceHostInfo, final Set<QuotaAlertValue> alerts )
    {
        //not used
    }


    @Override
    public void onContainerCreated( final ContainerHostInfo containerInfo )
    {
        //not used
    }
}