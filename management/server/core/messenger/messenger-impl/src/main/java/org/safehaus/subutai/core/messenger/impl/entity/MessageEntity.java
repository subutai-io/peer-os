package org.safehaus.subutai.core.messenger.impl.entity;


import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.impl.Envelope;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;


/**
 * Implementation of Message
 */
@Entity
@Table( name = "message" )
@Access( AccessType.FIELD )
public class MessageEntity implements Message, Serializable
{
    public static final int MAX_SENDER_LEN = 50;
    @Id
    @Column( name = "message_id" )
    private String id;
    @Column( name = "source_peer_id" )
    private String sourcePeerId;
    @Column( name = "environment_id" )
    private String environmentId;
    @Column( name = "sender" )
    private String sender;
    @Column( name = "payload" )
    @Lob
    private String payloadString;
    @Column( name = "target_peer_id" )
    private String targetPeerId;
    @Column( name = "recipient" )
    private String recipient;
    @Column( name = "ttl" )
    private Integer timeToLive;
    @Column( name = "is_sent" )
    private Boolean isSent;
    @Column( name = "create_date" )
    private Long createDate;
    @Column( name = "attempts" )
    private Integer attempts = 0;


    public MessageEntity( Envelope envelope )
    {
        Preconditions.checkNotNull( envelope, "Envelope is null" );
        this.id = envelope.getMessage().getId().toString();
        this.sourcePeerId = envelope.getMessage().getSourcePeerId().toString();
        this.payloadString = envelope.getMessage().getPayload();
        this.sender = envelope.getMessage().getSender();
        this.targetPeerId = envelope.getTargetPeerId().toString();
        this.recipient = envelope.getRecipient();
        this.timeToLive = envelope.getTimeToLive();
        this.isSent = envelope.isSent();
        this.createDate = System.currentTimeMillis();
    }


    public MessageEntity()
    {
    }


    @Override
    public UUID getSourcePeerId()
    {
        return UUID.fromString( sourcePeerId );
    }


    @Override
    public UUID getId()
    {
        return UUID.fromString( id );
    }


    @Override
    public <T> T getPayload( Class<T> clazz )
    {
        return JsonUtil.fromJson( payloadString, clazz );
    }


    @Override
    public String getSender()
    {
        return sender;
    }


    @Override
    public void setSender( final String sender )
    {
        Preconditions.checkArgument( StringUtil.getLen( sender ) <= MAX_SENDER_LEN,
                String.format( "Max sender length must be %d", MAX_SENDER_LEN ) );

        this.sender = sender;
    }


    @Override
    public String getPayload()
    {
        return payloadString;
    }


    @Override
    public void setEnvironmentId( final UUID environmentId )
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        this.environmentId = environmentId.toString();
    }


    @Override
    public UUID getEnvironmentId()
    {
        if ( UUIDUtil.isStringAUuid( environmentId ) )
        {
            return UUID.fromString( environmentId );
        }

        return null;
    }


    @Override
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "id", id ).add( "sourcePeerId", sourcePeerId )
                      .add( "sender", sender ).add( "payloadString", payloadString ).toString();
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        final MessageEntity that = ( MessageEntity ) o;

        return id.equals( that.id );
    }


    @Override
    public int hashCode()
    {
        return id.hashCode();
    }


    public UUID getTargetPeerId()
    {
        return UUID.fromString( this.targetPeerId );
    }


    public String getRecipient()
    {
        return this.recipient;
    }


    public Integer getTimeToLive()
    {
        return timeToLive;
    }


    public Boolean getIsSent()
    {
        return isSent;
    }


    public Long getCreateDate()
    {
        return createDate;
    }
}
