package io.subutai.hub.share.dto;


public class SystemConfDto
{
    private SystemConfigurationType key;
    private String value;
    private String description;

    private String[] globalKurjunUrls;
    private String[] localKurjunUrls;

    public int securePortX1;
    public int securePortX2;
    public int securePortX3;
    public String publicUrl;
    public int agentPort;
    public int publicSecurePort;


    public SystemConfDto()
    {
    }


    public SystemConfDto( SystemConfigurationType key )
    {
        this.key = key;
    }


    public SystemConfDto( final SystemConfigurationType key, final String value, final String description )
    {
        this.key = key;
        this.value = value;
        this.description = description;
    }


    public SystemConfigurationType getKey()
    {
        return key;
    }


    public void setKey( final SystemConfigurationType key )
    {
        this.key = key;
    }


    public String getValue()
    {
        return value;
    }


    public void setValue( final String value )
    {
        this.value = value;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription( final String description )
    {
        this.description = description;
    }


    public String[] getGlobalKurjunUrls()
    {
        return globalKurjunUrls;
    }


    public void setGlobalKurjunUrls( final String[] globalKurjunUrls )
    {
        this.globalKurjunUrls = globalKurjunUrls;
    }


    public String[] getLocalKurjunUrls()
    {
        return localKurjunUrls;
    }


    public void setLocalKurjunUrls( final String[] localKurjunUrls )
    {
        this.localKurjunUrls = localKurjunUrls;
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


    public String getPublicUrl()
    {
        return publicUrl;
    }


    public void setPublicUrl( final String publicUrl )
    {
        this.publicUrl = publicUrl;
    }


    public int getAgentPort()
    {
        return agentPort;
    }


    public void setAgentPort( final int agentPort )
    {
        this.agentPort = agentPort;
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
