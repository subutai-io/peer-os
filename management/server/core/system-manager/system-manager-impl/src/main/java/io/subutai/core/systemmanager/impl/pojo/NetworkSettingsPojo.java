package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.NetworkSettings;


public class NetworkSettingsPojo implements NetworkSettings
{
    private String publicUrl;
    private int publicSecurePort;
    private int startRange;
    private int endRange;
    private String hubIp;


    @Override
    public String getHubIp()
    {
        return hubIp;
    }


    @Override
    public void setHubIp( final String hubIp )
    {
        this.hubIp = hubIp;
    }


    @Override
    public String getPublicUrl()
    {
        return publicUrl;
    }


    @Override
    public void setPublicUrl( final String publicUrl )
    {
        this.publicUrl = publicUrl;
    }


    @Override
    public int getPublicSecurePort()
    {
        return publicSecurePort;
    }


    @Override
    public void setPublicSecurePort( final int publicSecurePort )
    {
        this.publicSecurePort = publicSecurePort;
    }


    @Override
    public int getStartRange()
    {
        return startRange;
    }


    @Override
    public void setStartRange( final int startRange )
    {
        this.startRange = startRange;
    }


    @Override
    public int getEndRange()
    {
        return endRange;
    }


    @Override
    public void setEndRange( final int endRange )
    {
        this.endRange = endRange;
    }
}
