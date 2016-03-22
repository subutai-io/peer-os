package io.subutai.core.localpeer.impl;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.task.ResponseCollector;
import io.subutai.common.task.Task;
import io.subutai.common.task.TaskManager;


/**
 * Task Manager
 */
public class TaskManagerImpl implements TaskManager
{
    private static final Logger LOG = LoggerFactory.getLogger( TaskManagerImpl.class );

    public static final long TASK_LIVE_TIME = TimeUnit.MINUTES.toMillis( 10 );
    private final LocalPeer localPeer;
    private final ScheduledExecutorService cleaner;

    private Map<Integer, Task> tasks = new ConcurrentHashMap<>();
    private Map<String, ExecutorService> executors = new ConcurrentHashMap<>();
    private AtomicInteger counter = new AtomicInteger( 0 );


    public TaskManagerImpl( LocalPeer localPeer )
    {
        this.localPeer = localPeer;
        cleaner = Executors.newSingleThreadScheduledExecutor();
        cleaner.scheduleWithFixedDelay( new TaskCleaner(), 10, 600, TimeUnit.SECONDS );
    }


    public void dispose()
    {
        cleaner.shutdown();
    }


    @Override
    public Future<Task> schedule( final Task task, ResponseCollector collector )
    {
        ExecutorService executor = getExecutor( task );
        final int taskId = counter.incrementAndGet();
        final Future<Task> result = executor.submit( new TaskExecutor( localPeer, task, taskId, collector ) );
        tasks.put( taskId, task );

        return result;
    }


    private ExecutorService getExecutor( final Task task )
    {
        String executorId = task.getRequest().getResourceHostId() + ( task.isSequential() ? "S" : "C" );
        ExecutorService executor = executors.get( executorId );
        if ( executor == null )
        {
            if ( task.isSequential() )
            {
                executor = Executors.newSingleThreadExecutor();
            }
            else
            {
                executor = Executors.newCachedThreadPool();
            }
            executors.put( executorId, executor );
        }
        return executor;
    }


    @Override
    public void cancel( final int taskId )
    {

    }


    @Override
    public void cancelAll()
    {

    }


    @Override
    public List<Task> getAllTasks()
    {
        List<Task> result = new ArrayList<>();
        for ( Task task : tasks.values() )
        {
            result.add( task );
        }
        return result;
    }


    @Override
    public Task getTask( final int id )
    {
        return tasks.get( id );
    }


    private class TaskCleaner implements Runnable
    {
        @Override
        public void run()
        {
            Iterator<Map.Entry<Integer, Task>> iter = tasks.entrySet().iterator();
            while ( iter.hasNext() )
            {
                Map.Entry<Integer, Task> entry = iter.next();
                final Task task = entry.getValue();
                if ( task.isDone() && System.currentTimeMillis() - task.getFinished() > TASK_LIVE_TIME )
                {
                    iter.remove();
                }
            }
        }
    }
}
