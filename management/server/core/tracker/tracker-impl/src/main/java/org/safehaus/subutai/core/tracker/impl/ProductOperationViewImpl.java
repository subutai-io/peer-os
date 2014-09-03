/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.tracker.impl;


import com.google.common.base.Preconditions;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;

import java.util.Date;
import java.util.UUID;


/**
 * This is an implementation of ProductOperationView
 */
public class ProductOperationViewImpl implements ProductOperationView
{

    /**
     * id of product operation
     */
    private final UUID id;
    /**
     * description of product operation
     */
    private final String description;
    /**
     * log of product operation
     */
    private final String log;
    /**
     * state of product operation
     */
    private final ProductOperationState state;
    /**
     * Creation date of product operation
     */
    private final Date createDate;


    public ProductOperationViewImpl( ProductOperation po )
    {
        Preconditions.checkNotNull( po, "Product operation is null" );

        id = po.getId();
        description = po.getDescription();
        log = po.getLog();
        state = po.getState();
        createDate = po.createDate();
    }


    public String getDescription()
    {
        return description;
    }


    public Date getCreateDate()
    {
        return ( Date ) createDate.clone();
    }


    public UUID getId()
    {
        return id;
    }


    public String getLog()
    {
        return log;
    }


    public ProductOperationState getState()
    {
        return state;
    }


    @Override
    public int hashCode()
    {
        return 3;
    }


    @Override
    public boolean equals( Object obj )
    {
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final ProductOperationViewImpl other = ( ProductOperationViewImpl ) obj;
        return !( this.id != other.id && ( this.id == null || !this.id.equals( other.id ) ) );
    }


    @Override
    public String toString()
    {
        return "ProductOperationViewImpl{" +
            "id=" + id +
            ", description='" + description + '\'' +
            ", log='" + log + '\'' +
            ", state=" + state +
            ", createDate=" + createDate +
            '}';
    }
}
