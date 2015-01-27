package org.safehaus.subutai.core.identity.api;


import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;


public interface IdentityManager
{
    public SecurityManager getSecurityManager();

    public void login( AuthenticationToken token );

    public Subject getSubject();

    public void logout();
}

