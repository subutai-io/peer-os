package io.subutai.core.hubmanager.impl.appscale;


import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Preconditions;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.tunnel.TunnelHelper;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.AppScaleConfigDto;
import io.subutai.hub.share.dto.TunnelInfoDto;
import io.subutai.hub.share.json.JsonUtil;

import static java.lang.String.format;


public class AppScaleManager
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final LocalPeer localPeer;


    public AppScaleManager( PeerManager peerManager )
    {
        this.localPeer = peerManager.getLocalPeer();
    }


    void installCluster( AppScaleConfigDto config ) throws CommandException
    {
        log.debug( "AppScale installation started" );

        Preconditions.checkArgument( config != null, "Null config" );

        Preconditions.checkArgument( !StringUtils.isEmpty( config.getUserDomain() ), "User domain is null" );

        ContainerHost controllerHost = getContainerHost( config.getClusterName() );

        createAppScalefile( controllerHost, config );

        installAppScale( controllerHost, config );

        setupRevproxy( controllerHost, config );

        log.debug( "AppScale installation done" );
    }


    private void createAppScalefile( ContainerHost containerHost, AppScaleConfigDto config ) throws CommandException
    {
        Map<String, String> ipList = config.getContainerAddresses();

        String masterIP = ipList.get( config.getClusterName() ).replace( "/24", "" );

        String cmd = format( "sudo /var/lib/appscale/create-appscalefile.sh -master %s -appengine %s -database %s "
                        + "-zookeeper %s", masterIP, formatIps( ipList, config.getAppenList() ),
                formatIps( ipList, config.getCassList() ), formatIps( ipList, config.getZooList() ) );

        execute( containerHost, cmd );
    }


    private void installAppScale( ContainerHost containerHost, AppScaleConfigDto config ) throws CommandException
    {
        String email = "a@a.com";
        String password = "aaaaaa";

        for ( String s : config.getNodes() )
        {
            if ( StringUtils.startsWith( s, "email:" ) )
            {
                email = StringUtils.substringAfter( s, "email:" );
            }
            else if ( StringUtils.startsWith( s, "password:" ) )
            {
                password = StringUtils.substringAfter( s, "password:" );
            }
        }

        String cmd = format( "sudo /var/lib/appscale/setup.sh %s %s %s", config.getUserDomain(), email, password );

        execute( containerHost, cmd );
    }


    private String formatIps( Map<String, String> ipList, List<String> hosts )
    {
        String s = "";

        for ( String hostname : hosts )
        {
            s += ipList.get( hostname ) + " ";
        }

        return s.replace( "/24", "" );
    }


    private void setupRevproxy( ContainerHost containerHost, AppScaleConfigDto config )
    {
        String ipAddress = config.getContainerAddresses().get( config.getClusterName() ).replace( "/24", "" );

        try
        {
            ResourceHost resourceHostByContainerId = localPeer.getResourceHostByContainerId( containerHost.getId() );

            String vlanString = getVlan( config, resourceHostByContainerId );

            resourceHostByContainerId.execute( new RequestBuilder( "subutai proxy del " + vlanString + " -d" ) );

            resourceHostByContainerId.execute( new RequestBuilder(
                    "subutai proxy add " + vlanString + " -d \"*." + config.getUserDomain() + "\" -f /mnt/lib/lxc/"
                            + config.getClusterName() + "/rootfs/etc/nginx/ssl.pem" ) );

            resourceHostByContainerId
                    .execute( new RequestBuilder( "subutai proxy add " + vlanString + " -h " + ipAddress ) );
        }
        catch ( Exception e )
        {
            log.error( "Error to set proxy settings: ", e );
        }
    }


    private ContainerHost getContainerHost( String hostname )
    {
        ContainerHost ch = null;

        try
        {
            ch = localPeer.getContainerHostByName( hostname );
        }
        catch ( HostNotFoundException e )
        {
            log.error( "Error to get container by name: ", e );
        }

        return ch;
    }


    private void execute( ContainerHost ch, String command ) throws CommandException
    {
        if ( isChConnected( ch ) )
        {
            CommandResult result = ch.execute( new RequestBuilder( command ).withTimeout( 10000 ) );

            if ( result.getExitCode() != 0 )
            {
                throw new CommandException( format( "Error to execute command: %s. %s", command, result.getStdErr() ) );
            }
        }
    }


    private boolean isChConnected( final ContainerHost ch )
    {
        boolean exec = true;
        int tryCount = 0;

        while ( exec )
        {
            tryCount++;
            exec = tryCount > 3 ? false : true;

            if ( !ch.isConnected() )
            {
                return true;
            }

            try
            {
                Thread.sleep( 5000 );
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
        }

        return true;
    }


    public void createTunnel( String link, final AppScaleConfigDto config, ConfigManager configManager )
    {
        TunnelInfoDto tunnelInfoDto = config.getTunnelInfoDto();

        String cmd = "subutai tunnel add %s:%s %s -g";

        ResourceHost resourceHost = getResourceHost( config );


        CommandResult commandResult = TunnelHelper.execute( resourceHost,
                String.format( cmd, tunnelInfoDto.getIp(), tunnelInfoDto.getPortToOpen(), "" ) );

        tunnelInfoDto = TunnelHelper.parseResult( link, commandResult.getStdOut(), configManager );
        tunnelInfoDto.setTunnelStatus( TunnelInfoDto.TunnelStatus.READY );

        String tunnelLink = link + "/tunnel";

        String vlanString = null;
        try
        {
            vlanString = getVlan( config, resourceHost );
        }
        catch ( Exception e )
        {
            log.error( "Error getting vlan : {}", e.getMessage() );
            e.printStackTrace();
        }

        String revpx = "sed -i -e 's/https:\\/\\/$host$request_uri/https:\\/\\/$host:%s$request_uri/g' "
                + "/var/lib/apps/subutai/current/nginx-includes/%s.conf";

        String port = tunnelInfoDto.getOpenedPort().replaceAll( "\\n", "" );
        String ccmd = String.format( revpx, port, vlanString ).replaceAll( "\\n", "" );
        TunnelHelper.execute( resourceHost, ccmd );

        TunnelHelper.execute( resourceHost, "systemctl restart *nginx*" );

        updateTunnelStatus( tunnelLink, tunnelInfoDto, configManager );
    }


    private String getVlan( final AppScaleConfigDto config, ResourceHost resourceHost ) throws Exception
    {
        CommandResult res =
                TunnelHelper.execute( resourceHost, "grep vlan /mnt/lib/lxc/" + config.getClusterName() + "/config" );

        String vlanString = res.getStdOut().substring( 11, 14 );
        return vlanString;
    }


    private ResourceHost getResourceHost( final AppScaleConfigDto config )
    {
        ContainerHost containerHost = null;
        ResourceHost resourceHost = null;
        try
        {

            Set<ContainerHost> chs = localPeer.findContainersByEnvironmentId( config.getEnvironmentId() );
            for ( ContainerHost containerHost1 : chs )
            {
                if ( containerHost1.getContainerName().equals( config.getClusterName() ) )
                {
                    containerHost = containerHost1;
                    break;
                }
            }
            if ( containerHost != null )
            {
                resourceHost = localPeer.getResourceHostByContainerId( containerHost.getId() );
            }
        }
        catch ( HostNotFoundException e )
        {
            log.error( e.getMessage() );
            e.printStackTrace();
        }

        return resourceHost;
    }


    private Response updateTunnelStatus( String link, TunnelInfoDto tunnelInfoDto, ConfigManager configManager )
    {
        WebClient client = null;
        try
        {
            client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );
            byte[] cborData = JsonUtil.toCbor( tunnelInfoDto );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            return client.post( encryptedData );
        }
        catch ( Exception e )
        {
            String mgs = "Could not sent tunnel peer data to hub.";
            log.error( mgs, e.getMessage() );
            return null;
        }
        finally
        {
            if ( client != null )
            {
                client.close();
            }
        }
    }
}
