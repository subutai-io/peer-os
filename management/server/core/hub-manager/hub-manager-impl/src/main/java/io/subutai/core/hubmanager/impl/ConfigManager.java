package io.subutai.core.hubmanager.impl;


import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import io.subutai.common.security.crypto.certificate.CertificateData;
import io.subutai.common.security.crypto.certificate.CertificateTool;
import io.subutai.common.security.crypto.key.KeyPairType;
import io.subutai.common.security.crypto.keystore.KeyStoreData;
import io.subutai.common.security.crypto.keystore.KeyStoreTool;
import io.subutai.common.security.crypto.keystore.KeyStoreType;
import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.settings.Common;
import io.subutai.core.hubmanager.api.model.Config;
import io.subutai.core.hubmanager.impl.model.ConfigEntity;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.hub.share.pgp.key.PGPKeyHelper;
import io.subutai.hub.share.pgp.message.PGPMessenger;


public class ConfigManager
{
    private static final Logger LOG = LoggerFactory.getLogger( ConfigManager.class.getName() );

    public static final String H_PUB_KEY = Common.SUBUTAI_APP_DATA_PATH + "/keystores/h.public.gpg";
    public static final String PEER_KEYSTORE = Common.SUBUTAI_APP_DATA_PATH + "/keystores/peer.jks";
    public static final String PEER_SECRET_KEY = Common.SUBUTAI_APP_DATA_PATH + "/keystores/peer.secret.key";


    public SecurityManager getSecurityManager()
    {
        return securityManager;
    }


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    private SecurityManager securityManager;
    private PeerManager peerManager;

    private PGPPublicKey hPublicKey;
    private PGPPublicKey ownerPublicKey;
    private PGPPublicKey peerPublicKey;
    private PGPPrivateKey sender;
    private KeyStore keyStore;
    private String peerId;
    private PGPMessenger messenger;
    private Config hubConfiguration = new ConfigEntity();
    private String hubIp;
    private String superNodeIp;


    public ConfigManager( final SecurityManager securityManager, final PeerManager peerManager,
                          final String peerSecretKeyringPwd )
            throws IOException, PGPException, KeyStoreException, CertificateException, NoSuchAlgorithmException
    {
        this.peerManager = peerManager;
        this.securityManager = securityManager;

        if ( sender == null )
        {
            PGPSecretKeyRing signingKeyRing = PGPKeyUtil.readSecretKeyRing( new FileInputStream( PEER_SECRET_KEY ) );
            PGPSecretKey signingKey = signingKeyRing.getSecretKey();
            this.sender = PGPEncryptionUtil.getPrivateKey( signingKey, peerSecretKeyringPwd );
        }

        if ( peerId == null )
        {
            this.peerId = peerManager.getLocalPeer().getId();
            LOG.debug( "Getting peer id: " + peerId );
        }

        if ( hPublicKey == null )
        {
            this.hPublicKey = PGPKeyHelper.readPublicKey( H_PUB_KEY );
            LOG.debug( "Getting hPublicKey from keystores folder: " + hPublicKey.toString() );
        }

        if ( ownerPublicKey == null )
        {
//            this.ownerPublicKey = securityManager.getKeyManager().getPublicKey( "owner-" + peerId );
            this.ownerPublicKey =
                    securityManager.getKeyManager().getPublicKeyRing( securityManager.getKeyManager().getPeerOwnerId() )
                                   .getPublicKey();
        }

        if ( peerPublicKey == null )
        {
            this.peerPublicKey = securityManager.getKeyManager().getPublicKey( null );
        }

        if ( keyStore == null )
        {
            generateX509Certificate();
            this.keyStore = KeyStore.getInstance( "JKS" );
            this.keyStore.load( new FileInputStream( PEER_KEYSTORE ), "subutai".toCharArray() );
        }

        if ( messenger == null )
        {
            this.messenger = new PGPMessenger( sender, hPublicKey );
        }
    }


    private void generateX509Certificate()
    {
        try
        {
            String fingerprint = PGPKeyUtil.getFingerprint( peerPublicKey.getFingerprint() );

            //**************Saving X509 certificate******************************************
            io.subutai.common.security.crypto.key.KeyManager sslkeyMan =
                    new io.subutai.common.security.crypto.key.KeyManager();
            KeyPairGenerator keyPairGenerator = sslkeyMan.prepareKeyPairGeneration( KeyPairType.RSA, 1024 );
            java.security.KeyPair sslKeyPair = sslkeyMan.generateKeyPair( keyPairGenerator );

            KeyStoreData keyStoreData = new KeyStoreData();
            keyStoreData.setKeyStoreFile( PEER_KEYSTORE );
            keyStoreData.setAlias( "root_server_px1" );
            keyStoreData.setPassword( "subutai" );
            keyStoreData.setKeyStoreType( KeyStoreType.JKS );

            KeyStoreTool keyStoreTool = new KeyStoreTool();
            KeyStore sslkeyStore = keyStoreTool.load( keyStoreData );


            CertificateData certificateData = new CertificateData();
            certificateData.setCommonName( fingerprint );

            CertificateTool certificateTool = new CertificateTool();

            X509Certificate x509cert = certificateTool
                    .generateSelfSignedCertificate( sslKeyPair,certificateData );

            keyStoreTool.saveX509Certificate( sslkeyStore, keyStoreData, x509cert, sslKeyPair );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }


    public KeyStore getKeyStore()
    {
        return keyStore;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public PGPPublicKey gethPublicKey()
    {
        return hPublicKey;
    }


    public PGPPublicKey getOwnerPublicKey()
    {
        return ownerPublicKey;
    }


    public PGPPublicKey getPeerPublicKey()
    {
        return peerPublicKey;
    }


    public PGPPrivateKey getSender()
    {
        return sender;
    }


    public PGPMessenger getMessenger()
    {
        return messenger;
    }


    public WebClient getTrustedWebClientWithAuth( String path )
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException
    {
        String baseUrl = String.format( "https://%s", getHubIp() );
        return HttpClient.createTrustedWebClientWithAuth( baseUrl + path, keyStore, "subutai".toCharArray(),
                hPublicKey.getFingerprint() );
    }


    public WebClient getTrustedWebClient()
    {
        String baseUrl = String.format( "https://%s", getHubIp() );
        return HttpClient.createTrustedWebClient( baseUrl );
    }


    public void addHubConfig( final String hubIp )
    {
        hubConfiguration.setHubIp( hubIp );
        hubConfiguration.setPeerId( getPeerId() );
        setHubIp( hubIp );
        setSuperNodeIp( superNodeIp );
    }


    public Config getHubConfiguration()
    {
        return hubConfiguration;
    }

    public String getHubIp()
    {
        return hubIp;
    }


    public void setHubIp( final String hubIp )
    {
        this.hubIp = hubIp;
    }


    public String getSuperNodeIp()
    {
        return superNodeIp;
    }


    public void setSuperNodeIp( final String superNodeIp )
    {
        this.superNodeIp = superNodeIp;
    }

}
