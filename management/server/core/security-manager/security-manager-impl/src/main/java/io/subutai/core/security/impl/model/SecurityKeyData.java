package io.subutai.core.security.impl.model;


import io.subutai.core.security.api.crypto.EncryptionTool;


/**
 * KeyRing Data for ManagementHost
 */
public class SecurityKeyData
{
    private String secretKeyringPwd;
    private String manHostId;
    private String manHostKeyFingerprint;
    private Object jsonProvider;


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


    public String getManHostKeyFingerprint()
    {
        return manHostKeyFingerprint;
    }


    public void setManHostKeyFingerprint( final String manHostKeyFingerprint )
    {
        this.manHostKeyFingerprint = manHostKeyFingerprint;
    }


    public Object getJsonProvider()
    {
        return jsonProvider;
    }


    public void setJsonProvider( final Object jsonProvider )
    {
        this.jsonProvider = jsonProvider;
    }

}
