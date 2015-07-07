package io.subutai.core.messenger.api;


/**
 * Status of message.
 */
public enum MessageStatus
{
    //message is in process
    IN_PROCESS,
    //message is successfully sent
    SENT,
    //message expired
    EXPIRED,
    //message is either not found or has been deleted after eviction timeout
    NOT_FOUND
}
