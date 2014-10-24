package org.safehaus.subutai.common.tracker;


import java.util.Date;
import java.util.UUID;


/**
 * Entity is to logs actions of product operations such as installation, start, stop etc.
 */
public interface TrackerOperation
{

    /**
     * Description of operation
     *
     * @return description of product operation
     */
    public String getDescription();

    /**
     * Id of operation
     *
     * @return uuid of product operation
     */
    public UUID getId();

    /**
     * Log of operation
     *
     * @return log of product operation
     */
    public String getLog();

    /**
     * Date of operation creation
     *
     * @return creation date of product operation
     */
    public Date createDate();

    /**
     * State of operation
     *
     * @return state of product operation
     */
    public OperationState getState();

    /**
     * Adds log to operation
     *
     * @param logString log to add to product operation
     */
    public void addLog( String logString );

    /**
     * Adds log to operation and marks operation as succeeded
     *
     * @param logString log to add to product operation
     */
    public void addLogDone( String logString );

    /**
     * Adds log to operation and marks operation as failed
     *
     * @param logString log to add to product operation
     */
    public void addLogFailed( String logString );
}
