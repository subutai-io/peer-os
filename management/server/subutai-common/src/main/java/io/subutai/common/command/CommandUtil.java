package io.subutai.common.command;


import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.peer.Host;
import io.subutai.common.util.CollectionUtil;


/**
 * Provide utility functions for command execution
 */
public class CommandUtil
{
    private static final int MAX_EXECUTOR_SIZE = 10;

    private static final Logger LOG = LoggerFactory.getLogger( CommandUtil.class );

    private final Map<String, Map<String, EnvironmentCommandFuture>> environmentCommandsFuturesMap =
            Maps.newConcurrentMap();


    /**
     * Allows to execute command on host. Throws CommandException if command has completed with status other then
     * SUCCEEDED.
     *
     * @param requestBuilder - request
     * @param host - host
     *
     * @return -  command result
     *
     * @throws CommandException - exception thrown if something went wrong
     */
    public CommandResult execute( RequestBuilder requestBuilder, Host host ) throws CommandException
    {

        Preconditions.checkNotNull( requestBuilder );
        Preconditions.checkNotNull( host );

        CommandResult result = host.execute( requestBuilder );

        if ( !result.hasSucceeded() )
        {
            throw new CommandException( String.format( "Error executing command on host %s: %s", host.getHostname(),
                    result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
        }
        return result;
    }


    public CommandResult execute( RequestBuilder requestBuilder, Host host, CommandCallback callback )
            throws CommandException
    {

        Preconditions.checkNotNull( requestBuilder );
        Preconditions.checkNotNull( host );

        CommandResult result = host.execute( requestBuilder, callback );

        if ( !result.hasSucceeded() )
        {
            throw new CommandException( String.format( "Error executing command on host %s: %s", host.getHostname(),
                    result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
        }
        return result;
    }


    /**
     * Execute request on host with callback. Allows to stop callback from being triggered by calling stop() from inside
     * a callback. Please make sure that the command is not a daemon command (command which forks a daemon process).
     * Otherwise please set request as a daemon request by calling RequestBuilder.daemon()
     *
     * @param requestBuilder - request
     * @param host - host
     * @param callback - stoppable callback
     *
     * @throws CommandException - exception thrown if something went wrong
     */
    public void executeAsync( RequestBuilder requestBuilder, Host host, final StoppableCallback callback )
            throws CommandException
    {

        Preconditions.checkNotNull( requestBuilder );
        Preconditions.checkNotNull( host );
        Preconditions.checkNotNull( callback );

        host.executeAsync( requestBuilder, new CommandCallback()
        {
            @Override
            public void onResponse( final Response response, final CommandResult commandResult )
            {
                if ( !callback.isStopped() )
                {
                    callback.onResponse( response, commandResult );
                }
            }
        } );
    }


    /**
     * Allows to execute the same command on multiple hosts in parallel, with the same callback for responses from each
     * host
     *
     * @param requestBuilder - request
     * @param hosts - hosts
     * @param callback - callback
     *
     * @throws CommandException - exception thrown if something went wrong
     */

    public void executeAsync( RequestBuilder requestBuilder, Set<Host> hosts, final CommandCallback callback )
            throws CommandException
    {

        Preconditions.checkNotNull( requestBuilder );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( hosts ) );
        Preconditions.checkNotNull( callback );

        final ReentrantLock lock = new ReentrantLock( true );

        for ( Host host : hosts )
        {
            host.executeAsync( requestBuilder, new CommandCallback()
            {
                @Override
                public void onResponse( final Response response, final CommandResult commandResult )
                {
                    lock.lock();
                    try
                    {
                        callback.onResponse( response, commandResult );
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
            } );
        }
    }


    /**
     * Allows to execute command on each host one-by-one
     *
     * @param requestBuilder - request
     * @param hosts - hosts
     *
     * @return -  map containing command results
     *
     * @throws CommandException - exception thrown if something went wrong
     */
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


    /**
     * Executes command on hosts in parallel
     *
     * Returns results of commands
     *
     * @return {@code HostCommandResults}
     */
    public HostCommandResults execute( final RequestBuilder requestBuilder, Set<Host> hosts, String environmentId )
    {
        return executeParallel( requestBuilder, hosts, false, environmentId );
    }


    /**
     * Executes command on hosts in parallel. Fails fast if any execution failed.
     *
     * Returns results of commands completed so far
     *
     * @return {@code HostCommandResults}
     */
    public HostCommandResults executeFailFast( final RequestBuilder requestBuilder, Set<Host> hosts,
                                               String environmentId )
    {
        return executeParallel( requestBuilder, hosts, true, environmentId );
    }


    protected HostCommandResults executeParallel( final RequestBuilder requestBuilder, Set<Host> hosts,
                                                  boolean failFast, final String environmentId )
    {
        Preconditions.checkNotNull( requestBuilder );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( hosts ) );

        final Set<HostCommandResult> hostCommandResults = Sets.newHashSet();

        ExecutorService taskExecutor = Executors.newFixedThreadPool( Math.min( MAX_EXECUTOR_SIZE, hosts.size() ) );

        CompletionService<CommandResult> taskCompletionService = new ExecutorCompletionService<>( taskExecutor );

        Map<Host, Future<CommandResult>> commandFutures = Maps.newHashMap();

        final String hostCommandPrefix = String.valueOf( System.currentTimeMillis() );

        for ( final Host host : hosts )
        {
            final String hostCommandId = String.format( "%s-%s", host.getId(), hostCommandPrefix );

            Future<CommandResult> commandFuture = taskCompletionService.submit( new Callable<CommandResult>()
            {
                @Override
                public CommandResult call() throws Exception
                {
                    try
                    {
                        return execute( requestBuilder, host );
                    }
                    finally
                    {
                        if ( environmentId != null )
                        {
                            synchronized ( environmentCommandsFuturesMap )
                            {
                                Map<String, EnvironmentCommandFuture> environmentCommandFutures =
                                        environmentCommandsFuturesMap.get( environmentId );

                                if ( environmentCommandFutures != null )
                                {
                                    environmentCommandFutures.remove( hostCommandId );

                                    if ( environmentCommandFutures.isEmpty() )
                                    {
                                        environmentCommandsFuturesMap.remove( environmentId );
                                    }
                                }
                            }
                        }
                    }
                }
            } );


            if ( environmentId != null )
            {
                synchronized ( environmentCommandsFuturesMap )
                {
                    Map<String, EnvironmentCommandFuture> environmentCommandFutures =
                            environmentCommandsFuturesMap.get( environmentId );

                    if ( environmentCommandFutures == null )
                    {
                        environmentCommandFutures = Maps.newHashMap();

                        environmentCommandsFuturesMap.put( environmentId, environmentCommandFutures );
                    }

                    environmentCommandFutures
                            .put( hostCommandId, new EnvironmentCommandFuture( environmentId, commandFuture ) );
                }
            }

            commandFutures.put( host, commandFuture );
        }

        taskExecutor.shutdown();


        futuresLoop:
        while ( !Thread.interrupted() && !commandFutures.isEmpty() )
        {
            Iterator<Map.Entry<Host, Future<CommandResult>>> mapIterator = commandFutures.entrySet().iterator();

            while ( mapIterator.hasNext() )
            {
                Map.Entry<Host, Future<CommandResult>> commandEntry = mapIterator.next();

                Host host = commandEntry.getKey();

                Future<CommandResult> future = commandEntry.getValue();

                try
                {
                    if ( future.isDone() )
                    {
                        mapIterator.remove();

                        hostCommandResults.add( new HostCommandResult( host, future.get() ) );
                    }
                }
                catch ( Exception e )
                {
                    if ( !future.isCancelled() )
                    {
                        LOG.error( "Error executing command on host {}", host.getHostname(), e );

                        hostCommandResults.add( new HostCommandResult( host, e ) );
                    }
                    else
                    {
                        hostCommandResults.add( new HostCommandResult( host, new CancellationException(
                                String.format( "Command was cancelled on host %s", host.getHostname() ) ) ) );
                    }

                    if ( failFast )
                    {
                        break futuresLoop;
                    }
                }
            }

            try
            {
                Thread.sleep( 100 );
            }
            catch ( InterruptedException e )
            {
                Thread.currentThread().interrupt();
            }
        }

        return new HostCommandResults( hostCommandResults );
    }


    public static class HostCommandResults
    {
        private final Set<HostCommandResult> commandResults;
        private boolean hasFailures = false;


        HostCommandResults( final Set<HostCommandResult> commandResults )
        {
            Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( commandResults ) );

            this.commandResults = commandResults;

            for ( HostCommandResult commandResult : commandResults )
            {
                if ( !commandResult.hasSucceeded() )
                {
                    hasFailures = true;

                    break;
                }
            }
        }


        public Set<HostCommandResult> getCommandResults()
        {
            return commandResults;
        }


        public boolean hasFailures()
        {
            return hasFailures;
        }
    }


    public static class HostCommandResult
    {
        private Host host;
        private CommandResult commandResult;
        private Exception exception;
        private boolean hasSucceeded = true;


        HostCommandResult( final Host host, final CommandResult commandResult )
        {
            this.host = host;
            this.commandResult = commandResult;
        }


        HostCommandResult( final Host host, final Exception exception )
        {
            this.host = host;
            this.exception = exception;
            this.hasSucceeded = false;
        }


        public Exception getException()
        {
            return exception;
        }


        public boolean hasSucceeded()
        {
            return hasSucceeded;
        }


        public Host getHost()
        {
            return host;
        }


        public CommandResult getCommandResult()
        {
            return commandResult;
        }


        public String getFailureReason()
        {
            return exception == null ? "Unknown" : exception.getMessage();
        }
    }


    /**
     * Callback that can be stopped from being triggered by calling stop() from inside onSuccess method
     */
    public static abstract class StoppableCallback implements CommandCallback
    {

        private final AtomicBoolean stopped = new AtomicBoolean( false );


        public final boolean isStopped()
        {
            return stopped.get();
        }


        public final void stop()
        {
            stopped.set( true );
        }
    }


    private class EnvironmentCommandFuture
    {
        private final String environmentId;
        private final Future<CommandResult> commandFuture;


        EnvironmentCommandFuture( final String environmentId, final Future<CommandResult> commandFuture )
        {
            this.environmentId = environmentId;
            this.commandFuture = commandFuture;
        }


        public String getEnvironmentId()
        {
            return environmentId;
        }


        Future<CommandResult> getCommandFuture()
        {
            return commandFuture;
        }
    }


    public boolean cancelEnvironmentCommands( String environmentId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        Map<String, EnvironmentCommandFuture> environmentCommandFutures =
                environmentCommandsFuturesMap.remove( environmentId );

        boolean hasActiveTasks = environmentCommandFutures != null && !environmentCommandFutures.isEmpty();

        if ( hasActiveTasks )
        {
            for ( EnvironmentCommandFuture environmentCommandFuture : environmentCommandFutures.values() )
            {
                environmentCommandFuture.getCommandFuture().cancel( true );
            }
        }

        return hasActiveTasks;
    }


    public void cancelAll()
    {
        for ( Map<String, EnvironmentCommandFuture> commandFutureMap : environmentCommandsFuturesMap.values() )
        {
            for ( EnvironmentCommandFuture commandFuture : commandFutureMap.values() )
            {
                commandFuture.getCommandFuture().cancel( true );
            }
        }

        environmentCommandsFuturesMap.clear();
    }


    public void dispose()
    {
        cancelAll();
    }
}
