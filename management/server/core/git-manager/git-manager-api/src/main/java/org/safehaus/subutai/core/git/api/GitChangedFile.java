package org.safehaus.subutai.core.git.api;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Represents a path to a changed file with the status of file
 */
public class GitChangedFile
{

    private GitFileStatus gitFileStatus;
    private String gitFilePath;


    public GitChangedFile( final GitFileStatus gitFileStatus, final String gitFilePath )
    {
        Preconditions.checkNotNull( gitFileStatus, "Git file status is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( gitFilePath ), "Git file path is null or empty" + "" );
        this.gitFileStatus = gitFileStatus;
        this.gitFilePath = gitFilePath;
    }


    /**
     * Returns status of a changed file
     */
    public GitFileStatus getGitFileStatus()
    {
        return gitFileStatus;
    }


    /**
     * Returns path of a changed file
     */
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

        return !( gitFilePath != null ? !gitFilePath.equals( that.gitFilePath ) : that.gitFilePath != null )
                && gitFileStatus == that.gitFileStatus;
    }


    @Override
    public int hashCode()
    {
        int result = gitFileStatus != null ? gitFileStatus.hashCode() : 0;
        result = 31 * result + ( gitFilePath != null ? gitFilePath.hashCode() : 0 );
        return result;
    }
}
