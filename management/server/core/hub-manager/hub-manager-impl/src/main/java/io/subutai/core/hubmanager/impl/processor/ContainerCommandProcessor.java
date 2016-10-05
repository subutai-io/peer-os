package io.subutai.core.hubmanager.impl.processor;


import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.hub.share.dto.environment.container.ContainerCommandBatchDto;
import io.subutai.hub.share.dto.environment.container.ContainerCommandRequestDto;
import io.subutai.hub.share.dto.environment.container.ContainerCommandResponseDto;


public class ContainerCommandProcessor implements StateLinkProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( ContainerCommandProcessor.class.getName() );

    private static final String STATE_LINK_PATTERN = "/rest/v2/peers/.*/execute";
    private static final Pattern PATTERN = Pattern.compile( STATE_LINK_PATTERN );

    private final Context context;
    private ExecutorService pool = Executors.newCachedThreadPool();


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
                process( link );
            }
        }

        return false;
    }


    protected void process( final String link )
    {
        try
        {
            ContainerCommandBatchDto commandBatchDto =
                    context.restClient.getStrict( link, ContainerCommandBatchDto.class );

            if ( commandBatchDto != null )
            {
                for ( ContainerCommandRequestDto commandRequestDto : commandBatchDto.getCommandRequestDtos() )
                {

                    pool.execute( new ContainerCommandTask( link, commandRequestDto ) );
                }
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error obtaining command requests from Hub: {}", ex.getMessage() );
        }
    }


    private class ContainerCommandTask implements Runnable
    {
        private final String link;
        private final ContainerCommandRequestDto commandRequestDto;


        ContainerCommandTask( final String link, final ContainerCommandRequestDto commandRequestDto )
        {
            this.link = link;
            this.commandRequestDto = commandRequestDto;
        }


        @Override
        public void run()
        {
            ContainerCommandResponseDto commandResponseDto;

            try
            {
                ContainerHost containerHost =
                        context.localPeer.getContainerHostById( commandRequestDto.getContainerId() );

                CommandResult commandResult = containerHost.execute(
                        new RequestBuilder( commandRequestDto.getCommand() )
                                .withTimeout( commandRequestDto.getTimeout() ) );

                commandResponseDto = new ContainerCommandResponseDto( commandRequestDto.getContainerId(),
                        commandRequestDto.getCommandId(), commandResult.getExitCode(), commandResult.getStdOut(),
                        commandResult.getStdErr(), commandResult.hasTimedOut() );
            }
            catch ( Exception e )
            {
                LOG.error( "Error executing Hub command {}: {}", commandRequestDto, e.getMessage() );

                commandResponseDto = new ContainerCommandResponseDto( commandRequestDto.getContainerId(),
                        commandRequestDto.getCommandId(), e.getMessage() );
            }

            //send response
            sendResponse( link, commandResponseDto );
        }


        void sendResponse( final String link, final ContainerCommandResponseDto commandResponseDto )
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
                    LOG.error( "Error sending command response {} to Hub: {}", commandResponseDto, e.getMessage() );
                }
            }
        }
    }
}
