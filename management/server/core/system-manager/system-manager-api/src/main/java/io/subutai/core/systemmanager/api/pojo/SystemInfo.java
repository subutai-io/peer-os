package io.subutai.core.systemmanager.api.pojo;


import java.util.Map;

public interface SystemInfo
{
    String getProjectVersion();


    void setProjectVersion( final String projectVersion );


    String getGitBuildUserEmail();


    void setGitBuildUserEmail( final String gitBuildUserEmail );


    String getGitBuildHost();


    void setGitBuildHost( final String gitBuildHost );


    String getGitClosestTagName();


    void setGitClosestTagName( final String gitClosestTagName );


    String getGitCommitIdDescribeShort();


    void setGitCommitIdDescribeShort( final String gitCommitIdDescribeShort );


    String getGitCommitTime();


    void setGitCommitTime( final String gitCommitTime );


    String getGitBranch();


    void setGitBranch( final String gitBranch );


    String getGitBuildUserName();


    void setGitBuildUserName( final String gitBuildUserName );


    String getGitClosestTagCommitCount();


    void setGitClosestTagCommitCount( final String gitClosestTagCommitCount );


    String getGitCommitIdDescribe();


    void setGitCommitIdDescribe( final String gitCommitIdDescribe );


    String getGitCommitId();


    void setGitCommitId( final String gitCommitId );


    String getGitBuildTime();


    void setGitBuildTime( final String gitBuildTime );


    String getGitCommitUserName();


    void setGitCommitUserName( final String gitCommitUserName );


    String getGitCommitUserEmail();


    void setGitCommitUserEmail( final String gitCommitUserEmail );

    String getRhVersion();


    void setRhVersion( final String rhVersion );


    String getP2pVersion();


    void setP2pVersion( final String p2pVersion );


    boolean isUpdatesAvailable();


    void setUpdatesAvailable( final boolean updatesAvailable );


    Map getPeerP2PVersions();


    void setPeerP2PVersions(Map peerP2PVersions);
}
