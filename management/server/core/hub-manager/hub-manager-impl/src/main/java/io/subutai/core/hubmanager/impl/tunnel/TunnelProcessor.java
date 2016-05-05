package io.subutai.core.hubmanager.impl.tunnel;


import java.util.Set;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.StateLinkProccessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.TunnelInfoDto;
import io.subutai.hub.share.json.JsonUtil;

//TODO close web clients and responses
public class TunnelProcessor implements StateLinkProccessor
{

    private final Logger LOG = LoggerFactory.getLogger( getClass() );

    private static final String TUNNEL_COMMAND = "subutai tunnel %s:%s %s -g";

    private PeerManager peerManager;

    private ConfigManager configManager;


    public TunnelProcessor( PeerManager peerManager, ConfigManager configManager )
    {
        this.peerManager = peerManager;
        this.configManager = configManager;
    }


    @Override
    public void processStateLinks( final Set<String> stateLinks ) throws HubPluginException
    {
        for ( String stateLink : stateLinks )
        {
            if ( !stateLink.contains( "tunnel" ) )
            {
                return;
            }
            processLink( stateLink );
        }
    }


    private void processLink( String stateLink )
    {
        TunnelInfoDto tunnelInfoDto = getData( stateLink );
        ResourceHost resourceHost = null;
        try
        {
            resourceHost = peerManager.getLocalPeer().getManagementHost();

            CommandResult result = execute( resourceHost,
                    String.format( TUNNEL_COMMAND, tunnelInfoDto.getIp(), tunnelInfoDto.getPortToOpen(),
                            tunnelInfoDto.getTtl() ) );

            if ( result.hasSucceeded() )
            {
                String[] data = result.getStdOut().split( ":" );
                tunnelInfoDto.setOpenedIp( data[0] );
                tunnelInfoDto.setOpenedPort( data[1] );

                updateTunnelStatus( stateLink, tunnelInfoDto );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    private void updateTunnelStatus( String link, TunnelInfoDto tunnelInfoDto )
    {
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );
            byte[] cborData = JsonUtil.toCbor( tunnelInfoDto );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            Response r = client.put( encryptedData );

            if ( r.getStatus() == HttpStatus.SC_OK )
            {
                String mgs = "Tunnel peer data successfully sent to hub";
                LOG.debug( mgs );
            }
        }
        catch ( Exception e )
        {
            String mgs = "Could not sent tunnel peer data to hub.";
            LOG.error( mgs, e.getMessage() );
        }
    }


    private CommandResult execute( ResourceHost resourceHost, String cmd )
    {
        boolean exec = true;
        int tryCount = 0;
        CommandResult result = null;

        while ( exec )
        {
            tryCount++;
            exec = tryCount > 3 ? false : true;
            try
            {
                result = resourceHost.execute( new RequestBuilder( cmd ) );
                exec = false;
                return result;
            }
            catch ( CommandException e )
            {
                e.printStackTrace();
            }

            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
        }

        return null;
    }


    private TunnelInfoDto getData( String link )
    {
        LOG.debug( "Getting tunnel data from Hub: {}", link );

        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );

            Response res = client.get();

            LOG.debug( "Response: HTTP {} - {}", res.getStatus(), res.getStatusInfo().getReasonPhrase() );

            if ( res.getStatus() != HttpStatus.SC_OK )
            {
                LOG.error( "Error to get tunnel  data from Hub: HTTP {} - {}", res.getStatus(),
                        res.getStatusInfo().getReasonPhrase() );

                return null;
            }

            byte[] encryptedContent = configManager.readContent( res );

            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

            return JsonUtil.fromCbor( plainContent, TunnelInfoDto.class );
        }
        catch ( Exception e )
        {
            LOG.error( "Error to get TunnelInfoDto data from Hub: ", e );

            return null;
        }
    }
}
