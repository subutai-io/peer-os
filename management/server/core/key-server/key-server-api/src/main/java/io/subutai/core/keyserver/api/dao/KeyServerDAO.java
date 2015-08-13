package io.subutai.core.keyserver.api.dao;


import java.util.List;

import io.subutai.core.keyserver.api.model.SecurityKey;


/**
 * Data access interface for KeyServer
 */

public interface KeyServerDAO
{
    /********************************
     * Finds public key with given fingerprint.
     *
     * @param fingerprint hex encoded fingerprint to search
     * @return public key if the key with given fingerprint exists; {@code null} otherwise
     */
    public SecurityKey findByFingerprint( String fingerprint );


    /********************************
     * Finds public key with given keyId.
     *
     * @param keyId hex encoded fingerprint to search
     * @return public key if the key with given keyId exists; {@code null} otherwise
     */
    public SecurityKey findByKeyId( String keyId );


    /********************************
     * Finds public key with given keyId.
     *
     * @param keyId hex encoded fingerprint to search
     * @return public key if the key with given keyId exists; {@code null} otherwise
     */
    public SecurityKey find( String keyId );


    /********************************
     * Get all public keys.
     *
     * @return all public keys
     */
    public List<SecurityKey> findAll();


    /********************************
     * Saves the given public key.
     *
     * @param securityKey to save
     */
    public void save( SecurityKey securityKey );


    /********************************
     * Deletes the given public key.
     *
     * @param securityKey key to delete
     */
    public void delete( SecurityKey securityKey );


    /********************************
     * Deletes public key with given key ID.
     *
     * @param keyId key ID of a public key to delete
     */
    public void deleteByKeyId( String keyId );

}
