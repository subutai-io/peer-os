package io.subutai.common.peer;


/**
 * Alert handler interface
 */
public interface AlertHandler
{
    /**
     * Returns the alert handler's identifier
     *
     * @return - handler identifier
     */
    String getId();

 /*   *//**
     * Returns default alert handler priority
     *
     * @return alert handler priority
     *//*
    AlertHandlerPriority getPriority();
*/
    /**
     * Returns the description of alert handler
     */
    String getDescription();

    /**
     * Pre processor implementation. Should be implemented preparation actions before alert processing
     */
    void preProcess( final AlertPack alert ) throws AlertHandlerException;

    /**
     * Main processor. Processes given alert. Should be implemented main actions of alert processing
     *
     * @param alertPack - {@code AlertPack} alert package of the host where thresholds are being exceeded
     */
    void process( AlertPack alertPack ) throws AlertHandlerException;

    /**
     * Post processor implementation. Will be invoked after main processor. Should be implemented finalization works
     * after processing alert.
     */
    void postProcess( final AlertPack alert ) throws AlertHandlerException;
}
