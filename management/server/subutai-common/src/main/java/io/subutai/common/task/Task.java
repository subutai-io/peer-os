package io.subutai.common.task;


import java.util.List;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandStatus;
import io.subutai.common.command.RequestBuilder;


/**
 * Task interface
 */
public interface Task<R extends TaskRequest, T extends TaskResponse>
{

    enum State
    {
        PENDING, RUNNING, SUCCESS, FAILURE

    }

    long getElapsedTime();

    long getFinished();

    int getId();

    RequestBuilder getRequestBuilder() throws Exception;

    void start( int id );

    void done( final CommandResult commandResult );

    State getState();

    List<Throwable> getExceptions();

    CommandBatch getCommandBatch() throws Exception;

    R getRequest();

    T waitAndGetResponse();

    /**
     * Number of instances of this task that can be launched simultaneously.
     *
     * value <= 0 for unlimited
     */
    int getNumberOfParallelTasks();

    String getExceptionsAsString();

    int getTimeout();


    boolean isDone();

    CommandStatus getCommandStatus();

    Integer getExitCode();

    String getStdOut();

    String getStdErr();
}
