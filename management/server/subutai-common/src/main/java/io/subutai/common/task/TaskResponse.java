package io.subutai.common.task;


public interface TaskResponse<R extends TaskRequest>
{
    String getResourceHostId();

    String getLog();

    String getDescription();

    boolean hasSucceeded();

    long getElapsedTime();
}
