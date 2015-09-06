package io.subutai.core.channel.impl.test;


import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;


/**
 * Created by dilshat on 9/6/15.
 */
public class StepUtil
{
    public static boolean isActive( final Message message, Step step )
    {
        boolean requestor = MessageUtils.isRequestor( message );
        boolean outbound = MessageUtils.isOutbound( message );

        if ( requestor )
        {

            if ( outbound )
            {
                return step == Step.ONE;
            }
            else
            {
                return step == Step.FOUR;
            }
        }
        else
        {
            if ( outbound )
            {
                return step == Step.THREE;
            }
            else
            {
                return step == Step.TWO;
            }
        }
    }
}
