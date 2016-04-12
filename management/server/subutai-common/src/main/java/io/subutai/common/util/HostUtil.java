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
    private static final int MAX_EXECUTOR_SIZE = 10;

    private static final Logger LOG = LoggerFactory.getLogger( HostUtil.class );

    private final Set<Task> allTasks = Sets.newConcurrentHashSet();

    private Map<String, ExecutorService> taskExecutors = Maps.newConcurrentMap();


    public Set<Task> getAllTasks()
    {
        return Collections.unmodifiableSet( allTasks );
    }


    public Results executeNWait( Tasks tasks )
    {
        Preconditions.checkNotNull( tasks, "Invalid allTasks" );
        Preconditions.checkArgument( !tasks.isEmpty(), "No allTasks" );

        Results results = new Results( tasks );

        List<Future<Boolean>> taskFutures = Lists.newArrayList();

        for ( Map.Entry<Host, Set<Task>> hostTasksEntry : tasks.getHostTasks().entrySet() )
        {
            Host host = hostTasksEntry.getKey();

            Set<Task> hostTasks = hostTasksEntry.getValue();

            for ( Task hostTask : hostTasks )
            {
                taskFutures.add( submitTask( host, hostTask ) );
            }
        }

        for ( Future<Boolean> taskFuture : taskFutures )
        {
            try
            {
                taskFuture.get();
            }
            catch ( Exception e )
            {
                LOG.error( "Error in #execute", e );
            }
        }

        return results;
    }


    public Results executeNWaitFirstFailure( Tasks tasks )
    {
        Preconditions.checkNotNull( tasks, "Invalid allTasks" );
        Preconditions.checkArgument( !tasks.isEmpty(), "No allTasks" );

        Results results = new Results( tasks );

        List<Future<Boolean>> taskFutures = Lists.newArrayList();

        for ( Map.Entry<Host, Set<Task>> hostTasksEntry : tasks.getHostTasks().entrySet() )
        {
            Host host = hostTasksEntry.getKey();

            Set<Task> hostTasks = hostTasksEntry.getValue();

            for ( Task hostTask : hostTasks )
            {
                taskFutures.add( submitTask( host, hostTask ) );
            }
        }

        for ( Future<Boolean> taskFuture : taskFutures )
        {
            try
            {
                if ( !taskFuture.get() )
                {
                    break;
                }
            }
            catch ( Exception e )
            {
                LOG.error( "Error in #execute", e );
            }
        }

        return results;
    }


    public static class Tasks
    {
        private Map<Host, Set<Task>> hostTasks = Maps.newHashMap();


        public void addTask( Host host, Task task )
        {
            Preconditions.checkNotNull( host );
            Preconditions.checkNotNull( task );

            Set<Task> tasks = hostTasks.get( host );

            if ( tasks == null )
            {
                tasks = Sets.newHashSet();

                hostTasks.put( host, tasks );
            }

            tasks.add( task );
        }


        Map<Host, Set<Task>> getHostTasks()
        {
            return hostTasks;
        }


        public Set<Task> getTasks()
        {
            Set<Task> allTasks = Sets.newHashSet();

            for ( Set<Task> tasks : hostTasks.values() )
            {
                allTasks.addAll( tasks );
            }

            return allTasks;
        }


        public boolean isEmpty()
        {
            return hostTasks.isEmpty();
        }
    }


    public static class Results
    {
        private final Tasks tasks;


        Results( Tasks tasks )
        {
            Preconditions.checkNotNull( tasks );
            Preconditions.checkArgument( !tasks.isEmpty() );

            this.tasks = tasks;
        }


        public boolean hasFailures()
        {
            for ( Set<Task> hostTasks : tasks.getHostTasks().values() )
            {
                for ( Task task : hostTasks )
                {
                    if ( task.getTaskState() == Task.TaskState.FAILED )
                    {
                        return true;
                    }
                }
            }

            return false;
        }


        public Tasks getTasks()
        {
            return tasks;
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
         * A value less than or equal to 0 indicates unlimited number of parallel allTasks
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
                    allTasks.remove( task );
                }
            }
        } );
    }


    public void dispose()
    {
        allTasks.clear();

        for ( ExecutorService executorService : taskExecutors.values() )
        {
            executorService.shutdownNow();
        }
    }
}
