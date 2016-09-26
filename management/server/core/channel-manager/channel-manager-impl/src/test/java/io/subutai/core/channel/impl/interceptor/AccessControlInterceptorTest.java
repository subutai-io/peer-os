package io.subutai.core.channel.impl.interceptor;


import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.Common;
import io.subutai.core.channel.impl.ChannelManagerImpl;
import io.subutai.core.channel.impl.util.InterceptorState;
import io.subutai.core.identity.api.IdentityManager;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class AccessControlInterceptorTest
{
    @Mock
    ChannelManagerImpl channelManager;
    @Mock
    Message message;
    @Mock
    HttpServletRequest request;
    @Mock
    Exchange exchange;
    @Mock
    Cookie cookie;
    @Mock
    IdentityManager identityManager;
    @Mock
    HttpServletResponse response;
    @Mock
    InterceptorChain interceptorChain;


    AccessControlInterceptor accessControlInterceptor;


    @Before
    public void setUp() throws Exception
    {
        accessControlInterceptor = spy( new AccessControlInterceptor( channelManager ) );
        doReturn( identityManager ).when( channelManager ).getIdentityManager();
        doReturn( exchange ).when( message ).getExchange();
        doReturn( message ).when( exchange ).getInMessage();
        doReturn( new Cookie[] { cookie } ).when( request ).getCookies();
    }


    @Test
    public void testAuthenticateAccess() throws Exception
    {
        accessControlInterceptor.authenticateAccess( null, request );

        verify( identityManager ).loginSystemUser();
    }


    @Test
    public void testAuthenticateAccess2() throws Exception
    {
        assertNull( accessControlInterceptor.authenticateAccess( message, request ) );
    }


    @Test
    public void testAuthenticateAccess3() throws Exception
    {
        doReturn( "sptoken" ).when( cookie ).getName();
        doReturn( "sptoken" ).when( cookie ).getValue();

        accessControlInterceptor.authenticateAccess( message, request );

        verify( identityManager ).login( "token", "sptoken" );
    }


    @Test
    public void testHandleMessage() throws Exception
    {
        Message message = InterceptorStateHelper.getMessage( InterceptorState.SERVER_IN );

        doReturn( request ).when( message ).get( AbstractHTTPDestination.HTTP_REQUEST );
        doReturn( exchange ).when( message ).getExchange();
        doReturn( interceptorChain ).when( message ).getInterceptorChain();
        doReturn( message ).when( exchange ).getInMessage();
        doReturn( Common.DEFAULT_PUBLIC_SECURE_PORT ).when( request ).getLocalPort();
        doReturn( response ).when( message ).get( AbstractHTTPDestination.HTTP_RESPONSE );
        ServletOutputStream outputStream = mock( ServletOutputStream.class );
        doReturn( outputStream ).when( response ).getOutputStream();

        accessControlInterceptor.handleMessage( message );

        verify( accessControlInterceptor ).authenticateAccess( null, null );

        verify( interceptorChain ).abort();

        doReturn( ChannelSettings.URL_ACCESS_PX1[0] ).when( request ).getRequestURI();
        doReturn( -1 ).when( request ).getLocalPort();

        accessControlInterceptor.handleMessage( message );

        verify( accessControlInterceptor, times( 2 ) ).authenticateAccess( null, null );

        doReturn( Common.DEFAULT_PUBLIC_PORT ).when( request ).getLocalPort();
        doReturn( "/blabla" ).when( request ).getRequestURI();

        accessControlInterceptor.handleMessage( message );

        verify( accessControlInterceptor ).authenticateAccess( message, request );
    }
}
