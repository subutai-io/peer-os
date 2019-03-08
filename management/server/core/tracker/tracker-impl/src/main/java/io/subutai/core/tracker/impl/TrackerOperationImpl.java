package io.subutai.core.tracker.impl;


import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import io.subutai.common.tracker.OperationState;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.UUIDUtil;


/**
 * This is an implementation of TrackerOperation
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
        Preconditions.checkArgument( !StringUtils.isBlank( source ), "Source is null or empty" );
        Preconditions.checkArgument( !StringUtils.isBlank( description ), "Description is null or empty" );
        Preconditions.checkNotNull( tracker, "Tracker is null" );

        this.description = description;
        this.source = source;
        this.tracker = tracker;
        log = new StringBuilder();
        state = OperationState.RUNNING;
        id = UUIDUtil.generateTimeBasedUUID();
        createDate = new Date();
    }


    @Override
    public String getDescription()
    {
        return description;
    }


    @Override
    public UUID getId()
    {
        return id;
    }


    @Override
    public synchronized String getLog()
    {
        return log.toString();
    }


    @Override
    public String getSource()
    {
        return source;
    }


    @Override
    public Date createDate()
    {
        return ( Date ) createDate.clone();
    }


    @Override
    public OperationState getState()
    {
        return state;
    }


    @Override
    public void addLog( String logString )
    {
        addLog( logString, state );
    }


    @Override
    public void addLogDone( String logString )
    {
        addLog( logString, OperationState.SUCCEEDED );
    }


    @Override
    public void addLogFailed( String logString )
    {
        addLog( logString, OperationState.FAILED );
    }


    private synchronized void addLog( String logString, OperationState state )
    {
        //ignore all messages after operation was marked as complete
        if ( this.state != OperationState.RUNNING )
        {
            return;
        }

        if ( !StringUtils.isBlank( logString ) )
        {
            if ( log.length() > 0 )
            {
                log.append( "\n" );
            }
            log.append( String.format( "{\"date\" : %s, \"log\" : \"%s\", \"state\" : \"%s\"},",
                    new Timestamp( System.currentTimeMillis() ).getTime(),
                    logString.replaceAll( "\r", "" ).replaceAll( "\n", "" ).replaceAll( "\\\\", "" )
                             .replaceAll( "\"", "" ).replaceAll( "\\{", "" ).replaceAll( "}", "" ), state ) );
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
