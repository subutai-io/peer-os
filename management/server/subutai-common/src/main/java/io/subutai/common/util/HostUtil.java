package io.subutai.common.util;


import java.util.Collections;
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

import io.subutai.common.peer.Host;


public class HostUtil
{
    public static final int MAX_EXECUTOR_SIZE = 20;

    private static final Logger LOG = LoggerFactory.getLogger( HostUtil.class );

    private final Set<HostTask> tasks = Sets.newConcurrentHashSet();

    private Map<String, ExecutorService> taskExecutors = Maps.newConcurrentMap();


    public Set getTasks()
    {
        return Collections.unmodifiableSet( tasks );
    }


    public TaskResults execute( Set<HostTask> tasks )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( tasks ), "No task found for execution" );

        this.tasks.addAll( tasks );

        executeParallel( tasks );

        return new TaskResults( tasks );
    }


    public abstract static class HostTask<R> implements Callable<R>
    {

        public static enum TaskState
        {
            NEW, RUNNING, SUCCEEDED, FAILED
        }


        private long submitTimestamp;

        private final Host host;

        private R result;

        private Exception exception;

        private TaskState taskState = TaskState.NEW;


        public HostTask( final Host host )
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


    public static class TaskResults
    {
        private final Set<HostTask> tasks;
        private boolean hasFailures = false;


        protected TaskResults( final Set<HostTask> tasks )
        {
            Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( tasks ) );

            this.tasks = tasks;

            for ( HostTask task : tasks )
            {
                if ( task.getTaskState() == HostTask.TaskState.FAILED )
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


        public Set<HostTask> getTasks()
        {
            return tasks;
        }
    }


    private ExecutorService getTaskExecutor( HostTask task )
    {
        String executorId = task.name() + "-" + task.getClass().getName();

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


    protected void executeParallel( Set<HostTask> tasks )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( tasks ) );

        //todo use maxParallelTasks to obtain executor from static cache
        ExecutorService taskExecutor = Executors.newFixedThreadPool( tasks.size() );
        CompletionService taskCompletionService = new ExecutorCompletionService<>( taskExecutor );

        Map<HostTask, Future> taskFutures = Maps.newHashMap();

        for ( HostTask task : tasks )
        {
            task.setSubmitTimestamp( System.currentTimeMillis() );
            task.setTaskState( HostTask.TaskState.RUNNING );
            taskFutures.put( task, taskCompletionService.submit( task ) );
        }

        taskExecutor.shutdown();

        for ( Map.Entry<HostTask, Future> taskFuture : taskFutures.entrySet() )
        {
            try
            {
                taskFuture.getKey().setTaskState( HostTask.TaskState.SUCCEEDED );
                taskFuture.getKey().setResult( taskFuture.getValue().get() );
            }
            catch ( Exception e )
            {
                LOG.error( "Error executing task ", e );

                taskFuture.getKey().setTaskState( HostTask.TaskState.FAILED );
                taskFuture.getKey().setException( e );
            }

            //remove completed task
            this.tasks.remove( taskFuture.getKey() );
        }
    }
}
