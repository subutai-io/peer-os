package io.subutai.core.localpeer.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandUtil;
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
    private final LocalPeer localPeer;
    private Map<Integer, Task> tasks = new ConcurrentHashMap<>();
    private Map<String, ExecutorService> executors = new ConcurrentHashMap<>();
    private AtomicInteger counter = new AtomicInteger( 0 );
    protected CommandUtil commandUtil = new CommandUtil();


    public TaskManagerImpl( LocalPeer localPeer )
    {
        this.localPeer = localPeer;
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
}
