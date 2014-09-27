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


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof GitChangedFile ) )
        {
            return false;
        }

        final GitChangedFile that = ( GitChangedFile ) o;

        if ( gitFilePath != null ? !gitFilePath.equals( that.gitFilePath ) : that.gitFilePath != null )
        {
            return false;
        }
        if ( gitFileStatus != that.gitFileStatus )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        int result = gitFileStatus != null ? gitFileStatus.hashCode() : 0;
        result = 31 * result + ( gitFilePath != null ? gitFilePath.hashCode() : 0 );
        return result;
    }
}
