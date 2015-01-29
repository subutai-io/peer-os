package org.safehaus.subutai.core.metric.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;


@Entity
@Table( name = "monitor_subscriber" )
@Access( AccessType.FIELD )
@IdClass( SubscriberPK.class )
public class Subscriber
{
    @Id
    @Column( name = "environment_id" )
    private String environmentId;

    @Id
    @Column( name = "subscriber_id" )
    private String subscriberId;


    public Subscriber( final String environmentId, final String subscriberId )
    {
        this.environmentId = environmentId;
        this.subscriberId = subscriberId;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public String getSubscriberId()
    {
        return subscriberId;
    }
}
