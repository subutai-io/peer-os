package io.subutai.common.security;


import java.security.Principal;


/**
 * Shiro principal to store SubutaiLoginContext
 */
public class ShiroPrincipal implements Principal
{
    private static String PRINCIPAL_NAME = SubutaiLoginContext.SUBUTAI_LOGIN_CONTEXT_NAME;

    private SubutaiLoginContext loginContext;


    public ShiroPrincipal( final SubutaiLoginContext loginContext )
    {
        this.loginContext = loginContext;
    }


    public SubutaiLoginContext getSubutaiLoginContext()
    {
        return loginContext;
    }


    @Override
    public String getName()
    {
        return PRINCIPAL_NAME;
    }
}
