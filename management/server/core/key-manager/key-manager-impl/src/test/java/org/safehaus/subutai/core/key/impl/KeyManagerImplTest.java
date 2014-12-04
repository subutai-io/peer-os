package org.safehaus.subutai.core.key.impl;


import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.key.api.KeyInfo;
import org.safehaus.subutai.core.key.api.KeyManagerException;
import org.safehaus.subutai.core.peer.api.CommandUtil;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerManager;

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
            "pub sub real email\n" + "4668B463 4668B464:4668B465 '%s' %s\n" + "4668B466 4668B467 :4668B468 '%1$s' %2$s",
            REAL_NAME, EMAIL );
    private static final String KEY_ID = "123";
    private static final String PATH = "path";

    @Mock
    PeerManager peerManager;
    @Mock
    CommandUtil commandUtil;
    @Mock
    LocalPeer localPeer;
    @Mock
    ManagementHost managementHost;
    @Mock
    CommandResult commandResult;
    @Mock
    RequestBuilder requestBuilder;

    KeyManagerImpl keyManager;


    @Before
    public void setUp() throws Exception
    {
        keyManager = new KeyManagerImpl( peerManager );
        keyManager.commandUtil = commandUtil;
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getManagementHost() ).thenReturn( managementHost );
        when( commandUtil.execute( any( RequestBuilder.class ), any( Host.class ) ) ).thenReturn( commandResult );
        when( commandResult.getStdOut() ).thenReturn( OUTPUT );
    }


    @Test( expected = KeyManagerException.class )
    public void testGetManagementHost() throws Exception
    {
        assertEquals( managementHost, keyManager.getManagementHost() );

        doThrow( new HostNotFoundException( null ) ).when( localPeer ).getManagementHost();

        keyManager.getManagementHost();
    }


    @Test( expected = KeyManagerException.class )
    public void testExecute() throws Exception
    {

        assertEquals( OUTPUT, keyManager.execute( requestBuilder ) );

        doThrow( new CommandException( "" ) ).when( commandUtil )
                                             .execute( any( RequestBuilder.class ), any( Host.class ) );
        keyManager.execute( requestBuilder );
    }


    @Test( expected = KeyManagerException.class )
    public void testGenerateKey() throws Exception
    {
        KeyInfo keyInfo = keyManager.generateKey( REAL_NAME, EMAIL );

        assertNotNull( keyInfo );
        assertEquals( REAL_NAME, keyInfo.getRealName() );
        assertEquals( EMAIL, keyInfo.getEmail() );


        when( commandResult.getStdOut() ).thenReturn( "" );

        keyManager.generateKey( REAL_NAME, EMAIL );
    }


    @Test
    public void testExportSshKey() throws Exception
    {
        keyManager.exportSshKey( KEY_ID, PATH );

        verify( commandUtil ).execute( isA( RequestBuilder.class ), isA( Host.class ) );
    }


    @Test
    public void testSignFileWithKey() throws Exception
    {
        keyManager.signFileWithKey( KEY_ID, PATH );

        verify( commandUtil ).execute( isA( RequestBuilder.class ), isA( Host.class ) );
    }


    @Test
    public void testSendKeyToHub() throws Exception
    {
        keyManager.sendKeyToHub( KEY_ID );

        verify( commandUtil ).execute( isA( RequestBuilder.class ), isA( Host.class ) );
    }


    @Test( expected = KeyManagerException.class )
    public void testGetKey() throws Exception
    {
        KeyInfo keyInfo = keyManager.getKey( KEY_ID );

        assertNotNull( keyInfo );
        assertEquals( REAL_NAME, keyInfo.getRealName() );
        assertEquals( EMAIL, keyInfo.getEmail() );


        when( commandResult.getStdOut() ).thenReturn( "" );

        keyManager.getKey( KEY_ID );
    }


    @Test
    public void testGetKeys() throws Exception
    {
        Set<KeyInfo> keys = keyManager.getKeys();

        assertFalse( keys.isEmpty() );
        KeyInfo keyInfo = keys.iterator().next();
        assertEquals( REAL_NAME, keyInfo.getRealName() );
        assertEquals( EMAIL, keyInfo.getEmail() );
    }
}
