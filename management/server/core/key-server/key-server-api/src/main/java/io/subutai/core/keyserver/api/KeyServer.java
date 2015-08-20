package io.subutai.core.keyserver.api;


import java.io.IOException;
import java.util.List;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;

import io.subutai.core.keyserver.api.dao.KeyServerDAO;
import io.subutai.core.keyserver.api.model.SecurityKey;


/**
 * Interface Manages Security key (extends KeyServerDAO).
 */

public interface KeyServer
{
    /********************************
     * Get DAO object
     */
    public KeyServerDAO getKeyServerDAO();


    /********************************
     * Set DAO object
     */
    public void setKeyServerDAO( KeyServerDAO keyServerDAO );


    /********************************
     * Finds public key with given fingerprint.
     *
     * @param fingerprint hex encoded fingerprint to search
     * @return public key if the key with given fingerprint exists; {@code null} otherwise
     */
    public SecurityKey getSecurityKeyByFingerprint( String fingerprint );


    /********************************
     * Finds public key with given shortKeyId.
     *
     * @param shortKeyId hex encoded shortKeyId to search
     * @return public key if the key with given fingerprint exists; {@code null} otherwise
     */
    public SecurityKey getSecurityKeyByShortKeyId( String shortKeyId );


    /********************************
     * Finds public key with given keyId.
     *
     * @param keyId hex encoded fingerprint to search
     * @return public key if the key with given keyId exists; {@code null} otherwise
     */
    public SecurityKey getSecurityKeyByKeyId( String keyId );


    /********************************
     * Finds public key with given keyId.
     *
     * @param keyId hex encoded fingerprint to search
     * @return public key if the key with given keyId exists; {@code null} otherwise
     */
    public SecurityKey getSecurityKey( String keyId );


    /********************************
     * Get all public keys.
     *
     * @return all public keys
     */
    public List<SecurityKey> getSecurityKeyList();


    /********************************
     * Saves the given public key.
     *
     * @param key to save
     */
    public void addSecurityKey( String key ) throws PGPException, IOException;


    /********************************
     * Saves the given public key.
     *
     * @param publicKey to save
     */
    public void addSecurityKey( PGPPublicKey publicKey ) throws PGPException, IOException;


    /********************************
     * Saves the given public key.
     *
     * @param key to save
     */
    public PGPPublicKey addPublicKey( String key ) throws PGPException, IOException;


    /********************************
     * Saves the given public key.
     *
     * @param keyId to save
     * @param fingerprint to save
     * @param keyData to save
     */
    public void saveSecurityKey( String keyId,String fingerprint,short keyType,byte[] keyData);

    /********************************
     * Saves the given public key.
     *
     * @param securityKey to save
     */
    public void saveSecurityKey( SecurityKey securityKey );

    /********************************
     * Deletes the given public key.
     *
     * @param securityKey key to delete
     */
    public void removeSecurityKey( SecurityKey securityKey );


    /********************************
     * Deletes public key with given key ID.
     *
     * @param keyId key ID of a public key to delete
     */
    public void removeSecurityKeyByKeyId( String keyId );


    /********************************
     * converts SecurityKey entity to the PGPPublicKey
     *
     * @param securityKey
     */
    public PGPPublicKey convertKey( SecurityKey securityKey ) throws PGPException;


    /********************************
     * converts SecurityKey entity to ASCII Armored
     *
     * @param keyId
     */
    public String getSecurityKeyAsASCII( String keyId ) throws PGPException;

}
