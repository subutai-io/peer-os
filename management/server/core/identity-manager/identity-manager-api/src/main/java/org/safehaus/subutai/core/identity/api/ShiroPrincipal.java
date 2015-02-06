package org.safehaus.subutai.core.identity.api;


import java.security.Principal;

import org.apache.shiro.subject.Subject;


/**
 * Created by timur on 1/28/15.
 */
public class ShiroPrincipal implements Principal
{
    private static String PRINCIPAL_NAME = "SHIRO_PRINCIPAL";

    private Subject subject;


    public ShiroPrincipal( final Subject subject )
    {
        this.subject = subject;
    }


    public Subject getSubject()
    {
        return subject;
    }


    @Override
    public String getName()
    {
        return PRINCIPAL_NAME;
    }
}
