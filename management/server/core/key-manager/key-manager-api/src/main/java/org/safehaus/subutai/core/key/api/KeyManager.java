package org.safehaus.subutai.core.key.api;


import java.util.Set;


/**
 * Provides means to work with PGP keys
 */
public interface KeyManager
{
    /**
     * Generates a PGP key
     *
     * @param realName - user name
     * @param email - user email
     *
     * @return - {@code KeyInfo}
     */
    public KeyInfo generateKey( String realName, String email ) throws KeyManagerException;

    /**
     * Returns PGP public key
     *
     * @param keyId - id of pgp key
     */
    public String readKey( String keyId ) throws KeyManagerException;

    /**
     * Returns PGP public key as SSH key
     *
     * @param keyId - id of pgp key
     */
    public String readSshKey( String keyId ) throws KeyManagerException;


    /**
     * Sign file with specified key
     *
     * @param keyId - id of pgp key which is used to sign
     * @param filePath - full path to file to be signed
     */
    public void signFileWithKey( String keyId, String filePath ) throws KeyManagerException;

    /**
     * Sign key with specified key
     *
     * @param signerKeyId - id of pgp key which is used to sign
     * @param signedKeyId - id of pgp key which is to be signed
     */
    public void signKeyWithKey( String signerKeyId, String signedKeyId ) throws KeyManagerException;

    /**
     * Sends key to HUB
     *
     * @param keyId - id of pgp key to be sent
     */
    public void sendKeyToHub( String keyId ) throws KeyManagerException;

    /**
     * Return key info
     *
     * @param keyId - id of pgp key whose info to return
     *
     * @return - {@code KeyInfo}
     */
    public KeyInfo getKey( String keyId ) throws KeyManagerException;


    /**
     * Returns info of all existing keys
     *
     * @return - set of {@code KeyInfo}
     */
    public Set<KeyInfo> getKeys() throws KeyManagerException;


    /**
     * Deletes a key
     *
     * @param keyId - id of pgp key to delete
     */
    public void deleteKey( String keyId ) throws KeyManagerException;


    /**
     * Revokes a key
     *
     * @param keyId - id of pgp key to revoke
     */
    public void revokeKey( String keyId ) throws KeyManagerException;

    /**
     * Generates a subKey
     *
     * @param keyId - id of pgp key which sub key to generate
     *
     * @return - returns id of a newly created sub key
     */
    public String generateSubKey( String keyId ) throws KeyManagerException;

    /**
     * Deletes a subKey
     *
     * @param keyId - id of pgp sub key to delete
     */
    public void deleteSubKey( String keyId ) throws KeyManagerException;


    /**
     * Revokes a subKey
     *
     * @param keyId - id of pgp sub key to revoke
     */
    public void revokeSubKey( String keyId ) throws KeyManagerException;
}
