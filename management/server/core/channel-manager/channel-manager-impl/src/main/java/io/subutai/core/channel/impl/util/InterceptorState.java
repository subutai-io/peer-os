package io.subutai.core.channel.impl.util;


import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;


/**
 *
 */
public enum InterceptorState
{
    CLIENT_OUT,
    SERVER_IN,
    SERVER_OUT,
    CLIENT_IN;


    public boolean isActive( final Message message )
    {
        boolean requestor = MessageUtils.isRequestor( message );
        boolean outbound = MessageUtils.isOutbound( message );

        if ( requestor )
        {

            if ( outbound )
            {
                return this == CLIENT_OUT;
            }
            else
            {
                return this == CLIENT_IN;
            }
        }
        else
        {
            if ( outbound )
            {
                return this == SERVER_OUT;
            }
            else
            {
                return this == SERVER_IN;
            }
        }
    }
}
