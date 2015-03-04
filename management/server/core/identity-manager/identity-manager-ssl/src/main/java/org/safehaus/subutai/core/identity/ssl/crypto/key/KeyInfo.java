package org.safehaus.subutai.core.identity.ssl.crypto.key;


/**
 * Holds information about a key.
 */
public class KeyInfo
{
    private KeyType keyType;
    private String algorithm;
    private Integer size;


    public KeyInfo( KeyType keyType, String algorithm )
    {
        this( keyType, algorithm, null );
    }


    public KeyInfo( KeyType keyType, String algorithm, Integer size )
    {
        this.keyType = keyType;
        this.algorithm = algorithm;
        this.size = size;
    }


    public KeyType getKeyType()
    {
        return keyType;
    }


    public String getAlgorithm()
    {
        return algorithm;
    }


    public Integer getSize()
    {
        return size;
    }
}
