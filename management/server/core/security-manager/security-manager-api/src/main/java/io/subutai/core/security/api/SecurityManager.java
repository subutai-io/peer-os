package io.subutai.core.security.api;


import org.bouncycastle.openpgp.PGPException;

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
    KeyManager getKeyManager();


    /* **********************************
     *
     */
    EncryptionTool getEncryptionTool();


    /* *****************************
     *
     */
    KeyStoreManager getKeyStoreManager();


    /* *****************************
     *
     */

    HttpContextManager getHttpContextManager();

    String signNEncryptRequestToHost( String message, String hostId ) throws PGPException;

    String decryptNVerifyResponseFromHost( String message ) throws PGPException;
}
