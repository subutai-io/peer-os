package org.safehaus.subutai.core.metric.impl.model;


/**
 * Created by talas on 12/12/14.
 */
public class SubscriberPK
{
    private String environmentId;
    private String subscriberId;


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final String environmentId )
    {
        this.environmentId = environmentId;
    }


    public String getSubscriberId()
    {
        return subscriberId;
    }


    public void setSubscriberId( final String subscriberId )
    {
        this.subscriberId = subscriberId;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof SubscriberPK ) )
        {
            return false;
        }

        final SubscriberPK that = ( SubscriberPK ) o;

        return environmentId.equals( that.environmentId ) && subscriberId.equals( that.subscriberId );
    }


    @Override
    public int hashCode()
    {
        int result = environmentId.hashCode();
        result = 31 * result + subscriberId.hashCode();
        return result;
    }
}
