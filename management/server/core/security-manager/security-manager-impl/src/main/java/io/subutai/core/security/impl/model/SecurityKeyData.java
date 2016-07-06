package io.subutai.core.security.impl.model;


/**
 * KeyRing Data for ManagementHost
 */
public class SecurityKeyData
{
    private String secretKeyringPwd;
    private String manHostId;
    private String peerOwnerId;


    public String getSecretKeyringPwd()
    {
        return secretKeyringPwd;
    }


    public void setSecretKeyringPwd( final String secretKeyringPwd )
    {
        this.secretKeyringPwd = secretKeyringPwd;
    }


    public String getManHostId()
    {
        return manHostId;
    }


    public void setManHostId( final String manHostId )
    {
        this.manHostId = manHostId;
    }


    public String getPeerOwnerId()
    {
        return peerOwnerId;
    }


    public void setPeerOwnerId( final String peerOwnerId )
    {
        this.peerOwnerId = peerOwnerId;
    }
}
