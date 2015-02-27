package org.safehaus.subutai.common.security;


/**
 * Inheritable thread local context for storing currently logged in user data
 */
public class SubutaiThreadContext
{
    private static final InheritableThreadLocal<SubutaiLoginContext> context = new InheritableThreadLocal<>();


    public static void set( SubutaiLoginContext context )
    {
        SubutaiThreadContext.context.set( context );
    }


    public static void unset()
    {
        context.remove();
    }


    public static SubutaiLoginContext get()
    {
        return context.get();
    }
}
