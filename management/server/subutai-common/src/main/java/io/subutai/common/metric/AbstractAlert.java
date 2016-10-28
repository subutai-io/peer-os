package io.subutai.common.metric;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import io.subutai.common.host.HostId;


public abstract class AbstractAlert<T extends AlertValue> implements Alert
{
    protected static final Logger LOG = LoggerFactory.getLogger( AbstractAlert.class );

    @JsonProperty( "hostId" )
    protected final HostId hostId;

    @JsonProperty( "alert" )
    protected final T alert;

    @JsonProperty( "createdTime" )
    protected Long createdTime = System.currentTimeMillis();


    public AbstractAlert( final HostId hostId, final T alert )
    {
        this.hostId = hostId;
        this.alert = alert;
    }


    @Override
    public HostId getHostId()
    {
        return hostId;
    }


    @Override
    public <T extends AlertValue> T getAlertValue( final Class<T> format )
    {
        try
        {
            T result = ( T ) alert;
            return result;
        }
        catch ( ClassCastException cce )
        {
            LOG.error( cce.getMessage() );

            return null;
        }
    }


    @Override
    public long getCreatedTime()
    {
        return createdTime;
    }


    @Override
    @JsonIgnore
    public long getLiveTime()
    {
        return System.currentTimeMillis() - createdTime;
    }


    @Override
    public boolean validate()
    {
        try
        {
            Preconditions.checkNotNull( hostId );
            Preconditions.checkNotNull( hostId.getId() );
            Preconditions.checkNotNull( alert );
            Preconditions.checkNotNull( getId() );
            Preconditions.checkArgument( alert.validate() );
        }
        catch ( Exception e )
        {
            LOG.warn( e.getMessage() );

            return false;
        }
        return true;
    }
}
