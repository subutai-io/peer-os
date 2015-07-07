/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.subutai.core.tracker.impl;


import java.util.Date;
import java.util.UUID;

import io.subutai.common.tracker.OperationState;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.UUIDUtil;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * This is an implementation of ProductOperation
 */
public class TrackerOperationImpl implements TrackerOperation
{

    /**
     * operation id
     */
    private final UUID id;
    /**
     * operation description
     */
    private final String description;
    /**
     * reference to tracker
     */
    private final transient TrackerImpl tracker;

    /**
     * log of operation
     */
    private final StringBuilder log;
    /**
     * Creation date of operation
     */
    private final Date createDate;
    /**
     * Source of operation
     */
    private final String source;
    /**
     * State of operation
     */
    private OperationState state;


    public TrackerOperationImpl( String source, String description, TrackerImpl tracker )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( description ), "Description is null or empty" );
        Preconditions.checkNotNull( tracker, "Tracker is null" );

        this.description = description;
        this.source = source;
        this.tracker = tracker;
        log = new StringBuilder();
        state = OperationState.RUNNING;
        id = UUIDUtil.generateTimeBasedUUID();
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


    public OperationState getState()
    {
        return state;
    }


    public void addLog( String logString )
    {
        addLog( logString, state );
    }


    public void addLogDone( String logString )
    {
        addLog( logString, OperationState.SUCCEEDED );
    }


    public void addLogFailed( String logString )
    {
        addLog( logString, OperationState.FAILED );
    }


    private void addLog( String logString, OperationState state )
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
        tracker.saveTrackerOperation( source, this );
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
        final TrackerOperationImpl other = ( TrackerOperationImpl ) obj;
        return !( this.id != other.id && ( this.id == null || !this.id.equals( other.id ) ) );
    }
}
