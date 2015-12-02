package io.subutai.common.metric;


import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;

import io.subutai.common.host.HostId;


public abstract class AbstractAlert<T extends AlertValue> implements Alert
{
    @JsonProperty( "hostId" )
    protected HostId hostId;

    @JsonProperty( "alert" )
    protected T alert;

    @JsonProperty( "createdTime" )
    protected Long createdTime = System.currentTimeMillis();


    abstract public String getId();

//    abstract public AlertType getType();


    public HostId getHostId()
    {
        return hostId;
    }


    public <T extends AlertValue> T getAlertValue( final Class<T> format )
    {
        try
        {
            T result = ( T ) alert;
            return result;
        }
        catch ( ClassCastException cce )
        {
            return null;
        }
    }


    @Override
    public long getCreatedTime()
    {
        return createdTime;
    }


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
            return false;
        }
        return true;
    }
}
