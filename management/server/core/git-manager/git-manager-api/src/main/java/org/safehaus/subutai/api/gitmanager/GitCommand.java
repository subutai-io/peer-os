package org.safehaus.subutai.api.gitmanager;


/**
 * Contains common git commands
 */
public enum GitCommand {
    ADD( "add" ),
    DELETE( "rm" ),
    COMMIT( "commit" ),
    BRANCH( "branch" ),
    CHECKOUT( "checkout" ),
    CLONE( "clone" ),
    DIFF( "diff" ),
    FETCH( "fetch" ),
    MERGE( "merge" ),
    PULL( "pull" ),
    PUSH( "push" ),
    RESET( "checkout" ),
    TAG( "tag" );

    private String command;


    private GitCommand( final String command ) {
        this.command = command;
    }


    /**
     * Returns corresponding to this enum git command
     */
    public String getCommand() {
        return command;
    }
}
