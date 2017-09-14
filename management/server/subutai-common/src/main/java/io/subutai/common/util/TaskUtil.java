package io.subutai.common.util;


import java.util.Iterator;
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

import io.subutai.common.settings.Common;


public class TaskUtil<T>
{
    private static final Logger LOG = LoggerFactory.getLogger( TaskUtil.class );

    private final Set<Task<T>> tasks = Sets.newHashSet();


    public void addTask( Task<T> task )
    {
        Preconditions.checkNotNull( task, "Invalid task" );

        this.tasks.add( task );
    }


    public boolean hasTasks()
    {
        return !tasks.isEmpty();
    }


    /**
     * Executes tasks in parallel.
     */
    public TaskResults<T> executeParallel()
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( tasks ), "No task found for execution" );

        Set<TaskResult<T>> results = executeParallel( tasks, false );

        tasks.clear();

        return new TaskResults<>( results );
    }


    /**
     * Executes tasks in parallel. Fails fast if any execution failed
     *
     * Returns results of tasks completed so far
     */
    public TaskResults<T> executeParallelFailFast()
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( tasks ), "No task found for execution" );

        Set<TaskResult<T>> results = executeParallel( tasks, true );

        tasks.clear();

        return new TaskResults<>( results );
    }


    protected Set<TaskResult<T>> executeParallel( Set<Task<T>> tasks, boolean failFast )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( tasks ) );

        Set<TaskResult<T>> taskResults = Sets.newHashSet();

        ExecutorService taskExecutor =
                Executors.newFixedThreadPool( Math.min( Common.MAX_EXECUTOR_SIZE, tasks.size() ) );

        CompletionService<T> taskCompletionService = new ExecutorCompletionService<>( taskExecutor );

        Map<Task<T>, Future<T>> taskFutures = Maps.newHashMap();

        for ( Task<T> task : tasks )
        {
            taskFutures.put( task, taskCompletionService.submit( task ) );
        }

        taskExecutor.shutdown();


        futuresLoop:
        while ( !Thread.interrupted() && !taskFutures.isEmpty() )
        {

            Iterator<Map.Entry<Task<T>, Future<T>>> mapIterator = taskFutures.entrySet().iterator();

            while ( mapIterator.hasNext() )
            {

                Map.Entry<Task<T>, Future<T>> futureEntry = mapIterator.next();

                Task<T> task = futureEntry.getKey();

                Future<T> future = futureEntry.getValue();

                try
                {

                    if ( future.isDone() )
                    {
                        mapIterator.remove();

                        taskResults.add( new TaskResult<>( task, future.get() ) );
                    }
                }
                catch ( Exception e )
                {
                    LOG.error( "Error executing task ", e );

                    taskResults.add( new TaskResult<>( task, e ) );

                    if ( failFast )
                    {
                        break futuresLoop;
                    }
                }
            }

            sleep( 100 );
        }

        return taskResults;
    }


    public static <T> TaskResult<T> execute( Task<T> task )
    {
        TaskUtil<T> taskUtil = new TaskUtil<>();

        taskUtil.addTask( task );

        TaskUtil.TaskResults<T> taskResults = taskUtil.executeParallel();

        return taskResults.getResults().iterator().next();
    }


    public static abstract class Task<T> implements Callable<T>
    {
        // Reserved for future
    }


    public static class TaskResult<T>
    {
        private final Task<T> task;
        private T result;
        private Exception exception;
        private boolean hasSucceeded = true;


        protected TaskResult( final Task<T> task, final T result )
        {
            this.task = task;
            this.result = result;
        }


        protected TaskResult( final Task<T> task, final Exception exception )
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


        public Task<T> getTask()
        {
            return task;
        }
    }


    public static class TaskResults<T>
    {
        private final Set<TaskResult<T>> results;
        private boolean hasFailures = false;


        protected TaskResults( final Set<TaskResult<T>> results )
        {
            Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( results ) );

            this.results = results;

            for ( TaskResult taskResult : results )
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


        public Set<TaskResult<T>> getResults()
        {
            return results;
        }
    }


    public static void sleep( long millis )
    {
        try
        {
            Thread.sleep( millis );
        }
        catch ( InterruptedException e )
        {
            Thread.currentThread().interrupt();
        }
    }
}
