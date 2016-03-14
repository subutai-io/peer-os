package io.subutai.common.task;


import java.util.List;
import java.util.concurrent.Future;


/**
 * Task manager interface
 */
public interface TaskManager
{
    int RESULT_FAILURE = 0;

    int RESULT_SUCCESS = 1;

    Future<Task> schedule( Task task, ResponseCollector collector );

    void cancel( int taskId );

    void cancelAll();

    List<Task> getAllTasks();

    Task getTask( int id );
}
