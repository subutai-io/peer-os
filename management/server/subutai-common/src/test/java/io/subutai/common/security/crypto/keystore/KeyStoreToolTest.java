package io.subutai.common.security.crypto.keystore;


import java.security.KeyStore;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class KeyStoreToolTest
{
    private KeyStoreTool keyStoreTool;

    @Mock
    KeyStoreData keyStoreData;
    @Mock
    KeyStore keyStore;


    @Before
    public void setUp() throws Exception
    {
        keyStoreTool = new KeyStoreTool();
    }


    @Test
    @Ignore
    public void testGetKeyPair() throws Exception
    {
        when( keyStoreData.getAlias() ).thenReturn( "alias" );
        when( keyStoreData.getPassword() ).thenReturn( "password" );

        keyStoreTool.getKeyPair( keyStore, keyStoreData );
    }


    @Test
    public void testEnumKeyStoreType()
    {
        KeyStoreType bks = KeyStoreType.BKS;
        assertNotNull( bks.jce() );
        assertNotNull( bks.isFileBased() );
        assertNotNull( bks.hasEntryPasswords() );
        assertNotNull( bks.supportsKeyEntries() );
        assertNotNull( bks.getCryptoFileType() );
        assertNotNull( bks.toString() );
        assertNotNull( bks.resolveJce( "BKS" ) );
        assertNull( bks.resolveJce( "jce" ) );
    }


    @Test
    public void testEnumMsCapiStoreType()
    {
        MsCapiStoreType personal = MsCapiStoreType.PERSONAL;
        assertNotNull( personal.jce() );
    }
}