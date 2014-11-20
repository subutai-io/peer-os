package org.safehaus.subutai.common.command;


/**
 * Agent response types
 */
public enum ResponseType
{
    EXECUTE_RESPONSE,
    EXECUTE_TIMEOUT,
    IN_QUEUE,
    TERMINATE_RESPONSE,
    PS_RESPONSE,
    LIST_INOTIFY_RESPONSE,
    SET_INOTIFY_RESPONSE,
    UNSET_INOTIFY_RESPONSE,
    INOTIFY_EVENT
}
