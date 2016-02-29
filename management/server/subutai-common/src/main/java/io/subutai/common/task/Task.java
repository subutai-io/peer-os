package io.subutai.common.task;


import java.util.List;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.ResourceHost;


/**
 * Task interface
 */
public interface Task<R extends TaskRequest, T extends TaskResponse>
{
    enum State
    {
        PENDING, RUNNING, SUCCESS, FAILURE;
    }

    long getElapsedTime();

    int getId();

    RequestBuilder getRequestBuilder() throws Exception;

    void start( int id );

    void done( final CommandResult commandResult );

    State getState();

    List<Throwable> getExceptions();

    CommandBatch getCommandBatch() throws Exception;

    R getRequest();

    //    T getResponse();

    T waitAndGetResponse();

    boolean isSequential();

    String getExceptionsAsString();

    int getTimeout();


    boolean isDone();
}
