package io.subutai.core.identity.rest.ui.entity;


/**
 *
 */
public class KeyDataDto
{
    private int keyType = 1;
    private String key = "";
    private String fingerprint = "";
    private String id = "";
    private String userId = "";
    private String expiryDate = "";


    public int getKeyType()
    {
        return keyType;
    }


    public void setKeyType( final int keyType )
    {
        this.keyType = keyType;
    }


    public String getKey()
    {
        return key;
    }


    public void setKey( final String key )
    {
        this.key = key;
    }


    public String getFingerprint()
    {
        return fingerprint;
    }


    public void setFingerprint( final String fingerprint )
    {
        this.fingerprint = fingerprint;
    }


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getUserId()
    {
        return userId;
    }


    public void setUserId( final String userId )
    {
        this.userId = userId;
    }


    public String getExpiryDate()
    {
        return expiryDate;
    }


    public void setExpiryDate( final String expiryDate )
    {
        this.expiryDate = expiryDate;
    }
}
