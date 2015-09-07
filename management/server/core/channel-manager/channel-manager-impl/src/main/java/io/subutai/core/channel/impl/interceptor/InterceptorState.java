package io.subutai.core.channel.impl.interceptor;


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


    public static boolean isActive( final Message message, InterceptorState state )
    {
        boolean requestor = MessageUtils.isRequestor( message );
        boolean outbound = MessageUtils.isOutbound( message );

        if ( requestor )
        {

            if ( outbound )
            {
                return state == CLIENT_OUT;
            }
            else
            {
                return state == CLIENT_IN;
            }
        }
        else
        {
            if ( outbound )
            {
                return state == SERVER_IN;
            }
            else
            {
                return state == CLIENT_OUT;
            }
        }
    }
    }
