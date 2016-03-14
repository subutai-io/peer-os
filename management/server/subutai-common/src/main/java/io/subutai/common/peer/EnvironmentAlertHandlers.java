package io.subutai.common.peer;


import java.util.Collection;
import java.util.Map;


/**
 * Environments alert handlers
 */
public interface EnvironmentAlertHandlers
{
    /**
     * Adds alert handler by id. AlertHandler may be null.
     *
     * @param environmentAlertHandler alert handler id
     * @param alertHandler alert handler
     */
    void add( EnvironmentAlertHandler environmentAlertHandler, AlertHandler alertHandler );


    public EnvironmentId getEnvironmentId();


    public Map<EnvironmentAlertHandler, AlertHandler> getAllHandlers();


    public AlertHandler getHandler( final EnvironmentAlertHandler handlerId );

    /**
     * Returns prioritised not null alert handlers collection
     */
    Collection<AlertHandler> getEffectiveHandlers();
}
