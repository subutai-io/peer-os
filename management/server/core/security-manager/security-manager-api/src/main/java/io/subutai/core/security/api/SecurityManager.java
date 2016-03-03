package io.subutai.core.security.api;


import org.bouncycastle.openpgp.PGPException;

import io.subutai.core.security.api.crypto.CertificateManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.security.api.crypto.KeyStoreManager;
import io.subutai.core.security.api.jetty.HttpContextManager;


/**
 * Interface for Security Manager
 */
public interface SecurityManager
{

    /* **********************************
     *
     */
    public KeyManager getKeyManager();


    /* **********************************
     *
     */
    public EncryptionTool getEncryptionTool();


    /* *****************************
     *
     */
    public KeyStoreManager getKeyStoreManager();


    /* *****************************
     *
     */
    public CertificateManager getCertificateManager();

    public HttpContextManager getHttpContextManager();

    public String signNEncryptRequestToHost(String message, String hostId) throws PGPException;

    public String decryptNVerifyResponseFromHost(String message) throws PGPException;
}
