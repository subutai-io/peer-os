package org.safehaus.subutai.core.message.impl;


import java.util.UUID;

import org.safehaus.subutai.core.message.api.Message;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Message envelope (for internal use)
 */
public class Envelope
{
    private final MessageImpl message;
    private final UUID sourcePeerId;
    private final UUID targetPeerId;
    private final String recipient;
    private final int timeToLive;


    public Envelope( final MessageImpl message, UUID sourcePeerId, UUID targetPeerId, String recipient, int timeToLive )
    {

        Preconditions.checkNotNull( sourcePeerId, "Source peer id is null" );
        Preconditions.checkNotNull( message, "Message is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( recipient ), "Invalid recipient" );
        Preconditions.checkArgument( timeToLive > 0, "Invalid time-to-live" );

        this.message = message;
        this.sourcePeerId = sourcePeerId;
        this.targetPeerId = targetPeerId;
        this.recipient = recipient;
        this.timeToLive = timeToLive;
    }


    public Message getMessage()
    {
        return message;
    }


    public UUID getSourcePeerId()
    {
        return sourcePeerId;
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
}
