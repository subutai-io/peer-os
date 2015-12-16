package io.subutai.core.environment.impl.entity;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.AlertHandlerPriority;
import io.subutai.common.peer.EnvironmentAlertHandler;


/**
 * Database entity to store subscribed alert handler.
 */
@Entity
@Table( name = "env_alert_handler" )
@Access( AccessType.FIELD )
public class EnvironmentAlertHandlerImpl
        implements EnvironmentAlertHandler, Serializable, Comparable<EnvironmentAlertHandler>
{
    @Column( name = "h_id", nullable = false )
    private String handlerId;

    @Column( name = "h_priority", nullable = false )
    @Enumerated( EnumType.STRING )
    private AlertHandlerPriority handlerPriority;

    @ManyToOne( targetEntity = EnvironmentImpl.class )
    @JoinColumn( name = "environment_id" )
    private Environment environment;


    public EnvironmentAlertHandlerImpl( final String handlerId, final AlertHandlerPriority priority )
    {
        this.handlerId = handlerId;
        this.handlerPriority = priority;
    }


    public void setEnvironment( final Environment environment )
    {
        this.environment = environment;
    }


    @Override
    public String getAlertHandlerId()
    {
        return handlerId;
    }


    public AlertHandlerPriority getAlertHandlerPriority()
    {
        return handlerPriority;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof EnvironmentAlertHandler ) )
        {
            return false;
        }

        final EnvironmentAlertHandler that = ( EnvironmentAlertHandler ) o;

        if ( !handlerId.equals( that.getAlertHandlerId() ) )
        {
            return false;
        }
        return handlerPriority == that.getAlertHandlerPriority();
    }


    @Override
    public int hashCode()
    {
        int result = handlerId.hashCode();
        result = 31 * result + handlerPriority.hashCode();
        return result;
    }


    @Override
    public int compareTo( final EnvironmentAlertHandler o )
    {
        if ( o == null )
        {
            return -1;
        }

        int p = o.getAlertHandlerPriority().compareTo( handlerPriority );
        return p == 0 ? handlerId.compareTo( o.getAlertHandlerId() ) : p;
    }


    @Override
    public String toString()
    {
        return handlerId + ":" + handlerPriority;
    }
}
