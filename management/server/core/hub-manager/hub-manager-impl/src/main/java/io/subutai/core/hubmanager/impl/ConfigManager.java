package io.subutai.core.hubmanager.impl;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;

import io.subutai.common.security.crypto.certificate.CertificateData;
import io.subutai.common.security.crypto.certificate.CertificateTool;
import io.subutai.common.security.crypto.key.KeyPairType;
import io.subutai.common.security.crypto.keystore.KeyStoreData;
import io.subutai.common.security.crypto.keystore.KeyStoreTool;
import io.subutai.common.security.crypto.keystore.KeyStoreType;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SecuritySettings;
import io.subutai.core.hubmanager.api.dao.ConfigDataService;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.hub.share.pgp.key.PGPKeyHelper;
import io.subutai.hub.share.pgp.message.PGPMessenger;


public class ConfigManager
{
    private static final Logger LOG = LoggerFactory.getLogger( ConfigManager.class.getName() );

    public static final String H_PUB_KEY = Common.SUBUTAI_APP_DATA_PATH + "/keystores/h.public.gpg";

    public static final String PEER_KEYSTORE = Common.SUBUTAI_APP_DATA_PATH + "/keystores/peer.jks";

    private static final String PEER_CERT_ALIAS = "peer_cert";

    private static final int HUB_PORT = 444;

    private SecurityManager securityManager;
    private IdentityManager identityManager;
    private PeerManager peerManager;

    private PGPPublicKey hPublicKey;
    private PGPPublicKey ownerPublicKey;
    private PGPPublicKey peerPublicKey;
    private PGPPrivateKey sender;
    private KeyStore keyStore;
    private String peerId;
    private PGPMessenger messenger;


    public SecurityManager getSecurityManager()
    {
        return securityManager;
    }


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public ConfigManager( final SecurityManager securityManager, final PeerManager peerManager,
                          final ConfigDataService configDataService, final IdentityManager identityManager )
            throws IOException, PGPException, KeyStoreException, CertificateException, NoSuchAlgorithmException
    {
        this.identityManager = identityManager;
        this.peerManager = peerManager;
        this.securityManager = securityManager;

        this.sender = securityManager.getKeyManager().getPrivateKey( null );

        this.peerId = peerManager.getLocalPeer().getId();

        this.hPublicKey = PGPKeyHelper.readPublicKey( H_PUB_KEY );
        LOG.debug( "Getting hPublicKey from keystores folder: " + hPublicKey.toString() );

        this.ownerPublicKey =
                securityManager.getKeyManager().getPublicKeyRing( securityManager.getKeyManager().getPeerOwnerId() )
                               .getPublicKey();

        this.peerPublicKey = securityManager.getKeyManager().getPublicKey( null );

        this.messenger = new PGPMessenger( sender, hPublicKey );

        this.keyStore = loadKeyStore();
    }


    private KeyStore loadKeyStore() throws KeyStoreException
    {
        KeyStoreData keyStoreData = new KeyStoreData();
        keyStoreData.setKeyStoreFile( PEER_KEYSTORE );
        keyStoreData.setAlias( PEER_CERT_ALIAS );
        keyStoreData.setPassword( SecuritySettings.KEYSTORE_PX1_PSW );
        keyStoreData.setKeyStoreType( KeyStoreType.JKS );

        KeyStoreTool keyStoreTool = new KeyStoreTool();
        KeyStore sslkeyStore = keyStoreTool.load( keyStoreData );

        //generate X509 cert for mutual SSL connection with Hub
        if ( sslkeyStore.size() == 0 )
        {
            String fingerprint = PGPKeyUtil.getFingerprint( peerPublicKey.getFingerprint() );

            io.subutai.common.security.crypto.key.KeyManager sslkeyMan =
                    new io.subutai.common.security.crypto.key.KeyManager();
            KeyPairGenerator keyPairGenerator = sslkeyMan.prepareKeyPairGeneration( KeyPairType.RSA, 1024 );
            java.security.KeyPair sslKeyPair = sslkeyMan.generateKeyPair( keyPairGenerator );

            CertificateData certificateData = new CertificateData();

            certificateData.setCommonName( fingerprint );

            CertificateTool certificateTool = new CertificateTool();

            X509Certificate x509cert = certificateTool.generateSelfSignedCertificate( sslKeyPair, certificateData );

            keyStoreTool.addNSaveX509Certificate( sslkeyStore, keyStoreData, x509cert, sslKeyPair );
        }

        return sslkeyStore;
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


    public WebClient getTrustedWebClientWithAuth( String path, final String hubIp )
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException
    {
        String baseUrl = String.format( "https://%s:" + HUB_PORT, hubIp );

        return HttpClient.createTrustedWebClientWithAuth( baseUrl + path, keyStore,
                SecuritySettings.KEYSTORE_PX1_PSW.toCharArray(), hPublicKey.getFingerprint() );
    }


    @Deprecated
    public void addHubConfig( final String hubIp )
    {
//        this.hubIp = hubIp;
    }


    public String getHubIp()
    {
//        return configDataService.getHubConfig( peerId ).getHubIp();
        return "hub.subut.ai";
    }

    public byte[] readContent( Response response ) throws IOException
    {
        if ( response.getEntity() == null )
        {
            return null;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        InputStream is = ( ( InputStream ) response.getEntity() );

        IOUtils.copy( is, bos );
        return bos.toByteArray();
    }

    public UserToken getPermanentToken()
    {
        Date newDate = new Date();
        java.util.Calendar cal = Calendar.getInstance();
        cal.setTime( newDate );
        cal.add( Calendar.YEAR, 2 );

        User user = identityManager.getActiveUser();

        return identityManager.createUserToken( user, null, null, null, 2, cal.getTime() );
    }


    public IdentityManager getIdentityManager()
    {
        return identityManager;
    }
}
