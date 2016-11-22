package io.subutai.common.util;


import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.peer.Host;
import io.subutai.common.settings.Common;


public class HostUtil
{
    private static final Logger LOG = LoggerFactory.getLogger( HostUtil.class );

    private final Set<Task> allTasks = Sets.newConcurrentHashSet();

    private Map<String, ExecutorService> taskExecutors = Maps.newConcurrentMap();

    private final Map<String, Map<Task, EnvironmentTaskFuture>> environmentTasksFuturesMap = Maps.newConcurrentMap();


    public Set<Task> getAllTasks()
    {
        return Collections.unmodifiableSet( allTasks );
    }


    public boolean cancelEnvironmentTasks( String environmentId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        Map<Task, EnvironmentTaskFuture> environmentTaskFutures = environmentTasksFuturesMap.remove( environmentId );

        boolean hasActiveTasks = environmentTaskFutures != null && !environmentTaskFutures.isEmpty();

        if ( hasActiveTasks )
        {
            for ( EnvironmentTaskFuture environmentTaskFuture : environmentTaskFutures.values() )
            {
                environmentTaskFuture.getTask().setTaskState( Task.TaskState.CANCELLED );

                environmentTaskFuture.getFuture().cancel( true );
            }
        }

        return hasActiveTasks;
    }


    public void cancelAll()
    {
        for ( ExecutorService executorService : taskExecutors.values() )
        {
            executorService.shutdownNow();
        }

        taskExecutors.clear();

        allTasks.clear();

        environmentTasksFuturesMap.clear();
    }


    /**
     * Executes tasks in parallel
     */
    public Results execute( Tasks tasks, String environmentId )
    {
        return executeParallel( tasks, false, environmentId );
    }


    /**
     * Executes tasks in parallel. Fails fast if any execution failed
     *
     * Returns results of tasks completed so far
     */
    public Results executeFailFast( Tasks tasks, String environmentId )
    {
        return executeParallel( tasks, true, environmentId );
    }


    protected Results executeParallel( Tasks tasks, boolean failFast, String environmentId )
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
                taskFutures.add( submitTask( host, hostTask, environmentId ) );
            }
        }


        futuresLoop:
        while ( !Thread.interrupted() && !taskFutures.isEmpty() )
        {

            Iterator<Future<Boolean>> listIterator = taskFutures.iterator();

            while ( listIterator.hasNext() )
            {

                Future<Boolean> taskFuture = listIterator.next();

                try
                {
                    if ( taskFuture.isDone() )
                    {

                        listIterator.remove();

                        if ( !taskFuture.get() && failFast )
                        {
                            break futuresLoop;
                        }
                    }
                }
                catch ( Exception e )
                {
                    if ( !( e instanceof CancellationException ) )
                    {
                        LOG.error( "Error in #execute", e );
                    }

                    if ( failFast )
                    {
                        break futuresLoop;
                    }
                }
            }

            TaskUtil.sleep( 100 );
        }

        return results;
    }


    public Map<Task, Future<Boolean>> submit( final Tasks tasks, String environmentId )
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
                taskFutures.put( hostTask, submitTask( host, hostTask, environmentId ) );
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
            NEW, RUNNING, SUCCEEDED, CANCELLED, FAILED
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
            if ( this.taskState != TaskState.CANCELLED )
            {
                this.taskState = taskState;
            }
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
            executorService = task.maxParallelTasks() > 0 ? Executors
                    .newFixedThreadPool( Math.min( Common.MAX_EXECUTOR_SIZE, task.maxParallelTasks() ) ) :
                              Executors.newCachedThreadPool();

            taskExecutors.put( executorId, executorService );
        }

        return executorService;
    }


    private <R> Future<Boolean> submitTask( Host host, final Task<R> task, final String environmentId )
    {
        ExecutorService taskExecutor = getTaskExecutor( host, task );

        task.setHost( host );

        task.setSubmitTimestamp( System.currentTimeMillis() );

        task.setTaskState( Task.TaskState.RUNNING );

        final Future<Boolean> taskFuture = taskExecutor.submit( new HostTaskCommand<>( task, environmentId ) );

        if ( environmentId != null )
        {
            //add task future to map
            synchronized ( environmentTasksFuturesMap )
            {
                Map<Task, EnvironmentTaskFuture> environmentTaskFutures =
                        environmentTasksFuturesMap.get( environmentId );

                if ( environmentTaskFutures == null )
                {
                    environmentTaskFutures = Maps.newHashMap();

                    environmentTasksFuturesMap.put( environmentId, environmentTaskFutures );
                }

                environmentTaskFutures.put( task, new EnvironmentTaskFuture( task, taskFuture, environmentId ) );
            }
        }

        return taskFuture;
    }


    class HostTaskCommand<R> implements Callable<Boolean>
    {
        private final Task<R> task;
        private final String environmentId;


        HostTaskCommand( final Task<R> task, final String environmentId )
        {
            this.task = task;
            this.environmentId = environmentId;
        }


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
                task.setException( e );

                if ( task.getTaskState() == Task.TaskState.CANCELLED )
                {
                    LOG.warn( "Task {} was cancelled", task.name() );
                }
                else
                {
                    LOG.error( "Error executing task {}", task.name(), e );

                    task.setTaskState( Task.TaskState.FAILED );
                }

                return false;
            }
            finally
            {
                //remove completed task
                allTasks.remove( task );

                if ( environmentId != null )
                {
                    synchronized ( environmentTasksFuturesMap )
                    {
                        Map<Task, EnvironmentTaskFuture> environmentTaskFutures =
                                environmentTasksFuturesMap.get( environmentId );

                        if ( environmentTaskFutures != null )
                        {
                            environmentTaskFutures.remove( task );

                            if ( environmentTaskFutures.isEmpty() )
                            {
                                environmentTasksFuturesMap.remove( environmentId );
                            }
                        }
                    }
                }
            }
        }
    }


    private class EnvironmentTaskFuture
    {
        private final HostUtil.Task task;
        private final Future future;
        private final String environmentId;


        EnvironmentTaskFuture( final HostUtil.Task task, final Future future, final String environmentId )
        {
            this.task = task;
            this.future = future;
            this.environmentId = environmentId;
        }


        public HostUtil.Task getTask()
        {
            return task;
        }


        public Future getFuture()
        {
            return future;
        }


        public String getEnvironmentId()
        {
            return environmentId;
        }
    }


    public void dispose()
    {
        cancelAll();
    }
}
