package io.subutai.common.task;


import io.subutai.common.command.CommandResult;


public interface TaskResponseBuilder<R extends TaskRequest, T extends TaskResponse>
{
    T build( R request, CommandResult commandResult, long elapsedTime ) throws Exception;
}
