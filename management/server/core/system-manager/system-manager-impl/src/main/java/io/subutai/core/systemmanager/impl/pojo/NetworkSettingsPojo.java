package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.NetworkSettings;


public class NetworkSettingsPojo implements NetworkSettings
{
    private String publicUrl;
    private int publicSecurePort;
    private int startRange;
    private int endRange;


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


    public int getStartRange()
    {
        return startRange;
    }


    public void setStartRange( final int startRange )
    {
        this.startRange = startRange;
    }


    public int getEndRange()
    {
        return endRange;
    }


    public void setEndRange( final int endRange )
    {
        this.endRange = endRange;
    }
}
