package org.safehaus.subutai.core.git.api;


/**
 * Git file change statuses
 */
public enum GitFileStatus
{
    MODIFIED( "M" ),
    COPIED( "C" ),
    RENAMED( "R" ),
    ADDED( "A" ),
    DELETED( "D" ),
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
