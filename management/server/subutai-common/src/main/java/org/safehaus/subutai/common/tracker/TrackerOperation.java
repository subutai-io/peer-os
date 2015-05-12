package org.safehaus.subutai.common.tracker;


import java.util.Date;
import java.util.UUID;


/**
 * Class which enables to log message about progress of operations such as installation, start, stop etc.
 */
public interface TrackerOperation
{

    /**
     * Description of operation
     *
     * @return description of operation
     */
    public String getDescription();

    /**
     * Id of operation
     *
     * @return uuid of operation
     */
    public UUID getId();

    /**
     * Log of operation
     *
     * @return log of operation
     */
    public String getLog();

    /**
     * Date of operation creation
     *
     * @return creation date of operation
     */
    public Date createDate();

    /**
     * State of operation
     *
     * @return state of operation
     */
    public OperationState getState();

    /**
     * Adds log to operation
     *
     * @param logString log to add to operation
     */
    public void addLog( String logString );

    /**
     * Adds log to operation and marks operation as succeeded
     *
     * @param logString log to add to operation
     */
    public void addLogDone( String logString );

    /**
     * Adds log to operation and marks operation as failed
     *
     * @param logString log to add to operation
     */
    public void addLogFailed( String logString );
}
