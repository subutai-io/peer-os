package io.subutai.core.channel.impl.interceptor;


import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;


/**
 *
 */
public enum InterceptorState
{
    CLIENT_OUT(1),
    SERVER_IN(2),
    SERVER_OUT(3),
    CLIENT_IN(4);

    private int step;


    InterceptorState( final int step )
    {
        this.step = step;
    }


    public int getStep()
    {
        return step;
    }


    public  boolean isActive( final Message message )
    {
        boolean requestor = MessageUtils.isRequestor( message );
        boolean outbound = MessageUtils.isOutbound( message );

        if ( requestor )
        {

            if ( outbound )
            {
                return step == CLIENT_OUT.step;
            }
            else
            {
                return step == CLIENT_IN.step;
            }
        }
        else
        {
            if ( outbound )
            {
                return step == SERVER_OUT.step;
            }
            else
            {
                return step == SERVER_IN.step;
            }
        }
    }
    }
