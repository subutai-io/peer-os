package io.subutai.core.communication.impl;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.security.auth.callback.PasswordCallback;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.ssl.TrustStrategy;

import io.subutai.common.pgp.key.PGPKeyHelper;
import io.subutai.core.communication.api.PGPKeyNotFound;
import io.subutai.core.communication.api.SecurityMaterials;


/**
 * SecurityMaterials implementation for file system stored materials.
 */
public class BundleSecurityMaterials implements SecurityMaterials
{
    private static final Logger log = LoggerFactory.getLogger( BundleSecurityMaterials.class );
    private Bundle bundle;
    private PasswordCallback privateKeyPasswordCallback;
    private PasswordCallback keyStorePasswordCallback;
    private String keyStoreType;
    private String senderKeyId;
    private String recipientKeyId;
    private TrustStrategy trustStrategy;
    private boolean devMode = false;


    public BundleSecurityMaterials( Bundle bundle, String senderKeyId, String receiverKeyId, String keyStoreType,
                                    final PasswordCallback keyStorePasswordCallback,
                                    final PasswordCallback privateKeyPasswordCallback, boolean devMode )
    {
        this.bundle = bundle;
        this.devMode = devMode;
        this.senderKeyId = senderKeyId;
        this.recipientKeyId = receiverKeyId;
        this.keyStoreType = keyStoreType;
        this.privateKeyPasswordCallback = privateKeyPasswordCallback;
        this.keyStorePasswordCallback = keyStorePasswordCallback;
        this.trustStrategy = new SubutaiTrustStrategy( this );
    }


    public BundleSecurityMaterials( Bundle bundle, String senderKeyId, String receiverKeyId, String keyStoreType,
                                    final PasswordCallback keyStorePasswordCallback,
                                    final PasswordCallback privateKeyPasswordCallback, boolean devMode,
                                    TrustStrategy trustStrategy )
    {
        this.bundle = bundle;
        this.devMode = devMode;
        this.senderKeyId = senderKeyId;
        this.recipientKeyId = receiverKeyId;
        this.keyStoreType = keyStoreType;
        this.privateKeyPasswordCallback = privateKeyPasswordCallback;
        this.keyStorePasswordCallback = keyStorePasswordCallback;
        this.trustStrategy = trustStrategy;
    }


    @Override
    public PGPPublicKey getRecipientGPGPublicKey() throws PGPKeyNotFound, PGPException
    {
        String filePath = String.format( "%skeys%s%s.public.gpg", File.separator, File.separator, recipientKeyId );
        PGPPublicKey result;
        try
        {
            log.debug( "Reading " + filePath );
            InputStream is = bundle.getEntry( filePath ).openStream();
            result = PGPKeyHelper.readPublicKey( is );
            is.close();
        }
        catch ( IOException e )
        {
            throw new PGPKeyNotFound( filePath );
        }
        return result;
    }


    @Override
    public PGPPrivateKey getSenderGPGPrivateKey() throws PGPKeyNotFound, PGPException
    {
        String filePath = String.format( "%skeys%s%s.secret.gpg", File.separator, File.separator, senderKeyId );

        PGPPrivateKey result;
        try
        {
            log.debug( "Reading " + filePath );
            InputStream is = bundle.getEntry( filePath ).openStream();
            result = PGPKeyHelper.readPrivateKey( is, getPrivateKeyPassword() );
            is.close();
        }
        catch ( IOException e )
        {
            throw new PGPKeyNotFound( filePath );
        }
        return result;
    }


    @Override
    public PGPPublicKey getSenderGPGPublicKey() throws PGPKeyNotFound, PGPException
    {
        String filePath = String.format( "%skeys%s%s.public.gpg", File.separator, File.separator, senderKeyId );

        PGPPublicKey result;
        try
        {
            log.debug( "Reading " + filePath );
            InputStream is = bundle.getEntry( filePath ).openStream();
            result = PGPKeyHelper.readPublicKey( is );
            is.close();
        }
        catch ( IOException e )
        {
            throw new PGPKeyNotFound( filePath );
        }
        return result;
    }


    @Override
    public boolean isDevMode()
    {
        return devMode;
    }


    @Override
    public KeyStore getKeyStore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException
    {
        String filePath =
                String.format( "%skeys%s%s.%s", File.separator, File.separator, senderKeyId, getStoreFileExtention() );
        log.debug( "Reading " + filePath );

        KeyStore keyStore = KeyStore.getInstance( keyStoreType );
        InputStream is = bundle.getEntry( filePath ).openStream();
        keyStore.load( is, getKeyStorePassword() );
        is.close();
        return keyStore;
    }


    @Override
    public TrustStrategy getTrustStrategy()
    {
        return trustStrategy;
    }


    @Override
    public void setTrustStrategy( final TrustStrategy trustStrategy )
    {
        this.trustStrategy = trustStrategy;
    }


    @Override
    public char[] getKeyStorePassword()
    {
        return keyStorePasswordCallback.getPassword();
    }


    @Override
    public char[] getPrivateKeyPassword()
    {
        return privateKeyPasswordCallback.getPassword();
    }


    public String getStoreFileExtention()
    {
        return "JKS".equals( keyStoreType.toUpperCase() ) ? "jks" : "p12";
    }
}
