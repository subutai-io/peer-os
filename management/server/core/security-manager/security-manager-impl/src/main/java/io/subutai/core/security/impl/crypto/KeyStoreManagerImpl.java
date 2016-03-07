package io.subutai.core.security.impl.crypto;


import java.security.KeyStore;

import io.subutai.common.security.crypto.keystore.KeyStoreData;
import io.subutai.common.security.crypto.keystore.KeyStoreTool;
import io.subutai.common.settings.SystemSettings;
import io.subutai.core.security.api.crypto.KeyStoreManager;


/**
 * Manages JKS keystore
 */
public class KeyStoreManagerImpl implements KeyStoreManager
{
    /* *****************************
     *
     */
    @Override
    public synchronized void importCertAsTrusted( int port, String storeAlias, String certificateHEX )
    {
        KeyStore keyStore;
        KeyStoreData keyStoreData;
        KeyStoreTool keyStoreTool;

        keyStoreData = new KeyStoreData();

        if ( port == SystemSettings.getSecurePortX1() )
        {
            keyStoreData.setupTrustStorePx1();
        }
        else if ( port == SystemSettings.getSecurePortX2() )
        {
            keyStoreData.setupTrustStorePx2();
        }

        keyStoreData.setHEXCert( certificateHEX );
        keyStoreData.setAlias( storeAlias );

        keyStoreTool = new KeyStoreTool();
        keyStore = keyStoreTool.load( keyStoreData );

        keyStoreTool.importCertificateInPem( keyStore, keyStoreData );
    }


    /* *****************************
     *
     */
    @Override
    public String exportCertificate( int port, String storeAlias )
    {
        String cert = "";
        KeyStore keyStore;
        KeyStoreData keyStoreData;
        KeyStoreTool keyStoreTool;

        keyStoreData = new KeyStoreData();

        if ( port == SystemSettings.getSecurePortX1() )
        {
            keyStoreData = new KeyStoreData();
            keyStoreData.setupKeyStorePx1();
        }
        else if ( port == SystemSettings.getSecurePortX2() )
        {
            keyStoreData = new KeyStoreData();
            keyStoreData.setupKeyStorePx2();
        }

        keyStoreTool = new KeyStoreTool();
        keyStore = keyStoreTool.load( keyStoreData );

        cert = keyStoreTool.exportCertificateInPem( keyStore, keyStoreData );

        return cert;
    }


    /* *********** Delete Trust SSL Cert ***************************
     *
     */
    public void removeCertFromTrusted( int port, String storeAlias )
    {

        KeyStore keyStore;
        KeyStoreData keyStoreData;
        KeyStoreTool keyStoreTool;

        keyStoreData = new KeyStoreData();

        if ( port == SystemSettings.getSecurePortX1() )
        {
            keyStoreData.setupTrustStorePx1();
        }
        else if ( port == SystemSettings.getSecurePortX2() )
        {
            keyStoreData.setupTrustStorePx2();
        }

        keyStoreData.setAlias( storeAlias );

        keyStoreTool = new KeyStoreTool();
        keyStore = keyStoreTool.load( keyStoreData );

        keyStoreTool.deleteEntry( keyStore, keyStoreData );
        //***********************************************************************
    }
}
