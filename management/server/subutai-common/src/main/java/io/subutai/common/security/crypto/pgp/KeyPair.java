package io.subutai.common.security.crypto.pgp;


import com.google.common.base.Objects;


/**
 * Represents a PGP keypair. The key is generated similarly to when using gpg commmand utility and selecting RSA & RSA
 * with key size 2048
 */
public class KeyPair
{
    private String primaryKeyId;
    private String primaryKeyFingerprint;
    private String subKeyId;
    private String subKeyFingerprint;
    private byte[] pubKeyring;
    private byte[] secKeyring;


    public String getPrimaryKeyId()
    {
        return primaryKeyId;
    }


    public String getPrimaryKeyFingerprint()
    {
        return primaryKeyFingerprint;
    }


    public String getSubKeyId()
    {
        return subKeyId;
    }


    public String getSubKeyFingerprint()
    {
        return subKeyFingerprint;
    }


    public byte[] getPubKeyring()
    {
        return pubKeyring;
    }


    public byte[] getSecKeyring()
    {
        return secKeyring;
    }


    protected void setPrimaryKeyId( final String primaryKeyId )
    {
        this.primaryKeyId = primaryKeyId;
    }


    protected void setPrimaryKeyFingerprint( final String primaryKeyFingerprint )
    {
        this.primaryKeyFingerprint = primaryKeyFingerprint;
    }


    protected void setSubKeyId( final String subKeyId )
    {
        this.subKeyId = subKeyId;
    }


    protected void setSubKeyFingerprint( final String subKeyFingerprint )
    {
        this.subKeyFingerprint = subKeyFingerprint;
    }


    protected void setPubKeyring( final byte[] pubKeyring )
    {
        this.pubKeyring = pubKeyring;
    }


    protected void setSecKeyring( final byte[] secKeyring )
    {
        this.secKeyring = secKeyring;
    }


    @Override
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "primaryKeyId", primaryKeyId )
                      .add( "primaryKeyFingerprint", primaryKeyFingerprint ).add( "subKeyId", subKeyId )
                      .add( "subKeyFingerprint", subKeyFingerprint ).add( "pubKeyring", new String( pubKeyring ) )
                      .add( "secKeyring", new String( secKeyring ) ).toString();
    }
}
