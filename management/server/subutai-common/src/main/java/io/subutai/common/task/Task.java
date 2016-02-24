package io.subutai.common.task;


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

    Exception getException();

    CommandBatch getCommandBatch() throws Exception;

    T getResult();

    Host getHost();

    boolean isSequential();

}
