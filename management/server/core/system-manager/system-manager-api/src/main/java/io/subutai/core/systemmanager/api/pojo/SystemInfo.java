package io.subutai.core.systemmanager.api.pojo;


public interface SystemInfo
{
    public String getProjectVersion();


    public void setProjectVersion( final String projectVersion );


    public String getGitBuildUserEmail();


    public void setGitBuildUserEmail( final String gitBuildUserEmail );


    public String getGitBuildHost();


    public void setGitBuildHost( final String gitBuildHost );


    public String getGitClosestTagName();


    public void setGitClosestTagName( final String gitClosestTagName );


    public String getGitCommitIdDescribeShort();


    public void setGitCommitIdDescribeShort( final String gitCommitIdDescribeShort );


    public String getGitCommitTime();


    public void setGitCommitTime( final String gitCommitTime );


    public String getGitBranch();


    public void setGitBranch( final String gitBranch );


    public String getGitBuildUserName();


    public void setGitBuildUserName( final String gitBuildUserName );


    public String getGitClosestTagCommitCount();


    public void setGitClosestTagCommitCount( final String gitClosestTagCommitCount );


    public String getGitCommitIdDescribe();


    public void setGitCommitIdDescribe( final String gitCommitIdDescribe );


    public String getGitCommitId();


    public void setGitCommitId( final String gitCommitId );


    public String getGitBuildTime();


    public void setGitBuildTime( final String gitBuildTime );


    public String getGitCommitUserName();


    public void setGitCommitUserName( final String gitCommitUserName );


    public String getGitCommitUserEmail();


    public void setGitCommitUserEmail( final String gitCommitUserEmail );

    public String getRhVersion();


    public void setRhVersion( final String rhVersion );


    public String getP2pVersion();


    public void setP2pVersion( final String p2pVersion );


    public boolean isUpdatesAvailable();


    public void setUpdatesAvailable( final boolean updatesAvailable );
}
