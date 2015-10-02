package io.subutai.core.messenger.impl;


import java.sql.Timestamp;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.core.messenger.api.Message;
import io.subutai.core.messenger.impl.entity.MessageEntity;


/**
 * Message envelope (for internal use)
 */
public class Envelope
{
    private final MessageImpl message;
    private final String targetPeerId;
    private final String recipient;
    private final int timeToLive;
    private final Map<String, String> headers;
    private transient boolean isSent;
    private transient Timestamp createDate;


    public Envelope( MessageEntity message )
    {
        Preconditions.checkNotNull( message, "Message is null" );

        this.message = new MessageImpl( message );
        this.targetPeerId = message.getTargetPeerId();
        this.recipient = message.getRecipient();
        this.timeToLive = message.getTimeToLive();
        this.isSent = message.getIsSent();
        this.createDate = new Timestamp( message.getCreateDate() );
        this.headers = message.getHeaders();
    }


    public Envelope( final Message message, String targetPeerId, String recipient, int timeToLive,
                     Map<String, String> headers )
    {

        Preconditions.checkNotNull( targetPeerId, "Target peer id is null" );
        Preconditions.checkNotNull( message, "Message is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( recipient ), "Invalid recipient" );
        Preconditions.checkArgument( timeToLive > 0, "Invalid time-to-live" );

        this.message = new MessageImpl( message );
        this.targetPeerId = targetPeerId;
        this.recipient = recipient;
        this.timeToLive = timeToLive;
        this.headers = headers;
    }


    public Message getMessage()
    {
        return message;
    }


    public String getTargetPeerId()
    {
        return targetPeerId;
    }


    public String getRecipient()
    {
        return recipient;
    }


    public int getTimeToLive()
    {
        return timeToLive;
    }


    public boolean isSent()
    {
        return isSent;
    }


    public void setSent( final boolean isSent )
    {
        this.isSent = isSent;
    }


    public Timestamp getCreateDate()
    {
        return ( Timestamp ) createDate.clone();
    }


    public void setCreateDate( final Timestamp createDate )
    {
        this.createDate = ( Timestamp ) createDate.clone();
    }


    public Map<String, String> getHeaders()
    {
        return headers;
    }
}
