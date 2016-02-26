package io.subutai.common.task;


import java.util.List;

import io.subutai.common.peer.Host;


/**
 * Task interface
 */
public interface Task<T>
{
    long getElapsedTime();

    enum State
    {
        PENDING, RUNNING, SUCCESS, FAILURE;
    }

    void start( final int taskId );

    State getState();

    List<Throwable> getExceptions();

    CommandBatch getCommandBatch() throws Exception;

    T getResult();

    Host getHost();

    boolean isSequential();

    int getTimeout();

    boolean isDone();

}
