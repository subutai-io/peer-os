package io.subutai.core.messenger.impl.entity;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import io.subutai.common.util.JsonUtil;
import io.subutai.core.messenger.api.Message;
import io.subutai.core.messenger.impl.Envelope;


/**
 * Implementation of Message
 */
@Entity
@Table( name = "message" )
@Access( AccessType.FIELD )
public class MessageEntity implements Message
{
    public static final int MAX_SENDER_LEN = 50;
    @Id
    @Column( name = "message_id" )
    private String id;
    @Column( name = "source_peer_id" )
    private String sourcePeerId;
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

    @ElementCollection( fetch = FetchType.EAGER )
    @CollectionTable( name = "message_headers" )
    @MapKeyColumn( name = "header_name" )
    @Column( name = "header_value" )
    private Map<String, String> headers = new HashMap<>();


    public MessageEntity( Envelope envelope )
    {
        Preconditions.checkNotNull( envelope, "Envelope is null" );
        this.id = envelope.getMessage().getId().toString();
        this.sourcePeerId = envelope.getMessage().getSourcePeerId();
        this.payloadString = envelope.getMessage().getPayload();
        this.sender = envelope.getMessage().getSender();
        this.targetPeerId = envelope.getTargetPeerId();
        this.recipient = envelope.getRecipient();
        this.timeToLive = envelope.getTimeToLive();
        this.isSent = envelope.isSent();
        this.createDate = System.currentTimeMillis();
        if ( envelope.getHeaders() != null )
        {
            this.headers = envelope.getHeaders();
        }
    }


    public MessageEntity()
    {
    }


    public Map<String, String> getHeaders()
    {
        return headers;
    }


    @Override
    public String getSourcePeerId()
    {
        return sourcePeerId;
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
        Preconditions.checkArgument( StringUtils.length( sender ) <= MAX_SENDER_LEN,
                String.format( "Max sender length must be %d", MAX_SENDER_LEN ) );

        this.sender = sender;
    }


    @Override
    public String getPayload()
    {
        return payloadString;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "id", id ).add( "sourcePeerId", sourcePeerId )
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


    public String getTargetPeerId()
    {
        return this.targetPeerId;
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
