package io.subutai.core.security.api;


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
}
