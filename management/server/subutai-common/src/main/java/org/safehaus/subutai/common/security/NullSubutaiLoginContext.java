package org.safehaus.subutai.common.security;


/**
 * Null object for SubutaiLoginContext
 */
public class NullSubutaiLoginContext extends SubutaiLoginContext
{
    private static NullSubutaiLoginContext instance = new NullSubutaiLoginContext( "NULL", "NULL", "NULL" );


    private NullSubutaiLoginContext( final String sessionId, final String username, final String remoteAddress )
    {
        super( sessionId, username, remoteAddress );
    }


    public static SubutaiLoginContext getInstance()
    {
        return instance;
    }
}
