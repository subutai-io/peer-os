package io.subutai.common.security;


/**
 * Subutai web user login context
 */
public class SubutaiLoginContext
{
    public static String SUBUTAI_LOGIN_CONTEXT_NAME = SubutaiLoginContext.class.getName();
    protected String sessionId;
    protected String username;
    protected String remoteAddress;


    public SubutaiLoginContext( final String sessionId, final String username, final String remoteAddress )
    {
        this.sessionId = sessionId;
        this.username = username;
        this.remoteAddress = remoteAddress;
    }


    public String getSessionId()
    {
        return sessionId;
    }


    public String getUsername()
    {
        return username;
    }


    public String getRemoteAddress()
    {
        return remoteAddress;
    }


    @Override
    public String toString()
    {
        return "SubutaiLoginContext{" +
                "sessionId='" + sessionId + '\'' +
                ", username='" + username + '\'' +
                ", remoteAddress='" + remoteAddress + '\'' +
                '}';
    }
}
