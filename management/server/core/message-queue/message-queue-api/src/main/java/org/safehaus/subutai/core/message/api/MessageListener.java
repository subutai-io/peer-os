package org.safehaus.subutai.core.message.api;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Message listener
 */
public abstract class MessageListener
{

    private String recipientId;


    protected MessageListener( final String recipientId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( recipientId ), "invalid recipient id" );

        this.recipientId = recipientId;
    }


    public abstract void onMessage( Message message );


    public String getRecipientId() {return recipientId;}


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof MessageListener ) )
        {
            return false;
        }

        final MessageListener listener = ( MessageListener ) o;

        if ( !recipientId.equals( listener.recipientId ) )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        return recipientId.hashCode();
    }
}
