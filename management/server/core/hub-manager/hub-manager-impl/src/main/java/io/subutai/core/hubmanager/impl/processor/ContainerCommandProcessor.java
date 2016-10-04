package io.subutai.core.hubmanager.impl.processor;


import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.hub.share.dto.environment.container.ContainerCommandRequestDto;
import io.subutai.hub.share.dto.environment.container.ContainerCommandResponseDto;


public class ContainerCommandProcessor implements StateLinkProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( ContainerCommandProcessor.class.getName() );

    private static final String STATE_LINK_PATTERN = "/rest/v2/peers/.*/execute";
    private static final Pattern PATTERN = Pattern.compile( STATE_LINK_PATTERN );

    private final Context context;


    public ContainerCommandProcessor( final Context context )
    {
        this.context = context;
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws Exception
    {
        for ( String link : stateLinks )
        {
            Matcher matcher = PATTERN.matcher( link );
            if ( matcher.matches() )
            {
                executeCommand( link );
            }
        }

        return false;
    }


    private void executeCommand( final String link )
    {

        ContainerCommandRequestDto commandRequestDto = null;
        ContainerCommandResponseDto commandResponseDto = null;

        try
        {
            commandRequestDto = context.restClient.getStrict( link, ContainerCommandRequestDto.class );

            if ( commandRequestDto != null )
            {
                ContainerHost containerHost =
                        context.localPeer.getContainerHostById( commandRequestDto.getContainerId() );

                CommandResult commandResult = containerHost.execute(
                        new RequestBuilder( commandRequestDto.getCommand() )
                                .withTimeout( commandRequestDto.getTimeout() ) );

                commandResponseDto = new ContainerCommandResponseDto( commandRequestDto.getContainerId(),
                        commandRequestDto.getCommand(), commandResult.getExitCode(), commandResult.getStdOut(),
                        commandResult.getStdErr(), commandResult.hasTimedOut() );
            }
        }
        catch ( Exception ex )
        {
            if ( commandRequestDto != null )
            {
                commandResponseDto = new ContainerCommandResponseDto( commandRequestDto.getContainerId(),
                        commandRequestDto.getCommandId(), ex.getMessage() );
            }
        }


        //send response
        sendResponse( link, commandResponseDto );
    }


    protected void sendResponse( final String link, final ContainerCommandResponseDto commandResponseDto )
    {
        if ( commandResponseDto != null )
        {
            LOG.debug( "COMMAND RESPONSE: {}", commandResponseDto );

            try
            {
                context.restClient.post( link, commandResponseDto );
            }
            catch ( Exception e )
            {
                LOG.error( "Error sending command response to Hub: {}", e.getMessage() );
            }
        }
    }
}
