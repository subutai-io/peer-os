package io.subutai.common.task;


/**
 * Response collector interface to collect task responses.
 */
public interface ResponseCollector
{
    void onResponse(TaskResponse response);
}
