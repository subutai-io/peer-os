package org.safehaus.subutai.core.key.api;


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
     * Exports PGP key as SSH key
     *
     * @param keyId - id of pgp key
     * @param pathToExportedSshKey - full path to file which will contain exported SSH key
     */
    public void exportSshKey( String keyId, String pathToExportedSshKey ) throws KeyManagerException;


    /**
     * Sign file with specified key
     *
     * @param keyId - id of pgp key which is used to sign
     * @param pathToFileToBeSigned - full path to file to be signed
     */
    public void signFileWithKey( String keyId, String pathToFileToBeSigned ) throws KeyManagerException;

    /**
     * Sends key to specified url
     *
     * @param keyId - id of pgp key to be sent
     * @param url - target recipient url
     */
    public void sendKeyToUrl( String keyId, String url ) throws KeyManagerException;
}
