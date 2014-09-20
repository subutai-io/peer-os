/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.tracker.impl;


import java.util.Date;
import java.util.UUID;

import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.util.UUIDUtil;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * This is an implementaion of ProductOperation
 */
public class ProductOperationImpl implements ProductOperation
{

    /**
     * product operation id
     */
    private final UUID id;
    /**
     * product operation description
     */
    private final String description;
    /**
     * reference to tracker
     */
    private final transient TrackerImpl tracker;

    /**
     * log of product operation
     */
    private final StringBuilder log;
    /**
     * Creation date of product operation
     */
    private final Date createDate;
    /**
     * Source of product operation
     */
    private final String source;
    /**
     * State of product operation
     */
    private ProductOperationState state;


    public ProductOperationImpl( String source, String description, TrackerImpl tracker )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( description ), "Description is null or empty" );
        Preconditions.checkNotNull( tracker, "Tracker is null" );

        this.description = description;
        this.source = source;
        this.tracker = tracker;
        log = new StringBuilder();
        state = ProductOperationState.RUNNING;
        id = UUIDUtil.generateCassandraUUID();
        createDate = new Date();
    }


    public String getDescription()
    {
        return description;
    }


    public UUID getId()
    {
        return id;
    }


    public String getLog()
    {
        return log.toString();
    }


    public Date createDate()
    {
        return ( Date ) createDate.clone();
    }


    public ProductOperationState getState()
    {
        return state;
    }


    public void addLog( String logString )
    {
        addLog( logString, state );
    }


    public void addLogDone( String logString )
    {
        addLog( logString, ProductOperationState.SUCCEEDED );
    }


    public void addLogFailed( String logString )
    {
        addLog( logString, ProductOperationState.FAILED );
    }


    private void addLog( String logString, ProductOperationState state )
    {
        if ( !Strings.isNullOrEmpty( logString ) )
        {

            if ( log.length() > 0 )
            {
                log.append( "\n" );
            }
            log.append( logString );
        }
        this.state = state;
        tracker.saveProductOperation( source, this );
    }


    @Override
    public int hashCode()
    {
        return 7;
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
        final ProductOperationImpl other = ( ProductOperationImpl ) obj;
        return !( this.id != other.id && ( this.id == null || !this.id.equals( other.id ) ) );
    }
}
