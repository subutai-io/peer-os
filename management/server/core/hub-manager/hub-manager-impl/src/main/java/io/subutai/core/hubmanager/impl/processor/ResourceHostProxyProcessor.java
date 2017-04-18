package io.subutai.core.hubmanager.impl.processor;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.network.NetworkResource;
import io.subutai.common.network.ReservedNetworkResources;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.ResourceHostProxyDto;

import static java.lang.String.format;


public class ResourceHostProxyProcessor implements StateLinkProcessor
{

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private static final HashSet<String> LINKS_IN_PROGRESS = new HashSet<>();

    private ConfigManager configManager;

    private PeerManager peerManager;

    private HubRestClient restClient;


    public ResourceHostProxyProcessor()
    {
    }


    public ResourceHostProxyProcessor( ConfigManager configManager, PeerManager peerManager, HubRestClient restClient )
    {
        this.configManager = configManager;
        this.peerManager = peerManager;
        this.restClient = restClient;
    }


    @Override
    public synchronized boolean processStateLinks( final Set<String> stateLinks ) throws HubManagerException
    {
        for ( String stateLink : stateLinks )
        {
            if ( stateLink.contains( "proxy" ) )
            {
                processLink( stateLink );
            }
        }

        return false;
    }


    private void processLink( String stateLink ) throws HubManagerException
    {
        try
        {
            log.info( "Link process - START: {}", stateLink );

            if ( LINKS_IN_PROGRESS.contains( stateLink ) )
            {
                log.info( "This link is in progress: {}", stateLink );

                return;
            }

            LINKS_IN_PROGRESS.add( stateLink );

            ResourceHostProxyDto rhpDto = getData( stateLink );

            log.error( rhpDto.getData() );

            setupP2p( rhpDto );

            String url = format( "/rest/v1/proxy/peers/%s/data", peerManager.getLocalPeer().getId() );

            RestResult<Object> result = restClient.post( url, rhpDto );

            if ( !result.isSuccess() )
            {
                log.error( "Error to send  data to Hub: " + result.getError() );
            }
            else
            {
                log.error( "Sent Data to HUB Success" );
            }
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
        finally
        {
            LINKS_IN_PROGRESS.remove( stateLink );
        }
    }


    private void setupP2p( ResourceHostProxyDto rhpDto )
    {
        JSONArray peesData = new JSONArray( rhpDto.getData() );
        List<String> list = new ArrayList<>();
        String subnet = "";

        for ( int i = 0; i < peesData.length(); i++ )
        {
            subnet = process( peesData.getJSONObject( i ) );

            if ( !subnet.isEmpty() )
            {
                list.add( subnet );
            }
        }

        rhpDto.setData( list.toString() );
    }


    private String process( JSONObject json )
    {
        try
        {
            String p2pSub = "";
            String peerId = peerManager.getLocalPeer().getId();

            if ( !peerId.equals( json.getString( "peerId" ) ) )
            {
                return "";
            }

            setupTunnel( json );

            ReservedNetworkResources reservedNetworkResources =
                    peerManager.getLocalPeer().getReservedNetworkResources();
            Set<NetworkResource> resources = reservedNetworkResources.getNetworkResources();


            for ( NetworkResource networkResource : resources )
            {
                p2pSub += networkResource.getP2pSubnet();
            }

            return p2pSub;
        }
        catch ( Exception e )
        {

            log.error( e.getMessage() );
            return "";
        }
    }


    private void setupTunnel( final JSONObject json )
    {
        try
        {
            String secretKey = json.getString( "secretKey" );
            String p2pIp = json.getString( "p2pIp" );
            String interfaceName = json.getString( "interfaceName" );
            String hash = json.getString( "hash" );
            long secretKeyTtlSec = json.getLong( "secretKeyTtlSec" );

            ResourceHost resourceHost = peerManager.getLocalPeer().getResourceHosts().iterator().next();

            resourceHost.joinP2PSwarm( p2pIp, interfaceName, hash, secretKey, secretKeyTtlSec );



        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
    }


    private ResourceHostProxyDto getData( String stateLink )
    {
        RestResult<ResourceHostProxyDto> restResult = restClient.get( stateLink, ResourceHostProxyDto.class );

        if ( !restResult.isSuccess() )
        {
            log.error( "Error to get user data from Hub: " + restResult.getError() );
        }

        return restResult.getEntity();
    }
}
