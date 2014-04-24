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
     * @return -description of product operation
     */
    public String getDescription();

    /**
     * Id of operation
     *
     * @return - uuid of product operation
     */
    public UUID getId();

    /**
     * Log of operation
     *
     * @return - log of product operation
     */
    public String getLog();

    /**
     * Date of operation creation
     *
     * @return - creation date of product operation
     */
    public Date createDate();

    /**
     * State of operation
     *
     * @return - state of product operation
     */
    public ProductOperationState getState();

    /**
     * Adds log to operation
     *
     * @param logString - log to add to product operation
     */
    public void addLog(String logString);

    /**
     * Adds log to operation and marks operation as succeeded
     *
     * @param logString - log to add to product operation
     */
    public void addLogDone(String logString);

    /**
     * Adds log to operation and marks operation as failed
     *
     * @param logString - log to add to product operation
     */
    public void addLogFailed(String logString);
}
