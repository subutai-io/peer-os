/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.common.tracker;


import java.util.Date;
import java.util.UUID;


/**
 * This is an interface for product operation view
 */
public interface ProductOperationView
{

    /**
     * Returns product operation description
     *
     * @return product operation description
     */
    public String getDescription();

    /**
     * Returns product operation creation date
     *
     * @return product operation create date
     */
    public Date getCreateDate();

    /**
     * Returns id of product operation
     *
     * @return product operation id
     */
    public UUID getId();

    /**
     * Returns product operation log
     *
     * @return log of product operation
     */
    public String getLog();

    /**
     * Returns state of product operation
     *
     * @return product operation state
     */
    public ProductOperationState getState();
}
