package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.NetworkSettings;


public class NetworkSettingsPojo implements NetworkSettings
{
    public int securePortX1;
    public int securePortX2;
    public int securePortX3;
    public String publicUrl;
    public int agentPort;


    public int getAgentPort()
    {
        return agentPort;
    }


    public void setAgentPort( final int agentPort )
    {
        this.agentPort = agentPort;
    }


    public String getPublicUrl()
    {
        return publicUrl;
    }


    public void setPublicUrl( final String publicUrl )
    {
        this.publicUrl = publicUrl;
    }


    public int getSecurePortX1()
    {
        return securePortX1;
    }


    public void setSecurePortX1( final int securePortX1 )
    {
        this.securePortX1 = securePortX1;
    }


    public int getSecurePortX2()
    {
        return securePortX2;
    }


    public void setSecurePortX2( final int securePortX2 )
    {
        this.securePortX2 = securePortX2;
    }


    public int getSecurePortX3()
    {
        return securePortX3;
    }


    public void setSecurePortX3( final int securePortX3 )
    {
        this.securePortX3 = securePortX3;
    }
}
