package io.subutai.core.security.rest.model;


/**
 *
 */
public class SecurityKeyData
{
    String sourceKeyIdentityId;
    String tagerKeyIdentityId;
    String keyText = "";
    int trustlevel = 0;


    public String getSourceKeyIdentityId()
    {
        return sourceKeyIdentityId;
    }


    public void setSourceKeyIdentityId( final String sourceKeyIdentityId )
    {
        this.sourceKeyIdentityId = sourceKeyIdentityId;
    }


    public String getTagerKeyIdentityId()
    {
        return tagerKeyIdentityId;
    }


    public void setTagerKeyIdentityId( final String tagerKeyIdentityId )
    {
        this.tagerKeyIdentityId = tagerKeyIdentityId;
    }


    public String getKeyText()
    {
        return keyText;
    }


    public void setKeyText( final String keyText )
    {
        this.keyText = keyText;
    }


    public int getTrustlevel()
    {
        return trustlevel;
    }


    public void setTrustlevel( final int trustlevel )
    {
        this.trustlevel = trustlevel;
    }
}
