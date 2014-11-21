package org.safehaus.subutai.core.messenger.impl;


import java.sql.Timestamp;
import java.util.UUID;

import org.safehaus.subutai.core.messenger.api.Message;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Message envelope (for internal use)
 */
public class Envelope
{
    private final MessageImpl message;
    private final UUID targetPeerId;
    private final String recipient;
    private final int timeToLive;
    private transient boolean isSent;
    private transient Timestamp createDate;


    public Envelope( final MessageImpl message, UUID targetPeerId, String recipient, int timeToLive )
    {

        Preconditions.checkNotNull( targetPeerId, "Target peer id is null" );
        Preconditions.checkNotNull( message, "Message is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( recipient ), "Invalid recipient" );
        Preconditions.checkArgument( timeToLive > 0, "Invalid time-to-live" );

        this.message = message;
        this.targetPeerId = targetPeerId;
        this.recipient = recipient;
        this.timeToLive = timeToLive;
    }


    public Message getMessage()
    {
        return message;
    }


    public UUID getTargetPeerId()
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
}
