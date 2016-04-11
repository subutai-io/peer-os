package io.subutai.common.util;


import java.util.Collections;
import java.util.List;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.peer.Host;


public class HostUtil
{
    private static final int MAX_EXECUTOR_SIZE = 20;

    private static final Logger LOG = LoggerFactory.getLogger( HostUtil.class );

    private final Set<Task> tasks = Sets.newConcurrentHashSet();

    private Map<String, ExecutorService> taskExecutors = Maps.newConcurrentMap();


    public Set<Task> getTasks()
    {
        return Collections.unmodifiableSet( tasks );
    }


    public Set<Results> execute( Map<Host, Set<Task>> hostsTasks )
    {
        Preconditions.checkNotNull( hostsTasks, "Invalid hosts tasks map" );
        Preconditions.checkArgument( !hostsTasks.isEmpty(), "No tasks in hosts tasks map" );

        final Set<Results> resultsSet = Sets.newHashSet();

        ExecutorService tasksExecutor = Executors.newCachedThreadPool();
        CompletionService<Object> completionService = new ExecutorCompletionService<>( tasksExecutor );
        List<Future<Object>> taskFutures = Lists.newArrayList();

        for ( Map.Entry<Host, Set<Task>> hostTasks : hostsTasks.entrySet() )
        {
            final Host host = hostTasks.getKey();

            final Set<Task> tasks = hostTasks.getValue();

            taskFutures.add( completionService.submit( new Runnable()
            {
                @Override
                public void run()
                {
                    resultsSet.add( execute( host, tasks ) );
                }
            }, null ) );
        }

        tasksExecutor.shutdown();

        for ( Future future : taskFutures )
        {
            try
            {
                future.get();
            }
            catch ( Exception e )
            {
                LOG.error( "Error in #execute", e );
            }
        }

        return resultsSet;
    }


    public Set<Results> executeUntilFirstFailure( Map<Host, Set<Task>> hostsTasks )
    {
        Preconditions.checkNotNull( hostsTasks, "Invalid hosts tasks map" );
        Preconditions.checkArgument( !hostsTasks.isEmpty(), "No tasks in hosts tasks map" );

        final Set<Results> resultsSet = Sets.newHashSet();

        ExecutorService tasksExecutor = Executors.newCachedThreadPool();
        CompletionService<Boolean> completionService = new ExecutorCompletionService<>( tasksExecutor );
        Map<Future<Boolean>, Set<Task>> taskFutures = Maps.newHashMap();

        for ( Map.Entry<Host, Set<Task>> hostTasks : hostsTasks.entrySet() )
        {
            final Host host = hostTasks.getKey();

            final Set<Task> tasks = hostTasks.getValue();

            taskFutures.put( completionService.submit( new Callable<Boolean>()
            {
                @Override
                public Boolean call()
                {
                    Results results = executeUntilFirstFailure( host, tasks );

                    resultsSet.add( results );

                    return !results.hasFailures();
                }
            } ), tasks );
        }

        tasksExecutor.shutdown();

        boolean skip = false;

        for ( Future<Boolean> future : taskFutures.keySet() )
        {
            Set<Task> tasks = taskFutures.get( future );

            Host host = tasks.iterator().next().getHost();

            if ( skip )
            {
                resultsSet.add( new Results( host, tasks ) );
            }
            else
            {
                try
                {
                    if ( !future.get() )
                    {
                        skip = true;
                    }
                }
                catch ( Exception e )
                {
                    resultsSet.add( new Results( host, tasks ) );

                    skip = true;

                    LOG.error( "Error in #executeUntilFirstFailure", e );
                }
            }
        }

        return resultsSet;
    }


    public Results executeUntilFirstFailure( Host host, Set<Task> tasks )
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( tasks ), "No tasks" );

        this.tasks.addAll( tasks );

        List<Future<Boolean>> futures = Lists.newArrayList();

        for ( Task task : tasks )
        {
            futures.add( submitTask( host, task ) );
        }

        for ( Future<Boolean> future : futures )
        {
            try
            {
                if ( !future.get() )
                {
                    break;
                }
            }
            catch ( Exception e )
            {
                LOG.error( "Error in #executeUntilFirstFailure", e );
            }
        }

        return new Results( host, tasks );
    }


    public Results execute( Host host, Set<Task> tasks )
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( tasks ), "No tasks" );

        this.tasks.addAll( tasks );

        List<Future> futures = Lists.newArrayList();

        for ( Task task : tasks )
        {
            futures.add( submitTask( host, task ) );
        }

        for ( Future future : futures )
        {
            try
            {
                future.get();
            }
            catch ( Exception e )
            {
                LOG.error( "Error in #execute", e );
            }
        }

        return new Results( host, tasks );
    }


    public void dispose()
    {
        tasks.clear();

        for ( ExecutorService executorService : taskExecutors.values() )
        {
            executorService.shutdownNow();
        }
    }


    public static class Results
    {
        private final Host host;
        private final Set<Task> tasks;
        private boolean hasFailures = false;


        Results( Host host, final Set<Task> tasks )
        {
            Preconditions.checkNotNull( host );
            Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( tasks ) );

            this.host = host;
            this.tasks = tasks;

            for ( Task task : tasks )
            {
                if ( task.getTaskState() != Task.TaskState.SUCCEEDED )
                {
                    hasFailures = true;

                    break;
                }
            }
        }


        public Host getHost()
        {
            return host;
        }


        public boolean hasFailures()
        {
            return hasFailures;
        }


        public Set<Task> getTasks()
        {
            return tasks;
        }


        @Override
        public boolean equals( final Object o )
        {
            if ( this == o )
            {
                return true;
            }
            if ( o == null || getClass() != o.getClass() )
            {
                return false;
            }

            final Results results = ( Results ) o;

            return host.equals( results.host );
        }


        @Override
        public int hashCode()
        {
            return host.hashCode();
        }
    }


    public abstract static class Task<R> implements Callable<R>
    {

        public enum TaskState
        {
            NEW, RUNNING, SUCCEEDED, FAILED
        }


        private long submitTimestamp;

        private Host host;

        private volatile R result;

        private volatile Exception exception;

        private volatile TaskState taskState = TaskState.NEW;


        private void setHost( Host host )
        {
            this.host = host;
        }


        private void setSubmitTimestamp( long submitTimestamp )
        {
            this.submitTimestamp = submitTimestamp;
        }


        private void setTaskState( final TaskState taskState )
        {
            this.taskState = taskState;
        }


        private void setResult( R result )
        {
            this.result = result;
        }


        private void setException( Exception exception )
        {
            this.exception = exception;
        }


        /**
         * Maximum instances of this task that can be run in parallel on a given host.
         *
         * A value less than or equal to 0 indicates unlimited number of parallel tasks
         */
        public abstract int maxParallelTasks();


        /**
         * Name of task. May contain short description of task
         */
        public abstract String name();


        /**
         * Returns result of task execution or null if task failed or null result was returned
         */
        public final R getResult()
        {
            return result;
        }


        /**
         * Returns exception thrown during execution of the task or null if task succeeded
         */
        public final Exception getException()
        {
            return exception;
        }


        /**
         * Returns string representation of failure
         */
        public final String getFailureReason()
        {
            return exception == null ? "Unknown" : exception.getMessage();
        }


        /**
         * Returns duration in millis after task was submitted to execution up to current time
         */
        public final long getDuration()
        {
            return System.currentTimeMillis() - submitTimestamp;
        }


        /**
         * Returns target host
         */
        public final Host getHost()
        {
            return host;
        }


        /**
         * Returns state of task
         */
        public final TaskState getTaskState()
        {
            return taskState;
        }
    }


    private ExecutorService getTaskExecutor( Host host, Task task )
    {
        String executorId = host.getId() + "-" + task.getClass().getName();

        ExecutorService executorService = taskExecutors.get( executorId );

        if ( executorService == null )
        {
            executorService = task.maxParallelTasks() > 0 ?
                              Executors.newFixedThreadPool( Math.min( MAX_EXECUTOR_SIZE, task.maxParallelTasks() ) ) :
                              Executors.newCachedThreadPool();

            taskExecutors.put( executorId, executorService );
        }

        return executorService;
    }


    private <R> Future<Boolean> submitTask( Host host, final Task<R> task )
    {
        ExecutorService taskExecutor = getTaskExecutor( host, task );

        task.setHost( host );
        task.setSubmitTimestamp( System.currentTimeMillis() );
        task.setTaskState( Task.TaskState.RUNNING );

        return taskExecutor.submit( new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                try
                {
                    task.setResult( task.call() );
                    task.setTaskState( Task.TaskState.SUCCEEDED );

                    return true;
                }
                catch ( Exception e )
                {
                    LOG.error( "Error executing task {}", task.name(), e );

                    task.setException( e );
                    task.setTaskState( Task.TaskState.FAILED );

                    return false;
                }
                finally
                {
                    //remove completed task
                    tasks.remove( task );
                }
            }
        } );
    }
}
