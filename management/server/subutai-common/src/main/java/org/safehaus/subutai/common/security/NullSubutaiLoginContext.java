package org.safehaus.subutai.common.security;


/**
 * Null object for SubutaiLoginContext
 */
public class NullSubutaiLoginContext extends SubutaiLoginContext
{
    public NullSubutaiLoginContext()
    {
        super( "*UNKNOWN*", "*UNKNOWN*", "*UNKNOWN*" );
    }

    @Override
    public String toString()
    {
        return "NullSubutaiLoginContext{" +
                "sessionId='" + sessionId + '\'' +
                ", username='" + username + '\'' +
                ", remoteAddress='" + remoteAddress + '\'' +
                '}';
    }
}
