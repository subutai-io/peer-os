package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.SystemInfo;


public class SystemInfoPojo implements SystemInfo
{
    private String gitCommitId;
    private String gitBranch;
    private String gitCommitUserName;
    private String gitCommitUserEmail;
    private String projectVersion;

    private String gitBuildUserName;
    private String gitBuildUserEmail;
    private String gitBuildHost;
    private String gitBuildTime;

    private String gitClosestTagName;
    private String gitCommitIdDescribeShort;
    private String gitCommitTime;
    private String gitClosestTagCommitCount;
    private String gitCommitIdDescribe;


    public String getProjectVersion()
    {
        return projectVersion;
    }


    public void setProjectVersion( final String projectVersion )
    {
        this.projectVersion = projectVersion;
    }


    public String getGitBuildUserEmail()
    {
        return gitBuildUserEmail;
    }


    public void setGitBuildUserEmail( final String gitBuildUserEmail )
    {
        this.gitBuildUserEmail = gitBuildUserEmail;
    }


    public String getGitBuildHost()
    {
        return gitBuildHost;
    }


    public void setGitBuildHost( final String gitBuildHost )
    {
        this.gitBuildHost = gitBuildHost;
    }


    public String getGitClosestTagName()
    {
        return gitClosestTagName;
    }


    public void setGitClosestTagName( final String gitClosestTagName )
    {
        this.gitClosestTagName = gitClosestTagName;
    }


    public String getGitCommitIdDescribeShort()
    {
        return gitCommitIdDescribeShort;
    }


    public void setGitCommitIdDescribeShort( final String gitCommitIdDescribeShort )
    {
        this.gitCommitIdDescribeShort = gitCommitIdDescribeShort;
    }


    public String getGitCommitTime()
    {
        return gitCommitTime;
    }


    public void setGitCommitTime( final String gitCommitTime )
    {
        this.gitCommitTime = gitCommitTime;
    }


    public String getGitBranch()
    {
        return gitBranch;
    }


    public void setGitBranch( final String gitBranch )
    {
        this.gitBranch = gitBranch;
    }


    public String getGitBuildUserName()
    {
        return gitBuildUserName;
    }


    public void setGitBuildUserName( final String gitBuildUserName )
    {
        this.gitBuildUserName = gitBuildUserName;
    }


    public String getGitClosestTagCommitCount()
    {
        return gitClosestTagCommitCount;
    }


    public void setGitClosestTagCommitCount( final String gitClosestTagCommitCount )
    {
        this.gitClosestTagCommitCount = gitClosestTagCommitCount;
    }


    public String getGitCommitIdDescribe()
    {
        return gitCommitIdDescribe;
    }


    public void setGitCommitIdDescribe( final String gitCommitIdDescribe )
    {
        this.gitCommitIdDescribe = gitCommitIdDescribe;
    }


    public String getGitCommitId()
    {
        return gitCommitId;
    }


    public void setGitCommitId( final String gitCommitId )
    {
        this.gitCommitId = gitCommitId;
    }


    public String getGitBuildTime()
    {
        return gitBuildTime;
    }


    public void setGitBuildTime( final String gitBuildTime )
    {
        this.gitBuildTime = gitBuildTime;
    }


    public String getGitCommitUserName()
    {
        return gitCommitUserName;
    }


    public void setGitCommitUserName( final String gitCommitUserName )
    {
        this.gitCommitUserName = gitCommitUserName;
    }


    public String getGitCommitUserEmail()
    {
        return gitCommitUserEmail;
    }


    public void setGitCommitUserEmail( final String gitCommitUserEmail )
    {
        this.gitCommitUserEmail = gitCommitUserEmail;
    }
}
