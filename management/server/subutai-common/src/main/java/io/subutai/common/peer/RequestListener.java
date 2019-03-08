package io.subutai.common.peer;


import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public abstract class RequestListener
{
    private final String recipient;


    protected RequestListener( final String recipient )
    {
        Preconditions.checkArgument( !StringUtils.isBlank( recipient ), "invalid recipient" );

        this.recipient = recipient;
    }


    public String getRecipient()
    {
        return recipient;
    }


    public abstract Object onRequest( Payload payload ) throws PeerException;
}
