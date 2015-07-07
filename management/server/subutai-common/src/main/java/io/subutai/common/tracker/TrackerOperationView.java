/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.subutai.common.tracker;


import java.util.Date;
import java.util.UUID;


/**
 * This is an interface for operation view
 */
public interface TrackerOperationView
{

    /**
     * Returns operation description
     *
     * @return operation description
     */
    public String getDescription();

    /**
     * Returns operation creation date
     *
     * @return operation create date
     */
    public Date getCreateDate();

    /**
     * Returns id of operation
     *
     * @return operation id
     */
    public UUID getId();

    /**
     * Returns operation log
     *
     * @return log of operation
     */
    public String getLog();

    /**
     * Returns state of operation
     *
     * @return operation state
     */
    public OperationState getState();
}
