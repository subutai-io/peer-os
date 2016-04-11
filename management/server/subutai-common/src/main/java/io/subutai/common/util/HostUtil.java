package io.subutai.common.util;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
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
    public static final int MAX_EXECUTOR_SIZE = 20;

    private static final Logger LOG = LoggerFactory.getLogger( HostUtil.class );

    private final Set<Task> tasks = Sets.newConcurrentHashSet();

    private Map<String, ExecutorService> taskExecutors = Maps.newConcurrentMap();


    public Set getTasks()
    {
        return Collections.unmodifiableSet( tasks );
    }


    public void execute( Host host, Set<Task> tasks )
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
                //ignore
            }
        }
    }


    public abstract static class Task<R> implements Callable<R>
    {

        public static enum TaskState
        {
            NEW, RUNNING, SUCCEEDED, FAILED
        }


        private long submitTimestamp;

        private Host host;

        private R result;

        private Exception exception;

        private TaskState taskState = TaskState.NEW;


        private void setHost( Host host ) {this.host = host;}


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


    protected ExecutorService getTaskExecutor( Host host, Task task )
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


    protected <R> Future submitTask( Host host, final Task<R> task )
    {
        ExecutorService taskExecutor = getTaskExecutor( host, task );

        task.setHost( host );
        task.setSubmitTimestamp( System.currentTimeMillis() );
        task.setTaskState( Task.TaskState.RUNNING );

        return taskExecutor.submit( new Callable<Object>()
        {
            @Override
            public R call() throws Exception
            {
                try
                {
                    task.setResult( task.call() );
                    task.setTaskState( Task.TaskState.SUCCEEDED );
                }
                catch ( Exception e )
                {
                    LOG.error( "Error executing task {}", task.name(), e );

                    task.setException( e );
                    task.setTaskState( Task.TaskState.FAILED );
                }

                //remove completed task
                tasks.remove( task );

                return null;
            }
        } );
    }
}
