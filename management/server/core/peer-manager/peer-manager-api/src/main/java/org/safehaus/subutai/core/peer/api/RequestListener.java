package org.safehaus.subutai.core.peer.api;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


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
