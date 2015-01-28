package org.safehaus.subutai.core.identity.api;


import java.io.Serializable;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;


public interface IdentityManager
{
    public SecurityManager getSecurityManager();

    public Subject login( AuthenticationToken token );

    public Subject getSubject( Serializable sessionId );

    public void logout( Serializable sessionId );
}

