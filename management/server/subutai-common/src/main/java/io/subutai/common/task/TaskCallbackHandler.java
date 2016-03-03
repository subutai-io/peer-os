package io.subutai.common.task;


import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.task.Task;


public interface TaskCallbackHandler<R, T>
{
    void handle( Task task, R request, T result ) throws Exception;
}
