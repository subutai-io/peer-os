package io.subutai.core.executor.impl;


import java.util.UUID;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.Request;
import org.safehaus.subutai.common.command.RequestBuilder;
import io.subutai.core.executor.api.CommandExecutor;

import com.google.common.base.Preconditions;


/**
 * Implementation of CommandExecutor
 */
public class CommandExecutorImpl implements CommandExecutor
{

    protected CommandProcessor commandProcessor;


    public CommandExecutorImpl( CommandProcessor commandProcessor )
    {
        Preconditions.checkNotNull( commandProcessor );

        this.commandProcessor = commandProcessor;
    }


    @Override
    public CommandResult execute( final UUID hostId, final RequestBuilder requestBuilder ) throws CommandException
    {
        return execute( hostId, requestBuilder, new DummyCallback() );
    }


    @Override
    public CommandResult execute( final UUID hostId, final RequestBuilder requestBuilder,
                                  final CommandCallback callback ) throws CommandException
    {
        Preconditions.checkNotNull( hostId, "Invalid host id" );
        Preconditions.checkNotNull( requestBuilder, "Invalid request builder" );
        Preconditions.checkNotNull( requestBuilder, "Invalid callback" );

        Request request = requestBuilder.build( hostId );

        commandProcessor.execute( request, callback );

        return commandProcessor.getResult( request.getCommandId() );
    }


    @Override
    public void executeAsync( final UUID hostId, final RequestBuilder requestBuilder ) throws CommandException
    {
        executeAsync( hostId, requestBuilder, new DummyCallback() );
    }


    @Override
    public void executeAsync( final UUID hostId, final RequestBuilder requestBuilder, final CommandCallback callback )
            throws CommandException
    {
        Preconditions.checkNotNull( hostId, "Invalid host id" );
        Preconditions.checkNotNull( requestBuilder, "Invalid request builder" );
        Preconditions.checkNotNull( requestBuilder, "Invalid callback" );

        commandProcessor.execute( requestBuilder.build( hostId ), callback );
    }
}
