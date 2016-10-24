package io.subutai.common.peer;


import java.io.Serializable;


/**
 * Alert handler identifier class
 */
public interface EnvironmentAlertHandler extends Serializable
{
    String getAlertHandlerId();

    AlertHandlerPriority getAlertHandlerPriority();
}
