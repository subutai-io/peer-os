package io.subutai.core.key.impl;


import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.peer.Host;
import io.subutai.core.key.api.KeyInfo;
import io.subutai.core.key.api.KeyManagerException;
import io.subutai.core.key.impl.KeyManagerImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class KeyManagerImplTest
{

    private static final String REAL_NAME = "real name";
    private static final String EMAIL = "real@mail.com";
    private static final String OUTPUT = String.format(
            "pub sub real email\n---------\n" + "4668B463 4668B464:4668B465 %s %s\n" + "4668B466 4668B467 :4668B468 %1$s %2$s",
            REAL_NAME, EMAIL );
    private static final String KEY_ID = "123";
    private static final String PATH = "path";


    @Mock
    CommandUtil commandUtil;
    @Mock
    CommandResult commandResult;
    @Mock
    RequestBuilder requestBuilder;
    @Mock
    Host host;

    KeyManagerImpl keyManager;


    @Before
    public void setUp() throws Exception
    {
        keyManager = new KeyManagerImpl();
        keyManager.commandUtil = commandUtil;
        when( commandUtil.execute( any( RequestBuilder.class ), any( Host.class ) ) ).thenReturn( commandResult );
        when( commandResult.getStdOut() ).thenReturn( OUTPUT );
    }


    @Test( expected = KeyManagerException.class )
    public void testExecute() throws Exception
    {

        assertEquals( OUTPUT, keyManager.execute( requestBuilder, host ) );

        doThrow( new CommandException( "" ) ).when( commandUtil )
                                             .execute( any( RequestBuilder.class ), any( Host.class ) );
        keyManager.execute( requestBuilder, host );
    }


    @Test( expected = KeyManagerException.class )
    public void testGenerateKey() throws Exception
    {
        KeyInfo keyInfo = keyManager.generateKey( host, REAL_NAME, EMAIL );

        assertNotNull( keyInfo );
        assertEquals( REAL_NAME, keyInfo.getRealName() );
        assertEquals( EMAIL, keyInfo.getEmail() );


        when( commandResult.getStdOut() ).thenReturn( "" );

        keyManager.generateKey( host, REAL_NAME, EMAIL );
    }


    @Test
    public void testReadKey() throws Exception
    {
        String output = keyManager.readKey( host, KEY_ID );

        verify( commandUtil ).execute( isA( RequestBuilder.class ), isA( Host.class ) );
        assertEquals( OUTPUT, output );
    }


    @Test
    public void testReadSshKey() throws Exception
    {
        String output = keyManager.readSshKey( host, KEY_ID );

        verify( commandUtil ).execute( isA( RequestBuilder.class ), isA( Host.class ) );
        assertEquals( OUTPUT, output );
    }


    @Test
    public void testDeleteKey() throws Exception
    {
        keyManager.deleteKey( host, KEY_ID );

        verify( commandUtil ).execute( isA( RequestBuilder.class ), isA( Host.class ) );
    }


    @Test
    public void testRevokeKey() throws Exception
    {
        keyManager.revokeKey( host, KEY_ID );

        verify( commandUtil ).execute( isA( RequestBuilder.class ), isA( Host.class ) );
    }


    @Test
    public void testSignFileWithKey() throws Exception
    {
        keyManager.signFileWithKey( host, KEY_ID, PATH );

        verify( commandUtil ).execute( isA( RequestBuilder.class ), isA( Host.class ) );
    }


    @Test
    public void testSignKeyWithKey() throws Exception
    {
        keyManager.signKeyWithKey( host, KEY_ID, KEY_ID );

        verify( commandUtil ).execute( isA( RequestBuilder.class ), isA( Host.class ) );
    }


    @Test
    public void testSendKeyToHub() throws Exception
    {
        keyManager.sendRevocationKeyToPublicKeyServer( host, KEY_ID );

        verify( commandUtil ).execute( isA( RequestBuilder.class ), isA( Host.class ) );
    }


    @Test( expected = KeyManagerException.class )
    public void testGetKey() throws Exception
    {
        KeyInfo keyInfo = keyManager.getKey( host, KEY_ID );

        assertNotNull( keyInfo );
        assertEquals( REAL_NAME, keyInfo.getRealName() );
        assertEquals( EMAIL, keyInfo.getEmail() );


        when( commandResult.getStdOut() ).thenReturn( "" );

        keyManager.getKey( host, KEY_ID );
    }


    @Test
    public void testGetKeys() throws Exception
    {
        Set<KeyInfo> keys = keyManager.getKeys( host );

        assertFalse( keys.isEmpty() );
        KeyInfo keyInfo = keys.iterator().next();
        assertEquals( REAL_NAME, keyInfo.getRealName() );
        assertEquals( EMAIL, keyInfo.getEmail() );
    }
}
