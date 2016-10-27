package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.SystemInfo;


public class SystemInfoPojo implements SystemInfo
{
    private String gitCommitId;
    private String gitBranch;
    private String gitCommitUserName;
    private String gitCommitUserEmail;
    private String projectVersion;
    private String rhVersion;
    private String p2pVersion;

    private String gitBuildUserName;
    private String gitBuildUserEmail;
    private String gitBuildHost;
    private String gitBuildTime;

    private String gitClosestTagName;
    private String gitCommitIdDescribeShort;
    private String gitCommitTime;
    private String gitClosestTagCommitCount;
    private String gitCommitIdDescribe;

    private boolean isUpdatesAvailable;


    @Override
    public String getProjectVersion()
    {
        return projectVersion;
    }


    @Override
    public void setProjectVersion( final String projectVersion )
    {
        this.projectVersion = projectVersion;
    }


    @Override
    public String getGitBuildUserEmail()
    {
        return gitBuildUserEmail;
    }


    @Override
    public void setGitBuildUserEmail( final String gitBuildUserEmail )
    {
        this.gitBuildUserEmail = gitBuildUserEmail;
    }


    @Override
    public String getGitBuildHost()
    {
        return gitBuildHost;
    }


    @Override
    public void setGitBuildHost( final String gitBuildHost )
    {
        this.gitBuildHost = gitBuildHost;
    }


    @Override
    public String getGitClosestTagName()
    {
        return gitClosestTagName;
    }


    @Override
    public void setGitClosestTagName( final String gitClosestTagName )
    {
        this.gitClosestTagName = gitClosestTagName;
    }


    @Override
    public String getGitCommitIdDescribeShort()
    {
        return gitCommitIdDescribeShort;
    }


    @Override
    public void setGitCommitIdDescribeShort( final String gitCommitIdDescribeShort )
    {
        this.gitCommitIdDescribeShort = gitCommitIdDescribeShort;
    }


    @Override
    public String getGitCommitTime()
    {
        return gitCommitTime;
    }


    @Override
    public void setGitCommitTime( final String gitCommitTime )
    {
        this.gitCommitTime = gitCommitTime;
    }


    @Override
    public String getGitBranch()
    {
        return gitBranch;
    }


    @Override
    public void setGitBranch( final String gitBranch )
    {
        this.gitBranch = gitBranch;
    }


    @Override
    public String getGitBuildUserName()
    {
        return gitBuildUserName;
    }


    @Override
    public void setGitBuildUserName( final String gitBuildUserName )
    {
        this.gitBuildUserName = gitBuildUserName;
    }


    @Override
    public String getGitClosestTagCommitCount()
    {
        return gitClosestTagCommitCount;
    }


    @Override
    public void setGitClosestTagCommitCount( final String gitClosestTagCommitCount )
    {
        this.gitClosestTagCommitCount = gitClosestTagCommitCount;
    }


    @Override
    public String getGitCommitIdDescribe()
    {
        return gitCommitIdDescribe;
    }


    @Override
    public void setGitCommitIdDescribe( final String gitCommitIdDescribe )
    {
        this.gitCommitIdDescribe = gitCommitIdDescribe;
    }


    @Override
    public String getGitCommitId()
    {
        return gitCommitId;
    }


    @Override
    public void setGitCommitId( final String gitCommitId )
    {
        this.gitCommitId = gitCommitId;
    }


    @Override
    public String getGitBuildTime()
    {
        return gitBuildTime;
    }


    @Override
    public void setGitBuildTime( final String gitBuildTime )
    {
        this.gitBuildTime = gitBuildTime;
    }


    @Override
    public String getGitCommitUserName()
    {
        return gitCommitUserName;
    }


    @Override
    public void setGitCommitUserName( final String gitCommitUserName )
    {
        this.gitCommitUserName = gitCommitUserName;
    }


    @Override
    public String getGitCommitUserEmail()
    {
        return gitCommitUserEmail;
    }


    @Override
    public void setGitCommitUserEmail( final String gitCommitUserEmail )
    {
        this.gitCommitUserEmail = gitCommitUserEmail;
    }


    @Override
    public String getRhVersion()
    {
        return rhVersion;
    }


    @Override
    public void setRhVersion( final String rhVersion )
    {
        this.rhVersion = rhVersion;
    }


    @Override
    public String getP2pVersion()
    {
        return p2pVersion;
    }


    @Override
    public void setP2pVersion( final String p2pVersion )
    {
        this.p2pVersion = p2pVersion;
    }


    @Override
    public boolean isUpdatesAvailable()
    {
        return isUpdatesAvailable;
    }


    @Override
    public void setUpdatesAvailable( final boolean updatesAvailable )
    {
        isUpdatesAvailable = updatesAvailable;
    }
}
