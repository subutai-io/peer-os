package io.subutai.bazaar.share.dto;


public class VersionInfoDto
{
    private String peerId;
    private String commitId;
    private String buildTime;
    private String branch;
    private String ssVersion;
    private String rhVersion;
    private String p2pVersion;


    public VersionInfoDto()
    {
    }


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public String getCommitId()
    {
        return commitId;
    }


    public void setCommitId( final String commitId )
    {
        this.commitId = commitId;
    }


    public String getBuildTime()
    {
        return buildTime;
    }


    public void setBuildTime( final String buildTime )
    {
        this.buildTime = buildTime;
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


    public String getBranch()
    {
        return branch;
    }


    public void setBranch( final String branch )
    {
        this.branch = branch;
    }
}
