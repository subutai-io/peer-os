package io.subutai.bazaar.share.dto;


public class RhVersionInfoDto
{
    private String rhId;
    private boolean isManagement;
    private String ssVersion;
    private String rhVersion;
    private String p2pVersion;


    public RhVersionInfoDto()
    {
    }


    public String getRhId()
    {
        return rhId;
    }


    public void setRhId( final String rhId )
    {
        this.rhId = rhId;
    }


    public boolean isManagement()
    {
        return isManagement;
    }


    public void setManagement( final boolean management )
    {
        isManagement = management;
    }


    public String getSsVersion()
    {
        return ssVersion;
    }


    public void setSsVersion( final String ssVersion )
    {
        this.ssVersion = ssVersion;
    }


    public String getRhVersion()
    {
        return rhVersion;
    }


    public void setRhVersion( final String rhVersion )
    {
        this.rhVersion = rhVersion;
    }


    public String getP2pVersion()
    {
        return p2pVersion;
    }


    public void setP2pVersion( final String p2pVersion )
    {
        this.p2pVersion = p2pVersion;
    }
}
