package io.subutai.common.task;


public interface TaskCallbackHandler<R, T>
{
    void handle( Task task, R request, T result ) throws Exception;
}
