package io.subutai.core.security.api.dao;


import javax.persistence.EntityManager;

import io.subutai.core.security.api.model.SecretKeyStore;


/**
 *
 */
public interface SecretKeyStoreDAO
{
    /******************************************
     *
     */
    public void saveSecretKeyRing(SecretKeyStore secretKeyStore);



    /******************************************
     *
     */
    public void saveSecretKeyRing(String fingerprint,byte[] data, String pwd,short type);


    /******************************************
     *
     */
    public void removeSecretKeyRing( String fingerprint );


    /******************************************
     * Get Secret KeyId from DB
     */
    public SecretKeyStore getSecretKeyData( String fingerprint );
}
