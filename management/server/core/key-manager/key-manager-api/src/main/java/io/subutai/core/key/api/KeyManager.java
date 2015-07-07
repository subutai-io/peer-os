package io.subutai.core.key.api;


import java.util.Set;

import org.safehaus.subutai.common.peer.Host;


/**
 * Provides means to work with PGP keys
 */
public interface KeyManager
{
    /**
     * Generates a PGP key
     *
     * @param host to execute command at
     * @param realName - user name
     * @param email - user email
     *
     * @return - {@code KeyInfo}
     */
    public KeyInfo generateKey( Host host, String realName, String email ) throws KeyManagerException;

    /**
     * Returns PGP public key
     *
     * @param host to execute command at
     * @param keyId - id of pgp key
     */
    public String readKey( Host host, String keyId ) throws KeyManagerException;

    /**
     * Returns a X.509 self-signed certificate content of a key ( generates it if there is no certificate created with
     * that given key id in the keyring)
     *
     * @param host to execute command at
     * @param keyId - id of pgp key
     *
     * @return - content of certificate
     */
    public String getCertificate( Host host, String keyId ) throws KeyManagerException;


    /**
     * Returns PGP public key as SSH key
     *
     * @param host to execute command at
     * @param keyId - id of pgp key
     */
    public String readSshKey( Host host, String keyId ) throws KeyManagerException;


    /**
     * Sign file with specified key
     *
     * @param host to execute command at
     * @param keyId - id of pgp key which is used to sign
     * @param filePath - full path to file to be signed
     */
    public void signFileWithKey( Host host, String keyId, String filePath ) throws KeyManagerException;

    /**
     * Sign key with specified key
     *
     * @param host to execute command at
     * @param signerKeyId - id of pgp key which is used to sign
     * @param signedKeyId - id of pgp key which is to be signed
     */
    public void signKeyWithKey( Host host, String signerKeyId, String signedKeyId ) throws KeyManagerException;

    /**
     * Sends key to public revocation server
     *
     * @param host to execute command at
     * @param keyId - id of pgp key to be sent
     */
    public void sendRevocationKeyToPublicKeyServer( Host host, String keyId ) throws KeyManagerException;

    /**
     * Generates revocation key for pgg key
     *
     * @param host to execute command at
     * @param keyId - id of pgp key
     */
    public void generateRevocationKey( Host host, String keyId ) throws KeyManagerException;

    /**
     * Return key info
     *
     * @param host to execute command at
     * @param keyId - id of pgp key whose info to return
     *
     * @return - {@code KeyInfo}
     */
    public KeyInfo getKey( Host host, String keyId ) throws KeyManagerException;


    /**
     * Returns info of all existing keys
     *
     * @param host to execute command at
     *
     * @return - set of {@code KeyInfo}
     */
    public Set<KeyInfo> getKeys( Host host ) throws KeyManagerException;


    /**
     * Deletes a key
     *
     * @param host to execute command at
     * @param keyId - id of pgp key to delete
     */
    public void deleteKey( Host host, String keyId ) throws KeyManagerException;


    /**
     * Revokes a key
     *
     * @param host to execute command at
     * @param keyId - id of pgp key to revoke
     */
    public void revokeKey( Host host, String keyId ) throws KeyManagerException;

    /**
     * Generates a subKey
     *
     * @param host to execute command at
     * @param keyId - id of pgp key which sub key to generate
     *
     * @return - returns id of a newly created sub key
     */
    public String generateSubKey( Host host, String keyId ) throws KeyManagerException;

    /**
     * Deletes a subKey
     *
     * @param host to execute command at
     * @param keyId - id of pgp sub key to delete
     */
    public void deleteSubKey( Host host, String keyId ) throws KeyManagerException;


    /**
     * Revokes a subKey
     *
     * @param host to execute command at
     * @param keyId - id of pgp sub key to revoke
     */
    public void revokeSubKey( Host host, String keyId ) throws KeyManagerException;
}
