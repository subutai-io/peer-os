package io.subutai.common.task;


import java.util.List;


/**
 * Task manager interface
 */
public interface TaskManager
{
    int RESULT_FAILURE = 0;

    int RESULT_SUCCESS = 1;

    int schedule( Task task );

    void cancel( int taskId );

    void cancelAll();

    List<Task> getAllTasks();
}
