package io.subutai.core.hubmanager.impl.processor;


import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.VehsDto;
import io.subutai.hub.share.dto.environment.EnvironmentDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.json.JsonUtil;


// TODO: Replace WebClient with HubRestClient.
public class VehsProcessor implements StateLinkProcessor
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private static final Pattern ENVIRONMENT_PEER_DATA_PATTERN = Pattern.compile(
            "/rest/v1/environments/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})/peers/"
                    + "[a-zA-z0-9]{1,100}" );

    private final ConfigManager configManager;

    private final PeerManager peerManager;


    public VehsProcessor( ConfigManager configManager, PeerManager peerManager )
    {
        this.configManager = configManager;

        this.peerManager = peerManager;
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws Exception
    {
        for ( String link : stateLinks )
        {
            Matcher environmentDataMatcher = ENVIRONMENT_PEER_DATA_PATTERN.matcher( link );

            if ( environmentDataMatcher.matches() )
            {
                EnvironmentPeerDto envPeerDto = getEnvPeerDto( link );

                if ( envPeerDto != null && envPeerDto.getPeerId() != null )
                {
                    setupHS( envPeerDto );
                }
            }
        }

        return false;
    }


    private EnvironmentPeerDto getEnvPeerDto( String link ) throws Exception
    {
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );

            Response r = client.get();
            byte[] encryptedContent = configManager.readContent( r );
            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
            EnvironmentPeerDto result = JsonUtil.fromCbor( plainContent, EnvironmentPeerDto.class );

            return result;
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            throw new Exception( "Could not retrieve environment peer data", e );
        }
    }


    private void setupHS( EnvironmentPeerDto peerDto )
    {
        EnvironmentDto environmentDto = getEnvironmentDto( peerDto.getEnvironmentInfo().getId() );
        if ( environmentDto != null )
        {
            VehsDto vehsDto = null;
            String containerDataURL = String.format( "/rest/v1/vehs/%s", peerDto.getEnvironmentInfo().getId() );
            try
            {
                WebClient client =
                        configManager.getTrustedWebClientWithAuth( containerDataURL, configManager.getHubIp() );
                Response r = client.get();
                byte[] encryptedContent = configManager.readContent( r );
                byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
                vehsDto = JsonUtil.fromCbor( plainContent, VehsDto.class );

                if ( vehsDto.getState() != null )
                {
                    switch ( vehsDto.getState() )
                    {
                        case DEPLOY:
                            deployHS( environmentDto, vehsDto, peerDto );
                            break;
                        case VERIFY_CHECKSUM:
                            verifyChecksumHS( environmentDto, vehsDto, peerDto );
                            break;
                        case COLLECT_METRIC:
                            collectMetric( environmentDto, vehsDto, peerDto );
                            break;
                        case DELETE:
                            deleteHS( environmentDto, vehsDto, peerDto );
                            break;
                    }
                }
                log.info( vehsDto.getProjectName() );
            }
            catch ( Exception e )
            {
                log.error( e.getMessage() );
            }
        }
    }


    private void collectMetric( EnvironmentDto environmentDto, VehsDto vehsDto, EnvironmentPeerDto peerDto )
    {
        List<ContainerHost> containerHosts = getContainers( environmentDto, vehsDto );
        for ( ContainerHost containerHost : containerHosts )
        {
            CommandResult commandResult = execute( containerHost, "bash collectMetrics.sh /var/log/nginx/access.log" );
            String verifyDataUrl = String.format( "/rest/v1/vehs/metric/%s", peerDto.getEnvironmentInfo().getId() );
            vehsDto.setData( commandResult.getStdOut() );
            sendPutRequest( verifyDataUrl, vehsDto, peerDto, VehsDto.VehsState.READY );
        }
    }


    private void deleteHS( final EnvironmentDto environmentDto, final VehsDto vehsDto, EnvironmentPeerDto peerDto )
    {

    }


    private void verifyChecksumHS( EnvironmentDto environmentDto, VehsDto vehsDto, EnvironmentPeerDto peerDto )
    {
        List<ContainerHost> containerHosts = getContainers( environmentDto, vehsDto );

        for ( ContainerHost containerHost : containerHosts )
        {
            CommandResult commandResult = execute( containerHost, "bash /checksum.sh /var/www/" );
            String verifyDataUrl = String.format( "/rest/v1/vehs/verify/%s", peerDto.getEnvironmentInfo().getId() );
            vehsDto.setData( commandResult.getStdOut() );
            sendPutRequest( verifyDataUrl, vehsDto, peerDto, VehsDto.VehsState.READY );
        }
    }


    private void deployHS( EnvironmentDto environmentDto, VehsDto vehsDto, EnvironmentPeerDto peerDto )
    {
        List<ContainerHost> containerHosts = getContainers( environmentDto, vehsDto );

        for ( ContainerHost containerHost : containerHosts )
        {
            String cloneCmd = "echo %s %s %s %s > /tmp/params";
            String cmd =
                    String.format( cloneCmd, vehsDto.getProjectName(), vehsDto.getProjectOwner(), vehsDto.getUserName(),
                            vehsDto.getUserPassword() );
            CommandResult commandResult = execute( containerHost, cmd );

            log.info( commandResult.getStdOut().toString() );
        }
        String vehsPeerDataUrl = String.format( "/rest/v1/vehs/%s", peerDto.getEnvironmentInfo().getId() );
        sendPutRequest( vehsPeerDataUrl, vehsDto, peerDto, VehsDto.VehsState.READY );
    }


    private void sendPutRequest( final String url, final VehsDto vehsDto, final EnvironmentPeerDto peerDto,
                                 final VehsDto.VehsState status )
    {
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( url, configManager.getHubIp() );
            vehsDto.setState( status );
            byte[] cborData = JsonUtil.toCbor( vehsDto );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            Response r = client.put( encryptedData );

            if ( r.getStatus() == HttpStatus.SC_OK )
            {
                String mgs = "VEHS peer data successfully sent to hub";
                log.debug( mgs );
            }
        }
        catch ( Exception e )
        {
            String mgs = "Could not sent environment peer data to hub.";
            log.error( mgs, e.getMessage() );
        }
    }


    private List<ContainerHost> getContainers( EnvironmentDto environmentDto, VehsDto vehsDto )
    {
        List<ContainerHost> cs = new ArrayList<>();
        LocalPeer localPeer = peerManager.getLocalPeer();

        if ( environmentDto != null )
        {
            for ( EnvironmentNodesDto nodesDto : environmentDto.getNodes() )
            {
                if ( nodesDto.getPeerId().equals( localPeer.getId() ) )
                {
                    for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
                    {
                        List<ContainerHost> containerHosts = localPeer.getPeerContainers( nodesDto.getPeerId() );
                        for ( ContainerHost containerHost : containerHosts )
                        {
                            if ( nodeDto.getTemplateName().equals( containerHost.getTemplateName() ) )
                            {
                                cs.add( containerHost );
                            }
                        }
                    }
                }
            }
        }
        return cs;
    }


    private CommandResult execute( ContainerHost containerHost, String cmd )
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
                result = containerHost.execute( new RequestBuilder( cmd ) );
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


    private EnvironmentDto getEnvironmentDto( String envId )
    {
        String envDataPath = String.format( "/rest/v1/environments/%s", envId );
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( envDataPath, configManager.getHubIp() );

            Response r = client.get();

            byte[] encryptedContent = configManager.readContent( r );

            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

            return JsonUtil.fromCbor( plainContent, EnvironmentDto.class );
        }
        catch ( Exception e )
        {
            log.error( "Could not  get environment data from Hub", e );
        }
        return null;
    }
}
