package io.subutai.core.git.api;


/**
 * Git file change statuses
 */
public enum GitFileStatus
{
    ADDED( "A" ),
    COPIED( "C" ),
    DELETED( "D" ),
    MODIFIED( "M" ),
    RENAMED( "R" ),
    TYPE_CHANGED( "T" ),
    UNMERGED( "U" ),
    UNVERSIONED( "X" ),
    UNMODIFIED( "" ),
    UNKNOWN( "unknown" );


    private final String statusAcronym;


    GitFileStatus( String statusAcronym )
    {
        this.statusAcronym = statusAcronym;
    }


    /**
     * parses status from String
     *
     * @param status string representation of status
     *
     * @return {@code GitFileStatus}
     */
    public static GitFileStatus parse( String status )
    {
        for ( GitFileStatus gitFileStatus : GitFileStatus.values() )
        {
            if ( gitFileStatus.statusAcronym.equals( status ) )
            {
                return gitFileStatus;
            }
        }
        return GitFileStatus.UNKNOWN;
    }
}
