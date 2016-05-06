package io.subutai.core.hubmanager.impl.appscale;


import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.AppScaleConfigDto;

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

        String masterIP = ipList.get( config.getClusterName() );

        String cmd = format( "sudo /var/lib/subutai-appscale/create-appscalefile.sh -master %s -appengine %s -database %s -zookeeper %s", masterIP,
            formatIps( ipList, config.getAppenList() ),
            formatIps( ipList, config.getCassList() ),
            formatIps( ipList, config.getZooList() )
                           );

        execute( containerHost, cmd );
    }


    private void installAppScale( ContainerHost containerHost, AppScaleConfigDto config ) throws CommandException
    {
        String cmd =  format( "sudo /var/lib/subutai-appscale/setup.sh %s %s %s", config.getUserDomain(), "a@a.com", "aaaaaa" );

        execute( containerHost, cmd );
    }


    private String formatIps( Map<String, String> ipList, List<String> hosts )
    {
        String s = "";

        for ( String hostname : hosts )
        {
            s += ipList.get( hostname ) + " ";
        }

        return s;
    }


    private void setupRevproxy( ContainerHost containerHost, AppScaleConfigDto config )
    {
        String ipAddress = config.getContainerAddresses().get( config.getClusterName() );

        try
        {
            ResourceHost resourceHostByContainerId = localPeer.getResourceHostByContainerId( containerHost.getId() );

            CommandResult resultStr = resourceHostByContainerId
                    .execute ( new RequestBuilder ( "grep vlan /mnt/lib/lxc/" + config.getClusterName() + "/config" ) );

            String stdOut = resultStr.getStdOut ();

            String vlanString = stdOut.substring ( 11, 14 );

            resourceHostByContainerId.execute ( new RequestBuilder ( "subutai proxy del " + vlanString + " -d" ) );

            resourceHostByContainerId.execute ( new RequestBuilder (
                    "subutai proxy add " + vlanString + " -d \"*." + config.getUserDomain () + "\" -f /mnt/lib/lxc/"
                            + config.getClusterName() + "/rootfs/etc/nginx/ssl.pem" ) );

            resourceHostByContainerId
                    .execute ( new RequestBuilder ( "subutai proxy add " + vlanString + " -h " + ipAddress ) );
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
        CommandResult result = ch.execute( new RequestBuilder ( command ).withTimeout( 10000 ) );

        if ( result.getExitCode() != 0 )
        {
            throw new CommandException( format( "Error to execute command: %s. %s", command, result.getStdErr() ) );
        }
    }

}
