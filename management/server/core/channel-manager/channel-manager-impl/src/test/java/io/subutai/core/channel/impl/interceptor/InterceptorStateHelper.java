package io.subutai.core.channel.impl.interceptor;


import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;

import io.subutai.core.channel.impl.util.InterceptorState;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


public class InterceptorStateHelper
{

    public static Message getMessage( InterceptorState state )
    {
        Message message = mock( Message.class );
        Message message2 = mock( Message.class );


        Exchange exchange = mock( Exchange.class );

        if ( state == InterceptorState.CLIENT_OUT )
        {
            doReturn( true ).when( message ).get( "org.apache.cxf.client" );
            doReturn( exchange ).when( message ).getExchange();
            doReturn( message ).when( exchange ).getOutMessage();
        }
        else if ( state == InterceptorState.CLIENT_IN )
        {
            doReturn( true ).when( message ).get( "org.apache.cxf.client" );
            doReturn( exchange ).when( message ).getExchange();
            doReturn( message2 ).when( exchange ).getOutMessage();
        }
        else if ( state == InterceptorState.SERVER_IN )
        {
            doReturn( false ).when( message ).get( "org.apache.cxf.client" );
            doReturn( exchange ).when( message ).getExchange();
            doReturn( message2 ).when( exchange ).getOutMessage();
        }
        else if ( state == InterceptorState.SERVER_OUT )
        {
            doReturn( false ).when( message ).get( "org.apache.cxf.client" );
            doReturn( exchange ).when( message ).getExchange();
            doReturn( message ).when( exchange ).getOutMessage();
        }

        return message;
    }
}
