package io.subutai.core.hintegration.impl;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import io.subutai.common.security.SecurityProvider;
import io.subutai.common.security.crypto.certificate.CertificateData;
import io.subutai.common.security.crypto.certificate.CertificateTool;
import io.subutai.common.security.crypto.key.KeyPairType;
import io.subutai.common.security.crypto.keystore.KeyStoreData;
import io.subutai.common.security.crypto.keystore.KeyStoreTool;
import io.subutai.common.security.crypto.keystore.KeyStoreType;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.core.hintegration.impl.settings.HSettings;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.hub.common.dto.EnvironmentDTO;


public class HIntegrationImpl
{
    private SecurityManager securityManager;
    private static final Logger LOG = LoggerFactory.getLogger( HIntegrationImpl.class.getName() );
    public static final String OWNER_USER_ID = "owner@subutai.io";
    private BundleContext bundleContext;


    public void setBundleContext( final BundleContext bundleContext )
    {
        this.bundleContext = bundleContext;
    }


    /**
     * @param keyStore - keystore with peer X509 certificate with CN=fingerprint of peer pub key
     * @param keyStorePassword - keystore password
     */
    private static HttpClient initHttpClient( KeyStore keyStore, String keyStorePassword ) throws Exception
    {

        SSLContext sslContext = SSLContexts.custom().loadKeyMaterial( keyStore, keyStorePassword.toCharArray() )
                                           .loadTrustMaterial( new TrustSelfSignedStrategy() ).build();

        SSLConnectionSocketFactory sslSocketFactory =
                new SSLConnectionSocketFactory( sslContext, NoopHostnameVerifier.INSTANCE );

        return HttpClients.custom().setSSLSocketFactory( sslSocketFactory )
                          .setRetryHandler( new DefaultHttpRequestRetryHandler( 0, false ) ).build();
    }


    public void init()
    {
        LOG.debug( "H-INTEGRATION" );

        new TrustSelfSignedStrategy();

        EnvironmentDTO environmentDTO;

        LOG.debug( "DTO" + EnvironmentDTO.class.toString() );

        generateKeys();
    }


    public SecurityManager getSecurityManager()
    {
        return securityManager;
    }


    public void setSecurityManager( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    private void generateKeys()
    {
        try
        {
            final KeyManager keyManager = securityManager.getKeyManager();


            //**************Get Peer Public keyring ******************************************
            PGPPublicKeyRing peerPublicKeyRing = keyManager.getPublicKeyRing( null );
            String fingerprint = PGPKeyUtil.getFingerprint( peerPublicKeyRing.getPublicKey().getFingerprint() );


            //**************Saving certificate******************************************
            io.subutai.common.security.crypto.key.KeyManager sslkeyMan =
                    new io.subutai.common.security.crypto.key.KeyManager();
            KeyPairGenerator keyPairGenerator = sslkeyMan.prepareKeyPairGeneration( KeyPairType.RSA, 1024 );
            java.security.KeyPair sslKeyPair = sslkeyMan.generateKeyPair( keyPairGenerator );

            KeyStoreData keyStoreData = new KeyStoreData();
            keyStoreData.setKeyStoreFile( HSettings.PEER_KEYSTORE );
            keyStoreData.setAlias( "root_server_px1" );
            keyStoreData.setPassword( "subutai" );
            keyStoreData.setKeyStoreType( KeyStoreType.JKS );

            KeyStoreTool keyStoreTool = new KeyStoreTool();
            KeyStore sslkeyStore = keyStoreTool.load( keyStoreData );


            CertificateData certificateData = new CertificateData();
            certificateData.setCommonName( fingerprint );

            CertificateTool certificateTool = new CertificateTool();
            X509Certificate x509cert = certificateTool
                    .generateSelfSignedCertificate( sslkeyStore, sslKeyPair, SecurityProvider.BOUNCY_CASTLE,
                            certificateData );

            keyStoreTool.saveX509Certificate( sslkeyStore, keyStoreData, x509cert, sslKeyPair );


            //****************Save Owner secret key ****************************************
            KeyPair ownerKeyPair = keyManager.generateKeyPair( OWNER_USER_ID, false );

            File ownerSecretKeyFile = new File( HSettings.PEER_OWNER_SECRET_KEY );

            try ( FileOutputStream fop = new FileOutputStream( ownerSecretKeyFile ) )
            {
                if ( !ownerSecretKeyFile.exists() )
                {
                    ownerSecretKeyFile.createNewFile();
                }

                fop.write( ownerKeyPair.getSecKeyring() );
                fop.flush();
                fop.close();

                LOG.debug( "owner secret key genereated" );
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
            }


            //****************Save Owner Public key ****************************************
            File ownerPubKeyFile = new File( HSettings.PEER_OWNER_PUB_KEY );

            try ( FileOutputStream fop = new FileOutputStream( ownerPubKeyFile ) )
            {
                if ( !ownerPubKeyFile.exists() )
                {
                    ownerPubKeyFile.createNewFile();
                }

                fop.write( ownerKeyPair.getPubKeyring() );
                fop.flush();
                fop.close();

                LOG.debug( "owner public key genereated" );
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
            }


            //****************Sign pub of peer with owner ****************************************
            PGPSecretKeyRing ownerSecretKeyRing = PGPKeyUtil.readSecretKeyRing( ownerKeyPair.getSecKeyring() );
            PGPPublicKeyRing publicKeyRing = PGPEncryptionUtil
                    .signPublicKey( peerPublicKeyRing, OWNER_USER_ID, ownerSecretKeyRing.getSecretKey(), "12345678" );


            //****************Save Peer Public key ****************************************
            File peerPubKeyFile = new File( HSettings.PEER_PUB_KEY );

            try ( FileOutputStream fop = new FileOutputStream( peerPubKeyFile ) )
            {
                if ( !peerPubKeyFile.exists() )
                {
                    peerPubKeyFile.createNewFile();
                }

                fop.write( publicKeyRing.getEncoded() );
                fop.flush();
                fop.close();

                LOG.debug( "peer public key genereated" );
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
            }


            //****************Loading hub key from resources ****************************************
            InputStream is = bundleContext.getBundle().getEntry( "keys/hub.public.gpg" ).openStream();
            PGPPublicKey hPubKey = PGPKeyUtil.readPublicKey( is );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }


    public void httpRequestOwnerPubKey() throws IOException, PGPException
    {
        String baseUrl = String.format( "https://%s:%s/", HSettings.IP, HSettings.SECURE_PORT_X1 );
        WebClient client = io.subutai.core.hintegration.impl.HttpClient.createTrustedWebClient( baseUrl );

        client.type( MediaType.APPLICATION_FORM_URLENCODED ).accept( MediaType.APPLICATION_JSON );

        Form form = new Form();
        form.set( "keytext", PGPEncryptionUtil.armorByteArrayToString( getOwnerPubKeyRing().getEncoded()) );

        Response response = client.path( "pks/add" ).put( form );
    }


    public PGPPublicKeyRing getOwnerPubKeyRing() throws IOException
    {
        InputStream in = PGPEncryptionUtil.getFileInputStream( HSettings.PEER_OWNER_PUB_KEY );
        PGPPublicKeyRing ownerPubKeyRing =
                new PGPPublicKeyRing( PGPUtil.getDecoderStream( in ), new JcaKeyFingerprintCalculator() );
        return ownerPubKeyRing;
    }


    public PGPPublicKeyRing getPeerPubKeyRing() throws IOException
    {
        InputStream in = PGPEncryptionUtil.getFileInputStream( HSettings.PEER_PUB_KEY );
        PGPPublicKeyRing peerPubKeyRing =
                new PGPPublicKeyRing( PGPUtil.getDecoderStream( in ), new JcaKeyFingerprintCalculator() );
        return peerPubKeyRing;
    }
}
