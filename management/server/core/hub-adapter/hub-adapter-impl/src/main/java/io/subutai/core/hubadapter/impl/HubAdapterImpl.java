package io.subutai.core.hubadapter.impl;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.hubadapter.api.HubAdapter;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.hub.share.json.JsonUtil;

import static java.lang.String.format;


public class HubAdapterImpl implements HubAdapter
{
    private static final String ENVIRONMENTS_URL = "/rest/v1/adapter/users/%s/environments";

    private static final String CONTAINERS_URL = "/rest/v1/adapter/environments/%s/containers/%s";

    private static final String PLUGIN_DATA_URL = "/rest/v1/adapter/users/%s/peers/%s/plugins/%s";

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

    private final DaoHelper daoHelper;

    private final HttpClient httpClient;

    private final IdentityManager identityManager;

    private final String peerId;


    public HubAdapterImpl( DaoManager daoManager, SecurityManager securityManager, PeerManager peerManager,
                           IdentityManager identityManager ) throws Exception
    {
        daoHelper = new DaoHelper( daoManager );

        httpClient = new HttpClient( securityManager );

        this.identityManager = identityManager;

        peerId = peerManager.getLocalPeer().getId();
    }


    public boolean isRegistered()
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
        if ( user.getUserName().equals( "admin" ) )
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


    private String getUserIdWithCheck()
    {
        if ( !isRegistered() )
        {
            log.debug( "Peer not registered to Hub." );

            return null;
        }

        return getUserId();
    }


    public void uploadEnvironment( String json )
    {
        String userId = getUserIdWithCheck();

        if ( userId != null )
        {
            log.debug( "json: {}", json );

            httpClient.doPost( format( ENVIRONMENTS_URL, userId ), json );
        }
    }


    @Override
    public void removeEnvironment( String envId )
    {
        String userId = getUserIdWithCheck();

        if ( userId != null )
        {
            String path = format( ENVIRONMENTS_URL, getUserId() ) + "/" + envId;

            httpClient.doDelete( path );
        }
    }


    @Override
    public String getUserEnvironmentsForPeer()
    {
        String userId = getUserIdWithCheck();

        if ( userId != null )
        {
            log.debug( "Peer registered to Hub. Getting environments for: user={}, peer={}", userId, peerId );

            return httpClient.doGet( format( ENVIRONMENTS_URL, userId ) );
        }

        return null;
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
}