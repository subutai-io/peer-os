package io.subutai.core.channel.impl.util;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class MessageContentUtilTest
{
    private static final String TARGET = "ID";
    private static final String SRC = "ID";
    @Mock
    Message message;
    @Mock
    Exchange exchange;
    @Mock
    HttpServletResponse response;
    @Mock
    ServletOutputStream servletOutputStream;
    @Mock
    InterceptorChain interceptors;
    @Mock
    SecurityManager securityManager;
    @Mock
    PGPSecretKeyRing secretKeys;
    @Mock
    KeyManager keyManager;
    @Mock
    EncryptionTool encryptionTool;
    @Mock
    CachedOutputStream cachedOutputStream;
    @Mock
    PGPPublicKey pgpPublicKey;


    @Before
    public void setUp() throws Exception
    {
        doReturn( exchange ).when( message ).getExchange();
        doReturn( message ).when( exchange ).getInMessage();
        doReturn( response ).when( message ).get( AbstractHTTPDestination.HTTP_RESPONSE );
        doReturn( servletOutputStream ).when( response ).getOutputStream();
        doReturn( interceptors ).when( message ).getInterceptorChain();
        doReturn( new ByteArrayInputStream( "test".getBytes() ) ).when( message ).getContent( InputStream.class );
        doReturn( keyManager ).when( securityManager ).getKeyManager();
        doReturn( encryptionTool ).when( securityManager ).getEncryptionTool();
        doReturn( secretKeys ).when( keyManager ).getSecretKeyRing( anyString() );
        doReturn( cachedOutputStream ).when( message ).getContent( OutputStream.class );
    }


    @Test
    public void testAbortChain() throws Exception
    {
        // 403
        MessageContentUtil.abortChain( message, new AccessControlException( "" ) );

        verify( response ).setStatus( 403 );


        // 401
        MessageContentUtil.abortChain( message, new LoginException( "" ) );

        verify( response ).setStatus( 401 );


        // 500
        MessageContentUtil.abortChain( message, new RuntimeException( "" ) );

        verify( response ).setStatus( 500 );

        verify( interceptors, atLeastOnce() ).abort();
    }


    @Test
    public void testDecryptContent() throws Exception
    {
        doReturn( "OK".getBytes() ).when( encryptionTool )
                                   .decrypt( isA( byte[].class ), isA( PGPSecretKeyRing.class ), anyString() );

        MessageContentUtil.decryptContent( securityManager, message, SRC, TARGET );

        verify( encryptionTool ).decrypt( isA( byte[].class ), isA( PGPSecretKeyRing.class ), anyString() );

        verify( message ).setContent( any( Class.class ), Matchers.anyObject() );

        //

        doReturn( null ).when( encryptionTool )
                        .decrypt( isA( byte[].class ), isA( PGPSecretKeyRing.class ), anyString() );

        MessageContentUtil.decryptContent( securityManager, message, SRC, TARGET );
    }


    @Test
    public void testEncryptContent() throws Exception
    {
        doReturn( new ByteArrayInputStream( "OK".getBytes() ) ).when( cachedOutputStream ).getInputStream();
        doReturn( pgpPublicKey ).when( keyManager ).getRemoteHostPublicKey( anyString() );

        doReturn( "OK".getBytes() ).when( encryptionTool )
                                   .encrypt( isA( byte[].class ), isA( PGPPublicKey.class ), eq( true ) );


        MessageContentUtil.encryptContent( securityManager, SRC, TARGET, message );

        verify( encryptionTool ).encrypt( isA( byte[].class ), eq( pgpPublicKey ), eq( true ) );
    }
}
