package org.safehaus.subutai.core.peer.api.message;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Handy peer message implementation
 */
public class PeerMessageImpl implements PeerMessage {

    private final String recipientName;
    private final Object message;


    public PeerMessageImpl( final String recipientName, final Object message ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( recipientName ), "Recipient name is null or empty" );
        Preconditions.checkNotNull( message, "Message is null" );

        this.recipientName = recipientName;
        this.message = message;
    }


    @Override
    public String getRecipientName() {
        return recipientName;
    }


    @Override
    public Object getMessage() {
        return message;
    }
}
