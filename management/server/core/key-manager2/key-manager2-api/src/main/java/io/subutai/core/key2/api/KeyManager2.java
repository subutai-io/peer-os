package io.subutai.core.key2.api;

import io.subutai.common.peer.Host;

import java.util.Set;

/**
 * Basically all methods are taken from key manager api.
 * since we will need them all with this version as well.
 */
public interface KeyManager2 {

    /**
     * Generates a PGP key
     *
     * @param host to execute command at
     * @param realName - user name
     * @param email - user email
     *
     * @return - {@code KeyInfo2}
     */
    public KeyInfo2 generateKey (String realName, String email) throws KeyManagerException;

    /**
     * Returns PGP public key
     *
     * @param host to execute command at
     * @param keyId - id of pgp key
     */
    public String readKey( String keyId ) throws KeyManagerException;


    public String generateCertificate ( String keyId ) throws KeyManagerException;

    /**
     * Returns a X.509 self-signed certificate content of a key ( generates it if there is no certificate created with
     * that given key id in the keyring)
     *
     * @param host to execute command at
     * @param keyId - id of pgp key
     *
     * @return - content of certificate
     */
    public String getCertificate( String keyId ) throws KeyManagerException;


    /**
     * Returns PGP public key as SSH key
     *
     * @param host to execute command at
     * @param keyId - id of pgp key
     */
    public String readSshKey( String keyId ) throws KeyManagerException;


    /**
     * Sign file with specified key
     *
     * @param host to execute command at
     * @param keyId - id of pgp key which is used to sign
     * @param filePath - full path to file to be signed
     */
    public void signFileWithKey( String keyId, String filePath ) throws KeyManagerException;

    /**
     * Sign key with specified key
     *
     * @param host to execute command at
     * @param signerKeyId - id of pgp key which is used to sign
     * @param signedKeyId - id of pgp key which is to be signed
     */
    public void signKeyWithKey(  String signerKeyId, String signedKeyId ) throws KeyManagerException;

<<<<<<< HEAD

    /**
     * experimental...
     */
    public void signKeyWithKey2 ( String signer, String singee ) throws KeyManagerException;

=======
>>>>>>> 1fe8dc4b77e11a89bfdf766fedca412a4fedef6f
    /**
     * Sends key to public revocation server
     *
     * @param host to execute command at
     * @param keyId - id of pgp key to be sent
     */
    public void sendRevocationKeyToPublicKeyServer( String keyId ) throws KeyManagerException;

    /**
     * Generates revocation key for pgg key
     *
     * @param host to execute command at
     * @param keyId - id of pgp key
     */
    public void generateRevocationKey( String keyId ) throws KeyManagerException;

    /**
     * Return key info
     *
     * @param host to execute command at
     * @param keyId - id of pgp key whose info to return
     *
     * @return - {@code KeyInfo2}
     */
    public KeyInfo2 getKey(  String keyId ) throws KeyManagerException;


    /**
     * Returns info of all existing keys
     *
     * @param host to execute command at
     *
     * @return - set of {@code KeyInfo2}
     */
    public Set<KeyInfo2> getKeys(  ) throws KeyManagerException;


    /**
     * Deletes a key
     *
     * @param host to execute command at
     * @param keyId - id of pgp key to delete
     */
    public void deleteKey(  String keyId ) throws KeyManagerException;


    /**
     * Revokes a key
     *
     * @param host to execute command at
     * @param keyId - id of pgp key to revoke
     */
    public void revokeKey( String keyId ) throws KeyManagerException;

    /**
     * Generates a subKey
     *
     * @param host to execute command at
     * @param keyId - id of pgp key which sub key to generate
     *
     * @return - returns id of a newly created sub key
     */
    public String generateSubKey( String keyId ) throws KeyManagerException;

    /**
     * Deletes a subKey
     *
     * @param host to execute command at
     * @param keyId - id of pgp sub key to delete
     */
    public void deleteSubKey(  String keyId ) throws KeyManagerException;


    /**
     * Revokes a subKey
     *
     * @param host to execute command at
     * @param keyId - id of pgp sub key to revoke
     */
    public void revokeSubKey( String keyId ) throws KeyManagerException;

}
