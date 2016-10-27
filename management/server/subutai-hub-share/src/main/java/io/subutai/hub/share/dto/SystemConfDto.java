package io.subutai.hub.share.dto;


public class SystemConfDto
{
    private SystemConfigurationType key;

    private String[] globalKurjunUrls;
    private String[] localKurjunUrls;

    private String publicUrl;
    private int publicSecurePort;


    public SystemConfDto( SystemConfigurationType key )
    {
        this.key = key;
    }


    public SystemConfigurationType getKey()
    {
        return key;
    }


    public void setGlobalKurjunUrls( final String[] globalKurjunUrls )
    {
        this.globalKurjunUrls = globalKurjunUrls;
    }


    public void setLocalKurjunUrls( final String[] localKurjunUrls )
    {
        this.localKurjunUrls = localKurjunUrls;
    }


    public void setPublicUrl( final String publicUrl )
    {
        this.publicUrl = publicUrl;
    }


    public void setPublicSecurePort( final int publicSecurePort )
    {
        this.publicSecurePort = publicSecurePort;
    }
}
