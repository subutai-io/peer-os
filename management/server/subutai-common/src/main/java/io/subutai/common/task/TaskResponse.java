package io.subutai.common.task;


public interface TaskResponse<R extends TaskRequest>
{
    String getLog();

    boolean hasSucceeded();

    long getElapsedTime();
}
