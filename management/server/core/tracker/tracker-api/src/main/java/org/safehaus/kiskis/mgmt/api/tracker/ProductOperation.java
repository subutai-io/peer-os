/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.tracker;

import java.util.Date;
import java.util.UUID;

/**
 * This is an interface for product operation entity which is used to log
 * actions done during product operation such as installation, start, stop etc
 *
 * @author dilshat
 */
public interface ProductOperation {

    /**
     * Description of operation
     *
     * @return
     */
    public String getDescription();

    /**
     * Id of operation
     *
     * @return
     */
    public UUID getId();

    /**
     * Log of operation
     *
     * @return
     */
    public String getLog();

    /**
     * Date of operation creation
     *
     * @return
     */
    public Date createDate();

    /**
     * State of operation
     *
     * @return
     */
    public ProductOperationState getState();

    /**
     * Adds log to operation
     *
     * @param logString
     */
    public void addLog(String logString);

    /**
     * Adds log to operation and marks operation as succeeded
     *
     * @param logString
     */
    public void addLogDone(String logString);

    /**
     * Adds log to operation and marks operation as failed
     *
     * @param logString
     */
    public void addLogFailed(String logString);
}
