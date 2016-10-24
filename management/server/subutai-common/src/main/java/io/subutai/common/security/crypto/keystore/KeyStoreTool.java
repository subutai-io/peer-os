package io.subutai.common.security.crypto.keystore;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.exception.ActionFailedException;
import io.subutai.common.security.crypto.certificate.CertificateData;
import io.subutai.common.security.crypto.certificate.CertificateTool;
import io.subutai.common.security.crypto.key.KeyManager;
import io.subutai.common.security.crypto.key.KeyPairType;
import io.subutai.common.security.utils.SafeCloseUtil;
import io.subutai.common.settings.SecuritySettings;


/**
 * Main Class for keystore management. Manages Keystores and Truststores
 */
public class KeyStoreTool
{

    private static final Logger LOGGER = LoggerFactory.getLogger( KeyStoreTool.class );
    private FileInputStream finStream = null;
    private FileOutputStream foutStream = null;
    private CertificateTool certificateTool = new CertificateTool();


    /**
     * KeyStoreManager constructor
     */
    public KeyStoreTool()
    {

    }


    /**
     * *********************************************************************************** Load keystore and create
     * KeyStore object
     *
     * @param keyStoreData KeyStoreData
     *
     * @return KeyStore
     */
    public KeyStore load( KeyStoreData keyStoreData )
    {
        KeyStore keyStore = null;

        try
        {
            if ( !keyStoreData.getKeyStoreType().isFileBased() )
            {
                LOGGER.error( "NoCreateKeyStoreNotFile.exception.message" );
            }
            else
            {
                File file = new File( keyStoreData.getKeyStoreFile() );

                if ( file.exists() )
                {
                    finStream = new FileInputStream( file );
                    keyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
                    keyStore.load( finStream, keyStoreData.getPassword().toCharArray() );
                }
                else
                {
                    File keyStoresFolder = new File( file.getParent() );
                    if ( keyStoresFolder.mkdirs() )
                    {
                        if ( file.createNewFile() )
                        {
                            LOGGER.info( "Created keystore file" );
                        }
                    }

                    keyStore = KeyStore.getInstance( keyStoreData.getKeyStoreType().jce() );
                    keyStore.load( null, null );
                    foutStream = new FileOutputStream( file );
                    keyStore.store( foutStream, keyStoreData.getPassword().toCharArray() );
                }
            }
        }
        catch ( java.security.cert.CertificateException e )
        {
            LOGGER.error( "Problem with certificate at keystore.load/store method", e );
        }
        catch ( NoSuchAlgorithmException e )
        {
            LOGGER.error( "Error at keystore.load/store method no such algorithm", e );
        }
        catch ( FileNotFoundException e )
        {
            LOGGER.error( "KeyStore file not found. Please check if it exists at filesystem.", e );
        }
        catch ( KeyStoreException e )
        {
            LOGGER.error( "KeyStore exception while saving/getting keyStore instance", e );
        }
        catch ( IOException e )
        {
            LOGGER.error( "Error accessing keyStore file", e );
        }
        finally
        {
            SafeCloseUtil.close( finStream );
            SafeCloseUtil.close( foutStream );
        }

        return keyStore;
    }


    /**
     * *********************************************************************************** Save changes in the keystore
     *
     * @param keyStore KeyStore
     * @param keyStoreData, KeyStoreData
     */
    public void save( KeyStore keyStore, KeyStoreData keyStoreData )
    {
        try
        {
            if ( !keyStoreData.getKeyStoreType().isFileBased() )
            {
                LOGGER.error( "Keystore is not file-based" );
            }
            else
            {
                File file = new File( keyStoreData.getKeyStoreFile() );
                foutStream = new FileOutputStream( file );
                keyStore.store( foutStream, keyStoreData.getPassword().toCharArray() );
            }
        }
        catch ( IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException ex )
        {
            LOGGER.error( "Error saving keystore", ex );
        }
        finally
        {
            SafeCloseUtil.close( foutStream );
        }
    }


    /**
     * *********************************************************************************** Get Keypair object
     *
     * @param keyStore KeyStore
     * @param keyStoreData KeyStoreData
     *
     * @return KeyPair
     */
    public KeyPair getKeyPair( KeyStore keyStore, KeyStoreData keyStoreData )
    {
        KeyPair keyPair = null;

        try
        {
            Key key = keyStore.getKey( keyStoreData.getAlias(), keyStoreData.getPassword().toCharArray() );

            if ( key instanceof PrivateKey )
            {
                Certificate cert = keyStore.getCertificate( keyStoreData.getAlias() );
                PublicKey publicKey = cert.getPublicKey();
                keyPair = new KeyPair( publicKey, ( PrivateKey ) key );
            }
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e )
        {
            LOGGER.error( "Error getting keyStore key or while getting certificate", e );
        }

        return keyPair;
    }


    /**
     * *********************************************************************************** Delete entry in the Keystore
     *
     * @param keyStore KeyStore
     * @param keyStoreData KeyStoreData
     *
     * @return boolean
     */
    public boolean deleteEntry( KeyStore keyStore, KeyStoreData keyStoreData )
    {
        try
        {
            keyStore.deleteEntry( keyStoreData.getAlias() );
            //save Keystore file
            this.save( keyStore, keyStoreData );
        }
        catch ( KeyStoreException e )
        {
            LOGGER.error( "Error deleting keyStore entry", e );
        }

        return true;
    }


    /**
     * *********************************************************************************** Export Certificate
     *
     * @param keyStore KeyStore ,
     * @param keyStoreData KeyStoreData
     *
     * @return String
     */
    public String exportCertificateInPem( KeyStore keyStore, KeyStoreData keyStoreData )
    {
        try
        {
            X509Certificate cert = ( X509Certificate ) keyStore.getCertificate( keyStoreData.getAlias() );
            return certificateTool.convertX509CertToPem( cert );
        }
        catch ( KeyStoreException e )
        {
            throw new ActionFailedException( "Error getting certificate", e );
        }
    }


    /**
     * *********************************************************************************** Import Certificate(HEX) into
     * Keystore
     *
     * @param keyStore KeyStore
     * @param keyStoreData KeyStoreData
     */
    public void importCertificateInPem( KeyStore keyStore, KeyStoreData keyStoreData )
    {

        try
        {
            X509Certificate cert = certificateTool.convertX509PemToCert( keyStoreData.getHEXCert() );

            keyStore.setCertificateEntry( keyStoreData.getAlias(), cert );

            //save Keystore file
            this.save( keyStore, keyStoreData );
        }
        catch ( Exception e )
        {
            throw new ActionFailedException( "Error importing certificate", e );
        }
    }


    public KeyStore createPeerCertKeystore( String alias, String cn )
            throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
    {

        KeyManager sslkeyMan = new KeyManager();

        KeyPairGenerator keyPairGenerator = sslkeyMan.prepareKeyPairGeneration( KeyPairType.RSA, 1024 );

        KeyPair sslKeyPair = sslkeyMan.generateKeyPair( keyPairGenerator );

        CertificateData certificateData = new CertificateData();

        certificateData.setCommonName( cn );

        CertificateTool certificateTool = new CertificateTool();

        X509Certificate x509cert = certificateTool.generateSelfSignedCertificate( sslKeyPair, certificateData );

        KeyStore keyStore = KeyStore.getInstance( KeyStoreType.JKS.jce() );

        keyStore.load( null, null );

        keyStore.setKeyEntry( alias, sslKeyPair.getPrivate(), SecuritySettings.KEYSTORE_PX1_PSW.toCharArray(),
                new java.security.cert.Certificate[] { x509cert } );

        return keyStore;
    }
}
