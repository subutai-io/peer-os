package io.subutai.core.security.impl.crypto;


import java.security.KeyStore;

import io.subutai.common.security.crypto.keystore.KeyStoreData;
import io.subutai.common.security.crypto.keystore.KeyStoreTool;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.SecuritySettings;
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
    public void importCertAsTrusted(String port, String storeAlias, String certificateHEX)
    {
        KeyStore keyStore;
        KeyStoreData keyStoreData;
        KeyStoreTool keyStoreTool;

        keyStoreData = new KeyStoreData();

        if(port.equals( ChannelSettings.SECURE_PORT_X1 ))
        {
            keyStoreData.setupTrustStorePx1();
        }
        else if(port.equals( ChannelSettings.SECURE_PORT_X2 ))
        {
            keyStoreData.setupTrustStorePx2();
        }

        keyStoreData.setHEXCert( certificateHEX);
        keyStoreData.setAlias( storeAlias );

        keyStoreTool = new KeyStoreTool();
        keyStore = keyStoreTool.load( keyStoreData );

        keyStoreTool.importCertificateHEXString( keyStore, keyStoreData );
    }


    /* *****************************
     *
     */
    @Override
    public String exportCertificate(String port,String storeAlias)
    {
        String cert = "";
        KeyStore keyStore;
        KeyStoreData keyStoreData;
        KeyStoreTool keyStoreTool;

        keyStoreData = new KeyStoreData();

        if(port.equals( ChannelSettings.SECURE_PORT_X1 ))
        {
            keyStoreData = new KeyStoreData();
            keyStoreData.setupKeyStorePx1();
        }
        else if(port.equals( ChannelSettings.SECURE_PORT_X2 ))
        {
            keyStoreData = new KeyStoreData();
            keyStoreData.setupKeyStorePx2();
        }

        keyStoreTool = new KeyStoreTool();
        keyStore = keyStoreTool.load( keyStoreData );

        cert = keyStoreTool.exportCertificateHEXString( keyStore, keyStoreData );

        return cert;
    }


    /* *********** Delete Trust SSL Cert ***************************
     *
     */
    public void removeCertFromTrusted(String port,String storeAlias)
    {

        KeyStore keyStore;
        KeyStoreData keyStoreData;
        KeyStoreTool keyStoreTool;

        keyStoreData = new KeyStoreData();

        if(port.equals( ChannelSettings.SECURE_PORT_X1 ))
        {
            keyStoreData.setupTrustStorePx1();
        }
        else if(port.equals( ChannelSettings.SECURE_PORT_X2 ))
        {
            keyStoreData.setupTrustStorePx2();
        }

        keyStoreData.setAlias( storeAlias);

        keyStoreTool = new KeyStoreTool();
        keyStore = keyStoreTool.load( keyStoreData );

        keyStoreTool.deleteEntry( keyStore, keyStoreData );
        //***********************************************************************
    }
}
