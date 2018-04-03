package io.subutai.core.hubmanager.impl.environment.state.build;


import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.dto.environment.EnvironmentTelemetryDto;
import io.subutai.hub.share.dto.environment.EnvironmentTelemetryOperation;


public class CheckNetworkStateHandler extends StateHandler
{
    public CheckNetworkStateHandler( Context ctx )
    {
        super( ctx, "Check networking" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws HubManagerException
    {
        ContainerHost containerHost = null;

        RequestBuilder command = null;

        CommandResult result = null;

        logStart();

        EnvironmentTelemetryDto etDto = peerDto.getEnvironmentTelemetryDto();

        for ( EnvironmentTelemetryOperation eto : etDto.getOperations() )
        {
            containerHost = getContainerHost( eto );

            if ( containerHost == null )
            {
                continue;
            }

            command = getCommand( eto );

            try
            {
                result = containerHost.execute( command );
            }
            catch ( Exception e )
            {
                log.error( e.getMessage() );
            }

            updateResult( eto, result );
        }

        logEnd();

        return peerDto;
    }


    private void updateResult( EnvironmentTelemetryOperation eto, CommandResult result )
    {
        if ( result == null || eto == null )
        {
            return;
        }

        if ( result.hasSucceeded() )
        {
            eto.setState( EnvironmentTelemetryOperation.State.SUCCESS );
            eto.setLogs( result.getStdOut() );
        }
        else
        {
            eto.setState( EnvironmentTelemetryOperation.State.FAILED );
            eto.setLogs( result.getStdErr() );
        }
    }


    private RequestBuilder getCommand( EnvironmentTelemetryOperation eto )
    {
        String cmd = String.format( eto.getCommand(), eto.getTargetHost() );

        return new RequestBuilder( cmd ).withTimeout( eto.getTimeout() );
    }


    private ContainerHost getContainerHost( EnvironmentTelemetryOperation eto )
    {
        try
        {
            return ctx.localPeer.getContainerHostById( eto.getContainerId() );
        }
        catch ( HostNotFoundException e )
        {
            log.error( e.getMessage() );
            return null;
        }
    }
}
