package io.subutai.core.tracker.impl;


import java.util.Date;
import java.util.UUID;

import com.google.common.base.Preconditions;

import io.subutai.common.tracker.OperationState;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.tracker.TrackerOperationView;


/**
 * This is an implementation of TrackerOperationView
 */
public class TrackerOperationViewImpl implements TrackerOperationView
{

    /**
     * id of operation
     */
    private final UUID id;
    /**
     * description of operation
     */
    private final String description;
    /**
     * log of operation
     */
    private final String log;
    /**
     * state of operation
     */
    private final OperationState state;
    /**
     * Creation date of operation
     */
    private final Date createDate;

    private final String source;


    public TrackerOperationViewImpl( TrackerOperation po )
    {
        Preconditions.checkNotNull( po, "Operation is null" );

        id = po.getId();
        description = po.getDescription();
        log = po.getLog();
        state = po.getState();
        createDate = po.createDate();
        source = po.getSource().toUpperCase();
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


    public OperationState getState()
    {
        return state;
    }


    public String getSource()
    {
        return source;
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
        final TrackerOperationViewImpl other = ( TrackerOperationViewImpl ) obj;
        return !( this.id != other.id && ( this.id == null || !this.id.equals( other.id ) ) );
    }


    @Override
    public String toString()
    {
        return "TrackerOperationViewImpl{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", log='" + log + '\'' +
                ", state=" + state +
                ", createDate=" + createDate +
                '}';
    }
}
