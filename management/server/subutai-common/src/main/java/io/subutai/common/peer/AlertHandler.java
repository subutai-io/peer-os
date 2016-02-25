package io.subutai.common.peer;


import io.subutai.common.environment.Environment;
import io.subutai.common.metric.Alert;
import io.subutai.common.metric.AlertValue;
import io.subutai.common.metric.QuotaAlertValue;


/**
 * Alert handler interface
 */
public interface AlertHandler<T extends AlertValue>
{
    /**
     * Returns the alert handler's identifier
     *
     * @return - handler identifier
     */
    String getId();

    /**
     * Returns supported alert class
     *
     * @return supported alert class
     */

    Class<? extends AlertValue> getSupportedAlertValue();

    /**
     * Returns the description of alert handler
     */
    String getDescription();

    //    void setAlertValue(AlertValue<T> alertValue);

    /**
     * Pre processor implementation. Should be implemented preparation actions before alert processing
     */
    void preProcess( final Environment environment, final T alert ) throws AlertHandlerException;

    /**
     * Main processor. Processes given alert. Should be implemented main actions of alert processing
     *
     * @param alertValue - {@code AlertValue} alert value of the host where thresholds are being exceeded
     */
    void process( final Environment environment, T alertValue ) throws AlertHandlerException;

    /**
     * Post processor implementation. Will be invoked after main processor. Should be implemented finalization works
     * after processing alert.
     */
    void postProcess( final Environment environment, final T alert ) throws AlertHandlerException;
}
