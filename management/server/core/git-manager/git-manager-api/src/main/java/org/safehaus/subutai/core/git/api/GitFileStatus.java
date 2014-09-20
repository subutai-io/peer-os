package org.safehaus.subutai.core.git.api;


/**
 * Git file change statuses
 */
public enum GitFileStatus
{
    MODIFIED,
    COPIED,
    RENAMED,
    ADDED,
    DELETED,
    UNMERGED,
    UNVERSIONED,
    UNMODIFIED;


    /**
     * parses status from String
     *
     * @param status string representation of status
     *
     * @return {@code GitFileStatus}
     */
    public static GitFileStatus parse( String status )
    {
        switch ( status )
        {
            case "M":
                return GitFileStatus.MODIFIED;
            case "C":
                return GitFileStatus.COPIED;
            case "R":
                return GitFileStatus.RENAMED;
            case "A":
                return GitFileStatus.ADDED;
            case "D":
                return GitFileStatus.DELETED;
            case "U":
                return GitFileStatus.UNMERGED;
            case "X":
                return GitFileStatus.UNVERSIONED;
            default:
                return GitFileStatus.UNMODIFIED;
        }
    }
}
