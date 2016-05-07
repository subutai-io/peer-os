package io.subutai.hub.share.dto;


public class UserTrustDto
{
    private String fingerprint;

    private Boolean isTrusted = false;


    public UserTrustDto()
    {
    }


    public String getFingerprint()
    {
        return fingerprint;
    }


    public void setFingerprint( final String fingerprint )
    {
        this.fingerprint = fingerprint;
    }


    public Boolean getTrusted()
    {
        return isTrusted;
    }


    public void setTrusted( final Boolean trusted )
    {
        isTrusted = trusted;
    }
}
