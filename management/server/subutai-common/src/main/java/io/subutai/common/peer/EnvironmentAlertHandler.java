package io.subutai.common.peer;


/**
 * Alert handler identifier class
 */
public interface EnvironmentAlertHandler
{
    public String getAlertHandlerId();

    public AlertHandlerPriority getAlertHandlerPriority();
}
