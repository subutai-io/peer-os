package io.subutai.core.security.impl.model;


import io.subutai.core.security.api.crypto.EncryptionTool;


/**
 * KeyRing Data for ManagementHost
 */
public class SecurityKeyData
{
    private String publicKeyringFile;
    private String secretKeyringFile;
    private String secretKeyringPwd;
    private String manHostId;
    private String manHostKeyFingerprint;
    private String ownerPublicKeyringFile;
    private Object jsonProvider;


    public String getPublicKeyringFile()
    {
        return publicKeyringFile;
    }


    public void setPublicKeyringFile( final String publicKeyringFile )
    {
        this.publicKeyringFile = publicKeyringFile;
    }


    public String getSecretKeyringFile()
    {
        return secretKeyringFile;
    }


    public void setSecretKeyringFile( final String secretKeyringFile )
    {
        this.secretKeyringFile = secretKeyringFile;
    }


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


    public String getOwnerPublicKeyringFile()
    {
        return ownerPublicKeyringFile;
    }


    public void setOwnerPublicKeyringFile( final String ownerPublicKeyringFile )
    {
        this.ownerPublicKeyringFile = ownerPublicKeyringFile;
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
