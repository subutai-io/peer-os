package org.safehaus.subutai.common.util;


import java.io.Serializable;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Set;

import javax.security.auth.Subject;

import org.safehaus.subutai.common.security.ShiroPrincipal;


/**
 * Security utility
 */
public abstract class SecurityUtil
{
    /**
     * Retrives shiro session ID from karaf session.
     */
    public static Serializable getSessionId()
    {
        AccessControlContext acc = AccessController.getContext();
        Subject subject = Subject.getSubject( acc );
        Set<ShiroPrincipal> shiroPrincipal = subject.getPrincipals( ShiroPrincipal.class );


        return shiroPrincipal != null ? shiroPrincipal.iterator().next().getSessionId() : null;
    }
}
