package io.subutai.core.bazaarmanager.impl.requestor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandException;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.settings.Common;
import io.subutai.core.desktop.api.DesktopManager;
import io.subutai.core.bazaarmanager.api.BazaarRequester;
import io.subutai.core.bazaarmanager.api.RestClient;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.BazaarManagerImpl;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.bazaar.share.dto.environment.ContainerStateDto;
import io.subutai.bazaar.share.dto.environment.container.ContainerDesktopInfoDto;
import io.subutai.bazaar.share.dto.environment.container.ContainerEventDto;


public class ContainerEventProcessor extends BazaarRequester
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private PeerManager peerManager;
    private DesktopManager desktopManager;


    public ContainerEventProcessor( final BazaarManagerImpl bazaarManager, final PeerManager peerManager,
                                    final RestClient restClient, final DesktopManager desktopManager )
    {
        super( bazaarManager, restClient );
        this.peerManager = peerManager;
        this.desktopManager = desktopManager;
    }


    @Override
    public void request() throws BazaarManagerException
    {
        try
        {
            for ( ResourceHost rh : peerManager.getLocalPeer().getResourceHosts() )
            {
                sendContainerStates( rh );
            }
        }
        catch ( Exception e )
        {
            log.error( "Oops error: ", e.getMessage() );
        }
    }


    private void sendContainerStates( ResourceHost rh ) throws BazaarManagerException
    {
        log.info( "ResourceHost: id={}, hostname={}, containers={}", rh.getId(), rh.getHostname(),
                rh.getContainerHosts().size() );

        for ( ContainerHost ch : rh.getContainerHosts() )
        {
            if ( !Common.MANAGEMENT_HOSTNAME.equals( ch.getContainerName() ) )
            {
                sendContainerState( ch );
            }
        }
    }


    private void sendContainerState( ContainerHost ch ) throws BazaarManagerException
    {
        log.info( "- ContainerHost: id={}, name={}, environmentId={}, state={}", ch.getId(), ch.getContainerName(),
                ch.getEnvironmentId(), ch.getState() );

        ContainerStateDto state = ContainerStateDto.valueOf( ch.getState().name() );
        ContainerEventDto dto = new ContainerEventDto( ch.getId(), ch.getEnvironmentId().getId(), state );

        Boolean isDesktop = null;
        if ( !desktopManager.existInCache( ch.getId() ) )
        {
            try
            {
                //get information about desktop env and remote desktop server
                String deskEnv = desktopManager.getDesktopEnvironmentInfo( ch );
                String rDServer = desktopManager.getRDServerInfo( ch );

                if ( !deskEnv.isEmpty() && !rDServer.isEmpty() )
                {
                    //add to cache as a desktop container
                    ContainerDesktopInfoDto desktopInfo = new ContainerDesktopInfoDto( ch.getId(), deskEnv, rDServer );
                    dto.setDesktopInfo( desktopInfo );
                    try
                    {
                        desktopManager.createDesktopUser( ch );
                    }
                    catch ( Exception e )
                    {
                        log.error( e.getMessage() );
                    }
                    isDesktop = true;
                }
                else
                {
                    //add to cache as not desktop container
                    isDesktop = false;
                }
            }
            catch ( CommandException e )
            {
                log.error( e.getMessage() );
            }
        }

        try
        {
            desktopManager.copyKeys( ch );
        }
        catch ( CommandException e )
        {
            log.error( "Could not copy SSH keys to x2go usr" );
        }

        RestResult res = doRequest( dto );

        if ( isDesktop != null )
        {
            if ( isDesktop )
            {
                desktopManager.hostIsDesktop( ch.getId() );
            }
            else
            {
                desktopManager.hostIsNotDesktop( ch.getId() );
            }
        }

        log.info( "Response status: {}", res.getStatus() );
    }


    private RestResult doRequest( ContainerEventDto dto ) throws BazaarManagerException
    {
        try
        {
            String path = String.format( "/rest/v1/containers/%s/events", dto.getContainerId() );

            return restClient.post( path, dto );
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e );
        }
    }
}
