package io.subutai.core.executor.impl;


import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.karaf.jaas.boot.principal.RolePrincipal;

import com.google.common.base.Preconditions;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.Request;
import io.subutai.common.command.RequestBuilder;
import io.subutai.core.executor.api.CommandExecutor;


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
    public CommandResult execute( final String hostId, final RequestBuilder requestBuilder ) throws CommandException
    {
        return execute( hostId, requestBuilder, new DummyCallback() );
    }


    @Override
    public CommandResult execute( final String hostId, final RequestBuilder requestBuilder,
                                  final CommandCallback callback ) throws CommandException
    {
        Preconditions.checkNotNull( hostId, "Invalid host id" );
        Preconditions.checkNotNull( requestBuilder, "Invalid request builder" );
        Preconditions.checkNotNull( requestBuilder, "Invalid callback" );

        Request request = requestBuilder.build( hostId );

        RolePrincipal rolePrincipal = new RolePrincipal( "Peer-Management|Update" );
        AccessControlContext acc = AccessController.getContext();
        Subject subject = Subject.getSubject( acc );
        if ( subject != null )
        {
            Set<Principal> principalSet = subject.getPrincipals();
            if ( principalSet != null )
            {
                principalSet.add( rolePrincipal );
                commandProcessor.executeSystemCall( request, callback );
                principalSet.remove( rolePrincipal );
            }
        }
        return commandProcessor.getResult( request.getCommandId() );
    }


    @Override
    public CommandResult authorizedExecute( final String hostId, final RequestBuilder requestBuilder )
            throws CommandException
    {
        return authorizedExecute( hostId, requestBuilder, new DummyCallback() );
    }


    @Override
    public CommandResult authorizedExecute( final String hostId, final RequestBuilder requestBuilder,
                                            final CommandCallback callback ) throws CommandException
    {
        Preconditions.checkNotNull( hostId, "Invalid host id" );
        Preconditions.checkNotNull( requestBuilder, "Invalid request builder" );
        Preconditions.checkNotNull( requestBuilder, "Invalid callback" );

        Request request = requestBuilder.build( hostId );

        commandProcessor.executeSystemCall( request, callback );

        return commandProcessor.getResult( request.getCommandId() );
    }


    @Override
    public void executeAsync( final String hostId, final RequestBuilder requestBuilder ) throws CommandException
    {
        executeAsync( hostId, requestBuilder, new DummyCallback() );
    }


    @Override
    public void executeAsync( final String hostId, final RequestBuilder requestBuilder, final CommandCallback callback )
            throws CommandException
    {
        Preconditions.checkNotNull( hostId, "Invalid host id" );
        Preconditions.checkNotNull( requestBuilder, "Invalid request builder" );
        Preconditions.checkNotNull( requestBuilder, "Invalid callback" );

        RolePrincipal rolePrincipal = new RolePrincipal( "Peer-Management|Update" );
        AccessControlContext acc = AccessController.getContext();
        Subject subject = Subject.getSubject( acc );
        if ( subject != null )
        {
            Set<Principal> principalSet = subject.getPrincipals();
            if ( principalSet != null )
            {
                principalSet.add( rolePrincipal );
                commandProcessor.executeSystemCall( requestBuilder.build( hostId ), callback );
                principalSet.remove( rolePrincipal );
            }
        }
    }


    @Override
    public void authorizedExecuteAsync( final String hostId, final RequestBuilder requestBuilder ) throws CommandException
    {
        executeAsync( hostId, requestBuilder, new DummyCallback() );
    }


    @Override
    public void authorizedExecuteAsync( final String hostId, final RequestBuilder requestBuilder, final CommandCallback callback )
            throws CommandException
    {
        Preconditions.checkNotNull( hostId, "Invalid host id" );
        Preconditions.checkNotNull( requestBuilder, "Invalid request builder" );
        Preconditions.checkNotNull( requestBuilder, "Invalid callback" );

        commandProcessor.executeSystemCall( requestBuilder.build( hostId ), callback );
    }
}
