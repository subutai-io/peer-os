package io.subutai.core.localpeer.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.task.Task;
import io.subutai.common.task.TaskManager;


/**
 * Task Manager
 */
public class TaskManagerImpl implements TaskManager
{
    private static final Logger LOG = LoggerFactory.getLogger( TaskManagerImpl.class );
    private Map<Integer, Task> tasks = new ConcurrentHashMap<>();
    private Map<String, Executor> executors = new ConcurrentHashMap<>();
    private AtomicInteger counter = new AtomicInteger( 0 );


    public TaskManagerImpl()
    {
    }


    @Override
    public int schedule( final Task task )
    {
        Executor executor = getExecutor( task );
        final int taskId = counter.incrementAndGet();
        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                task.start( taskId );
            }
        } );

        tasks.put( taskId, task );
        return taskId;
    }


    private Executor getExecutor( final Task task )
    {
        String executorId = task.getHost().getId() + ( task.isSequential() ? "S" : "C" );
        Executor executor = executors.get( executorId );
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
}
