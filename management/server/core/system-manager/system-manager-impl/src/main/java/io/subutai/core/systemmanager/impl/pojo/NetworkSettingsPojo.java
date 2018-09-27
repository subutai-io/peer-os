package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.NetworkSettings;


public class NetworkSettingsPojo implements NetworkSettings
{
    private String publicUrl;
    private int publicSecurePort;
    private int startRange;
    private int endRange;
    private String bazaarIp;
    private boolean useRhIp;


    @Override
    public boolean getUseRhIp()
    {
        return useRhIp;
    }


    public void setUseRhIp( final boolean useRhIp )
    {
        this.useRhIp = useRhIp;
    }


    @Override
    public String getBazaarIp()
    {
        return bazaarIp;
    }


    public void setBazaarIp( final String bazaarIp )
    {
        this.bazaarIp = bazaarIp;
    }


    @Override
    public String getPublicUrl()
    {
        return publicUrl;
    }


    public void setPublicUrl( final String publicUrl )
    {
        this.publicUrl = publicUrl;
    }


    @Override
    public int getPublicSecurePort()
    {
        return publicSecurePort;
    }


    public void setPublicSecurePort( final int publicSecurePort )
    {
        this.publicSecurePort = publicSecurePort;
    }


    @Override
    public int getStartRange()
    {
        return startRange;
    }


    public void setStartRange( final int startRange )
    {
        this.startRange = startRange;
    }


    @Override
    public int getEndRange()
    {
        return endRange;
    }


    public void setEndRange( final int endRange )
    {
        this.endRange = endRange;
    }
}
