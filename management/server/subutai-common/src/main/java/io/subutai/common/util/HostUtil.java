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
    private static final Logger LOG = LoggerFactory.getLogger( HostUtil.class );

    private final Set<HostTask> tasks = Sets.newConcurrentHashSet();


    public Set getTasks()
    {
        return Collections.unmodifiableSet( tasks );
    }


    public HostResults execute( Set<HostTask> tasks )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( tasks ), "No task found for execution" );

        this.tasks.addAll( tasks );

        Set<HostResult> results = executeParallel( tasks );

        return new HostResults( results );
    }


    public abstract static class HostTask<R> implements Callable<R>
    {

        public static enum TaskState
        {
            NEW, RUNNING, SUCCEEDED, FAILED
        }


        private long submitTimestamp;

        private final Host host;

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


        /**
         * Returns duration in millis after task was submitted to execution up to current time
         */
        public final long getDuration()
        {
            return System.currentTimeMillis() - submitTimestamp;
        }


        /**
         * Maximum instances of this task that can be run in parallel on a given host.
         *
         * A value less than or equal to 0 indicates unlimited number of parallel tasks
         */
        abstract int maxParallelTasks();


        /**
         * Name of task. May contain short description of task
         */
        abstract String name();


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


    public static class HostResult
    {
        private final HostTask task;
        private Object result;
        private Exception exception;


        protected HostResult( final HostTask task, final Object result )
        {
            this.task = task;
            this.result = result;
        }


        protected HostResult( final HostTask task, final Exception exception )
        {
            this.task = task;
            this.exception = exception;
        }


        public HostTask getTask()
        {
            return task;
        }


        public Object getResult()
        {
            return result;
        }


        public boolean hasSucceeded()
        {
            return task.getTaskState() == HostTask.TaskState.SUCCEEDED;
        }


        public Exception getException()
        {
            return exception;
        }


        public String getFailureReason()
        {
            return exception == null ? "Unknown" : exception.getMessage();
        }
    }


    public static class HostResults
    {
        private final Set<HostResult> hostResults;
        private boolean hasFailures = false;


        protected HostResults( final Set<HostResult> hostResults )
        {
            Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( hostResults ) );

            this.hostResults = hostResults;

            for ( HostResult taskResult : hostResults )
            {
                if ( taskResult.getTask().getTaskState() == HostTask.TaskState.FAILED )
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


        public Set<HostResult> getHostResults()
        {
            return hostResults;
        }
    }


    protected Set<HostResult> executeParallel( Set<HostTask> tasks )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( tasks ) );

        Set<HostResult> taskResults = Sets.newHashSet();

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
                taskResults.add( new HostResult( taskFuture.getKey(), taskFuture.getValue().get() ) );
            }
            catch ( Exception e )
            {
                LOG.error( "Error executing task ", e );

                taskFuture.getKey().setTaskState( HostTask.TaskState.FAILED );
                taskResults.add( new HostResult( taskFuture.getKey(), e ) );
            }

            //remove completed task
            this.tasks.remove( taskFuture.getKey() );
        }

        return taskResults;
    }
}
