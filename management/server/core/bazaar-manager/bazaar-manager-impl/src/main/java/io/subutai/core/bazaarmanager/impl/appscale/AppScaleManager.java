package io.subutai.core.bazaarmanager.impl.appscale;


import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.bazaarmanager.impl.util.Utils;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.bazaar.share.dto.AppScaleConfigDto;

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

        setupRevproxy( config );

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
        StringBuilder s = new StringBuilder();

        for ( String hostname : hosts )
        {
            s.append( ipList.get( hostname ) ).append( " " );
        }

        return s.toString().replace( "/24", "" );
    }


    private void setupRevproxy( AppScaleConfigDto config )
    {
        String ipAddress = config.getContainerAddresses().get( config.getClusterName() ).replace( "/24", "" );

        try
        {
            ProxyLoadBalanceStrategy balanceStrategy = ProxyLoadBalanceStrategy.LOAD_BALANCE;
            String sslCertPath = getSSLCertPath( config );

            Long vni = getVni( config );

            if ( vni != 0 )
            {
                localPeer.removeVniDomain( vni );
                localPeer.setVniDomain( vni, "*." + config.getUserDomain().trim(), balanceStrategy, sslCertPath );
                localPeer.addIpToVniDomain( ipAddress, vni );
            }
            else
            {
                log.error( "Error getting vni" );
            }
        }
        catch ( Exception e )
        {
            log.error( "Error to set proxy settings: ", e );
        }
    }


    private String getSSLCertPath( final AppScaleConfigDto config )
    {
        return config.getClusterName() + ":/etc/nginx/ssl.pem";
    }


    private ContainerHost getContainerHost( String containerName )
    {
        ContainerHost ch = null;

        try
        {
            ch = localPeer.getContainerHostByContainerName( containerName );
        }
        catch ( HostNotFoundException e )
        {
            log.error( "Error to get container by name: ", e );
        }

        return ch;
    }

    private void execute( ContainerHost ch, String command ) throws CommandException
    {
        if ( Utils.waitTillConnects( ch, 15 ) )
        {
            CommandResult result = ch.execute( new RequestBuilder( command ).withTimeout( 18000 ) );

            if ( !result.hasSucceeded() )
            {
                throw new CommandException( format( "Error to execute command: %s. %s", command, result.getStdErr() ) );
            }
        }
    }


    private Long getVni( final AppScaleConfigDto config )
    {
        try
        {
            ContainerHost ch = getContainerHost( config.getClusterName() );

            NetworkResource resource =
                    localPeer.getReservedNetworkResources().findByEnvironmentId( ch.getEnvironmentId().getId() );

            return resource.getVni();
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }

        return 0L;
    }
}
