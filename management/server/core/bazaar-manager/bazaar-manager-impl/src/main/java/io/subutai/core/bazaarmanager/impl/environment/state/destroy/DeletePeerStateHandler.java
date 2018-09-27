package io.subutai.core.bazaarmanager.impl.environment.state.destroy;


import java.util.Set;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.environment.state.Context;
import io.subutai.core.bazaarmanager.impl.environment.state.StateHandler;
import io.subutai.core.bazaarmanager.impl.processor.port_map.DestroyPortMap;
import io.subutai.core.bazaarmanager.impl.tunnel.TunnelHelper;
import io.subutai.bazaar.share.dto.domain.ContainerPortMapDto;
import io.subutai.bazaar.share.dto.domain.PortMapDto;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto;


public class DeletePeerStateHandler extends StateHandler
{

    private static final String GET_ENVIRONMENT_PORT_MAP = "/rest/v1/environments/%s/ports/map";


    public DeletePeerStateHandler( Context ctx )
    {
        super( ctx, "Deleting peer" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws BazaarManagerException
    {
        try
        {
            logStart();

            Environment env = getEnvironment( peerDto );

            log.info( "env: {}", env );

            boolean isBazaarEnvironment = env == null || Common.BAZAAR_ID.equals( env.getPeerId() );

            if ( isBazaarEnvironment )
            {
                deleteBazaarEnvironment( peerDto );
            }
            else
            {
                deleteLocalEnvironment( env );
            }

            logEnd();

            return null;
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e );
        }
    }


    private void deleteLocalEnvironment( Environment env )
            throws EnvironmentDestructionException, EnvironmentNotFoundException
    {
        cleanTunnels( env.getId() );

        ctx.envManager.destroyEnvironment( env.getId(), false );
    }


    private Environment getEnvironment( EnvironmentPeerDto peerDto )
    {
        String envId = peerDto.getEnvironmentInfo().getId();

        for ( Environment env : ctx.envManager.getEnvironments() )
        {
            if ( envId.equals( env.getId() ) )
            {
                return env;
            }
        }

        return null;
    }


    private void deleteBazaarEnvironment( EnvironmentPeerDto peerDto ) throws PeerException
    {
        EnvironmentId envId = new EnvironmentId( peerDto.getEnvironmentInfo().getId() );

        cleanTunnels( peerDto.getEnvironmentInfo().getId() );

        cleanPortMap( peerDto.getEnvironmentInfo().getId() );

        ctx.localPeer.cleanupEnvironment( envId );

        ctx.envManager.notifyOnEnvironmentDestroyed( envId.getId() );

        ctx.envUserHelper.handleEnvironmentOwnerDeletion( peerDto );
    }


    private void cleanPortMap( String environmentId )
    {
        try
        {
            RestResult<ContainerPortMapDto> result = ctx.restClient
                    .get( String.format( GET_ENVIRONMENT_PORT_MAP, environmentId ), ContainerPortMapDto.class );

            DestroyPortMap destroyPortMap = new DestroyPortMap( ctx );

            for ( PortMapDto portMapDto : result.getEntity().getContainerPorts() )
            {
                destroyPortMap.deleteMap( portMapDto );
            }
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
    }


    private void cleanTunnels( String environmentId )
    {
        try
        {
            Set<ContainerHost> containerHosts = ctx.localPeer.findContainersByEnvironmentId( environmentId );

            for ( ContainerHost containerHost : containerHosts )
            {
                deleteTunnel( containerHost.getIp() );
            }
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
    }


    private void deleteTunnel( final String ip )
    {
        TunnelHelper.deleteAllTunnelsForIp( ctx.localPeer.getResourceHosts(), ip );
    }


    @Override
    protected String getToken( EnvironmentPeerDto peerDto )
    {
        try
        {
            UserToken userToken = ctx.envUserHelper.getUserTokenFromBazaar( peerDto.getSsUserId() );
            return userToken.getFullToken();
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
        return null;
    }


    @Override
    protected RestResult<Object> post( EnvironmentPeerDto peerDto, Object body )
    {
        return ctx.restClient.delete( path( "/rest/v1/environments/%s/peers/%s", peerDto ) );
    }
}