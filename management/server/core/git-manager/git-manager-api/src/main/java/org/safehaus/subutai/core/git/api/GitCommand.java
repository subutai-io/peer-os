package org.safehaus.subutai.core.git.api;


/**
 * Contains common git commands
 */
public enum GitCommand
{
    INIT( "init" ),
    ADD( "add" ),
    DELETE( "rm" ),
    DIFF( "diff" ),
    COMMIT( "commitAll" ),
    BRANCH( "branch" ),
    CHECKOUT( "checkout" ),
    CLONE( "clone" ),
    REVERT( "revert" ),
    FETCH( "fetch" ),
    MERGE( "merge" ),
    STASH( "stash" ),
    PULL( "pull" ),
    PUSH( "push" );

    private String command;


    private GitCommand( final String command )
    {
        this.command = command;
    }


    /**
     * Returns corresponding to this enum git command
     */
    public String getCommand()
    {
        return command;
    }
}
