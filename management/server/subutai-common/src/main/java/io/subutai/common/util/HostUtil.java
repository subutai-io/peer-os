package io.subutai.common.util;


import java.util.Collections;
import java.util.Iterator;
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


    /**
     * Executes tasks in parallel
     */
    public Results execute( Tasks tasks )
    {
        return executeParallel( tasks, false );
    }


    /**
     * Executes tasks in parallel. Fails fast if any execution failed
     *
     * Returns results of tasks completed so far
     */
    public Results executeFailFast( Tasks tasks )
    {
        return executeParallel( tasks, true );
    }


    protected Results executeParallel( Tasks tasks, boolean failFast )
    {
        Preconditions.checkNotNull( tasks, "Invalid tasks" );
        Preconditions.checkArgument( !tasks.isEmpty(), "No tasks" );

        Results results = new Results( tasks );

        this.allTasks.addAll( tasks.getTasks() );

        List<Future<Boolean>> taskFutures = Lists.newArrayList();

        for ( Map.Entry<Host, Set<Task>> hostTasksEntry : tasks.getHostsTasks().entrySet() )
        {
            Host host = hostTasksEntry.getKey();

            Set<Task> hostTasks = hostTasksEntry.getValue();

            for ( Task hostTask : hostTasks )
            {
                taskFutures.add( submitTask( host, hostTask ) );
            }
        }

        int doneTasks = 0;

        int totalTasks = taskFutures.size();

        futuresLoop:
        while ( !Thread.interrupted() && doneTasks < totalTasks && !taskFutures.isEmpty() )
        {

            Iterator<Future<Boolean>> listIterator = taskFutures.iterator();

            while ( listIterator.hasNext() )
            {

                Future<Boolean> taskFuture = listIterator.next();

                try
                {
                    if ( taskFuture.isDone() )
                    {
                        doneTasks++;

                        listIterator.remove();

                        if ( !taskFuture.get() && failFast )
                        {
                            break futuresLoop;
                        }
                    }
                }
                catch ( Exception e )
                {
                    LOG.error( "Error in #execute", e );

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
                break;
            }
        }

        return results;
    }


    public Map<Task, Future<Boolean>> submit( final Tasks tasks )
    {
        Preconditions.checkNotNull( tasks, "Invalid tasks" );
        Preconditions.checkArgument( !tasks.isEmpty(), "No tasks" );

        this.allTasks.addAll( tasks.getTasks() );

        Map<Task, Future<Boolean>> taskFutures = Maps.newHashMap();

        for ( Map.Entry<Host, Set<Task>> hostTasksEntry : tasks.getHostsTasks().entrySet() )
        {
            Host host = hostTasksEntry.getKey();

            Set<Task> hostTasks = hostTasksEntry.getValue();

            for ( Task hostTask : hostTasks )
            {
                taskFutures.put( hostTask, submitTask( host, hostTask ) );
            }
        }

        return taskFutures;
    }


    public static class Tasks
    {
        private Map<Host, Set<Task>> hostsTasks = Maps.newHashMap();


        public void addTask( Host host, Task task )
        {
            Preconditions.checkNotNull( host );
            Preconditions.checkNotNull( task );

            Set<Task> tasks = hostsTasks.get( host );

            if ( tasks == null )
            {
                tasks = Sets.newHashSet();

                hostsTasks.put( host, tasks );
            }

            tasks.add( task );
        }


        Map<Host, Set<Task>> getHostsTasks()
        {
            return hostsTasks;
        }


        public Set<Task> getTasks()
        {
            Set<Task> tasks = Sets.newHashSet();

            for ( Set<Task> hostTasks : hostsTasks.values() )
            {
                tasks.addAll( hostTasks );
            }

            return tasks;
        }


        public boolean isEmpty()
        {
            return hostsTasks.isEmpty();
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
            return getFirstFailedTask() != null;
        }


        public Tasks getTasks()
        {
            return tasks;
        }


        public Task getFirstFailedTask()
        {
            for ( Task task : tasks.getTasks() )
            {
                if ( task.getTaskState() == Task.TaskState.FAILED )
                {
                    return task;
                }
            }

            return null;
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


        public final String getDurationFormatted()
        {
            return DateUtil.convertMillisToHHMMSS( getDuration() );
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
