package org.safehaus.subutai.common.util;


import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Set;

import javax.security.auth.Subject;

import org.safehaus.subutai.common.security.NullSubutaiLoginContext;
import org.safehaus.subutai.common.security.ShiroPrincipal;
import org.safehaus.subutai.common.security.SubutaiLoginContext;


/**
 * Security utility
 */
public abstract class SecurityUtil
{
    /**
     * Retrives shiro session ID from karaf session.
     */
    public static SubutaiLoginContext getSubutaiLoginContext()
    {
        SubutaiLoginContext nullResult = new NullSubutaiLoginContext();
        AccessControlContext acc = AccessController.getContext();
        Subject subject = Subject.getSubject( acc );
        if ( subject == null )
        {
            return nullResult;
        }
        Set<ShiroPrincipal> shiroPrincipal = subject.getPrincipals( ShiroPrincipal.class );

        return shiroPrincipal.isEmpty() ? nullResult : shiroPrincipal.iterator().next().getSubutaiLoginContext();
    }
}
