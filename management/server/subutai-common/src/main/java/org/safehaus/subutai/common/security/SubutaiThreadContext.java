package org.safehaus.subutai.common.security;


import org.apache.log4j.MDC;


/**
 * Context for storing currently logged in user data
 */
public class SubutaiThreadContext
{
    private static final String THE_KEY = "SUBUTAI_LOGIN_CONTEXT_KEY";


    public static void set( SubutaiLoginContext context )
    {
        MDC.put( THE_KEY, context );
    }


    public static void unset()
    {
        MDC.remove( THE_KEY );
    }


    public static SubutaiLoginContext get()
    {
        Object result = MDC.get( THE_KEY );
        return result == null ? NullSubutaiLoginContext.getInstance() : ( SubutaiLoginContext ) result;
    }
}
