package org.safehaus.subutai.common.security.crypto.ssl;


import java.security.KeyStore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.security.crypto.keystore.KeyStoreData;

import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class SSLManagerTest
{
    private SSLManager sslManager;

    @Mock
    KeyStore keyStore;
    @Mock
    KeyStoreData keyStoreData;


    @Before
    public void setUp() throws Exception
    {
        sslManager = new SSLManager( keyStore, keyStoreData, keyStore, keyStoreData );
    }


    @Test
    public void testGetClientKeyManagers() throws Exception
    {
        when( keyStoreData.getPassword() ).thenReturn( "password" );

        sslManager.getClientKeyManagers();
    }


    @Test
    public void testGetClientTrustManagers() throws Exception
    {
        sslManager.getClientTrustManagers();
    }


    @Test
    public void testGetClientTrustManagersException() throws Exception
    {
        when( keyStoreData.getPassword() ).thenThrow( Exception.class );

        sslManager.getClientTrustManagers();
    }


    @Test
    public void testGetClientFullTrustManagers() throws Exception
    {
        sslManager.getClientFullTrustManagers();
    }
}