package io.subutai.core.systemmanager.api.pojo;


public interface SystemInfo
{
    String getProjectVersion();


    String getGitBuildUserEmail();


    String getGitBuildHost();


    String getGitClosestTagName();


    String getGitCommitIdDescribeShort();


    String getGitCommitTime();


    String getGitBranch();


    String getGitBuildUserName();


    String getGitClosestTagCommitCount();


    String getGitCommitIdDescribe();


    String getGitCommitId();


    String getGitBuildTime();


    String getGitCommitUserName();


    String getGitCommitUserEmail();


    String getRhVersion();


    String getP2pVersion();


    boolean isUpdatesAvailable();

    String getOsName();
}
