package org.safehaus.subutai.common.security;


import java.io.Serializable;
import java.security.Principal;


/**
 * Shiro session id
 */
public class ShiroPrincipal implements Principal
{
    private static String PRINCIPAL_NAME = "SHIRO_SESSION_ID";

    private Serializable sessionId;


    public ShiroPrincipal( final Serializable subject )
    {
        this.sessionId = subject;
    }


    public Serializable getSessionId()
    {
        return sessionId;
    }


    @Override
    public String getName()
    {
        return PRINCIPAL_NAME;
    }
}
