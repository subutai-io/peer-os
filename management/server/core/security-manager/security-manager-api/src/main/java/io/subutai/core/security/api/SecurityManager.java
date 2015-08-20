package io.subutai.core.security.api;


import io.subutai.core.security.api.crypto.KeyManager;


/**
 * Interface for Security Manager
 */
public interface SecurityManager
{

    /********************************
     *
     */
    public KeyManager getKeyManager();



    /********************************
     *
     */
    public void setKeyManager( KeyManager keyManager );
}
