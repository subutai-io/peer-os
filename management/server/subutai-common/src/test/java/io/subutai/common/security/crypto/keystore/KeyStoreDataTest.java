package io.subutai.common.security.crypto.keystore;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.security.crypto.keystore.KeyStoreData;
import io.subutai.common.security.crypto.keystore.KeyStoreType;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class KeyStoreDataTest
{
    private KeyStoreData keyStoreData;


    @Before
    public void setUp() throws Exception
    {
        keyStoreData = new KeyStoreData();
        keyStoreData.setupKeyStorePx1();
        keyStoreData.setupKeyStorePx2();
        keyStoreData.setupTrustStorePx1();
        keyStoreData.setupTrustStorePx2();

        keyStoreData.setKeyStoreFile( "keyStoreFile" );
        keyStoreData.setKeyStoreType( KeyStoreType.BKS );
        keyStoreData.setPassword( "password" );
        keyStoreData.setAlias( "alias" );
        keyStoreData.setOverride( true );
        keyStoreData.setImportFileLocation( "importFileLocation" );
        keyStoreData.setExportFileLocation( "exportFileLocation" );
        keyStoreData.setHEXCert( "hexCert" );
    }


    @Test
    public void testProperties()
    {
        assertNotNull( keyStoreData.getKeyStoreFile() );
        assertNotNull( keyStoreData.getKeyStoreType() );
        assertNotNull( keyStoreData.getPassword() );
        assertNotNull( keyStoreData.getAlias() );
        assertNotNull( keyStoreData.isOverride() );
        assertNotNull( keyStoreData.getImportFileLocation() );
        assertNotNull( keyStoreData.getExportFileLocation() );
        assertNotNull( keyStoreData.getHEXCert() );
    }
}