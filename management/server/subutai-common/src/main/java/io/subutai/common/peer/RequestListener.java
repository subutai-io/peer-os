package io.subutai.common.peer;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.peer.Payload;


public abstract class RequestListener
{
    private final String recipient;


    protected RequestListener( final String recipient )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( recipient ), "invalid recipient" );

        this.recipient = recipient;
    }


    public String getRecipient()
    {
        return recipient;
    }


    public abstract Object onRequest( Payload payload ) throws Exception;
}
