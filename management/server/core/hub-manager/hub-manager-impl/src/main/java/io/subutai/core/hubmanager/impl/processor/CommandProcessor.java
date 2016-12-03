package io.subutai.core.hubmanager.impl.processor;


import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.OutputRedirection;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.Host;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.hub.share.dto.host.CommandBatchDto;
import io.subutai.hub.share.dto.host.CommandRequestDto;
import io.subutai.hub.share.dto.host.CommandResponseDto;


public class CommandProcessor implements StateLinkProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( CommandProcessor.class.getName() );

    private static final String STATE_LINK_PATTERN = "/rest/v2/peers/.*/hosts/execute";
    private static final Pattern PATTERN = Pattern.compile( STATE_LINK_PATTERN );

    private final Context context;
    private ExecutorService pool = Executors.newCachedThreadPool();


    public CommandProcessor( final Context context )
    {
        this.context = context;
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks )
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
            CommandBatchDto commandBatchDto = context.restClient.getStrict( link, CommandBatchDto.class );

            if ( commandBatchDto != null )
            {
                for ( CommandRequestDto commandRequestDto : commandBatchDto.getCommandRequestDtos() )
                {

                    pool.execute( new CommandTask( link, commandRequestDto ) );
                }
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error obtaining command requests from Hub: {}", ex.getMessage() );
        }
    }


    private class CommandTask implements Runnable
    {
        private final String link;
        private final CommandRequestDto commandRequestDto;


        CommandTask( final String link, final CommandRequestDto commandRequestDto )
        {
            this.link = link;
            this.commandRequestDto = commandRequestDto;
        }


        @Override
        public void run()
        {
            CommandResponseDto commandResponseDto;

            try
            {
                Host host = context.localPeer.findHost( commandRequestDto.getHostId() );

                CommandResult commandResult = host.execute( new RequestBuilder( commandRequestDto.getCommand() )
                        .withTimeout( commandRequestDto.getTimeout() ).withStdOutRedirection(
                                commandRequestDto.grabOutput() ? OutputRedirection.RETURN : OutputRedirection.NO ) );

                commandResponseDto =
                        new CommandResponseDto( commandRequestDto.getHostId(), commandRequestDto.getCommandId(),
                                commandResult.getExitCode(), commandResult.getStdOut(), commandResult.getStdErr(),
                                commandResult.hasTimedOut() );
            }
            catch ( Exception e )
            {
                LOG.error( "Error executing Hub command {}: {}", commandRequestDto, e.getMessage() );

                commandResponseDto =
                        new CommandResponseDto( commandRequestDto.getHostId(), commandRequestDto.getCommandId(),
                                e.getMessage() );
            }

            //send response
            sendResponse( link, commandResponseDto );
        }


        void sendResponse( final String link, final CommandResponseDto commandResponseDto )
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
