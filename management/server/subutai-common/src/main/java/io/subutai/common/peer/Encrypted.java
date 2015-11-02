package io.subutai.common.peer;


import javax.crypto.Cipher;

import io.subutai.common.serialize.Serializer;
import io.subutai.common.util.SecurityUtilities;


/**
 * Encrypted
 */
public class Encrypted
{
    private byte[] encrypted;


    public Encrypted() {}


    /**
     * Stores encrypted binary data for later decryption (see decrypt).
     *
     * @param data encrypted binary data
     */
    public Encrypted( byte[] data )
    {
        this.encrypted = data;
    }


    /**
     * Serializes the object and encrypts the resulting binary data using the given key.
     *
     * @param object the object to encrypt
     * @param key the secret key
     *
     * @throws Exception if serialization or encryption failed
     */
    public Encrypted( Object object, byte[] key ) throws Exception
    {
        this.encrypted = encrypt( Serializer.getInstance().serialize( object ), key );
    }


    /**
     * Decrypts the stored encrypted data using the given secret key and deserializes the resulting binary data to an
     * object of given class.
     *
     * @param key the secret key
     * @param clazz the type of the resulting object
     *
     * @return an object of given type
     *
     * @throws Exception if decryption or deserialization failed
     */
    public <T> T decrypt( byte[] key, Class<T> clazz ) throws Exception
    {
        byte[] decrypted = this.decrypt( key );
        return Serializer.getInstance().deserialize( decrypted, clazz );
    }


    /**
     * Decrypts the stored encrypted data using the given secret key.
     *
     * @param key the secret key
     *
     * @return the encrypted binary data
     *
     * @throws Exception if an exception occured
     */
    public byte[] decrypt( byte[] key ) throws Exception
    {
        return decrypt( this.encrypted, key );
    }


    /**
     * Decrypts the given data using the given secret key.
     *
     * @param data the encrypted data
     * @param key the secret key
     *
     * @return the decrypted data
     *
     * @throws Exception if an exception occurred
     */
    private byte[] decrypt( byte[] data, byte[] key ) throws Exception
    {
        return SecurityUtilities.cipher( data, key, Cipher.DECRYPT_MODE );
    }


    /**
     * Encrypts the given data using the given secret key.
     *
     * @param data the decrypted data
     * @param key the secret key
     *
     * @return the encrypted data
     *
     * @throws Exception if an exception occurred
     */
    private byte[] encrypt( byte[] data, byte[] key ) throws Exception
    {
        return SecurityUtilities.cipher( data, key, Cipher.ENCRYPT_MODE );
    }


    public byte[] getEncrypted()
    {
        return encrypted;
    }


    public void setEncrypted( byte[] encrypted )
    {
        this.encrypted = encrypted;
    }
}
