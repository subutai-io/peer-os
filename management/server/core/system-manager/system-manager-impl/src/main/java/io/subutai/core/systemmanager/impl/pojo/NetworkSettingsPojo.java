package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.NetworkSettings;


public class NetworkSettingsPojo implements NetworkSettings
{
    public String publicUrl;
    public int publicSecurePort;


    public String getPublicUrl()
    {
        return publicUrl;
    }


    public void setPublicUrl( final String publicUrl )
    {
        this.publicUrl = publicUrl;
    }


    public int getPublicSecurePort()
    {
        return publicSecurePort;
    }


    public void setPublicSecurePort( final int publicSecurePort )
    {
        this.publicSecurePort = publicSecurePort;
    }
}
