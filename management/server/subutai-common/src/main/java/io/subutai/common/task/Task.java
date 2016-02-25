package io.subutai.common.task;


import java.util.List;

import io.subutai.common.peer.Host;


/**
 * Task interface
 */
public interface Task<T>
{
    int getTimeout();

    boolean isDone();

    //    void checkTimeout();


    enum State
    {
        PENDING, RUNNING, SUCCESS, FAILURE;
    }

    void start();

    State getState();

    List<Throwable> getExceptions();

    CommandBatch getCommandBatch() throws Exception;

    T getResult();

    Host getHost();

    boolean isSequential();
}
