package io.subutai.core.communication.api;


import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;

import org.apache.http.ssl.TrustStrategy;


/**
 * Interface for store security materials
 */
public interface SecurityMaterials
{
    PGPPublicKey getRecipientGPGPublicKey() throws PGPKeyNotFound, PGPException;

    PGPPrivateKey getSenderGPGPrivateKey() throws PGPKeyNotFound, PGPException;

    KeyStore getKeyStore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException;

    char[] getKeyStorePassword();

    char[] getPrivateKeyPassword();


    TrustStrategy getTrustStrategy();

    void setTrustStrategy( TrustStrategy trustStrategy );
}
