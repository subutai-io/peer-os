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
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.hub.share.dto.host.ResourceHostCommandBatchDto;
import io.subutai.hub.share.dto.host.ResourceHostCommandRequestDto;
import io.subutai.hub.share.dto.host.ResourceHostCommandResponseDto;


public class ResourceHostCommandProcessor implements StateLinkProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( ResourceHostCommandProcessor.class.getName() );

    private static final String STATE_LINK_PATTERN = "/rest/v2/peers/.*/resourcehosts/execute";
    private static final Pattern PATTERN = Pattern.compile( STATE_LINK_PATTERN );

    private final Context context;
    private ExecutorService pool = Executors.newCachedThreadPool();


    public ResourceHostCommandProcessor( final Context context )
    {
        this.context = context;
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws Exception
    {
        boolean fastMode = false;

        for ( String link : stateLinks )
        {
            Matcher matcher = PATTERN.matcher( link );

            if ( matcher.matches() )
            {
                process( link );

                fastMode = true;
            }
        }

        return fastMode;
    }


    protected void process( final String link )
    {
        try
        {
            ResourceHostCommandBatchDto commandBatchDto =
                    context.restClient.getStrict( link, ResourceHostCommandBatchDto.class );

            if ( commandBatchDto != null )
            {
                for ( ResourceHostCommandRequestDto commandRequestDto : commandBatchDto
                        .getResourceHostCommandRequestDtos() )
                {

                    pool.execute( new ResourceHostCommandTask( link, commandRequestDto ) );
                }
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error obtaining command requests from Hub: {}", ex.getMessage() );
        }
    }


    private class ResourceHostCommandTask implements Runnable
    {
        private final String link;
        private final ResourceHostCommandRequestDto commandRequestDto;


        ResourceHostCommandTask( final String link, final ResourceHostCommandRequestDto commandRequestDto )
        {
            this.link = link;
            this.commandRequestDto = commandRequestDto;
        }


        @Override
        public void run()
        {
            ResourceHostCommandResponseDto commandResponseDto;

            try
            {
                ResourceHost resourceHost =
                        context.localPeer.getResourceHostById( commandRequestDto.getResourceHostId() );

                CommandResult commandResult = resourceHost.execute( new RequestBuilder( commandRequestDto.getCommand() )
                        .withTimeout( commandRequestDto.getTimeout() ) );

                commandResponseDto = new ResourceHostCommandResponseDto( commandRequestDto.getResourceHostId(),
                        commandRequestDto.getCommandId(), commandResult.getExitCode(), commandResult.getStdOut(),
                        commandResult.getStdErr(), commandResult.hasTimedOut() );
            }
            catch ( Exception e )
            {
                LOG.error( "Error executing Hub command {}: {}", commandRequestDto, e.getMessage() );

                commandResponseDto = new ResourceHostCommandResponseDto( commandRequestDto.getResourceHostId(),
                        commandRequestDto.getCommandId(), e.getMessage() );
            }

            //send response
            sendResponse( link, commandResponseDto );
        }


        void sendResponse( final String link, final ResourceHostCommandResponseDto commandResponseDto )
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
