package org.safehaus.subutai.core.peer.api;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.util.CollectionUtil;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;


/**
 * Provide utility functions for command execution
 */
public class CommandUtil
{

    public CommandResult execute( RequestBuilder requestBuilder, Host host ) throws CommandException
    {

        Preconditions.checkNotNull( requestBuilder );
        Preconditions.checkNotNull( host );

        CommandResult result;

        result = host.execute( requestBuilder );

        if ( !result.hasSucceeded() )
        {
            throw new CommandException( String.format( "Error on container %s: %s", host.getHostname(),
                    result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
        }
        return result;
    }


    public Map<Host, CommandResult> executeSequential( RequestBuilder requestBuilder, Set<Host> hosts )
            throws CommandException
    {
        Preconditions.checkNotNull( requestBuilder );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( hosts ) );

        Map<Host, CommandResult> resultMap = Maps.newHashMap();

        for ( Host host : hosts )
        {
            resultMap.put( host, host.execute( requestBuilder ) );
        }

        return resultMap;
    }


    public Map<Host, CommandResult> executeParallel( RequestBuilder requestBuilder, Set<Host> hosts )
            throws CommandException
    {
        Preconditions.checkNotNull( requestBuilder );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( hosts ) );

        final Map<Host, CommandResult> resultMap = Maps.newConcurrentMap();
        ExecutorService taskExecutor = Executors.newFixedThreadPool( hosts.size() );
        CompletionService<HostCommandResult> taskCompletionService = new ExecutorCompletionService<>( taskExecutor );

        for ( Host host : hosts )
        {
            taskCompletionService.submit( new CommandTask( host, requestBuilder ) );
        }

        for ( int i = 0; i < hosts.size(); i++ )
        {
            try
            {
                Future<HostCommandResult> result = taskCompletionService.take();
                HostCommandResult hostCommandResult = result.get();
                resultMap.put( hostCommandResult.getHost(), hostCommandResult.commandResult );
            }
            catch ( ExecutionException | InterruptedException e )
            {
                throw new CommandException( e );
            }
        }

        taskExecutor.shutdown();

        return resultMap;
    }


    private class HostCommandResult
    {
        private Host host;
        private CommandResult commandResult;


        public HostCommandResult( final Host host, final CommandResult commandResult )
        {
            this.host = host;
            this.commandResult = commandResult;
        }


        public Host getHost()
        {
            return host;
        }


        public CommandResult getCommandResult()
        {
            return commandResult;
        }
    }


    private class CommandTask implements Callable<HostCommandResult>
    {
        private Host host;
        private RequestBuilder requestBuilder;


        public CommandTask( final Host host, final RequestBuilder requestBuilder )
        {
            this.host = host;
            this.requestBuilder = requestBuilder;
        }


        @Override
        public HostCommandResult call() throws Exception
        {
            return new HostCommandResult( host, host.execute( requestBuilder ) );
        }
    }
}
