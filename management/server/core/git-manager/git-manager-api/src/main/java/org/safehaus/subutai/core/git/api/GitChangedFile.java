package org.safehaus.subutai.core.git.api;


/**
 * Represents a path to a changed file with the status of file
 */
public class GitChangedFile
{

    private GitFileStatus gitFileStatus;
    private String gitFilePath;


    public GitChangedFile( final GitFileStatus gitFileStatus, final String gitFilePath )
    {
        this.gitFileStatus = gitFileStatus;
        this.gitFilePath = gitFilePath;
    }


    public GitFileStatus getGitFileStatus()
    {
        return gitFileStatus;
    }


    public String getGitFilePath()
    {
        return gitFilePath;
    }


    @Override
    public String toString()
    {
        return "GitFileDiff{" +
                "gitFileStatus=" + gitFileStatus +
                ", gitFilePath='" + gitFilePath + '\'' +
                '}';
    }
}
