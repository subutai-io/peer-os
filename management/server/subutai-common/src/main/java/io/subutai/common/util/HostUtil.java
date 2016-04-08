package io.subutai.common.util;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


public class HostUtil<T>
{
    private static final Logger LOG = LoggerFactory.getLogger( HostUtil.class );

    private final Set<HostTask<T>> tasks = Sets.newHashSet();


    public void addTask( HostTask<T> task )
    {
        Preconditions.checkNotNull( task, "Invalid task" );

        this.tasks.add( task );
    }


    public HostResults<T> execute()
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( tasks ), "No task found for execution" );

        Set<HostResult<T>> results = executeParallel( tasks );

        tasks.clear();

        return new HostResults<>( results );
    }


    public abstract static class HostTask<T> implements Callable<T>
    {
        /**
         * Maximum instances of this task that can be run in parallel on a given host.
         *
         * A value less than or equal to 0 indicates unlimited number of parallel tasks
         */
        abstract int maxParallelTasks();
    }


    public static class HostResult<T>
    {
        private final HostTask<T> task;
        private T result;
        private Exception exception;
        private boolean hasSucceeded = true;


        protected HostResult( final HostTask<T> task, final T result )
        {
            this.task = task;
            this.result = result;
        }


        protected HostResult( final HostTask<T> task, final Exception exception )
        {
            this.task = task;
            this.exception = exception;
            this.hasSucceeded = false;
        }


        public T getResult()
        {
            return result;
        }


        public Exception getException()
        {
            return exception;
        }


        public String getFailureReason()
        {
            return exception == null ? "Unknown" : exception.getMessage();
        }


        public boolean hasSucceeded()
        {
            return hasSucceeded;
        }


        public HostTask<T> getTask()
        {
            return task;
        }
    }


    public static class HostResults<T>
    {
        private final Set<HostResult<T>> hostResults;
        private boolean hasFailures = false;


        protected HostResults( final Set<HostResult<T>> hostResults )
        {
            Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( hostResults ) );

            this.hostResults = hostResults;

            for ( HostResult taskResult : hostResults )
            {
                if ( !taskResult.hasSucceeded() )
                {
                    hasFailures = true;

                    break;
                }
            }
        }


        public boolean hasFailures()
        {
            return hasFailures;
        }


        public Set<HostResult<T>> getHostResults()
        {
            return hostResults;
        }
    }


    protected Set<HostResult<T>> executeParallel( Set<HostTask<T>> tasks )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( tasks ) );

        Set<HostResult<T>> taskResults = Sets.newHashSet();

        ExecutorService taskExecutor = Executors.newFixedThreadPool( tasks.size() );
        CompletionService<T> taskCompletionService = new ExecutorCompletionService<>( taskExecutor );

        Map<HostTask<T>, Future<T>> taskFutures = Maps.newHashMap();

        for ( HostTask<T> task : tasks )
        {
            taskFutures.put( task, taskCompletionService.submit( task ) );
        }

        taskExecutor.shutdown();

        for ( Map.Entry<HostTask<T>, Future<T>> futureEntry : taskFutures.entrySet() )
        {
            try
            {
                taskResults.add( new HostResult<>( futureEntry.getKey(), futureEntry.getValue().get() ) );
            }
            catch ( Exception e )
            {
                LOG.error( "Error executing task ", e );

                taskResults.add( new HostResult<>( futureEntry.getKey(), e ) );
            }
        }

        return taskResults;
    }
}
