package io.subutai.core.messenger.api;


import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Message listener
 */
public abstract class MessageListener
{

    public static final int MAX_RECIPIENT_ID_LEN = 50;
    private String recipient;


    protected MessageListener( final String recipient )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( recipient ), "invalid recipient id" );
        Preconditions.checkArgument( StringUtils.length( recipient ) <= MAX_RECIPIENT_ID_LEN,
                String.format( "Max recipient length must be %d", MAX_RECIPIENT_ID_LEN ) );

        this.recipient = recipient;
    }


    public abstract void onMessage( Message message );


    public String getRecipient()
    {
        return recipient;
    }


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

        if ( !recipient.equals( listener.recipient ) )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        return recipient.hashCode();
    }
}
