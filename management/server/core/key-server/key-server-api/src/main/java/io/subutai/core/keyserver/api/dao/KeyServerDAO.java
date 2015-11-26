package io.subutai.core.keyserver.api.dao;


import java.util.List;

import io.subutai.core.keyserver.api.model.PublicKeyStore;


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
    public PublicKeyStore findByFingerprint( String fingerprint );


    /********************************
     * Finds public key with given shortKeyId.
     *
     * @param shortKeyId hex encoded shortKeyId to search
     * @return public key if the key with given fingerprint exists; {@code null} otherwise
     */
    PublicKeyStore findByShortKeyId( String shortKeyId );


    /********************************
     * Finds public key with given keyId.
     *
     * @param keyId hex encoded fingerprint to search
     * @return public key if the key with given keyId exists; {@code null} otherwise
     */
    public PublicKeyStore findByKeyId( String keyId );


    /********************************
     * Finds public key with given keyId.
     *
     * @param keyId hex encoded fingerprint to search
     * @return public key if the key with given keyId exists; {@code null} otherwise
     */
    public PublicKeyStore find( String keyId );


    /********************************
     * Get all public keys.
     *
     * @return all public keys
     */
    public List<PublicKeyStore> findAll();


    /********************************
     * Saves the given public key.
     *
     * @param securityKey to save
     */
    public void save( PublicKeyStore securityKey );


    void update( PublicKeyStore keyStore );

    /********************************
     * Deletes the given public key.
     *
     * @param securityKey key to delete
     */
    public void delete( PublicKeyStore securityKey );


    /********************************
     * Deletes public key with given key ID.
     *
     * @param keyId key ID of a public key to delete
     */
    public void deleteByKeyId( String keyId );


}
